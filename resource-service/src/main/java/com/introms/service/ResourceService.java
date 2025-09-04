package com.introms.service;

import com.introms.exception.InvalidMp3Exception;
import com.introms.util.Utility;
import com.introms.client.SongMetadataWebClient;
import com.introms.dto.SongMetadataCreateRequest;
import com.introms.entity.Resource;
import com.introms.exception.ResourceNotFoundException;
import com.introms.repository.ResourceRepository;
import dto.ResourceCreateRequest;
import dto.ResourceCreateResponse;
import dto.ResourceResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

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
            if (request.data() == null || request.data().length == 0) {
                throw new InvalidMp3Exception("The request body is invalid MP3");
            }

            String detected = tika.detect(request.data());
            log.info("Tika detection: {}", detected);

            if (!detected.equalsIgnoreCase(CONTENT_TYPE)) {
                throw new InvalidMp3Exception("Invalid file format: application/json. Only MP3 files are allowed");
            }

            Resource resource = resourceCreateRequesttoResource(request);
            Resource savedResource = resourceRepository.saveAndFlush(resource);

            Metadata metadata = extractMetadata(savedResource);

            SongMetadataCreateRequest songMetadataCreateRequest = buildSongCreateRequest(savedResource.getId(), metadata);

           songMetadataWebClient.createSongMetadata(songMetadataCreateRequest);

            return resourceToResourceResponse(savedResource);
    }

    public ResourceResponse getResource(String sid) {
        boolean idValid = Utility.isIdValid(sid);
        if (!idValid) {
            throw new InvalidMp3Exception(String.format("Invalid value '%s' for ID. Must be a positive integer", sid));
        }

        Integer id = Integer.parseInt(sid);
        Resource resource = resourceRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format("Resource with ID=%d not found", id)));
        return new ResourceResponse(resource.getContent(), tika.detect(resource.getContent()));
    }

    public List<Integer> deleteByIds(String ids) {
        List<Integer> idList = Utility.validateAndParse(ids, MAX_IDS_LENGTH);
        List<Integer> existingIds = resourceRepository.findExistingIds(idList);

        if (!existingIds.isEmpty()) {
            resourceRepository.deleteAllByIdInBatch(existingIds);
            songMetadataWebClient.deleteSongMetadata(existingIds);
        }

        return existingIds;
    }

    private Metadata extractMetadata(Resource resource)  {
        try {
            Metadata metadata = new Metadata();
            autoDetectParser.parse(new ByteArrayInputStream(resource.getContent()), new DefaultHandler(), metadata, new ParseContext());
            return metadata;
        } catch (IOException  | SAXException | TikaException e) {
            throw new RuntimeException("Metadata extraction failed");
        }
    }

    private SongMetadataCreateRequest buildSongCreateRequest(Integer id, Metadata metadata) {
        String name = metadata.get("dc:title"); // Title (Name)
        String artist = metadata.get("xmpDM:artist"); // Artist
        String album = metadata.get("xmpDM:album"); // Album
        String duration = Utility.formatDuration(metadata.get("xmpDM:duration")); // Duration (mm:ss format)
        String year = metadata.get("xmpDM:releaseDate"); // Release Year

        // Map parsed metadata into a SongCreateRequest DTO
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
