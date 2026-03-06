package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.StorageLocation;
import org.chemtrovina.cmtmsys.model.enums.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, Long> {

    Optional<StorageLocation> findByCode(String code);

    List<StorageLocation> findByTypeAndIsActive(LocationType type, Boolean isActive);

    List<StorageLocation> findByIsActive(Boolean isActive);

    Optional<StorageLocation> findByCodeAndIsActive(String code, Boolean isActive);
}