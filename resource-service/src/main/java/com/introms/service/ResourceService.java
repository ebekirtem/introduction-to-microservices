package com.introms.service;

import com.introms.dto.SongMetadataCreateResponse;
import com.introms.util.Utility;
import com.introms.client.SongRestClient;
import com.introms.dto.SongMetadataCreateRequest;
import com.introms.entity.Resource;
import com.introms.exception.BadRequestException;
import com.introms.exception.ResourceNotFoundException;
import com.introms.repository.ResourceRepository;
import dto.ResourceCreateRequest;
import dto.ResourceCreateResponse;
import dto.ResourceResponse;
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
    private final SongRestClient songRestClient;

    private static final String CONTENT_TYPE = "audio/mpeg";
    private static final Integer MAX_IDS_LENGTH=200;

    public ResourceCreateResponse saveResource(ResourceCreateRequest request) {
        try {
            String detect = tika.detect(request.data());
            log.info("Tika detection: {}", detect);

            if (!detect.equalsIgnoreCase(CONTENT_TYPE)) {
                throw new BadRequestException("Invalid Mp3");
            }

            Resource resource = resourceCreateRequesttoResource(request);
            Resource savedResource = resourceRepository.save(resource);
            ResourceCreateResponse resourceCreateResponse = resourceToResourceResponse(savedResource);

            Metadata metadata = extractMetadata(savedResource);
            SongMetadataCreateRequest songMetadataCreateRequest = buildSongCreateRequest(savedResource.getId(), metadata);
            SongMetadataCreateResponse songMetadataCreateResponse = songRestClient.createSongMetadata(songMetadataCreateRequest);
            log.info("SongResponse: {}", songMetadataCreateResponse);
            return resourceCreateResponse;
        } catch (IOException | SAXException | TikaException e) {
            throw new RuntimeException(e);
        }
    }

    public ResourceResponse getResource(String sid) {
        boolean idValid = Utility.isIdValid(sid);
        if (!idValid) {
            throw new BadRequestException(String.format("The provided ID: '%s'  is invalid (e.g., contains letters, decimals, is negative, or zero)",sid));
        }

        Integer id = Integer.parseInt(sid);
        Resource resource = resourceRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format("Resource with ID=%d not found",id)));
        return new ResourceResponse(resource.getContent(), CONTENT_TYPE);
    }

    public List<Integer> deleteByIds(String ids) {
        List<Integer> idList = Utility.validateAndParse(ids, MAX_IDS_LENGTH);
        List<Integer> existingIds = resourceRepository.findExistingIds(idList);

        if (!existingIds.isEmpty()) {
            resourceRepository.deleteAllByIdInBatch(existingIds);
            songRestClient.deleteSongMetadata(existingIds);
        }

        return existingIds;
    }

    private Metadata extractMetadata(Resource resource) throws IOException, SAXException, TikaException {
        Metadata metadata = new Metadata();
        autoDetectParser.parse(new ByteArrayInputStream(resource.getContent()), new DefaultHandler(), metadata, new ParseContext());

        return metadata;
    }

    private SongMetadataCreateRequest buildSongCreateRequest(Integer id, Metadata metadata) {

        String name = metadata.get("dc:title"); // Title (Name)
        String artist = metadata.get("xmpDM:artist"); // Artist
        String album = metadata.get("xmpDM:album"); // Album
        String duration = Utility.formatDuration(metadata.get("xmpDM:duration")); // Duration (mm:ss format)
        String year = metadata.get("xmpDM:releaseDate"); // Release Year

        // Map parsed metadata into a SongCreateRequest DTO
        return new SongMetadataCreateRequest(
                id, // Resource ID
                name != null ? name : "Unknown Name", // Default if missing
                artist != null ? artist : "Unknown Artist", // Default if missing
                album != null ? album : "Unknown Album", // Default if missing
                duration, // Default to "00:00"
                Utility.parseYear(year) // Parsed year
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
