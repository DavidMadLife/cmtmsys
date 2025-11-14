package org.chemtrovina.cmtmsys.service.Impl;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chemtrovina.cmtmsys.dto.MaterialDto;
import org.chemtrovina.cmtmsys.model.Material;
import org.chemtrovina.cmtmsys.model.TransferLog;
import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.repository.base.MaterialRepository;
import org.chemtrovina.cmtmsys.service.base.MaterialService;
import org.chemtrovina.cmtmsys.service.base.TransferLogService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;
    private final WarehouseService warehouseService;
    private final TransferLogService transferLogService;

    public MaterialServiceImpl(MaterialRepository materialRepository,
                               WarehouseService warehouseService,
                               TransferLogService transferLogService) {
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
    public void updateMaterialDto(MaterialDto dto) {
        Material material = materialRepository.findById(dto.getMaterialId());
        if (material == null) {
            throw new IllegalArgumentException("Không tìm thấy vật liệu với ID: " + dto.getMaterialId());
        }

        material.setQuantity(dto.getQuantity());
        material.setLot(dto.getLot());
        material.setMaker(dto.getMaker()); // ✅ thêm cập nhật maker
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

        TransferLog log = new TransferLog(0, barcode, fromId, targetWarehouseId,
                LocalDateTime.now(), "", employeeId);
        transferLogService.addTransfer(log);

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
                if (row.getRowNum() == 0) continue;

                Cell sapCell = row.getCell(0);
                Cell specCell = row.getCell(1);
                Cell rollCell = row.getCell(2);
                Cell qtyCell = row.getCell(3);
                Cell lotCell = row.getCell(4);   // ✅ cột Lot
                Cell makerCell = row.getCell(5); // ✅ cột Maker (mới)

                if (sapCell == null || rollCell == null || qtyCell == null) continue;

                String sapCode = getCellString(sapCell);
                String spec = getCellString(specCell);
                String rollCode = getCellString(rollCell);
                String lot = getCellString(lotCell);
                String maker = getCellString(makerCell);

                int quantity = 0;
                if (qtyCell.getCellType() == CellType.NUMERIC) {
                    quantity = (int) qtyCell.getNumericCellValue();
                }

                if (sapCode.isEmpty() || rollCode.isEmpty() || quantity <= 0) continue;

                Material existing = materialRepository.findByRollCode(rollCode);
                if (existing != null) {
                    existing.setSapCode(sapCode);
                    existing.setSpec(spec);
                    existing.setQuantity(quantity);
                    existing.setLot(lot.isEmpty() ? existing.getLot() : lot);
                    existing.setMaker(maker.isEmpty() ? existing.getMaker() : maker); // ✅ thêm maker
                    existing.setCreatedAt(LocalDateTime.now());
                    existing.setEmployeeId(employeeId);
                    materialRepository.updateIgnoreTreeId(existing);
                } else {
                    Material material = new Material(
                            0, sapCode, rollCode, quantity,
                            warehouseId, LocalDateTime.now(), spec, employeeId, null,
                            lot.isEmpty() ? null : lot,
                            maker.isEmpty() ? null : maker // ✅ thêm maker
                    );
                    materialRepository.add(material);
                }

                processedCount++;
            }

            System.out.println("Đã import " + processedCount + " dòng dữ liệu (có Maker).");
        } catch (IOException e) {
            throw new RuntimeException("Không thể đọc file Excel", e);
        }
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }

    @Override
    public List<MaterialDto> searchMaterials(String sapCode, String barCode,
                                             LocalDateTime fromDate, LocalDateTime toDate, Integer warehouseId) {
        List<Material> materials = materialRepository.search(sapCode, barCode, fromDate, toDate, warehouseId);
        Map<Integer, String> warehouseMap = warehouseService.getAllWarehouses().stream()
                .collect(Collectors.toMap(Warehouse::getWarehouseId, Warehouse::getName));

        return materials.stream()
                .map(m -> new MaterialDto(
                        m.getMaterialId(),
                        m.getSapCode(),
                        m.getRollCode(),
                        m.getQuantity(),
                        warehouseMap.getOrDefault(m.getWarehouseId(), "N/A"),
                        m.getSpec(),
                        m.getCreatedAt(),
                        m.getEmployeeId(),
                        m.getLot(),
                        m.getMaker() // ✅ thêm maker
                ))
                .toList();
    }

    @Override
    public List<Material> getMaterialsByIds(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return materialRepository.findByIds(ids);
    }

    @Override
    public List<Material> findBySapCode(String sapCode) {
        return materialRepository.findBySapCode(sapCode);
    }

    @Override
    public List<Material> getByTreeId(int treeId) {
        return materialRepository.getByTreeId(treeId);
    }

    @Override
    public List<MaterialDto> getAllMaterialDtos() {
        List<Material> materials = materialRepository.findAll();
        Map<Integer, String> warehouseMap = warehouseService.getAllWarehouses().stream()
                .collect(Collectors.toMap(Warehouse::getWarehouseId, Warehouse::getName));

        return materials.stream()
                .map(m -> new MaterialDto(
                        m.getMaterialId(),
                        m.getSapCode(),
                        m.getRollCode(),
                        m.getQuantity(),
                        warehouseMap.getOrDefault(m.getWarehouseId(), "N/A"),
                        m.getSpec(),
                        m.getCreatedAt(),
                        m.getEmployeeId(),
                        m.getLot(),
                        m.getMaker() // ✅ thêm maker
                ))
                .toList();
    }

    @Override
    public Material addMaterialToTree(int treeId, String rollCode) {
        Material material = materialRepository.findByRollCode(rollCode);
        if (material == null) {
            throw new IllegalArgumentException("Không tìm thấy cuộn với mã: " + rollCode);
        }

        material.setTreeId(treeId);
        materialRepository.update(material);
        return material;
    }

    @Override
    public Map<String, Material> getMaterialsByRollCodes(Set<String> rollCodes) {
        if (rollCodes == null || rollCodes.isEmpty()) return Collections.emptyMap();
        List<Material> materials = materialRepository.findByRollCodes(new ArrayList<>(rollCodes));
        return materials.stream().collect(Collectors.toMap(Material::getRollCode, m -> m));
    }
}
