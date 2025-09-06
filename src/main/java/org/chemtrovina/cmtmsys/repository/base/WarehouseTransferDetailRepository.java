package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.WarehouseTransfer;
import org.chemtrovina.cmtmsys.model.WarehouseTransferDetail;

import java.util.List;
import java.util.Optional;

public interface WarehouseTransferDetailRepository {
    void add(WarehouseTransferDetail detail);
    List<WarehouseTransferDetail> findByTransferId(int transferId);
    List<WarehouseTransferDetail> findByWorkOrderId(int workOrderId);
    boolean existsByTransferIdAndRollCode(int transferId, String rollCode);
    Optional<WarehouseTransfer> findByFields(int fromWarehouseId, int toWarehouseId, int workOrderId, String employeeId);
    void deleteByWorkOrderId(int workOrderId);
    Optional<WarehouseTransferDetail> findByRollCode(String rollCode);
    void updateReturnInfo(String rollCode, int actualReturned, boolean active);
    void reopenReturn(String rollCode);

    void deleteByTransferIdAndRollCode(int transferId, String rollCode);


}
