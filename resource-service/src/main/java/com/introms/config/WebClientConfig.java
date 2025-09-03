package com.introms.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.util.concurrent.TimeUnit;


@Configuration
@EnableConfigurationProperties(SongServiceConfigProperties.class)
@RequiredArgsConstructor
public class WebClientConfig {

    private final SongServiceConfigProperties configProperties;

    @Bean
    public WebClient songServiceClient(WebClient.Builder builder) {
        var httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) configProperties.getTimeout().getConnect().toMillis())
                .doOnConnected(connection ->
                        connection
                                .addHandlerLast(new ReadTimeoutHandler(
                                        configProperties.getTimeout().getRead().toMillis(),
                                        TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(
                                        configProperties.getTimeout().getRead().toMillis(),
                                        TimeUnit.MILLISECONDS))
                );

        // Build WebClient with baseUrl and HttpClient
        return builder
                .baseUrl(configProperties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Accept","application/json")
                .filter((request, next) ->
                        next.exchange(request)
                                .retryWhen(Retry.backoff(
                                        configProperties.getRetry().getMaxAttempts(),
                                        configProperties.getRetry().getBackoff().getInitial()
                                )
                                        .maxBackoff(configProperties.getRetry().getBackoff().getMax())

                                )
                )
                .build();
    }
}