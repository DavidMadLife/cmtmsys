package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.dto.MaterialUsage;
import org.chemtrovina.cmtmsys.model.MaterialConsumeDetailLog;
import org.chemtrovina.cmtmsys.model.PcbPerformanceLog;
import org.chemtrovina.cmtmsys.model.enums.ModelType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MaterialConsumeDetailLogService {
    void addLog(MaterialConsumeDetailLog log);

    List<MaterialConsumeDetailLog> getLogsByPlanItemAndDate(int planItemId, LocalDate runDate);

    void deleteLogsByPlanItemAndDate(int planItemId, LocalDate runDate);
    List<MaterialConsumeDetailLog> getDetailsBySourceLog(int sourceLogId);

    void consumeByAoiLog(PcbPerformanceLog log);

    List<MaterialUsage> getMaterialUsageBySourceLog(int sourceLogId);
    List<MaterialUsage> getMaterialUsageByCarrier(String carrierId);
    // (tùy chọn) lọc theo khoảng thời gian + model
    List<MaterialUsage> searchUsage(String modelCode, ModelType modelType, LocalDateTime from, LocalDateTime to);
}

