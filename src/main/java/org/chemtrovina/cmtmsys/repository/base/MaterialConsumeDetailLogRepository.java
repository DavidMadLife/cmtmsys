package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.MaterialConsumeDetailLog;

import java.time.LocalDate;
import java.util.List;

public interface MaterialConsumeDetailLogRepository {
    void insert(MaterialConsumeDetailLog log);
    List<MaterialConsumeDetailLog> findByPlanItemAndDate(int planItemId, LocalDate runDate);
    void deleteByPlanItemAndDate(int planItemId, LocalDate runDate);
}
