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

    // ComboBox hiển thị tên kho
    @FXML private ComboBox<String> cbSourceWarehouse;
    @FXML private ComboBox<String> cbTargetWarehouse;

    // Các field khác (đã có sẵn trong FXML)
    @FXML private TextField txtEmployeeID;
    @FXML private TextField txtBarcode;
    @FXML private TableView<TransferredDto> tblTransferred;
    @FXML private TableColumn<TransferredDto, String> colBarcode;
    @FXML private TableColumn<TransferredDto, String> colSapCode;
    @FXML private TableColumn<TransferredDto, String> colSpec;
    @FXML private TableColumn<TransferredDto, Integer> colQuantity;
    @FXML private TableColumn<TransferredDto, String> colFromWarehouse;
    @FXML private TableColumn<TransferredDto, String> colToWarehouse;

    @FXML private TableView<SAPSummaryDto> tblRequiredSummary;
    @FXML private TableColumn<SAPSummaryDto, String> colSapCodeRequired;
    @FXML private TableColumn<SAPSummaryDto, Integer> colRequired;
    @FXML private TableColumn<SAPSummaryDto, Integer> colScanned;
    @FXML private TableColumn<SAPSummaryDto, String> colStatus;
    @FXML private TableColumn<Object, Integer> colNoRequired;
    @FXML private TableColumn<Object, Integer> colNoTransferred;

    @FXML private TextField txtDeleteBarcode;
    @FXML private Button btnDeleteFromWO;



    private ObservableList<SAPSummaryDto> sapSummaryList = FXCollections.observableArrayList();
    private Map<String, SAPSummaryDto> sapSummaryMap = new HashMap<>();


    @FXML private ComboBox<String> cbWorkOrder;



    private final List<TransferredDto> transferredList = new ArrayList<>();
    private Set<String> alreadyScannedRollCodes = new HashSet<>();
    @FXML private Button btnImportFromExcel;

    private boolean isBatchImport = false;


    private final WarehouseTransferService warehouseTransferService;
    private WarehouseTransfer currentTransfer;
    private final WorkOrderService workOrderService;
    private final WarehouseService warehouseService;
    private final TransferLogService transferLogService;
    private final MaterialService materialService;

    @Autowired
    public InventoryTransferController(WarehouseTransferService warehouseTransferService, WarehouseService warehouseService, MaterialService materialService, TransferLogService transferLogService, WorkOrderService workOrderService) {
        this.warehouseTransferService = warehouseTransferService;
        this.warehouseService = warehouseService;
        this.materialService = materialService;
        this.transferLogService = transferLogService;
        this.workOrderService = workOrderService;
    }

    @FXML
    public void initialize() {
        loadWarehouses();
        loadWorkOrders();

        // Disable combobox trước
        cbSourceWarehouse.setDisable(true);
        cbTargetWarehouse.setDisable(true);

        // Enable combobox nếu nhập employeeID
        txtEmployeeID.textProperty().addListener((obs, oldText, newText) -> {
            boolean notEmpty = !newText.trim().isEmpty();
            cbSourceWarehouse.setDisable(!notEmpty);
            cbTargetWarehouse.setDisable(!notEmpty);
        });

        setupBarcodeScanner();
        setupTransferredTable();
        tblTransferred.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblTransferred.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode().toString().equals("C")) {
                copyTransferredSelectionToClipboard();
            }
        });

        tblRequiredSummary.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode().toString().equals("C")) {
                FxClipboardUtils.copySelectionToClipboard(tblRequiredSummary);
            }
        });

        setupRequiredSummaryTable();

        cbWorkOrder.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                loadRequiredSummary(newVal);
            }
        });
        cbWorkOrder.setOnMouseClicked(e -> loadWorkOrders());


        tblRequiredSummary.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        btnImportFromExcel.setOnAction(e -> handleImportFromExcel());
        btnDeleteFromWO.setOnAction(e -> handleDeleteBarcode());



        tblRequiredSummary.getSelectionModel().setCellSelectionEnabled(true);
        tblRequiredSummary.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tblTransferred.getSelectionModel().setCellSelectionEnabled(true);
        tblTransferred.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    }

    private void setupRequiredSummaryTable() {
        colNoRequired.setCellValueFactory(cell ->
                new SimpleIntegerProperty(tblRequiredSummary.getItems().indexOf(cell.getValue()) + 1).asObject()
        );

        colSapCodeRequired.setCellValueFactory(new PropertyValueFactory<>("sapCode"));
        colRequired.setCellValueFactory(new PropertyValueFactory<>("required")); // <-- đúng
        colScanned.setCellValueFactory(new PropertyValueFactory<>("scanned"));  // <-- đúng
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }



        private void setupTransferredTable() {
        colNoTransferred.setCellValueFactory(cell ->
                new SimpleIntegerProperty(tblTransferred.getItems().indexOf(cell.getValue()) + 1).asObject()
        );
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("rollCode"));
        colSapCode.setCellValueFactory(new PropertyValueFactory<>("sapCode"));
        colSpec.setCellValueFactory(new PropertyValueFactory<>("spec"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colFromWarehouse.setCellValueFactory(new PropertyValueFactory<>("fromWarehouse"));
        colToWarehouse.setCellValueFactory(new PropertyValueFactory<>("toWarehouse"));
        tblTransferred.getSelectionModel().setCellSelectionEnabled(true); // chọn từng ô
        tblTransferred.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE); // cho chọn nhiều ô

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void loadWarehouses() {
        List<Warehouse> warehouses = warehouseService.getAllWarehouses();
        List<String> warehouseNames = warehouses.stream()
                .map(Warehouse::getName)
                .toList();

        cbSourceWarehouse.setItems(FXCollections.observableArrayList(warehouseNames));
        cbTargetWarehouse.setItems(FXCollections.observableArrayList(warehouseNames));
    }
    private void loadWorkOrders() {
        cbWorkOrder.getItems().clear();

        List<String> workOrderCodes = workOrderService.getAllWorkOrders().stream()
                .map(WorkOrder::getWorkOrderCode)
                .toList();

        cbWorkOrder.setItems(FXCollections.observableArrayList(workOrderCodes));
    }


    private void loadRequiredSummary(String workOrderCode) {
        sapSummaryMap.clear();
        sapSummaryList.clear();
        alreadyScannedRollCodes.clear();

        // Lấy danh sách yêu cầu vật liệu
        List<MaterialRequirementDto> data = workOrderService.getGroupedMaterialRequirements(workOrderCode);
        for (MaterialRequirementDto dto : data) {
            sapSummaryMap.put(dto.getSappn(), new SAPSummaryDto(dto.getSappn(), dto.getRequiredQty()));
        }

        // Lấy các cuộn đã chuyển theo W/O
        int workOrderId = workOrderService.getWorkOrderIdByCode(workOrderCode);
        List<WarehouseTransferDetail> details = warehouseTransferService.getDetailsByWorkOrderId(workOrderId);

        for (WarehouseTransferDetail detail : details) {
            SAPSummaryDto summary = sapSummaryMap.get(detail.getSapCode());
            if (summary != null) {
                summary.setScanned(summary.getScanned() + detail.getQuantity());
                summary.setStatus(summary.getScanned() >= summary.getRequired() ? "Đủ" : "Thiếu");
            }
        }


        List<WarehouseTransferDetail> pastDetails =
                warehouseTransferService.getDetailsByWorkOrderId(workOrderId);

        for (WarehouseTransferDetail d : pastDetails) {
            alreadyScannedRollCodes.add(d.getRollCode());
        }

        sapSummaryList.addAll(sapSummaryMap.values());
        tblRequiredSummary.setItems(sapSummaryList);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void setupBarcodeScanner() {
        txtBarcode.setOnAction(e -> handleBarcodeScanned());
    }


    private void handleBarcodeScanned() {
        String barcode = txtBarcode.getText().trim();
        if (barcode.isEmpty()) return;

        int toId = getSelectedWarehouseId(cbTargetWarehouse);
        int fromId = getSelectedWarehouseId(cbSourceWarehouse);
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
            showAlert("Không tìm thấy vật liệu có mã vạch: " + barcode);
            return;
        }

        if (alreadyScannedRollCodes.contains(barcode)) {
            showAlert("Cuộn mã vạch này đã được quét trước đó cho W/O này.");
            return;
        }




        String sapCode = material.getSapCode();
        SAPSummaryDto summary = sapSummaryMap.get(sapCode);
        if (summary == null) {
            showAlert("Mã SAP " + sapCode + " không có trong W/O");
            return;
        }

        // Khởi tạo transfer nếu chưa có
        // Nếu chưa có currentTransfer thì kiểm tra DB
        if (currentTransfer == null) {
            int workOrderId = workOrderService.getWorkOrderIdByCode(woCode);
            currentTransfer = warehouseTransferService.findExistingTransfer(fromId, toId, workOrderId, employeeId);

            if (currentTransfer == null) {
                currentTransfer = new WarehouseTransfer();
                currentTransfer.setFromWarehouseId(fromId);
                currentTransfer.setToWarehouseId(toId);
                currentTransfer.setEmployeeId(employeeId);
                currentTransfer.setTransferDate(LocalDateTime.now());
                currentTransfer.setNote("Chuyển từ W/O: " + woCode);
                currentTransfer.setWorkOrderId(workOrderId);

                warehouseTransferService.createTransfer(currentTransfer, new ArrayList<>());
                currentTransfer = warehouseTransferService.getAllTransfers().getLast();
            }
        }


        // Đã có cuộn này trong Transfer hiện tại?
        if (warehouseTransferService.getDetailRepository().existsByTransferIdAndRollCode(currentTransfer.getTransferId(), material.getRollCode())) {
            showAlert("Cuộn mã vạch đã được quét trong W/O này.");
            return;
        }

        // Tạo detail mới
        WarehouseTransferDetail detail = new WarehouseTransferDetail();
        detail.setTransferId(currentTransfer.getTransferId());
        detail.setRollCode(material.getRollCode());
        detail.setSapCode(sapCode);
        detail.setQuantity(material.getQuantity());
        detail.setCreatedAt(LocalDateTime.now());

        // Lưu detail
        warehouseTransferService.getDetailRepository().add(detail);

        // Cập nhật UI (tải lại toàn bộ danh sách đã chuyển)
        List<TransferredDto> transferredDtos = warehouseTransferService.getDetailsByTransferId(currentTransfer.getTransferId())
                .stream()
                .map(d -> new TransferredDto(
                        d.getRollCode(), d.getSapCode(), materialService.getMaterialByRollCode(d.getRollCode()).getSpec(),
                        d.getQuantity(), cbSourceWarehouse.getValue(), cbTargetWarehouse.getValue()))
                .toList();

        tblTransferred.setItems(FXCollections.observableArrayList(transferredDtos));


        summary.setScanned(summary.getScanned() + material.getQuantity());
        summary.setStatus(summary.getScanned() >= summary.getRequired() ? "Đủ" : "Thiếu");
        tblRequiredSummary.refresh();

        txtBarcode.clear();

        // Cập nhật kho hiện tại của cuộn
        material.setWarehouseId(toId);
        materialService.updateMaterial(material);

        // Ghi log chuyển kho
        TransferLog log = new TransferLog();
        log.setTransferId(currentTransfer.getTransferId());
        log.setRollCode(material.getRollCode());
        log.setFromWarehouseId(fromId);
        log.setToWarehouseId(toId);
        log.setTransferDate(LocalDateTime.now());
        log.setNote("Chuyển từ W/O: " + woCode);
        log.setEmployeeId(employeeId);
        transferLogService.addTransfer(log);

    }

    private void handleDeleteBarcode() {
        String barcode = txtDeleteBarcode.getText().trim();
        if (barcode.isEmpty()) return;

        String woCode = cbWorkOrder.getValue();
        if (woCode == null || woCode.isEmpty()) {
            showAlert("Vui lòng chọn Work Order.");
            return;
        }

        int workOrderId = workOrderService.getWorkOrderIdByCode(woCode);
        Material material = materialService.getMaterialByRollCode(barcode);
        if (material == null) {
            showAlert("Không tìm thấy vật liệu có mã vạch: " + barcode);
            return;
        }

        // Tìm transfer hiện tại
        List<WarehouseTransfer> transfers = warehouseTransferService.getAllTransfers();
        Optional<WarehouseTransfer> transferOpt = transfers.stream()
                .filter(t -> Objects.equals(t.getWorkOrderId(), workOrderId))
                .filter(t -> warehouseTransferService.getDetailRepository()
                        .existsByTransferIdAndRollCode(t.getTransferId(), barcode))
                .findFirst();

        if (transferOpt.isEmpty()) {
            showAlert("Không tìm thấy W/O chứa mã vạch này.");
            return;
        }

        WarehouseTransfer transfer = transferOpt.get();
        int transferId = transfer.getTransferId();

        // Xóa chi tiết
        warehouseTransferService.getDetailRepository().deleteByTransferIdAndRollCode(transferId, barcode);

        // Cập nhật lại bảng chuyển
        List<TransferredDto> updatedList = warehouseTransferService.getDetailsByTransferId(transferId)
                .stream()
                .map(d -> {
                    Material m = materialService.getMaterialByRollCode(d.getRollCode());
                    return new TransferredDto(
                            d.getRollCode(), d.getSapCode(), m.getSpec(), d.getQuantity(),
                            cbSourceWarehouse.getValue(), cbTargetWarehouse.getValue()
                    );
                })
                .toList();

        tblTransferred.setItems(FXCollections.observableArrayList(updatedList));

        // Trừ lại số đã quét
        String sapCode = material.getSapCode();
        SAPSummaryDto summary = sapSummaryMap.get(sapCode);
        if (summary != null) {
            summary.setScanned(Math.max(0, summary.getScanned() - material.getQuantity()));
            summary.setStatus(summary.getScanned() >= summary.getRequired() ? "Đủ" : "Thiếu");
            tblRequiredSummary.refresh();
        }

        // Gỡ khỏi danh sách cuộn đã quét
        alreadyScannedRollCodes.remove(barcode);
        txtDeleteBarcode.clear();

        showAlert("✅ Đã xóa khỏi W/O.");
    }


    private void showAlert(String msg) {
        if (isBatchImport) return; // ✅ Không hiện alert khi đang import
        Alert alert = new Alert(Alert.AlertType.WARNING, msg);
        alert.show();
    }


    private int getSelectedWarehouseId(ComboBox<String> cb) {
        String name = cb.getValue();
        if (name == null) return -1;
        return warehouseService.getAllWarehouses().stream()
                .filter(w -> w.getName().equals(name))
                .map(Warehouse::getWarehouseId)
                .findFirst().orElse(-1);
    }

    private void copyTransferredSelectionToClipboard() {
        StringBuilder clipboardString = new StringBuilder();
        ObservableList<TablePosition> positionList = tblTransferred.getSelectionModel().getSelectedCells();

        int prevRow = -1;
        for (TablePosition position : positionList) {
            int row = position.getRow();
            int col = position.getColumn();

            Object cell = tblTransferred.getColumns().get(col).getCellData(row);
            if (cell == null) {
                cell = "";
            }

            if (prevRow == row) {
                clipboardString.append('\t');
            } else if (prevRow != -1) {
                clipboardString.append('\n');
            }

            clipboardString.append(cell);
            prevRow = row;
        }

        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(clipboardString.toString());
        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }

    private void handleImportFromExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file barcode Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile == null) return;

        List<BarcodeError> errorList = new ArrayList<>();

        String woCode = cbWorkOrder.getValue();
        if (woCode == null || woCode.isEmpty()) {
            showAlert("Vui lòng chọn Work Order trước khi import.");
            return;
        }

        int workOrderId = workOrderService.getWorkOrderIdByCode(woCode);

        // 🔁 Load toàn bộ rollCode đã được chuyển cho WorkOrder này
        Set<String> existingCodesInWO = warehouseTransferService.getDetailsByWorkOrderId(workOrderId)
                .stream()
                .map(WarehouseTransferDetail::getRollCode)
                .collect(Collectors.toSet());

        isBatchImport = true; // ✅ Bắt đầu batch import
        try (FileInputStream fis = new FileInputStream(selectedFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String barcode = getCellString(row.getCell(0)).trim();
                if (barcode.isEmpty()) continue;

                try {
                    if (existingCodesInWO.contains(barcode)) {
                        errorList.add(new BarcodeError(barcode, "Đã tồn tại trong Work Order hiện tại"));
                        continue;
                    }

                    txtBarcode.setText(barcode);
                    handleBarcodeScanned(); // ✅ Thêm cuộn

                    existingCodesInWO.add(barcode);
                    alreadyScannedRollCodes.add(barcode);

                } catch (Exception ex) {
                    errorList.add(new BarcodeError(barcode, ex.getMessage()));
                }
            }

            if (!errorList.isEmpty()) {
                exportErrorListToExcel(errorList);
                Alert alert = new Alert(Alert.AlertType.WARNING, "Import hoàn tất! Một số cuộn bị lỗi đã được ghi ra file.");
                alert.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "✅ Import thành công toàn bộ cuộn.");
                alert.show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "❌ Lỗi import: " + e.getMessage());
            alert.show();
        } finally {
            isBatchImport = false; // ✅ Luôn reset lại
        }

    }


    private void exportErrorListToExcel(List<BarcodeError> errors) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Lỗi import");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Barcode");
        header.createCell(1).setCellValue("Lý do");

        int rowNum = 1;
        for (BarcodeError error : errors) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(error.getBarcode());
            row.createCell(1).setCellValue(error.getReason());
        }

        try {
            FileChooser saveChooser = new FileChooser();
            saveChooser.setTitle("Lưu danh sách lỗi");
            saveChooser.setInitialFileName("ImportErrors.xlsx");
            saveChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));

            File file = saveChooser.showSaveDialog(null);
            if (file != null) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi khi lưu file lỗi: " + e.getMessage());
        }
    }



    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
