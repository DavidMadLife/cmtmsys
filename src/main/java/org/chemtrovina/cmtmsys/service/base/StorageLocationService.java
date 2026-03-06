package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.StorageLocation;

import java.util.List;

public interface StorageLocationService {

    StorageLocation getById(Long id);

    StorageLocation getByCode(String code);

    List<StorageLocation> getAllActive();

    StorageLocation create(StorageLocation location);

    StorageLocation update(Long id, StorageLocation location);

    void deactivate(Long id);
}