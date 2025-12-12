package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.dto.MaterialUsage;
import org.chemtrovina.cmtmsys.model.MaterialConsumeDetailLog;
import org.chemtrovina.cmtmsys.model.PcbPerformanceLog;
import org.chemtrovina.cmtmsys.model.enums.ModelType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MaterialConsumeDetailLogRepository {
    void insert(MaterialConsumeDetailLog log);
    List<MaterialConsumeDetailLog> findByPlanItemAndDate(int planItemId, LocalDate runDate);
    void deleteByPlanItemAndDate(int planItemId, LocalDate runDate);
    List<MaterialConsumeDetailLog> findBySourceLogId(int sourceLogId);
    List<String> consumeByAoiLog(PcbPerformanceLog log);

    List<MaterialUsage> findUsageBySourceLogId(int sourceLogId);
    List<MaterialUsage> findUsageByCarrierId(String carrierId);
    List<MaterialUsage> searchUsage(String modelCode, ModelType modelType, LocalDateTime from, LocalDateTime to);
}
