package com.introms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${resource-service.base-url}")
    private String resourceServiceBaseUrl;

    @Value("${song-service.base-url}")
    private String songServiceBaseUrl;

    @Bean
    public WebClient resourceServiceWebClient() {
        return WebClient.builder()
                .baseUrl(resourceServiceBaseUrl)
                .build();
    }

    @Bean
    public WebClient songServiceWebClient() {
        return WebClient.builder()
                .baseUrl(songServiceBaseUrl)
                .build();
    }
}
