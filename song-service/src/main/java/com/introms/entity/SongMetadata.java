package com.introms.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="song_metadata")
public class SongMetadata {
    @Id
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String artist;

    @Column(nullable = false, length = 100)
    private String album;

    @Column(nullable = false)
    private String duration;

    @Column(nullable = false)
    private Integer year;

    @Builder.Default
    @Column(nullable = false)
    private OffsetDateTime createdAt=OffsetDateTime.now(ZoneOffset.UTC);
}