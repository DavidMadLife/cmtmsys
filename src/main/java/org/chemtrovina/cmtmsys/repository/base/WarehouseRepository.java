package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.Warehouse;

import java.util.List;
import java.util.Optional;

public interface WarehouseRepository {
    void add(Warehouse warehouse);
    void update(Warehouse warehouse);
    void deleteById(int warehouseId);
    Warehouse findById(int warehouseId);
    List<Warehouse> findAll();

    Warehouse findByName(String name);
    String getWarehouseNameByTransferId(int transferId, boolean isFrom);
    Integer findIdByLineToken(String lineToken);
    Warehouse findByNameContainingNumber(int number);

    Integer getIdByName(String name);
}
