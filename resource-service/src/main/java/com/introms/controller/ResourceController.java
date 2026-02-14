package com.introms.controller;

import com.introms.service.ResourceCoordinator;
import com.introms.dto.ResourceCreateRequest;
import com.introms.dto.ResourceCreateResponse;
import com.introms.dto.ResourceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("resources")
@RequiredArgsConstructor
public class ResourceController {

 private final ResourceCoordinator resourceCoordinator;
    @PostMapping
    public ResponseEntity<ResourceCreateResponse> uploadResource(@RequestBody(required = false) byte[] mp3Data ,
                                                                 @RequestHeader(value = HttpHeaders.CONTENT_TYPE,required = false) String contentType) {
        ResourceCreateRequest resourceCreateRequest =new ResourceCreateRequest(mp3Data,contentType);
        return ResponseEntity.ok(resourceCoordinator.saveResource(resourceCreateRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte []> getResource(@PathVariable("id") String id) {
        ResourceResponse resource = resourceCoordinator.getResource(id);
        return ResponseEntity.ok().contentType(MediaType.valueOf(resource.contentType()))
                .body(resource.data());
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Integer>>> deleteResources(@RequestParam("id") String ids) {
        return ResponseEntity.ok(resourceCoordinator.deleteByIds(ids));
    }
}
