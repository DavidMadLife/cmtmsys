package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.ProductionPlanItem;

import java.util.List;

public interface ProductionPlanItemService {
    void addPlanItem(ProductionPlanItem item);
    void updatePlanItem(ProductionPlanItem item);
    void deletePlanItem(int itemId);
    List<ProductionPlanItem> getItemsByPlanId(int planId);
}
