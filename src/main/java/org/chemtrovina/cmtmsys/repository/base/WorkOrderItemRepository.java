package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.WorkOrderItem;

import java.util.List;

public interface WorkOrderItemRepository {
    void add(WorkOrderItem item);
    void update(WorkOrderItem item);
    void delete(int itemId);
    void deleteByWorkOrderId(int workOrderId);
    List<WorkOrderItem> findByWorkOrderId(int workOrderId);
}
