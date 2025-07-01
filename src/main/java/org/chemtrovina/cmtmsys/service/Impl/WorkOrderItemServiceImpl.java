package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.WorkOrderItem;
import org.chemtrovina.cmtmsys.repository.base.WorkOrderItemRepository;
import org.chemtrovina.cmtmsys.service.base.WorkOrderItemService;

import java.util.List;

public class WorkOrderItemServiceImpl implements WorkOrderItemService {

    private final WorkOrderItemRepository repository;

    public WorkOrderItemServiceImpl(WorkOrderItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addItem(WorkOrderItem item) {
        repository.add(item);
    }

    @Override
    public void updateItem(WorkOrderItem item) {
        repository.update(item);
    }

    @Override
    public void deleteItem(int itemId) {
        repository.delete(itemId);
    }

    @Override
    public void deleteItemsByWorkOrderId(int workOrderId) {
        repository.deleteByWorkOrderId(workOrderId);
    }

    @Override
    public List<WorkOrderItem> getItemsByWorkOrderId(int workOrderId) {
        return repository.findByWorkOrderId(workOrderId);
    }
}
