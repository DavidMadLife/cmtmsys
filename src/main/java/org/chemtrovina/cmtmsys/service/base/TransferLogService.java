package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.dto.TransferLogDto;
import org.chemtrovina.cmtmsys.model.TransferLog;

import java.time.LocalDateTime;
import java.util.List;

public interface TransferLogService {
    void addTransfer(TransferLog log);
    List<TransferLog> getAllTransfers();
    List<TransferLog> getTransfersByRollCode(String rollCode);
    List<TransferLogDto> getAllTransferLogDtos();
    List<TransferLog> searchTransfers(String sapCode, String barcode, Integer fromWarehouse, Integer toWarehouse, LocalDateTime fromDate, LocalDateTime toDate);
}
