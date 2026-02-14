package com.introms.service;

import com.introms.exception.IdValidationException;
import com.introms.exception.InvalidMp3Exception;
import com.introms.exception.S3Exception;
import com.introms.exception.ResourceSaveException;
import com.introms.exception.ResourceStorageException;
import com.introms.util.Utility;
import com.introms.client.SongMetadataWebClient;
import com.introms.entity.Resource;
import com.introms.exception.ResourceNotFoundException;
import com.introms.repository.ResourceRepository;
import com.introms.dto.ResourceCreateRequest;
import com.introms.dto.ResourceCreateResponse;
import com.introms.dto.ResourceResponse;
import com.introms.dto.ResourceCreatedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceCoordinator {
    private final ResourceService resourceService;
    private final FileStorageService fileStorageService;
    private final SongMetadataWebClient songMetadataWebClient;
    private final ApplicationEventPublisher applicationEventPublisher;

    private static final String CONTENT_TYPE = "audio/mpeg";
    private static final Integer MAX_IDS_LENGTH = 200;
    private static final String S3PREFIX = "s3://";

    @Transactional
    public ResourceCreateResponse saveResource(ResourceCreateRequest request) {
        // Validate request
        if (!request.contentType().equalsIgnoreCase(CONTENT_TYPE)) {
            throw new InvalidMp3Exception("Invalid file format. Only MP3 files are allowed");
        }

        if (request.data() == null || request.data().length == 0) {
            throw new InvalidMp3Exception("The request body is invalid MP3");
        }

        // Step 1: Upload to S3 (outside transaction)
        String s3Key = null;
        var key = UUID.randomUUID() + ".mp3";
        try {
            s3Key = fileStorageService.upload(key, request.data(), request.contentType());
            log.debug("File uploaded to S3 with key: {}", s3Key);
        } catch (Exception e) {
            log.error("Failed to upload file to S3. Error: {}", e.getMessage(), e);
            throw new S3Exception("Failed to upload file to S3: " + e.getMessage(), e);
        }

        // Step 2: Save to database (inside transaction)
        Resource resource = new Resource();
        resource.setS3Key(s3Key);

        Resource savedResource;
        try {
            savedResource = resourceService.saveResource(resource);
            log.info("Resource saved to database with ID: {}", savedResource.getId());
        } catch (Exception e) {
            log.error("Failed to save resource to database. Attempting to cleanup S3 file with key: {}. Error: {}", 
                s3Key, e.getMessage(), e);
            
            // Compensating transaction: Delete from S3 if DB save fails
            try {
                fileStorageService.delete(s3Key);
                log.info("Successfully cleaned up S3 file with key: {}", s3Key);
            } catch (Exception cleanupException) {
                log.error("Failed to cleanup S3 file with key: {}. This file may be orphaned. Error: {}", 
                    s3Key, cleanupException.getMessage(), cleanupException);
                throw new ResourceStorageException(
                    String.format("Failed to save resource and cleanup S3 file. S3 key: %s", s3Key), 
                    cleanupException);
            }
            
            throw new ResourceSaveException("Failed to save resource to database: " + e.getMessage(), e);
        }

        // Step 3: Publish event AFTER transaction commits
        // Using ApplicationEventPublisher with @TransactionalEventListener
        // This ensures event is published only after transaction commits successfully
        ResourceCreatedEvent event = new ResourceCreatedEvent(savedResource.getId());
        applicationEventPublisher.publishEvent(event);
        log.debug("Event published to ApplicationEventPublisher. Will be processed after transaction commit for resource ID: {}", 
            savedResource.getId());

        return resourceToResourceResponse(savedResource);
    }

    public ResourceResponse getResource(String sid) {
        log.info("Resource requested with ID:{}", sid);
        boolean idValid = Utility.isIdValid(sid);
        if (!idValid) {
            throw new IdValidationException(String.format("Invalid value '%s' for ID. Must be a positive integer", sid));
        }

        Integer id = Integer.parseInt(sid);
        Resource resource = resourceService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + id));

        byte[] fileContent = fileStorageService.download(resource.getS3Key());

        log.info("Resource found with content length:{}", fileContent.length);
        return new ResourceResponse(fileContent,CONTENT_TYPE );
    }

    @Transactional
    public Map<String, List<Integer>> deleteByIds(String ids) {
        List<Integer> idList = Utility.validateAndParse(ids, MAX_IDS_LENGTH);
        
        if (idList.isEmpty()) {
            return Map.of("ids", new ArrayList<>());
        }

        // Find all existing resources in one query
        List<Integer> existingIds = resourceService.findExistingIds(idList);
        
        if (existingIds.isEmpty()) {
            return Map.of("ids", new ArrayList<>());
        }

        // Fetch all resources to get S3 keys
        List<Resource> resources = resourceService.findAllById(existingIds);
        List<Integer> removedIds = new ArrayList<>();
        
        // Delete from S3 and collect successfully deleted IDs
        for (Resource resource : resources) {
            try {
                fileStorageService.delete(resource.getS3Key());
                removedIds.add(resource.getId());
                log.debug("Deleted S3 object with key: {}", resource.getS3Key());
            } catch (Exception e) {
                log.error("Failed to delete S3 object with key: {}. Error: {}", 
                    resource.getS3Key(), e.getMessage());
                // Continue with other deletions even if one fails
            }
        }

        if (removedIds.isEmpty()) {
            log.warn("No resources were deleted from S3");
            return Map.of("ids", new ArrayList<>());
        }

        // Delete from database
        resourceService.deleteAllByIdInBatch(removedIds);
        log.info("Deleted resource Ids from database: {}", removedIds);

        // Delete from song-service
        Map<String, List<Integer>> songIdsMap;
        try {
            songIdsMap = songMetadataWebClient.deleteSongMetadata(removedIds);
            log.info("Deleted Song metadata Ids: {}", songIdsMap.get("ids"));
        } catch (WebClientResponseException e) {
            log.error("Failed to delete song metadata for resource ids: {}. Error: {}", 
                removedIds, e.getMessage());
            throw new RuntimeException("Failed to delete song metadata: " + e.getMessage(), e);
        }

        // Return the song IDs that were deleted
        List<Integer> deletedSongIds = songIdsMap != null && songIdsMap.containsKey("ids") 
            ? songIdsMap.get("ids") 
            : new ArrayList<>();
        
        return Map.of("ids", deletedSongIds);
    }



    private ResourceCreateResponse resourceToResourceResponse(Resource resource) {
        return new ResourceCreateResponse(resource.getId(),resource.getS3Key());
    }

}
