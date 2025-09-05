package com.introms.dto;

public record SongMetadataResponse(Integer id, String name, String artist,
                                   String album, String duration, String year) {
}
