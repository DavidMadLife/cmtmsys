package org.chemtrovina.cmtmsys.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.dto.TransferredDto;
import org.chemtrovina.cmtmsys.model.Material;
import org.chemtrovina.cmtmsys.model.TransferLog;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InventoryTransferController {

    // ComboBox hiển thị tên kho
    @FXML private ComboBox<String> cbSourceWarehouse;
    @FXML private ComboBox<String> cbTargetWarehouse;

    // Các field khác (đã có sẵn trong FXML)
    @FXML private TextField txtEmployeeID;
    @FXML private TextField txtBarcode;
    @FXML private TextField txtSapCode;
    @FXML private TextField txtSpec;
    @FXML private TextField txtQuantity;
    @FXML private Button btnTransfer;

    @FXML private TableView<TransferredDto> tblTransferred;
    @FXML private TableColumn<TransferredDto, String> colBarcode;
    @FXML private TableColumn<TransferredDto, String> colSapCode;
    @FXML private TableColumn<TransferredDto, String> colSpec;
    @FXML private TableColumn<TransferredDto, Integer> colQuantity;
    @FXML private TableColumn<TransferredDto, String> colFromWarehouse;
    @FXML private TableColumn<TransferredDto, String> colToWarehouse;


    private WarehouseService warehouseService;
    private TransferLogService transferLogService;
    private MaterialService materialService;
    private final List<TransferredDto> transferredList = new ArrayList<>();

    @FXML
    public void initialize() {
        setupWarehouseService();
        loadWarehouses();

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

    }


    private void setupWarehouseService() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getDataSource());
        WarehouseRepository warehouseRepository = new WarehouseRepositoryImpl(jdbcTemplate);
        this.warehouseService = new WarehouseServiceImpl(warehouseRepository);

        MaterialRepository materialRepository = new MaterialRepositoryImpl(jdbcTemplate);
        //this.materialService = new MaterialServiceImpl(materialRepository, warehouseService);

        TransferLogRepository transferLogRepository = new TransferLogRepositoryImpl(jdbcTemplate);
        this.transferLogService = new TransferLogServiceImpl(transferLogRepository, warehouseService, materialService);

        this.materialService = new MaterialServiceImpl(materialRepository, warehouseService, transferLogService);
    }
    private void setupTransferredTable() {
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void setupBarcodeScanner() {
        txtBarcode.setOnAction(e -> handleBarcodeScanned());
    }

    private void handleBarcodeScanned() {
        String barcode = txtBarcode.getText().trim();
        if (barcode.isEmpty()) return;

        int toId = getSelectedWarehouseId(cbTargetWarehouse);
        int fromId = getSelectedWarehouseId(cbSourceWarehouse);
        if (toId == -1 || fromId == -1 || toId == fromId) return;

        Material material = materialService.transferMaterial(barcode, txtEmployeeID.getText(), toId);
        if (material == null) {
            showAlert("Không tìm thấy cuộn vật liệu có mã: " + barcode);
            return;
        }

        txtSapCode.setText(material.getSapCode());
        txtSpec.setText(material.getSpec());
        txtQuantity.setText(String.valueOf(material.getQuantity()));

        TransferredDto dto = new TransferredDto(
                material.getRollCode(),
                material.getSapCode(),
                material.getSpec(),
                material.getQuantity(),
                cbSourceWarehouse.getValue(),
                cbTargetWarehouse.getValue()
        );
        transferredList.add(dto);
        if (tblTransferred.getItems().isEmpty()) {
            tblTransferred.setItems(FXCollections.observableArrayList(transferredList));
        } else {
            tblTransferred.getItems().add(dto);
        }

        txtBarcode.clear();
    }

    private void showAlert(String msg) {
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

}
