package com.introms.controller;

import com.introms.service.ResourceService;
import dto.ResourceCreateRequest;
import dto.ResourceCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("resources")
@RequiredArgsConstructor
public class ResourceController {

 private final ResourceService resourceService;
    @PostMapping
    public ResponseEntity<ResourceCreateResponse> uploadResource(@RequestBody byte[] mp3Data, @RequestHeader(value = "Content-Type") String contentType) {
        ResourceCreateRequest resourceCreateRequest =new ResourceCreateRequest(mp3Data);
        return ResponseEntity.ok(resourceService.saveResource(resourceCreateRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte []> getResource(@PathVariable("id") String id) {
        return ResponseEntity.ok(resourceService.getResource(id));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Integer>>> deleteResources(@RequestParam("id") String ids) {
        List<Integer> integers = resourceService.deleteByIds(ids);
        return ResponseEntity.ok(Map.of("ids",integers));
    }


}
