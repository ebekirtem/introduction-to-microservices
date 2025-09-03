package com.introms.repository;


import com.introms.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource,Integer> {

    @Query("select r.id from Resource r  where r.id in :ids")
    List<Integer> findExistingIds(@Param("ids") List<Integer> ids);
}
