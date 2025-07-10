package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.ProductionPlanItem;
import org.chemtrovina.cmtmsys.repository.base.ProductionPlanItemRepository;
import org.chemtrovina.cmtmsys.service.base.ProductionPlanItemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductionPlanItemServiceImpl implements ProductionPlanItemService {

    private final ProductionPlanItemRepository repository;

    public ProductionPlanItemServiceImpl(ProductionPlanItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addPlanItem(ProductionPlanItem item) {
        repository.add(item);
    }

    @Override
    public void updatePlanItem(ProductionPlanItem item) {
        repository.update(item);
    }

    @Override
    public void deletePlanItem(int itemId) {
        repository.deleteById(itemId);
    }

    @Override
    public List<ProductionPlanItem> getItemsByPlanId(int planId) {
        return repository.findByPlanId(planId);
    }
}
