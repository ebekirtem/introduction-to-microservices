package com.introms.service;

import com.introms.exception.IdValidationException;
import com.introms.exception.InvalidMp3Exception;
import com.introms.util.Utility;
import com.introms.client.SongMetadataWebClient;
import com.introms.dto.SongMetadataCreateRequest;
import com.introms.entity.Resource;
import com.introms.exception.ResourceNotFoundException;
import com.introms.repository.ResourceRepository;
import com.introms.dto.ResourceCreateRequest;
import com.introms.dto.ResourceCreateResponse;
import com.introms.dto.ResourceResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceService {
    private final Tika tika;
    private final AutoDetectParser autoDetectParser;
    private final ResourceRepository resourceRepository;
    private final SongMetadataWebClient songMetadataWebClient;

    private static final String CONTENT_TYPE = "audio/mpeg";
    private static final Integer MAX_IDS_LENGTH = 200;

    @Transactional
    public ResourceCreateResponse saveResource(ResourceCreateRequest request) {
        if (!request.contentType().equalsIgnoreCase(CONTENT_TYPE)) {
            throw new InvalidMp3Exception("Invalid file format: application/json. Only MP3 files are allowed");
        }

        if (request.data() == null || request.data().length == 0) {
            throw new InvalidMp3Exception("The request body is invalid MP3");
        }

        Resource resource = resourceCreateRequesttoResource(request);
        Resource savedResource = resourceRepository.saveAndFlush(resource);

        log.info("Resource saved with ID:{}", savedResource.getId());

        Metadata metadata = extractMetadata(savedResource);

        SongMetadataCreateRequest songMetadataCreateRequest = buildSongCreateRequest(savedResource.getId(), metadata);
        log.info("SongMetadataCreateRequest has been build: {}",songMetadataCreateRequest);

        songMetadataWebClient.createSongMetadata(songMetadataCreateRequest);

        log.info("SongMetadata has been created on song-service");
        return resourceToResourceResponse(savedResource);
    }

    public ResourceResponse getResource(String sid) {
        log.info("Resource requested with ID:{}", sid);
        boolean idValid = Utility.isIdValid(sid);
        if (!idValid) {
            throw new IdValidationException(String.format("Invalid value '%s' for ID. Must be a positive integer", sid));
        }

        Integer id = Integer.parseInt(sid);
        Resource resource = resourceRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format("Resource with ID=%d not found", id)));
        log.info("Resource found with content length:{}", resource.getContent().length);
        return new ResourceResponse(resource.getContent(), tika.detect(resource.getContent()));
    }

    @Transactional
    public Map<String, List<Integer>> deleteByIds(String ids) {
        List<Integer> idList = Utility.validateAndParse(ids, MAX_IDS_LENGTH);
        List<Integer> existingIds = resourceRepository.findExistingIds(idList);

        if (!existingIds.isEmpty()) {
            resourceRepository.deleteAllByIdInBatch(existingIds);
            log.info("Deleted resource Ids:{}",existingIds);
            try {
                Map<String, List<Integer>> songIdsMap = songMetadataWebClient.deleteSongMetadata(existingIds);
                log.info("Deleted Song metadata Ids:{}",songIdsMap.values());
            } catch (WebClientResponseException e) {
                log.warn("Roll backed delete resource with ids:{}",existingIds);
                throw new RuntimeException(e);
            }
        }

        return Map.of("ids",existingIds);
    }

    private Metadata extractMetadata(Resource resource) {
        try {
            Metadata metadata = new Metadata();
            autoDetectParser.parse(new ByteArrayInputStream(resource.getContent()), new DefaultHandler(), metadata, new ParseContext());
            return metadata;
        } catch (IOException | SAXException | TikaException e) {
            throw new RuntimeException("Metadata extraction failed");
        }
    }

    private SongMetadataCreateRequest buildSongCreateRequest(Integer id, Metadata metadata) {
        String name = metadata.get("dc:title"); // Title (Name)
        String artist = metadata.get("xmpDM:artist"); // Artist
        String album = metadata.get("xmpDM:album"); // Album
        String duration = Utility.formatDuration(metadata.get("xmpDM:duration")); // Duration (mm:ss format)
        String year = metadata.get("xmpDM:releaseDate"); // Release Year

        // Map parsed metadata into a SongMetadataCreateRequest DTO
        return new SongMetadataCreateRequest(
                id, name, artist, album, duration, year
        );
    }


    private Resource resourceCreateRequesttoResource(ResourceCreateRequest resourceCreateRequest) {
        Resource resource = new Resource();
        resource.setContent(resourceCreateRequest.data());
        return resource;
    }

    private ResourceCreateResponse resourceToResourceResponse(Resource resource) {
        return new ResourceCreateResponse(resource.getId());
    }

}
