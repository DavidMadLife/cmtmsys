package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.TransferLog;

import java.time.LocalDateTime;
import java.util.List;

public interface TransferLogRepository {
    void add(TransferLog log);
    List<TransferLog> findAll();
    List<TransferLog> findByRollCode(String rollCode);
    List<TransferLog> search(String sapCode, String barcode, Integer fromWarehouseId, Integer toWarehouseId, LocalDateTime fromDate, LocalDateTime toDate);

}
