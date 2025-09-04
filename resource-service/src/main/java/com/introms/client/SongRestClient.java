package com.introms.client;

import com.introms.dto.SongMetadataCreateRequest;
import com.introms.dto.SongMetadataCreateResponse;
import com.introms.dto.SongMetadataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongRestClient {
    private final WebClient songWebClient;

    public SongMetadataCreateResponse createSongMetadata(SongMetadataCreateRequest songMetadataCreateRequest) {
        return songWebClient.post()
                .uri("/songs")
                .bodyValue(songMetadataCreateRequest)
                .retrieve()
                .bodyToMono(SongMetadataCreateResponse.class)
                .block();
    }


    public Map<String,List<Integer>> deleteSongMetadata(List<Integer> ids) {
        String csv = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        return songWebClient.delete()
                .uri(uriBuilder ->
                        uriBuilder.path("/songs")
                                .queryParam("id", csv)
                                .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String,List<Integer>>>() {})
                .block();
    }
}
