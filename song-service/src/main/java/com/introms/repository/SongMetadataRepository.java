package com.introms.repository;

import com.introms.entity.SongMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SongMetadataRepository extends JpaRepository<SongMetadata,Integer> {

    @Query("select sm.id from SongMetadata sm  where sm.id in :ids")
    List<Integer> findExistingIds(@Param("ids") List<Integer> ids);
}
