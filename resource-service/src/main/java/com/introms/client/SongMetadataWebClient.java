package com.introms.client;

import com.introms.dto.SongMetadataCreateRequest;
import com.introms.exception.InvalidMp3Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongMetadataWebClient {
    private final WebClient songWebClient;

    public void createSongMetadata(SongMetadataCreateRequest songMetadataCreateRequest) {

        log.info("Webclient: {}",songWebClient.toString());

        songWebClient.post()
                .uri("/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(songMetadataCreateRequest)
                .retrieve()
                .onStatus(s -> s.value() == HttpStatus.BAD_REQUEST.value(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(b -> Mono.error(new InvalidMp3Exception("Invalid Mp3"))))
                .onStatus(HttpStatusCode::isError, clientResponse -> clientResponse.bodyToMono(String.class)
                        .flatMap(b -> Mono.error(new RuntimeException("SongMetadata Service failed:" + b))))
                .toBodilessEntity()
                .block();
    }


    public Map<String, List<Integer>> deleteSongMetadata(List<Integer> ids) {
        String csv = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        return songWebClient.delete()
                .uri(uriBuilder ->
                        uriBuilder.path("/songs")
                                .queryParam("id", csv)
                                .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, ClientResponse::createException)
                .bodyToMono(new ParameterizedTypeReference<Map<String, List<Integer>>>() {
                })
                .block();
    }
}
