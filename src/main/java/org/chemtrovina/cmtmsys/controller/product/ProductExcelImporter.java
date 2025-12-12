package org.chemtrovina.cmtmsys.controller.product;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.service.base.ProductBOMService;
import org.chemtrovina.cmtmsys.service.base.ProductService;
import org.chemtrovina.cmtmsys.utils.FxAlertUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.io.FileInputStream;

public class ProductExcelImporter {

    private final ProductService productService;
    private final ProductBOMService bomService;

    public ProductExcelImporter(ProductService productService, ProductBOMService bomService) {
        this.productService = productService;
        this.bomService = bomService;
    }

    public void importExcel(File file, Runnable onDone) {
        try (Workbook wb = new XSSFWorkbook(new FileInputStream(file))) {

            JdbcTemplate jdbc = new JdbcTemplate(DataSourceConfig.getDataSource());
            Sheet sheet = wb.getSheetAt(0);

            int inserted = 0;
            int updated  = 0;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String productCode = getCell(row.getCell(0));
                String sappn       = getCell(row.getCell(1));
                String qtyStr      = getCell(row.getCell(2));
                String modelType   = getCell(row.getCell(3));

                if (productCode.isBlank() || sappn.isBlank()) continue;

                double qty = qtyStr.isBlank() ? 0 : Double.parseDouble(qtyStr);
                if (modelType.isBlank()) modelType = "NONE";

                ProductExcelUtil.insertOrUpdate(jdbc,
                        productCode, sappn, qty, modelType);

                // count
                inserted++;
            }

            onDone.run();
            FxAlertUtils.info("Import BOM OK!\nThêm mới: "+inserted);

        } catch (Exception ex) {
            FxAlertUtils.error("Lỗi import: "+ex.getMessage());
        }
    }

    private String getCell(Cell c) {
        if (c == null) return "";
        return switch (c.getCellType()) {
            case STRING -> c.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(c.getNumericCellValue()).replaceAll("\\.0+$","");
            default -> "";
        };
    }


}
