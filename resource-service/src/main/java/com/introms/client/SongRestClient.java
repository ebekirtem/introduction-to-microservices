package com.introms.client;

import com.introms.dto.SongMetadataCreateRequest;
import com.introms.dto.SongMetadataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongRestClient {
    private final WebClient songWebClient;

    public SongMetadataResponse createSong(SongMetadataCreateRequest songMetadataCreateRequest){
        return songWebClient.post()
                .uri("/songs")
                .bodyValue(songMetadataCreateRequest)
                .retrieve()
                .bodyToMono(SongMetadataResponse.class)
                .block();
    }
}
