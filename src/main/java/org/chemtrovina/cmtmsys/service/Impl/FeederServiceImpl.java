package org.chemtrovina.cmtmsys.service.Impl;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chemtrovina.cmtmsys.model.*;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.repository.base.*;
import org.chemtrovina.cmtmsys.service.base.FeederService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

@Service
public class FeederServiceImpl implements FeederService {

    private final FeederRepository feederRepository;
    private final ModelLineRepository modelLineRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public FeederServiceImpl(
            FeederRepository feederRepository,
            ModelLineRepository modelLineRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository) {
        this.feederRepository = feederRepository;
        this.modelLineRepository = modelLineRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public void addFeeder(Feeder feeder) {
        feederRepository.add(feeder);
    }

    @Override
    public void updateFeeder(Feeder feeder) {
        feederRepository.update(feeder);
    }

    @Override
    public void deleteFeederById(int feederId) {
        // 1. Lấy feeder trước khi xóa (để biết modelLineId)
        Feeder feeder = feederRepository.findById(feederId);
        if (feeder == null) {
            throw new RuntimeException("Feeder không tồn tại.");
        }

        int modelLineId = feeder.getModelLineId();

        // 2. Xóa feeder
        feederRepository.deleteById(feederId);

        // 3. Kiểm tra xem còn feeder nào khác trong modelLine không
        List<Feeder> remaining = feederRepository.findByModelLineId(modelLineId);
        if (remaining.isEmpty()) {
            // 4. Nếu không còn feeder → xóa luôn modelLine
            modelLineRepository.deleteById(modelLineId);
        }
    }


    @Override
    public Feeder getFeederById(int id) {
        return feederRepository.findById(id);
    }

    @Override
    public List<Feeder> getAllFeeders() {
        return feederRepository.findAll();
    }

    @Override
    public List<Feeder> getFeedersByModelAndLine(int productId, int warehouseId) {
        return feederRepository.findByModelAndLine(productId, warehouseId);
    }

    @Override
    public List<Feeder> searchFeeders(int productId, int warehouseId, String feederCode, String sapCode) {
        return feederRepository.search(productId, warehouseId, feederCode, sapCode);
    }

    @Override
    public void importFeedersFromExcel(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String productCode = getCellValue(row.getCell(0));
                String lineName = getCellValue(row.getCell(1));
                String modelTypeStr = getCellValue(row.getCell(2));
                String feederCode = getCellValue(row.getCell(3));
                String sapCode = getCellValue(row.getCell(4));
                String qtyStr = getCellValue(row.getCell(5));
                int qty = 0;
                if (!qtyStr.isBlank()) {
                    qty = (int) Double.parseDouble(qtyStr);
                }

                String machine = getCellValue(row.getCell(6));

                if (productCode.isBlank() || lineName.isBlank() || modelTypeStr.isBlank()
                        || feederCode.isBlank() || sapCode.isBlank()) continue;



                ModelType modelType;
                try {
                    modelType = ModelType.valueOf(modelTypeStr.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    modelType = ModelType.NONE;
                }

                Product product = productRepository.findByCodeAndModelType(productCode, modelType);
                if (product == null) {
                    product = new Product();
                    product.setProductCode(productCode);
                    product.setModelType(modelType);
                    productRepository.add(product);
                    product = productRepository.findByCodeAndModelType(productCode, modelType);
                }

                Warehouse warehouse = warehouseRepository.findByName(lineName);
                if (warehouse == null) {
                    warehouse = new Warehouse();
                    warehouse.setName(lineName);
                    warehouseRepository.add(warehouse);
                    warehouse = warehouseRepository.findByName(lineName);
                }

                ModelLine modelLine = modelLineRepository.findOrCreateModelLine(
                        product.getProductId(), warehouse.getWarehouseId());

                Feeder existingFeeder = feederRepository.findByModelLineIdAndFeederCodeAndSapCode(
                        modelLine.getModelLineId(), feederCode, sapCode
                );

                if (existingFeeder == null) {
                    Feeder feeder = new Feeder();
                    feeder.setModelLineId(modelLine.getModelLineId());
                    feeder.setFeederCode(feederCode);
                    feeder.setSapCode(sapCode);
                    feeder.setQty(qty);
                    feeder.setMachine(machine); // <-- Gán machine
                    feederRepository.add(feeder);
                } else {
                    existingFeeder.setQty(qty);
                    existingFeeder.setMachine(machine); // <-- Cập nhật machine nếu cần
                    feederRepository.update(existingFeeder);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đọc file Excel: " + e.getMessage(), e);
        }
    }


    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
