package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.dto.MaterialRequirementDto;
import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.model.WorkOrder;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface WorkOrderService {
    void addWorkOrder(WorkOrder workOrder);
    void updateWorkOrder(WorkOrder workOrder);
    void deleteWorkOrder(int id);
    WorkOrder getWorkOrderById(int id);
    WorkOrder getWorkOrderByCode(String code);
    List<WorkOrder> getAllWorkOrders();
    List<MaterialRequirementDto> getMaterialRequirements(String workOrderCode);
    List<MaterialRequirementDto> getGroupedMaterialRequirements(String workOrderCode);
    List<WorkOrder> getWorkOrdersByDateRange(LocalDate from, LocalDate to);
    String generateNewWorkOrderCode(LocalDate date);
    void createWorkOrderWithItems(String description, Map<Integer, Integer> productIdToQuantity);
    Map<Product, Integer> getWorkOrderItems(int workOrderId);
    void updateWorkOrderWithItems(int workOrderId, String description, Map<Integer, Integer> products);
    int getWorkOrderIdByCode(String code);

    void importFromExcel(File excelFile);








}
