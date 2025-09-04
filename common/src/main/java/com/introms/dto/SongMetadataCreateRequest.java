package com.introms.dto;

import jakarta.validation.constraints.*;

public record SongMetadataCreateRequest(
        @NotNull(message = "Resource ID must not be null")
        Integer id,

        @NotBlank(message = "Name is required")
        @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
        String name,

        @NotBlank(message = "Artist is required")
        @Size(min = 1, max = 100, message = "Artist must be between 1 and 100 characters")
        String artist,

        @NotBlank(message = "Album is required")
        @Size(min = 1, max = 100, message = "Album must be between 1 and 100 characters")
        String album,

        @NotNull(message = "Duration is required")
        @Pattern(regexp = "^\\d{2}:(?:[0-5]\\d)$", message = "Duration must be in mm:ss format with leading zeros")
        String duration,

        @NotBlank(message = "Year is required")

        @Pattern(regexp = "^(19\\d{2}|20\\d{2})$", message = "year bust be between 1900 and 2099")
        String year
) {
}