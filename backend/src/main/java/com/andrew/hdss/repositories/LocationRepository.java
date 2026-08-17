package com.andrew.hdss.repositories;

import com.andrew.hdss.models.Location;
import com.andrew.hdss.models.enums.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findByParentId(Long parentId);

    List<Location> findByParentIsNull(); // roots — typically just "Kenya"

    List<Location> findByType(LocationType type);

    Optional<Location> findByParentIdAndName(Long parentId, String name);

    @Query("SELECT l FROM Location l WHERE l.ancestorPath LIKE CONCAT(:pathPrefix, '%')")
    List<Location> findAllDescendants(@Param("pathPrefix") String pathPrefix);

    boolean existsByParentIdAndName(Long parentId, String name);
}