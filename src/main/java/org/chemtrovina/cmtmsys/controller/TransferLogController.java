package org.chemtrovina.cmtmsys.controller;

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
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class TransferLogController {

    @FXML private TableView<TransferLogDto> tblTransferLogs;
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



    private TransferLogService transferLogService;
    private WarehouseService warehouseService;
    private MaterialService materialService;

    @FXML
    public void initialize() {
        setupServices();
        setupTable();
        setupSearch();
        loadData();
        tblTransferLogs.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupActions();

    }

    private void setupServices() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getDataSource());

        WarehouseRepository warehouseRepository = new WarehouseRepositoryImpl(jdbcTemplate);
        this.warehouseService = new WarehouseServiceImpl(warehouseRepository);

        MaterialRepository materialRepository = new MaterialRepositoryImpl(jdbcTemplate);
        this.materialService = new MaterialServiceImpl(materialRepository, this.warehouseService, null); // nếu cần null tạm thời

        TransferLogRepository transferLogRepository = new TransferLogRepositoryImpl(jdbcTemplate);
        this.transferLogService = new TransferLogServiceImpl(transferLogRepository, this.warehouseService, this.materialService);
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
        onSearch();

        // Gắn nút tìm kiếm
        btnSearch.setOnAction(e -> onSearch());
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void loadData() {
        List<TransferLogDto> logs = transferLogService.getAllTransferLogDtos();
        tblTransferLogs.setItems(FXCollections.observableArrayList(logs));
    }

    private void onSearch() {
        String sapCode = txtSapCode.getText();
        String barcode = txtBarcode.getText();
        Integer fromWarehouseId = cbFromWarehouse.getValue() != null ? cbFromWarehouse.getValue().getWarehouseId() : null;
        Integer toWarehouseId = cbToWarehouse.getValue() != null ? cbToWarehouse.getValue().getWarehouseId() : null;
        var fromDate = dpFromDate.getValue() != null ? dpFromDate.getValue().atStartOfDay() : null;
        var toDate = dpToDate.getValue() != null ? dpToDate.getValue().atTime(23, 59, 59) : null;

        var logs = transferLogService.searchTransfers(sapCode, barcode, fromWarehouseId, toWarehouseId, fromDate, toDate);

        var warehouseMap = warehouseService.getAllWarehouses().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Warehouse::getWarehouseId, Warehouse::getName
                ));

        var formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        var dtos = logs.stream().map(log -> {
            var material = materialService.getMaterialByRollCode(log.getRollCode());
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

        tblTransferLogs.setItems(FXCollections.observableArrayList(dtos));
    }

    private void clearFilters() {
        txtSapCode.clear();
        txtBarcode.clear();
        cbFromWarehouse.getSelectionModel().clearSelection();
        cbToWarehouse.getSelectionModel().clearSelection();
        dpFromDate.setValue(null);
        dpToDate.setValue(null);
        onSearch();
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
