package com.introms.controller;

import com.introms.dto.SongMetadataCreateRequest;
import com.introms.dto.SongMetadataCreateResponse;
import com.introms.dto.SongMetadataResponse;
import com.introms.service.SongMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/songs")
@RequiredArgsConstructor
public class SongMetadataController {

    private final SongMetadataService songMetadataService;
    @PostMapping
    public ResponseEntity<SongMetadataCreateResponse> createSongMetadata(@RequestBody SongMetadataCreateRequest songMetadataCreateRequest) {
        log.info("SongCreateRequest: {}", songMetadataCreateRequest);
       return ResponseEntity.ok(songMetadataService.createSong(songMetadataCreateRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongMetadataResponse> getSongMetadata(@PathVariable("id") String id) {
        return ResponseEntity.ok(songMetadataService.getSongMetadata(id));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Integer>>> deleteSongMetadata(@RequestParam("id") String ids) {
        List<Integer> integers = songMetadataService.deleteByIds(ids);
        return ResponseEntity.ok(Map.of("ids",integers));
    }
}