package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.WorkOrderItem;

import java.util.List;

public interface WorkOrderItemService {
    void addItem(WorkOrderItem item);
    void updateItem(WorkOrderItem item);
    void deleteItem(int itemId);
    void deleteItemsByWorkOrderId(int workOrderId);
    List<WorkOrderItem> getItemsByWorkOrderId(int workOrderId);
}
