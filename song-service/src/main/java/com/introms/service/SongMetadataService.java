package com.introms.service;

import com.introms.dto.SongMetadataCreateRequest;
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
    public SongMetadataResponse createSong(final SongMetadataCreateRequest songMetadataCreateRequest) {
        Map<String, String> errors = validate(songMetadataCreateRequest);

        if(!errors.isEmpty()){
            throw new ValidationException("Validation error",errors);
        }

        SongMetadata songMetadata = toSongMetadata(songMetadataCreateRequest);

        SongMetadata savedSongMetadata = songMetadataRepository.save(songMetadata);
        return toSongMetadataResponse(savedSongMetadata);
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
            throw new BadRequestException("The provided ID is invalid (e.g., contains letters, decimals, is negative, or zero)");
        }

        Integer id=Integer.parseInt(sid);
        SongMetadata songMetadata = songMetadataRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("SongMetadata with the specified ID does not exist"));
        return toSongMetadataResponse(songMetadata);

    }

    public List<Integer> deleteByIds(String ids){
        if(!Utility.isValidIds(ids)){
            throw new BadRequestException(" CSV string format is invalid or exceeds length restrictions");
        }

        List<Integer> listOfId = Arrays.stream(ids.split(","))
                .map(Integer::parseInt)
                .distinct().toList();

        List<Integer> existingIds = songMetadataRepository.findExistingIds(listOfId);

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
}
