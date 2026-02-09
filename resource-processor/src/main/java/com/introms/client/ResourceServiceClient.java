package com.introms.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
public class ResourceServiceClient {
    private final WebClient resourceServiceWebClient;

    @Retryable(
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 5000),
        retryFor = {WebClientException.class, RuntimeException.class},
        noRetryFor = {IllegalArgumentException.class}
    )
    public byte[] getResource(Integer resourceId) {
        log.info("Fetching resource with ID: {}", resourceId);
        
        try {
            byte[] resourceData = resourceServiceWebClient
                    .get()
                    .uri("/resources/{id}", resourceId)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), response -> {
                        log.error("Client error fetching resource with ID: {}. Status: {}", 
                            resourceId, response.statusCode());
                        return response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new IllegalArgumentException("Client error: " + response.statusCode() + " - " + body)));
                    })
                    .bodyToMono(byte[].class)
                    .block();
            
            if (resourceData == null) {
                throw new RuntimeException("Resource data is null for ID: " + resourceId);
            }
            
            log.info("Successfully fetched resource with ID: {}, size: {} bytes", resourceId, resourceData.length);
            return resourceData;
        } catch (WebClientResponseException e) {
            // 4xx errors should not be retried
            if (e.getStatusCode().is4xxClientError()) {
                log.error("Client error (4xx) fetching resource with ID: {}. Status: {}, Message: {}", 
                    resourceId, e.getStatusCode(), e.getMessage());
                throw new IllegalArgumentException("Client error: " + e.getMessage(), e);
            }
            // 5xx and other errors should be retried
            log.error("Server error fetching resource with ID: {}. Status: {}, Message: {}", 
                resourceId, e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Failed to fetch resource: " + e.getMessage(), e);
        } catch (WebClientException e) {
            // Network/timeout errors should be retried
            log.error("Network error fetching resource with ID: {}", resourceId, e);
            throw new RuntimeException("Network error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error fetching resource with ID: {}", resourceId, e);
            throw new RuntimeException("Failed to fetch resource: " + e.getMessage(), e);
        }
    }
}
