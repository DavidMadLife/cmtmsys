package org.chemtrovina.cmtmsys.service.Impl;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chemtrovina.cmtmsys.dto.MaterialRequirementDto;
import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.model.WorkOrder;
import org.chemtrovina.cmtmsys.model.WorkOrderItem;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.repository.base.WarehouseTransferDetailRepository;
import org.chemtrovina.cmtmsys.repository.base.WarehouseTransferRepository;
import org.chemtrovina.cmtmsys.repository.base.WorkOrderItemRepository;
import org.chemtrovina.cmtmsys.repository.base.WorkOrderRepository;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.chemtrovina.cmtmsys.service.base.WarehouseTransferService;
import org.chemtrovina.cmtmsys.service.base.WorkOrderService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderRepository repository;
    private final WorkOrderItemRepository workOrderItemRepository;
    private final WarehouseTransferRepository warehouseTransferRepository;
    private final WarehouseTransferDetailRepository warehouseTransferDetailRepository;

    private final JdbcTemplate jdbcTemplate; // Thêm dòng này

    public WorkOrderServiceImpl(WorkOrderRepository repository, JdbcTemplate jdbcTemplate, WorkOrderItemRepository workOrderItemRepository, WarehouseTransferRepository warehouseTransferRepository, WarehouseTransferDetailRepository warehouseTransferDetailRepository) {
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
        this.workOrderItemRepository = workOrderItemRepository;
        this.warehouseTransferRepository = warehouseTransferRepository;
        this.warehouseTransferDetailRepository = warehouseTransferDetailRepository;
    }


    @Override
    public void addWorkOrder(WorkOrder workOrder) {
        repository.add(workOrder);
    }

    @Override
    public void updateWorkOrder(WorkOrder workOrder) {
        repository.update(workOrder);
    }

    @Override
    public void deleteWorkOrder(int workOrderId) {
        // Xoá theo đúng thứ tự phụ thuộc
        warehouseTransferDetailRepository.deleteByWorkOrderId(workOrderId);
        warehouseTransferRepository.deleteByWorkOrderId(workOrderId);
        workOrderItemRepository.deleteByWorkOrderId(workOrderId);
        repository.delete(workOrderId);
    }



    @Override
    public WorkOrder getWorkOrderById(int id) {
        return repository.findById(id);
    }

    @Override
    public WorkOrder getWorkOrderByCode(String code) {
        return repository.findByCode(code);
    }

    @Override
    public List<WorkOrder> getAllWorkOrders() {
        return repository.findAll();
    }

    @Override
    public List<MaterialRequirementDto> getMaterialRequirements(String workOrderCode) {
        String sql = """
        SELECT 
            w.WorkOrderCode,
            p.ProductCode,
            i.Quantity AS ProductQty,
            b.SAPPN,
            b.Quantity AS BOMPerUnit,
            i.Quantity * b.Quantity AS RequiredQty
        FROM WorkOrders w
        JOIN WorkOrderItems i ON w.WorkOrderID = i.WorkOrderID
        JOIN Products p ON i.ProductID = p.ProductID
        JOIN ProductBOM b ON p.ProductID = b.ProductID
        WHERE w.WorkOrderCode = ?
    """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new MaterialRequirementDto(
                rs.getString("WorkOrderCode"),
                rs.getString("ProductCode"),
                rs.getInt("ProductQty"),
                rs.getString("SAPPN"),
                rs.getInt("BOMPerUnit"),
                rs.getInt("RequiredQty")
        ), workOrderCode);
    }

    @Override
    public List<WorkOrder> getWorkOrdersByDateRange(LocalDate from, LocalDate to) {
        List<WorkOrder> all = repository.findAll();
        return all.stream()
                .filter(wo -> {
                    LocalDate created = wo.getCreatedDate().toLocalDate();
                    return (created.isEqual(from) || created.isAfter(from)) &&
                            (created.isEqual(to) || created.isBefore(to));
                })
                .collect(Collectors.toList());
    }

    @Override
    public String generateNewWorkOrderCode(LocalDate date) {
        String prefix = "WO" + date.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        String latest = repository.findMaxCodeByDate(date);
        int index = 1;
        if (latest != null && latest.startsWith(prefix)) {
            String[] parts = latest.split("-");
            if (parts.length == 2) {
                try {
                    index = Integer.parseInt(parts[1]) + 1;
                } catch (NumberFormatException ignored) {}
            }
        }
        return prefix + "-" + index;
    }

    @Override
    public void createWorkOrderWithItems(String description, Map<Integer, Integer> productIdToQuantity) {
        String code = generateNewWorkOrderCode(LocalDate.now());
        WorkOrder wo = new WorkOrder();
        wo.setWorkOrderCode(code);
        wo.setDescription(description);
        wo.setCreatedDate(LocalDateTime.now());
        wo.setUpdatedDate(LocalDateTime.now());
        repository.add(wo);

        int workOrderId = repository.findByCode(code).getWorkOrderId();
        for (Map.Entry<Integer, Integer> entry : productIdToQuantity.entrySet()) {
            WorkOrderItem item = new WorkOrderItem();
            item.setWorkOrderId(workOrderId);
            item.setProductId(entry.getKey());
            item.setQuantity(entry.getValue());
            item.setCreatedDate(LocalDateTime.now());
            item.setUpdatedDate(LocalDateTime.now());
            workOrderItemRepository.add(item);
        }
    }

    @Override
    public Map<Product, Integer> getWorkOrderItems(int workOrderId) {
        List<WorkOrderItem> items = workOrderItemRepository.findByWorkOrderId(workOrderId);

        return items.stream().collect(Collectors.toMap(
                item -> jdbcTemplate.queryForObject(
                        "SELECT * FROM Products WHERE ProductID = ?",
                        (rs, rowNum) -> {
                            Product p = new Product();
                            p.setProductId(rs.getInt("ProductID"));
                            p.setProductCode(rs.getString("ProductCode"));
                            p.setDescription(rs.getString("Description"));

                            // ✅ Parse thêm modelType nếu có
                            String modelTypeStr = rs.getString("ModelType");
                            if (modelTypeStr != null && !modelTypeStr.isBlank()) {
                                p.setModelType(ModelType.valueOf(modelTypeStr));
                            }

                            return p;
                        },
                        item.getProductId()
                ),
                WorkOrderItem::getQuantity
        ));
    }


    @Override
    public void updateWorkOrderWithItems(int workOrderId, String description, Map<Integer, Integer> productMap) {
        // 1. Cập nhật mô tả + thời gian
        WorkOrder wo = repository.findById(workOrderId);
        wo.setDescription(description);
        wo.setUpdatedDate(LocalDateTime.now());
        repository.update(wo);

        // 2. Xóa các item cũ
        workOrderItemRepository.deleteByWorkOrderId(workOrderId);

        // 3. Thêm lại item mới
        for (Map.Entry<Integer, Integer> entry : productMap.entrySet()) {
            WorkOrderItem item = new WorkOrderItem();
            item.setWorkOrderId(workOrderId);
            item.setProductId(entry.getKey());
            item.setQuantity(entry.getValue());
            item.setCreatedDate(LocalDateTime.now());
            item.setUpdatedDate(LocalDateTime.now());
            workOrderItemRepository.add(item);
        }
    }

    @Override
    public int getWorkOrderIdByCode(String code) {
        return repository.findIdByCode(code);
    }

    @Override
    public List<MaterialRequirementDto> getGroupedMaterialRequirements(String workOrderCode) {
        String sql = """
        SELECT 
            b.SAPPN,
            SUM(i.Quantity * b.Quantity) AS RequiredQty
        FROM WorkOrders w
        JOIN WorkOrderItems i ON w.WorkOrderID = i.WorkOrderID
        JOIN Products p ON i.ProductID = p.ProductID
        JOIN ProductBOM b ON p.ProductID = b.ProductID
        WHERE w.WorkOrderCode = ?
        GROUP BY b.SAPPN
    """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new MaterialRequirementDto(
                workOrderCode,
                "",                 // productCode không cần ở dạng gộp
                0,                  // productQty không cần
                rs.getString("SAPPN"),
                0,                  // bomPerUnit không cần
                rs.getInt("RequiredQty")
        ), workOrderCode);
    }

    @Override
    public void importFromExcel(File excelFile) {
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            String woCode = generateNewWorkOrderCode(LocalDate.now());
            repository.insertWorkOrder(woCode, "Tạo từ import Excel");
            int workOrderId = repository.getWorkOrderIdByCode(woCode);

            int success = 0;
            int failed = 0;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String productCode = getCellString(row.getCell(0));
                int quantity = (int) row.getCell(1).getNumericCellValue();

                List<Integer> productIds = repository.findProductIdsByCode(productCode);
                if (productIds.isEmpty()) {
                    System.out.println("⚠ Không tìm thấy Product: " + productCode);
                    failed++;
                    continue;
                }

                repository.insertWorkOrderItem(workOrderId, productIds.get(0), quantity);
                success++;
            }

            System.out.printf("✅ Import thành công %d dòng, bỏ qua %d%n", success, failed);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi import Excel: " + e.getMessage(), e);
        }
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            default -> "";
        };
    }

}
