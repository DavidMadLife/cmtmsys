package org.chemtrovina.cmtmsys.controller.inventoryTransfer;


import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chemtrovina.cmtmsys.dto.BarcodeError;
import org.chemtrovina.cmtmsys.dto.SAPSummaryDto;
import org.chemtrovina.cmtmsys.dto.TransferredDto;
import org.chemtrovina.cmtmsys.model.WarehouseTransferDetail;
import org.chemtrovina.cmtmsys.service.base.WarehouseTransferService;
import org.chemtrovina.cmtmsys.service.base.WorkOrderService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.stream.Collectors;

public class TransferExcelImporter {

    private final TransferScanHandler scanHandler;
    private final WorkOrderService workOrderService;
    private final WarehouseTransferService warehouseTransferService;

    public TransferExcelImporter(
            TransferScanHandler scanHandler,
            WorkOrderService workOrderService,
            WarehouseTransferService warehouseTransferService) {

        this.scanHandler = scanHandler;
        this.workOrderService = workOrderService;
        this.warehouseTransferService = warehouseTransferService;
    }

    public void importFromExcel(
            ComboBox<String> cbWorkOrder,
            TextField txtBarcode,
            TextField txtEmployeeID,
            ComboBox<String> cbSourceWarehouse,
            ComboBox<String> cbTargetWarehouse,
            TableView<TransferredDto> tblTransferred,
            TableView<SAPSummaryDto> tblRequiredSummary) {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn file Excel");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel", "*.xlsx", "*.xls")
        );
        File file = chooser.showOpenDialog(null);
        if (file == null) return;

        String woCode = cbWorkOrder.getValue();
        if (woCode == null) {
            showAlert("Chọn Work Order trước khi import.", Alert.AlertType.WARNING);
            return;
        }

        int woId = workOrderService.getWorkOrderIdByCode(woCode);

        Set<String> existing = warehouseTransferService.getDetailsByWorkOrderId(woId)
                .stream().map(WarehouseTransferDetail::getRollCode).collect(Collectors.toSet());

        List<BarcodeError> errors = new ArrayList<>();
        scanHandler.setBatchImport(true);

        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(file))) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                String barcode = getCellString(row.getCell(0)).trim();
                if (barcode.isEmpty()) continue;

                if (existing.contains(barcode)) {
                    errors.add(new BarcodeError(barcode, "Đã tồn tại"));
                    continue;
                }

                try {
                    txtBarcode.setText(barcode);
                    scanHandler.handleBarcodeScanned(
                            txtBarcode,
                            cbSourceWarehouse,
                            cbTargetWarehouse,
                            txtEmployeeID,
                            cbWorkOrder,
                            tblTransferred,
                            tblRequiredSummary
                    );
                    existing.add(barcode);
                } catch (Exception ex) {
                    errors.add(new BarcodeError(barcode, ex.getMessage()));
                }
            }

            if (!errors.isEmpty()) {
                exportErrorList(errors);
                showAlert("Có lỗi trong quá trình import, đã export danh sách lỗi.", Alert.AlertType.WARNING);
            } else {
                showAlert("Import OK", Alert.AlertType.INFORMATION);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi import: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            scanHandler.setBatchImport(false);
        }
    }

    private String getCellString(Cell c) {
        if (c == null) return "";
        return switch (c.getCellType()) {
            case STRING -> c.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) c.getNumericCellValue());
            case BOOLEAN -> String.valueOf(c.getBooleanCellValue());
            default -> "";
        };
    }

    private void exportErrorList(List<BarcodeError> errors) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Lỗi import");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Barcode");
            header.createCell(1).setCellValue("Lý do");

            int r = 1;
            for (BarcodeError er : errors) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(er.getBarcode());
                row.createCell(1).setCellValue(er.getReason());
            }

            FileChooser save = new FileChooser();
            save.setInitialFileName("ImportErrors.xlsx");
            save.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Excel", "*.xlsx")
            );

            File file = save.showSaveDialog(null);
            if (file != null) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }
            }
        } catch (Exception ex) {
            showAlert("Lỗi export danh sách lỗi: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type, msg, ButtonType.OK);
        alert.showAndWait();
    }
}

