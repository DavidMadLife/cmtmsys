package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.Warehouse;

import java.util.List;
import java.util.Optional;

public interface WarehouseService {
    void addWarehouse(Warehouse warehouse);
    void updateWarehouse(Warehouse warehouse);
    void deleteWarehouseById(int id);
    Warehouse getWarehouseById(int id);
    List<Warehouse> getAllWarehouses();
    String getWarehouseNameByTransferId(int transferId, boolean isFrom);
    Integer getIdByName(String name);
    Warehouse findByName(String name);
    String getWarehouseNameById(int id);
}
