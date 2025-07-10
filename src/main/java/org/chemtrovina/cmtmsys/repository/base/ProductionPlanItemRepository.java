package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.ProductionPlanItem;

import java.util.List;

public interface ProductionPlanItemRepository {
    void add(ProductionPlanItem item);
    void update(ProductionPlanItem item);
    void deleteById(int itemId);
    List<ProductionPlanItem> findByPlanId(int planId);
}
