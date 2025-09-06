package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.dto.PcbPerformanceLogHistoryDTO;
import org.chemtrovina.cmtmsys.model.PcbPerformanceLog;
import org.chemtrovina.cmtmsys.model.enums.ModelType;

import java.time.LocalDateTime;
import java.util.List;

public interface PcbPerformanceLogRepository {
    void add(PcbPerformanceLog log);
    List<PcbPerformanceLog> findAll();
    List<PcbPerformanceLog> findByProductId(int productId);
    List<PcbPerformanceLog> findByWarehouseId(int warehouseId);
    boolean existsByCarrierId(String carrierId);
    public List<PcbPerformanceLogHistoryDTO> searchLogDTOs(String modelCode, ModelType modelType,
                                                           LocalDateTime from, LocalDateTime to);
    boolean existsByFileName(String fileName);

    List<PcbPerformanceLogHistoryDTO> getLogsByWarehouseAndDateRange(int warehouseId, LocalDateTime start, LocalDateTime end);


}
