package com.introms.service;

import com.introms.client.ResourceServiceClient;
import com.introms.client.SongServiceClient;
import com.introms.dto.ResourceCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.metadata.Metadata;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceEventConsumer {

    private final ResourceServiceClient resourceServiceClient;
    private final MetadataExtractorService metadataExtractorService;
    private final SongServiceClient songServiceClient;

    @Bean
    public Consumer<ResourceCreatedEvent> resourceCreated() {
        return event -> {
            log.info("Received ResourceCreatedEvent - Resource ID: {}",
                event.resourceId());

            try {
                log.info("Fetching resource data for ID: {}", event.resourceId());
                byte[] resourceData = resourceServiceClient.getResource(event.resourceId());

                log.info("Extracting metadata from resource ID: {}", event.resourceId());
                Metadata metadata = metadataExtractorService.extractMetadata(resourceData);

                log.info("Building song metadata request for resource ID: {}", event.resourceId());
                var songMetadataRequest = metadataExtractorService.buildSongCreateRequest(
                    event.resourceId(), metadata);

                log.info("Saving song metadata to song-service for resource ID: {}", event.resourceId());
                songServiceClient.createSongMetadata(songMetadataRequest);

                log.info("Successfully processed ResourceCreatedEvent for resource ID: {}", event.resourceId());
            } catch (Exception e) {
                log.error("Failed to process ResourceCreatedEvent for resource ID: {}. " +
                    "Event will be sent to DLQ after max attempts. Error: {}",
                    event.resourceId(), e.getMessage(), e);
                // Throw exception to trigger DLQ after max attempts
                throw new RuntimeException(
                    String.format("Failed to process resource event for ID: %d. Error: %s",
                        event.resourceId(), e.getMessage()),
                    e);
            }
        };
    }
}
