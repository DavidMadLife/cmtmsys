package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Callback;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.dto.TransferLogDto;
import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.repository.Impl.MaterialRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.Impl.TransferLogRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.Impl.WarehouseRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.base.MaterialRepository;
import org.chemtrovina.cmtmsys.repository.base.TransferLogRepository;
import org.chemtrovina.cmtmsys.repository.base.WarehouseRepository;
import org.chemtrovina.cmtmsys.service.Impl.MaterialServiceImpl;
import org.chemtrovina.cmtmsys.service.Impl.TransferLogServiceImpl;
import org.chemtrovina.cmtmsys.service.Impl.WarehouseServiceImpl;
import org.chemtrovina.cmtmsys.service.base.MaterialService;
import org.chemtrovina.cmtmsys.service.base.TransferLogService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
public class TransferLogController {

    @FXML private TableView<TransferLogDto> tblTransferLogs;
    @FXML private TableColumn<TransferLogDto, Integer> colNo;
    @FXML private TableColumn<TransferLogDto, String> colBarcode;
    @FXML private TableColumn<TransferLogDto, String> colFromWarehouse;
    @FXML private TableColumn<TransferLogDto, String> colToWarehouse;
    @FXML private TableColumn<TransferLogDto, String> colTransferTime;
    @FXML private TableColumn<TransferLogDto, String> colEmployeeId, colSpec, colSapCode;

    @FXML private TextField txtSapCode;
    @FXML private TextField txtBarcode;
    @FXML private ComboBox<Warehouse> cbFromWarehouse;
    @FXML private ComboBox<Warehouse> cbToWarehouse;
    @FXML private DatePicker dpFromDate;
    @FXML private DatePicker dpToDate;
    @FXML private Button btnSearch;
    @FXML private Button btnClear;



    private final TransferLogService transferLogService;
    private final WarehouseService warehouseService;
    private final MaterialService materialService;
    @Autowired
    public TransferLogController(TransferLogService transferLogService, WarehouseService warehouseService, MaterialService materialService) {
        this.transferLogService = transferLogService;
        this.warehouseService = warehouseService;
        this.materialService = materialService;
    }

    private List<TransferLogDto> allLogs = new ArrayList<>();

    @FXML
    public void initialize() {
        setupTable();
        setupSearch();
        tblTransferLogs.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupActions();
        loadData();
    }

    private void setupActions() {
        btnSearch.setOnAction(e -> onSearch());
        btnClear.setOnAction(e -> clearFilters());

        tblTransferLogs.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode().toString().equals("C")) {
                copySelectionToClipboard();
            }
        });
    }

    private void setupTable() {
        colNo.setCellValueFactory(cell ->
                new SimpleIntegerProperty(tblTransferLogs.getItems().indexOf(cell.getValue()) + 1).asObject()
        );
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colFromWarehouse.setCellValueFactory(new PropertyValueFactory<>("fromWarehouse"));
        colToWarehouse.setCellValueFactory(new PropertyValueFactory<>("toWarehouse"));
        colTransferTime.setCellValueFactory(new PropertyValueFactory<>("formattedTime"));
        colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colSpec.setCellValueFactory(new PropertyValueFactory<>("spec"));
        colSapCode.setCellValueFactory(new PropertyValueFactory<>("sapCode"));
        // ✅ Cho phép chọn từng ô
        tblTransferLogs.getSelectionModel().setCellSelectionEnabled(true);

        // ✅ Cho phép chọn nhiều ô (liên tiếp hoặc rời rạc)
        tblTransferLogs.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void setupSearch() {
        List<Warehouse> warehouses = warehouseService.getAllWarehouses();
        cbFromWarehouse.setItems(FXCollections.observableArrayList(warehouses));
        cbToWarehouse.setItems(FXCollections.observableArrayList(warehouses));

        Callback<ListView<Warehouse>, ListCell<Warehouse>> cellFactory = lv -> new ListCell<>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        };

        cbFromWarehouse.setCellFactory(cellFactory);
        cbFromWarehouse.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Từ kho");
                } else {
                    setText(item.getName());
                }
            }
        });
        cbToWarehouse.setCellFactory(cellFactory);
        cbToWarehouse.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Đến kho");
                } else {
                    setText(item.getName());
                }
            }
        });
        //onSearch();

        // Gắn nút tìm kiếm
        //btnSearch.setOnAction(e -> onSearch());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void loadData() {
        var rawLogs = transferLogService.getAllTransfers(); // Bạn cần thêm hàm này để lấy TransferLog (entity), không DTO
        var allMaterials = materialService.getAllMaterials();   // 1 lần duy nhất
        var warehouseMap = warehouseService.getAllWarehouses().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Warehouse::getWarehouseId, Warehouse::getName
                ));

        var materialMap = allMaterials.stream()
                .collect(java.util.stream.Collectors.toMap(
                        m -> m.getRollCode(), m -> m
                ));

        var formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        allLogs = rawLogs.stream().map(log -> {
            var material = materialMap.getOrDefault(log.getRollCode(), null);
            String spec = material != null ? material.getSpec() : "N/A";
            String sap = material != null ? material.getSapCode() : "N/A";
            return new TransferLogDto(
                    log.getRollCode(),
                    warehouseMap.getOrDefault(log.getFromWarehouseId(), "Unknown"),
                    warehouseMap.getOrDefault(log.getToWarehouseId(), "Unknown"),
                    log.getTransferDate().format(formatter),
                    log.getEmployeeId(),
                    spec,
                    sap
            );
        }).toList();

        tblTransferLogs.setItems(FXCollections.observableArrayList(allLogs));
    }

    private void onSearch() {
        String sapCode = txtSapCode.getText().toLowerCase().trim();
        String barcode = txtBarcode.getText().toLowerCase().trim();
        String from = cbFromWarehouse.getValue() != null ? cbFromWarehouse.getValue().getName() : null;
        String to = cbToWarehouse.getValue() != null ? cbToWarehouse.getValue().getName() : null;
        var fromDate = dpFromDate.getValue();
        var toDate = dpToDate.getValue();

        if (sapCode.isEmpty() && barcode.isEmpty() && from == null && to == null && fromDate == null && toDate == null) {
            loadData();
            return;
        }

        var filtered = allLogs.stream()
                .filter(log -> sapCode.isEmpty() || log.getSapCode().toLowerCase().contains(sapCode))
                .filter(log -> barcode.isEmpty() || log.getBarcode().toLowerCase().contains(barcode))
                .filter(log -> from == null || log.getFromWarehouse().equals(from))
                .filter(log -> to == null || log.getToWarehouse().equals(to))
                .filter(log -> {
                    // nếu không lọc theo ngày
                    if (fromDate == null && toDate == null) return true;

                    var logDate = java.time.LocalDateTime.parse(log.getFormattedTime(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).toLocalDate();
                    boolean ok = true;
                    if (fromDate != null) ok &= !logDate.isBefore(fromDate);
                    if (toDate != null) ok &= !logDate.isAfter(toDate);
                    return ok;
                })
                .toList();

        tblTransferLogs.setItems(FXCollections.observableArrayList(filtered));
    }


    private void clearFilters() {
        txtSapCode.clear();
        txtBarcode.clear();
        cbFromWarehouse.getSelectionModel().clearSelection();
        cbToWarehouse.getSelectionModel().clearSelection();
        dpFromDate.setValue(null);
        dpToDate.setValue(null);
        //onSearch();
        tblTransferLogs.setItems(FXCollections.emptyObservableList());

    }

    private void copySelectionToClipboard() {
        StringBuilder clipboardString = new StringBuilder();
        ObservableList<TablePosition> positionList = tblTransferLogs.getSelectionModel().getSelectedCells();

        int prevRow = -1;
        for (TablePosition position : positionList) {
            int row = position.getRow();
            int col = position.getColumn();

            Object cell = tblTransferLogs.getColumns().get(col).getCellData(row);
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



}
