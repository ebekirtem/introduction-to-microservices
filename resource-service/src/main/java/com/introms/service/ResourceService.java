package com.introms.service;

import com.introms.entity.Resource;
import com.introms.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResourceService {
    private final ResourceRepository resourceRepository;

    public Resource saveResource(Resource resource) {
        return resourceRepository.saveAndFlush(resource);
    }

    public Optional<Resource> findById(Integer id) {
        return resourceRepository.findById(id);
    }

    public List<Integer> findExistingIds(List<Integer> ids) {
        return resourceRepository.findExistingIds(ids);
    }

    public List<Resource> findAllById(List<Integer> ids) {
        return resourceRepository.findAllById(ids);
    }

    public void deleteAllByIdInBatch(List<Integer> ids){
        resourceRepository.deleteAllByIdInBatch(ids);
    }


}
