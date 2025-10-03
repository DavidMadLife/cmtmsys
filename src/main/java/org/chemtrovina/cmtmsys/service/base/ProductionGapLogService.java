package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.ProductionGapLog;
import java.util.List;

public interface ProductionGapLogService {
    void addGapLog(ProductionGapLog gapLog);
    void deleteGapLogById(long id);
    ProductionGapLog getGapLogById(long id);
    List<ProductionGapLog> getAllGapLogs();
    List<ProductionGapLog> getGapLogsByShift(int warehouseId, String start, String end);
}
