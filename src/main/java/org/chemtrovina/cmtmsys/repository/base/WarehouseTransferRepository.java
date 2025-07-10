package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.WarehouseTransfer;

import java.util.List;
import java.util.Map;

public interface WarehouseTransferRepository {
    void add(WarehouseTransfer transfer);
    List<WarehouseTransfer> getAll();
    WarehouseTransfer findById(int id);
    Map<String,Integer> getScannedQuantitiesByWO(int workOrderId);
    void deleteByWorkOrderId(int workOrderId);
    Map<String, Integer> getActualReturnedByWorkOrderId(int workOrderId);
    int getFromWarehouseIdByTransferId(int transferId);

}
