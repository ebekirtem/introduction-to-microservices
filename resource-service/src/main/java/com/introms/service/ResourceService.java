package com.introms.service;

import com.introms.util.Utility;
import com.introms.client.SongRestClient;
import com.introms.dto.SongMetadataCreateRequest;
import com.introms.dto.SongMetadataResponse;
import com.introms.entity.Resource;
import com.introms.exception.BadRequestException;
import com.introms.exception.ResourceNotFoundException;
import com.introms.repository.ResourceRepository;
import dto.ResourceCreateRequest;
import dto.ResourceCreateResponse;
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
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceService {
    private final Tika tika;
    private final AutoDetectParser autoDetectParser;
    private final ResourceRepository resourceRepository;
    private final SongRestClient songRestClient;

    public ResourceCreateResponse saveResource(ResourceCreateRequest request) {
        try {
            String detect = tika.detect(request.fileData());
            log.info("Tika detection: {}", detect);

            if (!detect.equalsIgnoreCase("audio/mpeg")) {
                throw new BadRequestException("Invalid Mp3");
            }

            Resource resource = resourceCreateRequesttoResource(request);
            Resource savedResource = resourceRepository.save(resource);
            ResourceCreateResponse resourceCreateResponse = resourceToResourceResponse(savedResource);

            Metadata metadata = extractMetadata(savedResource);
            SongMetadataCreateRequest songMetadataCreateRequest = buildSongCreateRequest(savedResource.getId(), metadata);
            SongMetadataResponse songMetadataResponse = songRestClient.createSong(songMetadataCreateRequest);
            log.info("SongResponse: {}", songMetadataResponse);
            return resourceCreateResponse;
        } catch (IOException | SAXException | TikaException e) {
            throw new RuntimeException(e);
        }
    }

    public byte [] getResource(String sid) {
        boolean idValid = Utility.isIdValid(sid);
        if(!idValid){
            throw new BadRequestException("The provided ID is invalid (e.g., contains letters, decimals, is negative, or zero)");
        }

        Integer id=Integer.parseInt(sid);
        Resource resource = resourceRepository.findById(id).orElseThrow(() ->
                    new ResourceNotFoundException("Resource with the specified ID does not exist"));
        return resource.getContent();

    }

    public List<Integer> deleteByIds(String ids){
     if(!Utility.isValidIds(ids)){
         throw new BadRequestException(" CSV string format is invalid or exceeds length restrictions");
     }

        List<Integer> listOfId = Arrays.stream(ids.split(","))
                .map(Integer::parseInt)
                .distinct().toList();

        List<Integer> existingIds = resourceRepository.findExistingIds(listOfId);

        if(!existingIds.isEmpty()){
            resourceRepository.deleteAllByIdInBatch(existingIds);
        }

        //existingIds.forEach(id->);
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
        resource.setContent(resourceCreateRequest.fileData());
        return resource;
    }

    private ResourceCreateResponse resourceToResourceResponse(Resource resource) {
        return new ResourceCreateResponse(resource.getId());
    }

}
