package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.WorkOrder;

import java.time.LocalDate;
import java.util.List;

public interface WorkOrderRepository {
    void add(WorkOrder workOrder);
    void update(WorkOrder workOrder);
    void delete(int workOrderId);
    WorkOrder findById(int workOrderId);
    WorkOrder findByCode(String workOrderCode);
    List<WorkOrder> findAll();
    String findMaxCodeByDate(LocalDate date);
    int findIdByCode(String workOrderCode);


}
