package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.ProductionGapLog;
import java.util.List;

public interface ProductionGapLogRepository {
    void add(ProductionGapLog gapLog);
    void deleteById(long gapId);
    ProductionGapLog findById(long gapId);
    List<ProductionGapLog> findAll();
    List<ProductionGapLog> findByShift(int warehouseId, String start, String end);
}
