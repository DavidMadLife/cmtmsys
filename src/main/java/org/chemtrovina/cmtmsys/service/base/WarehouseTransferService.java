package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.WarehouseTransfer;
import org.chemtrovina.cmtmsys.model.WarehouseTransferDetail;
import org.chemtrovina.cmtmsys.repository.base.WarehouseTransferDetailRepository;

import java.util.List;
import java.util.Map;

public interface WarehouseTransferService {
    void createTransfer(WarehouseTransfer transfer, List<WarehouseTransferDetail> details);
    List<WarehouseTransfer> getAllTransfers();
    List<WarehouseTransferDetail> getDetailsByTransferId(int transferId);
    WarehouseTransferDetailRepository getDetailRepository();
    List<WarehouseTransferDetail> getDetailsByWorkOrderId(int workOrderId);

    WarehouseTransfer findExistingTransfer(int fromWarehouseId, int toWarehouseId, int workOrderId, String employeeId);
    Map<String,Integer> getScannedQuantitiesByWO(int workOrderId);
    Map<String, Integer> getActualReturnedByWorkOrderId(int workOrderId);
    int getFromWarehouseIdByTransferId(int transferId);


}
