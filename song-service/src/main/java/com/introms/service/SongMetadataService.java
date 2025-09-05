package com.introms.service;

import com.introms.dto.SongMetadataCreateRequest;
import com.introms.dto.SongMetadataCreateResponse;
import com.introms.dto.SongMetadataResponse;
import com.introms.entity.SongMetadata;
import com.introms.exception.IdValidationException;
import com.introms.exception.ResourceNotFoundException;
import com.introms.exception.SongMetadataAlreadyExistException;
import com.introms.exception.MetadataValidationException;
import com.introms.repository.SongMetadataRepository;
import com.introms.util.Utility;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongMetadataService {
    private final SongMetadataRepository songMetadataRepository;
    private final Validator validator;

    private static final Integer MAX_IDS_LENGTH = 200;

    public SongMetadataCreateResponse createSong(final SongMetadataCreateRequest songMetadataCreateRequest) {

        log.info("Song metadata create request : {}",songMetadataCreateRequest);
        Map<String, String> errors = validate(songMetadataCreateRequest);

        if (!errors.isEmpty()) {
            throw new MetadataValidationException("Validation error", errors);
        }

        if(songMetadataRepository.findById(songMetadataCreateRequest.id()).isPresent()){
            throw new SongMetadataAlreadyExistException(String.format("Metadata for resource ID=%d already exists", songMetadataCreateRequest.id()));
        }

        SongMetadata songMetadata = toSongMetadata(songMetadataCreateRequest);

        SongMetadata savedSongMetadata = songMetadataRepository.save(songMetadata);

        log.info("Song metadata successfully saved: {}",savedSongMetadata);
        return toSongMetadataCreateResponse(savedSongMetadata);
    }

    private Map<String, String> validate(SongMetadataCreateRequest songMetadataCreateRequest) {
        Set<ConstraintViolation<SongMetadataCreateRequest>> violations = validator.validate(songMetadataCreateRequest);

        Map<String, String> details = new LinkedHashMap<>();

        for (ConstraintViolation<SongMetadataCreateRequest> v : violations) {
            String field = v.getPropertyPath().toString();
            details.put(field, v.getMessage());
        }
        return details;
    }

    public SongMetadataResponse getSongMetadata(String sid) {
        boolean idValid = Utility.isIdValid(sid);
        if (!idValid) {
            throw new IdValidationException(String.format("Invalid value '%s' for ID. Must be a positive integer", sid));
        }

        Integer id = Integer.parseInt(sid);
        SongMetadata songMetadata = songMetadataRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format("Song metadata with ID=%d not found", id)));
        return toSongMetadataResponse(songMetadata);

    }

    public Map<String, List<Integer>> deleteByIds(String csvIds) {
        List<Integer> idList = Utility.validateAndParse(csvIds, MAX_IDS_LENGTH);
        List<Integer> existingIds = songMetadataRepository.findExistingIds(idList);

        if (!existingIds.isEmpty()) {
            songMetadataRepository.deleteAllByIdInBatch(existingIds);
            log.info("Deleted Song metadata Ids:{}",existingIds);
        }

        return Map.of("ids",existingIds);
    }

    private SongMetadata toSongMetadata(final SongMetadataCreateRequest songMetadataCreateRequest) {
        return SongMetadata.builder()
                .id(songMetadataCreateRequest.id())
                .name(songMetadataCreateRequest.name())
                .artist(songMetadataCreateRequest.artist())
                .album(songMetadataCreateRequest.album())
                .duration(songMetadataCreateRequest.duration())
                .year(songMetadataCreateRequest.year())
                .build();

    }

    private SongMetadataResponse toSongMetadataResponse(final SongMetadata songMetadata) {
        return new SongMetadataResponse(
                songMetadata.getId(),
                songMetadata.getName(),
                songMetadata.getArtist(),
                songMetadata.getAlbum(),
                songMetadata.getDuration(),
                songMetadata.getYear());
    }

    private SongMetadataCreateResponse toSongMetadataCreateResponse(final SongMetadata songMetadata) {
        return new SongMetadataCreateResponse(
                songMetadata.getId());
    }
}
