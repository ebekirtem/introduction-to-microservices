package com.introms.client;

import com.introms.dto.SongMetadataCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongServiceClient {
    private final WebClient songServiceWebClient;

    @Retryable(
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 5000),
        retryFor = {WebClientException.class, RuntimeException.class},
        noRetryFor = {IllegalArgumentException.class}
    )
    public void createSongMetadata(SongMetadataCreateRequest request) {
        log.info("Creating song metadata for resource ID: {}", request.id());
        
        try {
            songServiceWebClient
                    .post()
                    .uri("/songs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response -> {
                        log.error("Client error creating song metadata for resource ID: {}. Status: {}", 
                            request.id(), response.statusCode());
                        return response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new IllegalArgumentException("Client error: " + response.statusCode() + " - " + body)));
                    })
                    .bodyToMono(Void.class)
                    .block();
            
            log.info("Successfully created song metadata for resource ID: {}", request.id());
        } catch (WebClientResponseException e) {
            // 4xx errors (validation, not found, etc.) should not be retried
            if (e.getStatusCode().is4xxClientError()) {
                log.error("Client error (4xx) creating song metadata for resource ID: {}. Status: {}, Message: {}", 
                    request.id(), e.getStatusCode(), e.getMessage());
                throw new IllegalArgumentException("Client error: " + e.getMessage(), e);
            }
            // 5xx and other errors should be retried
            log.error("Server error creating song metadata for resource ID: {}. Status: {}, Message: {}", 
                request.id(), e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Failed to create song metadata: " + e.getMessage(), e);
        } catch (WebClientException e) {
            // Network/timeout errors should be retried
            log.error("Network error creating song metadata for resource ID: {}", request.id(), e);
            throw new RuntimeException("Network error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error creating song metadata for resource ID: {}", request.id(), e);
            throw new RuntimeException("Failed to create song metadata: " + e.getMessage(), e);
        }
    }
}
