package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.dto.PcbPerformanceLogHistoryDTO;
import org.chemtrovina.cmtmsys.model.PcbPerformanceLog;
import org.chemtrovina.cmtmsys.model.enums.ModelType;

import java.time.LocalDateTime;
import java.util.List;

public interface PcbPerformanceLogService {
    void saveLog(PcbPerformanceLog log);
    boolean isAlreadyProcessed(String carrierId);
    List<PcbPerformanceLog> getAllLogs();
    List<PcbPerformanceLog> getLogsByProductId(int productId);
    List<PcbPerformanceLog> getLogsByWarehouseId(int warehouseId);
    List<PcbPerformanceLogHistoryDTO> searchLogs(String modelCode, ModelType modelType,
                                                 LocalDateTime from, LocalDateTime to);

    boolean isFileAlreadySaved(String fileName);
    List<PcbPerformanceLogHistoryDTO> fetchPerformanceGoodModules(String lineName, LocalDateTime start, LocalDateTime end);


}
