package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.dto.BarcodeError;
import org.chemtrovina.cmtmsys.dto.MaterialRequirementDto;
import org.chemtrovina.cmtmsys.dto.SAPSummaryDto;
import org.chemtrovina.cmtmsys.dto.TransferredDto;
import org.chemtrovina.cmtmsys.model.*;
import org.chemtrovina.cmtmsys.repository.Impl.*;
import org.chemtrovina.cmtmsys.repository.base.*;
import org.chemtrovina.cmtmsys.service.Impl.*;
import org.chemtrovina.cmtmsys.service.base.*;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.stereotype.Component;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
@Component
public class InventoryTransferController {

    /* ============================================================
     * 1. FXML FIELDS
     * ============================================================ */
    @FXML private ComboBox<String> cbSourceWarehouse;
    @FXML private ComboBox<String> cbTargetWarehouse;
    @FXML private ComboBox<String> cbWorkOrder;

    @FXML private TextField txtEmployeeID;
    @FXML private TextField txtBarcode;
    @FXML private TextField txtDeleteBarcode;

    @FXML private Button btnImportFromExcel;
    @FXML private Button btnDeleteFromWO;

    @FXML private TableView<TransferredDto> tblTransferred;
    @FXML private TableColumn<TransferredDto, String> colBarcode;
    @FXML private TableColumn<TransferredDto, String> colSapCode;
    @FXML private TableColumn<TransferredDto, String> colSpec;
    @FXML private TableColumn<TransferredDto, Integer> colQuantity;
    @FXML private TableColumn<TransferredDto, String> colFromWarehouse;
    @FXML private TableColumn<TransferredDto, String> colToWarehouse;
    @FXML private TableColumn<Object, Integer> colNoTransferred;

    @FXML private TableView<SAPSummaryDto> tblRequiredSummary;
    @FXML private TableColumn<SAPSummaryDto, String> colSapCodeRequired;
    @FXML private TableColumn<SAPSummaryDto, Integer> colRequired;
    @FXML private TableColumn<SAPSummaryDto, Integer> colScanned;
    @FXML private TableColumn<SAPSummaryDto, String> colStatus;
    @FXML private TableColumn<Object, Integer> colNoRequired;


    /* ============================================================
     * 2. SERVICES & VARIABLES
     * ============================================================ */
    private final WarehouseTransferService warehouseTransferService;
    private final WarehouseService warehouseService;
    private final MaterialService materialService;
    private final TransferLogService transferLogService;
    private final WorkOrderService workOrderService;

    private boolean isBatchImport = false;
    private WarehouseTransfer currentTransfer;

    private final Map<String, SAPSummaryDto> sapSummaryMap = new HashMap<>();
    private final ObservableList<SAPSummaryDto> sapSummaryList = FXCollections.observableArrayList();
    private final Set<String> alreadyScannedRollCodes = new HashSet<>();


    /* ============================================================
     * 3. CONSTRUCTOR
     * ============================================================ */
    @Autowired
    public InventoryTransferController(
            WarehouseTransferService warehouseTransferService,
            WarehouseService warehouseService,
            MaterialService materialService,
            TransferLogService transferLogService,
            WorkOrderService workOrderService) {

        this.warehouseTransferService = warehouseTransferService;
        this.warehouseService = warehouseService;
        this.materialService = materialService;
        this.transferLogService = transferLogService;
        this.workOrderService = workOrderService;
    }


    /* ============================================================
     * 4. INITIALIZE
     * ============================================================ */
    @FXML
    public void initialize() {

        loadWarehouses();
        loadWorkOrders();

        setupRequiredSummaryTable();
        setupTransferredTable();
        setupBarcodeScanner();
        setupUIEvents();

        FxClipboardUtils.enableCopyShortcut(tblTransferred);
        FxClipboardUtils.enableCopyShortcut(tblRequiredSummary);
    }


    /* ============================================================
     * 5. UI SETUP (Tables, Listeners)
     * ============================================================ */

    private void setupUIEvents() {

        cbWorkOrder.setOnMouseClicked(e -> loadWorkOrders());

        cbWorkOrder.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) loadRequiredSummary(newVal);
        });

        txtEmployeeID.textProperty().addListener((obs, oldText, newText) -> {
            boolean enable = !newText.trim().isEmpty();
            cbSourceWarehouse.setDisable(!enable);
            cbTargetWarehouse.setDisable(!enable);
        });

        btnImportFromExcel.setOnAction(e -> handleImportFromExcel());
        btnDeleteFromWO.setOnAction(e -> handleDeleteBarcode());
        txtDeleteBarcode.setOnAction(e -> handleDeleteBarcode());
    }

    private void setupRequiredSummaryTable() {
        colNoRequired.setCellValueFactory(c ->
                new SimpleIntegerProperty(tblRequiredSummary.getItems().indexOf(c.getValue()) + 1).asObject());
        colSapCodeRequired.setCellValueFactory(new PropertyValueFactory<>("sapCode"));
        colRequired.setCellValueFactory(new PropertyValueFactory<>("required"));
        colScanned.setCellValueFactory(new PropertyValueFactory<>("scanned"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void setupTransferredTable() {
        colNoTransferred.setCellValueFactory(c ->
                new SimpleIntegerProperty(tblTransferred.getItems().indexOf(c.getValue()) + 1).asObject());
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("rollCode"));
        colSapCode.setCellValueFactory(new PropertyValueFactory<>("sapCode"));
        colSpec.setCellValueFactory(new PropertyValueFactory<>("spec"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colFromWarehouse.setCellValueFactory(new PropertyValueFactory<>("fromWarehouse"));
        colToWarehouse.setCellValueFactory(new PropertyValueFactory<>("toWarehouse"));
    }


    private void setupBarcodeScanner() {
        txtBarcode.setOnAction(e -> handleBarcodeScanned());
    }


    /* ============================================================
     * 6. LOAD DATA (Warehouses, WorkOrders, Summary)
     * ============================================================ */

    private void loadWarehouses() {
        List<String> names = warehouseService.getAllWarehouses()
                .stream().map(Warehouse::getName).toList();
        cbSourceWarehouse.setItems(FXCollections.observableArrayList(names));
        cbTargetWarehouse.setItems(FXCollections.observableArrayList(names));
    }

    private void loadWorkOrders() {
        cbWorkOrder.getItems().clear();

        List<String> codes = workOrderService.getAllWorkOrders()
                .stream().map(WorkOrder::getWorkOrderCode).toList();

        cbWorkOrder.setItems(FXCollections.observableArrayList(codes));
    }


    /** Load summary for selected WO */
    private void loadRequiredSummary(String workOrderCode) {

        sapSummaryMap.clear();
        sapSummaryList.clear();
        alreadyScannedRollCodes.clear();

        int workOrderId = workOrderService.getWorkOrderIdByCode(workOrderCode);

        // 1) YÊU CẦU
        for (var req : workOrderService.getGroupedMaterialRequirements(workOrderCode)) {
            sapSummaryMap.put(req.getSappn(), new SAPSummaryDto(req.getSappn(), req.getRequiredQty()));
        }

        // 2) DETAILS đã chuyển
        List<WarehouseTransferDetail> details = warehouseTransferService.getDetailsByWorkOrderId(workOrderId);
        if (details.isEmpty()) {
            sapSummaryList.addAll(sapSummaryMap.values());
            tblRequiredSummary.setItems(sapSummaryList);
            tblTransferred.getItems().clear();
            return;
        }

        // Cache
        Set<Integer> transferIds = details.stream().map(WarehouseTransferDetail::getTransferId).collect(Collectors.toSet());
        Set<String> rollCodes = details.stream().map(WarehouseTransferDetail::getRollCode).collect(Collectors.toSet());

        Map<Integer, WarehouseTransfer> transferMap = warehouseTransferService.getTransfersByIds(transferIds);
        Map<String, Material> materialMap = materialService.getMaterialsByRollCodes(rollCodes);

        // Update summary
        for (WarehouseTransferDetail d : details) {
            SAPSummaryDto s = sapSummaryMap.get(d.getSapCode());
            if (s != null) {
                s.setScanned(s.getScanned() + d.getQuantity());
                s.setStatus(s.getScanned() >= s.getRequired() ? "Đủ" : "Thiếu");
                alreadyScannedRollCodes.add(d.getRollCode());
            }
        }

        sapSummaryList.addAll(sapSummaryMap.values());
        tblRequiredSummary.setItems(sapSummaryList);

        // Build transferred list
        List<TransferredDto> dtos = details.stream()
                .map(d -> {
                    Material m = materialMap.get(d.getRollCode());
                    WarehouseTransfer t = transferMap.get(d.getTransferId());
                    return new TransferredDto(
                            d.getRollCode(),
                            d.getSapCode(),
                            m != null ? m.getSpec() : "",
                            d.getQuantity(),
                            warehouseService.getWarehouseNameById(t.getFromWarehouseId()),
                            warehouseService.getWarehouseNameById(t.getToWarehouseId())
                    );
                })
                .toList();

        tblTransferred.setItems(FXCollections.observableArrayList(dtos));
    }


    /* ============================================================
     * 7. MAIN FEATURES (Scan, Delete, Import Excel)
     * ============================================================ */

    private void handleBarcodeScanned() {
        String barcode = txtBarcode.getText().trim();
        if (barcode.isEmpty()) return;

        // Validate basic info
        int toId = getWarehouseId(cbTargetWarehouse);
        int fromId = getWarehouseId(cbSourceWarehouse);
        if (toId == -1 || fromId == -1 || toId == fromId) {
            showAlert("Kho chuyển đến và đi không hợp lệ");
            return;
        }

        String employeeId = txtEmployeeID.getText().trim();
        if (employeeId.isEmpty()) {
            showAlert("Vui lòng nhập mã nhân viên");
            return;
        }

        String woCode = cbWorkOrder.getValue();
        if (woCode == null || woCode.isEmpty()) {
            showAlert("Vui lòng chọn Work Order");
            return;
        }

        Material material = materialService.getMaterialByRollCode(barcode);
        if (material == null) {
            showAlert("Không tìm thấy vật liệu: " + barcode);
            return;
        }

        if (alreadyScannedRollCodes.contains(barcode)) {
            showAlert("Cuộn đã được quét trong W/O này.");
            return;
        }

        String sapCode = material.getSapCode();
        SAPSummaryDto summary = sapSummaryMap.get(sapCode);
        if (summary == null) {
            showAlert("Mã SAP " + sapCode + " không có trong W/O");
            return;
        }

        // Create transfer if needed
        int woId = workOrderService.getWorkOrderIdByCode(woCode);
        initOrLoadCurrentTransfer(fromId, toId, employeeId, woCode, woId);

        // Check existed in transfer
        if (warehouseTransferService.getDetailRepository()
                .existsByTransferIdAndRollCode(currentTransfer.getTransferId(), barcode)) {
            showAlert("Cuộn đã có trong phiếu chuyển.");
            return;
        }

        // Save detail
        WarehouseTransferDetail detail = new WarehouseTransferDetail();
        detail.setTransferId(currentTransfer.getTransferId());
        detail.setRollCode(barcode);
        detail.setSapCode(sapCode);
        detail.setQuantity(material.getQuantity());
        detail.setCreatedAt(LocalDateTime.now());
        warehouseTransferService.getDetailRepository().add(detail);

        updateTransferredTable();
        updateSummaryAfterScan(summary, material);
        updateMaterialWarehouse(material, toId);
        addTransferLog(material, fromId, toId, employeeId, woCode);

        txtBarcode.clear();
    }


    private void handleDeleteBarcode() {
        String barcode = txtDeleteBarcode.getText().trim();
        if (barcode.isEmpty()) return;

        String woCode = cbWorkOrder.getValue();
        if (woCode == null) {
            showAlert("Vui lòng chọn Work Order.");
            return;
        }

        int woId = workOrderService.getWorkOrderIdByCode(woCode);
        Material material = materialService.getMaterialByRollCode(barcode);
        if (material == null) {
            showAlert("Không tìm thấy vật liệu: " + barcode);
            return;
        }

        Optional<WarehouseTransfer> transferOpt = warehouseTransferService.getAllTransfers()
                .stream()
                .filter(t -> Objects.equals(t.getWorkOrderId(), woId))
                .filter(t -> warehouseTransferService.getDetailRepository()
                        .existsByTransferIdAndRollCode(t.getTransferId(), barcode))
                .findFirst();

        if (transferOpt.isEmpty()) {
            showAlert("Không tìm thấy chi tiết chứa cuộn này.");
            return;
        }

        WarehouseTransfer transfer = transferOpt.get();
        int transferId = transfer.getTransferId();

        warehouseTransferService.getDetailRepository().deleteByTransferIdAndRollCode(transferId, barcode);
        updateTransferredTable();

        // Update summary
        SAPSummaryDto summary = sapSummaryMap.get(material.getSapCode());
        if (summary != null) {
            summary.setScanned(Math.max(0, summary.getScanned() - material.getQuantity()));
            summary.setStatus(summary.getScanned() >= summary.getRequired() ? "Đủ" : "Thiếu");
            tblRequiredSummary.refresh();
        }

        alreadyScannedRollCodes.remove(barcode);
        txtDeleteBarcode.clear();

        showAlert("Đã xóa.");
    }


    private void handleImportFromExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn file Excel");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx", "*.xls"));
        File file = chooser.showOpenDialog(null);
        if (file == null) return;

        String woCode = cbWorkOrder.getValue();
        if (woCode == null) {
            showAlert("Chọn Work Order trước khi import.");
            return;
        }

        int woId = workOrderService.getWorkOrderIdByCode(woCode);

        Set<String> existing = warehouseTransferService.getDetailsByWorkOrderId(woId)
                .stream().map(WarehouseTransferDetail::getRollCode).collect(Collectors.toSet());

        List<BarcodeError> errors = new ArrayList<>();
        isBatchImport = true;

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
                    handleBarcodeScanned();
                    existing.add(barcode);
                } catch (Exception ex) {
                    errors.add(new BarcodeError(barcode, ex.getMessage()));
                }
            }

            if (!errors.isEmpty()) exportErrorList(errors);
            showAlert(errors.isEmpty() ? "Import OK" : "Có lỗi, đã export.");

        } catch (Exception e) {
            showAlert("Lỗi import: " + e.getMessage());
            e.printStackTrace();
        } finally {
            isBatchImport = false;
        }
    }


    /* ============================================================
     * 8.  UTILS + SMALL METHODS
     * ============================================================ */

    private void initOrLoadCurrentTransfer(int fromId, int toId, String employeeId, String woCode, int woId) {

        if (currentTransfer != null) return;

        currentTransfer = warehouseTransferService.findExistingTransfer(fromId, toId, woId, employeeId);

        if (currentTransfer == null) {
            WarehouseTransfer t = new WarehouseTransfer();
            t.setFromWarehouseId(fromId);
            t.setToWarehouseId(toId);
            t.setEmployeeId(employeeId);
            t.setWorkOrderId(woId);
            t.setTransferDate(LocalDateTime.now());
            t.setNote("Chuyển từ W/O: " + woCode);

            warehouseTransferService.createTransfer(t, new ArrayList<>());
            currentTransfer = warehouseTransferService.getAllTransfers().getLast();
        }
    }


    private int getWarehouseId(ComboBox<String> cb) {
        String name = cb.getValue();
        if (name == null) return -1;

        return warehouseService.getAllWarehouses()
                .stream().filter(w -> w.getName().equals(name))
                .map(Warehouse::getWarehouseId)
                .findFirst().orElse(-1);
    }


    private void updateTransferredTable() {
        int id = currentTransfer.getTransferId();
        List<TransferredDto> dtos = warehouseTransferService.getDetailsByTransferId(id)
                .stream()
                .map(d -> {
                    Material m = materialService.getMaterialByRollCode(d.getRollCode());
                    return new TransferredDto(
                            d.getRollCode(),
                            d.getSapCode(),
                            m.getSpec(),
                            d.getQuantity(),
                            cbSourceWarehouse.getValue(),
                            cbTargetWarehouse.getValue());
                })
                .toList();

        tblTransferred.setItems(FXCollections.observableArrayList(dtos));
    }


    private void updateSummaryAfterScan(SAPSummaryDto summary, Material material) {
        summary.setScanned(summary.getScanned() + material.getQuantity());
        summary.setStatus(summary.getScanned() >= summary.getRequired() ? "Đủ" : "Thiếu");
        tblRequiredSummary.refresh();
        alreadyScannedRollCodes.add(material.getRollCode());
    }


    private void updateMaterialWarehouse(Material m, int toId) {
        m.setWarehouseId(toId);
        materialService.updateMaterial(m);
    }


    private void addTransferLog(Material material, int fromId, int toId, String emp, String woCode) {
        TransferLog log = new TransferLog();
        log.setTransferId(currentTransfer.getTransferId());
        log.setRollCode(material.getRollCode());
        log.setFromWarehouseId(fromId);
        log.setToWarehouseId(toId);
        log.setTransferDate(LocalDateTime.now());
        log.setEmployeeId(emp);
        log.setNote("Chuyển từ W/O: " + woCode);
        transferLogService.addTransfer(log);
    }


    private void exportErrorList(List<BarcodeError> errors) {
        try {
            Workbook workbook = new XSSFWorkbook();
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
            showAlert("Lỗi export: " + ex.getMessage());
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


    private void showAlert(String msg) {
        if (isBatchImport) return;
        Alert alert = new Alert(Alert.AlertType.WARNING, msg);
        alert.show();
    }
}

