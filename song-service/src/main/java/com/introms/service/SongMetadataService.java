package com.introms.service;

import com.introms.dto.SongMetadataCreateRequest;
import com.introms.dto.SongMetadataCreateResponse;
import com.introms.dto.SongMetadataResponse;
import com.introms.entity.SongMetadata;
import com.introms.exception.BadRequestException;
import com.introms.exception.ResourceNotFoundException;
import com.introms.exception.ValidationException;
import com.introms.repository.SongMetadataRepository;
import com.introms.util.Utility;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SongMetadataService {
    private final SongMetadataRepository songMetadataRepository;
    private final Validator validator;

    private static final Integer MAX_IDS_LENGTH=200;

    public SongMetadataCreateResponse createSong(final SongMetadataCreateRequest songMetadataCreateRequest) {
        Map<String, String> errors = validate(songMetadataCreateRequest);

        if(!errors.isEmpty()){
            throw new ValidationException("Validation error",errors);
        }

        SongMetadata songMetadata = toSongMetadata(songMetadataCreateRequest);

        SongMetadata savedSongMetadata = songMetadataRepository.save(songMetadata);
        return toSongMetadataCreateResponse(savedSongMetadata);
    }

    private Map<String,String> validate(SongMetadataCreateRequest songMetadataCreateRequest) {
        Set<ConstraintViolation<SongMetadataCreateRequest>> violations=validator.validate(songMetadataCreateRequest);

        Map<String,String> details=new LinkedHashMap<>();

        for(ConstraintViolation<SongMetadataCreateRequest> v:violations){
            String field = v.getPropertyPath().toString();
            details.put(field,v.getMessage());
        }
        return details;
    }

    public SongMetadataResponse getSongMetadata(String sid) {
        boolean idValid = Utility.isIdValid(sid);
        if(!idValid){
            throw new BadRequestException(String.format("The provided ID: '%s'  is invalid (e.g., contains letters, decimals, is negative, or zero)",sid));
        }

        Integer id=Integer.parseInt(sid);
        SongMetadata songMetadata = songMetadataRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format("Song metadata with ID=%d not found",id)));
        return toSongMetadataResponse(songMetadata);

    }

    public List<Integer> deleteByIds(String ids){
        List<Integer> idList = Utility.validateAndParse(ids, MAX_IDS_LENGTH);
        List<Integer> existingIds = songMetadataRepository.findExistingIds(idList);

        if(!existingIds.isEmpty()){
            songMetadataRepository.deleteAllByIdInBatch(existingIds);
        }

        return existingIds;
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
