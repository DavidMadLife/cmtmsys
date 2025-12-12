package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.dto.MaterialUsage;
import org.chemtrovina.cmtmsys.model.MaterialConsumeDetailLog;
import org.chemtrovina.cmtmsys.model.PcbPerformanceLog;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.repository.base.MaterialConsumeDetailLogRepository;
import org.chemtrovina.cmtmsys.service.base.MaterialConsumeDetailLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MaterialConsumeDetailLogServiceImpl implements MaterialConsumeDetailLogService {

    private final MaterialConsumeDetailLogRepository repository;

    public MaterialConsumeDetailLogServiceImpl(MaterialConsumeDetailLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addLog(MaterialConsumeDetailLog log) {
        if (log == null) return;
        repository.insert(log);
    }

    @Override
    public List<MaterialConsumeDetailLog> getLogsByPlanItemAndDate(int planItemId, LocalDate runDate) {
        return repository.findByPlanItemAndDate(planItemId, runDate);
    }

    @Override
    public void deleteLogsByPlanItemAndDate(int planItemId, LocalDate runDate) {
        repository.deleteByPlanItemAndDate(planItemId, runDate);
    }

    @Override
    public List<MaterialConsumeDetailLog> getDetailsBySourceLog(int sourceLogId) {
        return repository.findBySourceLogId(sourceLogId);
    }

    @Override
    public List<String> consumeByAoiLog(PcbPerformanceLog log) {
        if (log == null) return List.of();

        try {
            return repository.consumeByAoiLog(log);
            // ⚠ repository phải return List<String>
        }
        catch (Exception e) {
            System.err.println("[MaterialConsumeDetailLogService] ❌ Lỗi khi trừ liệu: " + e.getMessage());
            return List.of("❌ Lỗi khi trừ liệu: " + e.getMessage());
        }
    }


    @Override
    public List<MaterialUsage> getMaterialUsageBySourceLog(int sourceLogId) {
        return repository.findUsageBySourceLogId(sourceLogId);
    }

    @Override
    public List<MaterialUsage> getMaterialUsageByCarrier(String carrierId) {
        return repository.findUsageByCarrierId(carrierId);
    }

    @Override
    public List<MaterialUsage> searchUsage(String modelCode, ModelType modelType, LocalDateTime from, LocalDateTime to) {
        return repository.searchUsage(modelCode, modelType, from, to);
    }


}
