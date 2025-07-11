package org.chemtrovina.cmtmsys.service.Impl;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chemtrovina.cmtmsys.dto.MaterialDto;
import org.chemtrovina.cmtmsys.model.Material;
import org.chemtrovina.cmtmsys.model.TransferLog;
import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.repository.base.MaterialRepository;
import org.chemtrovina.cmtmsys.service.base.MaterialService;
import org.chemtrovina.cmtmsys.service.base.TransferLogService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;
    private final WarehouseService warehouseService;
    private final TransferLogService transferLogService;

    public MaterialServiceImpl(MaterialRepository materialRepository, WarehouseService warehouseService, TransferLogService transferLogService) {
        this.materialRepository = materialRepository;
        this.warehouseService = warehouseService;
        this.transferLogService = transferLogService;
    }

    @Override
    public void addMaterial(Material material) {
        materialRepository.add(material);
    }

    @Override
    public void updateMaterial(Material material) {
        materialRepository.update(material);
    }

    @Override
    public void deleteMaterialById(int id) {
        materialRepository.deleteById(id);
    }

    @Override
    public Material getMaterialById(int id) {
        return materialRepository.findById(id);
    }

    @Override
    public Material getMaterialByRollCode(String rollCode) {
        return materialRepository.findByRollCode(rollCode);
    }

    @Override
    public List<Material> getAllMaterials() {
        return materialRepository.findAll();
    }

    @Override
    public List<Material> getMaterialsByWarehouse(int warehouseId) {
        return materialRepository.findByWarehouseId(warehouseId);
    }

    @Override
    public Material transferMaterial(String barcode, String employeeId, int targetWarehouseId) {
        Material material = materialRepository.findByRollCode(barcode);
        if (material == null) return null;

        int fromId = material.getWarehouseId();
        if (fromId == targetWarehouseId) return material;

        // Tạo transfer log
        TransferLog log = new TransferLog(0, barcode, fromId, targetWarehouseId,
                LocalDateTime.now(), "", employeeId);
        transferLogService.addTransfer(log);

        // Cập nhật material
        material.setWarehouseId(targetWarehouseId);
        materialRepository.update(material);

        return material;
    }

    @Override
    public void importMaterialsFromExcel(File file, String employeeId) {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int warehouseId = warehouseService.getAllWarehouses().stream()
                    .filter(w -> w.getName().equalsIgnoreCase("SMT W/H"))
                    .map(Warehouse::getWarehouseId)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy kho SMT W/H"));

            int processedCount = 0;
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Bỏ qua dòng header

                String sapCode = row.getCell(0).getStringCellValue().trim();
                String spec = row.getCell(1).getStringCellValue().trim();
                String rollCode = row.getCell(2).getStringCellValue().trim();
                int quantity = (int) row.getCell(3).getNumericCellValue();

                Material existing = materialRepository.findByRollCode(rollCode);
                if (existing != null) {
                    // Update
                    existing.setSapCode(sapCode);
                    existing.setSpec(spec);
                    existing.setQuantity(quantity);
                    existing.setWarehouseId(warehouseId);
                    existing.setCreatedAt(LocalDateTime.now());
                    existing.setEmployeeId(employeeId);
                    materialRepository.update(existing);
                } else {
                    // Thêm mới
                    Material material = new Material(
                            0,
                            sapCode,
                            rollCode,
                            quantity,
                            warehouseId,
                            LocalDateTime.now(),
                            spec,
                            employeeId
                    );
                    materialRepository.add(material);
                }

                processedCount++;
            }

            System.out.println("Đã import " + processedCount + " dòng dữ liệu.");
        } catch (IOException e) {
            throw new RuntimeException("Không thể đọc file Excel", e);
        }
    }


    @Override
    public List<MaterialDto> searchMaterials(String sapCode, String barCode, LocalDateTime fromDate, LocalDateTime toDate, Integer warehouseId) {
        List<Material> materials = materialRepository.search(sapCode, barCode, fromDate, toDate, warehouseId);
        Map<Integer, String> warehouseMap = warehouseService.getAllWarehouses().stream()
                .collect(Collectors.toMap(Warehouse::getWarehouseId, Warehouse::getName));


        return materials.stream().map(m -> new MaterialDto(
                m.getSapCode(),
                m.getRollCode(),
                m.getQuantity(),
                warehouseMap.getOrDefault(m.getWarehouseId(), "N/A"),
                m.getSpec(),
                m.getCreatedAt(),
                m.getEmployeeId()
        )).toList();


    }

    @Override
    public List<Material> getMaterialsByIds(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return materialRepository.findByIds(ids);
    }





    @Override
    public List<MaterialDto> getAllMaterialDtos() {
        List<Material> materials = materialRepository.findAll();
        List<Warehouse> warehouses = warehouseService.getAllWarehouses();

        Map<Integer, String> warehouseMap = new HashMap<>();
        for (Warehouse w : warehouses) {
            warehouseMap.put(w.getWarehouseId(), w.getName());
        }

        return materials.stream()
                .map(m -> new MaterialDto(
                        m.getSapCode(),
                        m.getRollCode(),
                        m.getQuantity(),
                        warehouseMap.getOrDefault(m.getWarehouseId(), "N/A"),
                        m.getSpec(),
                        m.getCreatedAt(),
                        m.getEmployeeId()
                ))
                .toList();
    }

}
