package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.Warehouse;

import java.util.List;

public interface WarehouseRepository {
    void add(Warehouse warehouse);
    void update(Warehouse warehouse);
    void deleteById(int warehouseId);
    Warehouse findById(int warehouseId);
    List<Warehouse> findAll();

    Warehouse findByName(String name);
    String getWarehouseNameByTransferId(int transferId, boolean isFrom);
}
