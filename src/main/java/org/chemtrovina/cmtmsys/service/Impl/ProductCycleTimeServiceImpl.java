package org.chemtrovina.cmtmsys.service.Impl;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chemtrovina.cmtmsys.dto.ProductCycleTimeViewDto;
import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.model.ProductCycleTime;
import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.repository.base.ProductCycleTimeRepository;
import org.chemtrovina.cmtmsys.repository.base.ProductRepository;
import org.chemtrovina.cmtmsys.repository.base.WarehouseRepository;
import org.chemtrovina.cmtmsys.service.base.ProductCycleTimeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductCycleTimeServiceImpl implements ProductCycleTimeService {

    private final ProductCycleTimeRepository repository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public ProductCycleTimeServiceImpl(ProductCycleTimeRepository repository, ProductRepository productRepository, WarehouseRepository warehouseRepository) {
        this.repository = repository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public void add(ProductCycleTime pct) {
        if (pct == null) throw new IllegalArgumentException("pct is null");
        if (pct.getProductId() <= 0) throw new IllegalArgumentException("productId invalid");
        if (pct.getWarehouseId() <= 0) throw new IllegalArgumentException("warehouseId invalid");
        if (pct.getCtSeconds() == null || pct.getCtSeconds().signum() <= 0)
            throw new IllegalArgumentException("ctSeconds must be > 0");
        repository.add(pct);
    }

    @Override
    public ProductCycleTime getActive(int productId, int warehouseId) {
        return repository.getActive(productId, warehouseId);
    }

    @Override
    public List<ProductCycleTime> findHistory(int productId, int warehouseId) {
        return repository.findHistory(productId, warehouseId);
    }

    @Override
    public ProductCycleTime findById(int ctId) {
        return repository.findById(ctId);
    }

    @Override
    public int deactivateAllFor(int productId, int warehouseId) {
        return repository.deactivateAllFor(productId, warehouseId);
    }

    @Override
    public int deleteById(int ctId) {
        return repository.deleteById(ctId);
    }

    @Override
    public int deleteAllFor(int productId, int warehouseId) {
        return repository.deleteAllFor(productId, warehouseId);
    }

    @Override
    public List<ProductCycleTime> listActiveForProduct(int productId) {
        return repository.listActiveForProduct(productId);
    }

    @Override
    public List<ProductCycleTime> listActiveForWarehouse(int warehouseId) {
        return repository.listActiveForWarehouse(warehouseId);
    }

    @Override
    public void  importCycleTimesFromExcel(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Bỏ qua hàng header (i = 1)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // 0=ProductCode, 1=ModelType, 2=Array, 3=Line, 4=Ctime(s)
                String productCode = getCellValue(row.getCell(0));
                String modelTypeStr = getCellValue(row.getCell(1));
                String arrayStr     = getCellValue(row.getCell(2));
                String lineRaw      = getCellValue(row.getCell(3));
                String ctStr        = getCellValue(row.getCell(4));

                // Validate tối thiểu
                if (isBlank(productCode) || isBlank(modelTypeStr) || isBlank(lineRaw) || isBlank(ctStr)) {
                    continue;
                }

                // Parse ModelType
                ModelType modelType;
                try {
                    modelType = ModelType.valueOf(modelTypeStr.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    // model type sai -> bỏ qua
                    continue;
                }

                // Parse array và cycle time
                int array = parseIntSafe(arrayStr, 1);
                if (array <= 0) array = 1;

                BigDecimal ctSeconds = parseDecimalSafe(ctStr);
                if (ctSeconds == null || ctSeconds.signum() <= 0) {
                    // ct không hợp lệ -> bỏ qua
                    continue;
                }

                // Tách số line từ chuỗi (A15 -> 15)
                Integer lineNo = extractLineNumber(lineRaw);
                if (lineNo == null) {
                    // không có số -> bỏ
                    continue;
                }

                // Lấy/ tạo Product theo (code, type)
                Product product = productRepository.findByCodeAndModelType(productCode.trim(), modelType);
                if (product == null) {
                    product = new Product();
                    product.setProductCode(productCode.trim());
                    product.setModelType(modelType);
                    productRepository.add(product);
                    product = productRepository.findByCodeAndModelType(productCode.trim(), modelType);
                    if (product == null) continue; // không tạo được
                }

                // Tìm Warehouse có Name chứa số line, nếu không có thì tạo "Line {number}"
                Warehouse warehouse = warehouseRepository.findByNameContainingNumber(lineNo);
                if (warehouse == null) {
                    warehouse = new Warehouse();
                    warehouse.setName("Line " + lineNo);
                    warehouseRepository.add(warehouse);
                    warehouse = warehouseRepository.findByNameContainingNumber(lineNo);
                    if (warehouse == null) continue; // vẫn không lấy được
                }

                // Đặt cycle time active cho cặp (product, warehouse) với array
                repository.setActiveCycleTime(
                        product.getProductId(),
                        warehouse.getWarehouseId(),
                        ctSeconds,
                        array,
                        null // note
                );
            }

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi import cycle time từ Excel: " + e.getMessage(), e);
        }
    }

    /* ================= Helpers ================= */

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue() == null ? "" : cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // Không dùng ngày trong import này
                    return "";
                } else {
                    double d = cell.getNumericCellValue();
                    // Tránh "25.0"
                    if (Math.floor(d) == d) return String.valueOf((long) d);
                    return String.valueOf(d);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (IllegalStateException ex) {
                    double d = cell.getNumericCellValue();
                    if (Math.floor(d) == d) return String.valueOf((long) d);
                    return String.valueOf(d);
                }
            default:
                return "";
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** A15 -> 15; Line-007 -> 007 -> 7  */
    private Integer extractLineNumber(String s) {
        if (s == null) return null;
        String digits = s.replaceAll("\\D+", "");
        if (digits.isEmpty()) return null;
        try { return Integer.parseInt(digits); } catch (NumberFormatException e) { return null; }
    }

    private int parseIntSafe(String s, int fallback) {
        if (isBlank(s)) return fallback;
        try {
            double d = Double.parseDouble(s.trim());
            return (int) Math.round(d);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private BigDecimal parseDecimalSafe(String s) {
        if (isBlank(s)) return null;
        try {
            return new BigDecimal(s.trim());
        } catch (NumberFormatException ex) {
            try {
                double d = Double.parseDouble(s.trim());
                return BigDecimal.valueOf(d);
            } catch (NumberFormatException ex2) {
                return null;
            }
        }
    }

    @Override
    public List<ProductCycleTimeViewDto> searchView(String productCodeLike,
                                                    ModelType modelTypeOrNull,
                                                    String lineNameLike) {
        return repository.searchView(productCodeLike, modelTypeOrNull, lineNameLike);
    }

    @Override
    @Transactional
    public void setActiveCycleTime(int productId, int warehouseId, BigDecimal ctSeconds, int array, String note) {
        if (productId <= 0) throw new IllegalArgumentException("productId invalid");
        if (warehouseId <= 0) throw new IllegalArgumentException("warehouseId invalid");
        if (ctSeconds == null || ctSeconds.signum() <= 0) throw new IllegalArgumentException("ctSeconds must be > 0");
        if (array <= 0) throw new IllegalArgumentException("array must be > 0");
        repository.setActiveCycleTime(productId, warehouseId, ctSeconds, array, note);
    }
}
