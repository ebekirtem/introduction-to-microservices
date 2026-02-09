package com.introms.service;

import com.introms.dto.SongMetadataCreateRequest;
import com.introms.util.Utility;
import lombok.RequiredArgsConstructor;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MetadataExtractorService {

    private final AutoDetectParser autoDetectParser;

    public Metadata extractMetadata(byte[] mp3Data) {
        try {
            Metadata metadata = new Metadata();
            autoDetectParser.parse(new ByteArrayInputStream(mp3Data), new DefaultHandler(), metadata, new ParseContext());
            return metadata;
        } catch (IOException | SAXException | TikaException e) {
            throw new RuntimeException("Metadata extraction failed: " + e.getMessage(), e);
        }
    }

    public SongMetadataCreateRequest buildSongCreateRequest(Integer id, Metadata metadata) {
        // Extract metadata with fallback to default values if null
        String name = metadata.get("dc:title");
        if (name == null || name.isBlank()) {
            name = "Unknown Title";
        }

        String artist = metadata.get("xmpDM:artist");
        if (artist == null || artist.isBlank()) {
            artist = "Unknown Artist";
        }

        String album = metadata.get("xmpDM:album");
        if (album == null || album.isBlank()) {
            album = "Unknown Album";
        }

        String duration = Utility.formatDuration(metadata.get("xmpDM:duration")); // Duration (mm:ss format)
        if (duration == null || duration.isBlank()) {
            duration = "00:00";
        }

        String year = metadata.get("xmpDM:releaseDate");
        if (year == null || year.isBlank()) {
            year = "2000"; // Default year
        }

        // Map parsed metadata into a SongMetadataCreateRequest DTO
        return new SongMetadataCreateRequest(
                id, name, artist, album, duration, year
        );
    }
}
