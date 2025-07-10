package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.dto.MaterialDto;
import org.chemtrovina.cmtmsys.model.Material;
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
import org.chemtrovina.cmtmsys.utils.FxFilterUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Component
public class InventoryCheckController {

    @FXML private TableView<MaterialDto> tblMaterials;
    @FXML private TableColumn<MaterialDto, Integer> colNo;
    @FXML private TableColumn<MaterialDto, String> colSapCode;
    @FXML private TableColumn<MaterialDto, String> colSpec;
    @FXML private TableColumn<MaterialDto, String> colRollCode;
    @FXML private TableColumn<MaterialDto, Integer> colQuantity;
    @FXML private TableColumn<MaterialDto, String> colWarehouse;
    @FXML private TableColumn<MaterialDto, LocalDateTime> colCreatedAt;
    @FXML private TableColumn<MaterialDto, String> colEmployeeId;
    @FXML private Button btnChooseFile;
    @FXML private Button btnImportData;
    @FXML private Text txtFileName;
    @FXML private TextField txtFilterSapCode;
    @FXML private TextField txtFilterBarcode;
    @FXML private DatePicker dpFromDate;
    @FXML private DatePicker dpToDate;
    @FXML private ComboBox<Warehouse> cbWarehouses;
    @FXML private Button btnSearch;
    @FXML private Button btnClear;

    private File selectedFile;


    private final MaterialService materialService;
    private final WarehouseService warehouseService;
    private final TransferLogService transferLogService;

    @Autowired
    public InventoryCheckController(MaterialService materialService, WarehouseService warehouseService, TransferLogService transferLogService) {

        this.materialService = materialService;
        this.warehouseService = warehouseService;
        this.transferLogService = transferLogService;

    }

    @FXML
    public void initialize() {

        setupTable();
        //loadData();
        setupFileImport();
        setupSearch();
        tblMaterials.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        btnClear.setOnAction(e -> clearFilters());

    }

    private void setupTable() {
        colNo.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(tblMaterials.getItems().indexOf(cellData.getValue()) + 1).asObject()
        );

        colSapCode.setCellValueFactory(new PropertyValueFactory<>("sapCode"));
        colRollCode.setCellValueFactory(new PropertyValueFactory<>("rollCode"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colWarehouse.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        colSpec.setCellValueFactory(new PropertyValueFactory<>("spec"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        tblMaterials.getSelectionModel().setCellSelectionEnabled(true);
        tblMaterials.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


    }

    private void setupFileImport() {
        btnChooseFile.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Chọn file Excel");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
            selectedFile = fileChooser.showOpenDialog(btnChooseFile.getScene().getWindow());
            txtFileName.setText(selectedFile != null ? selectedFile.getName() : "No file selected");
        });

        btnImportData.setOnAction(e -> {
            if (selectedFile == null) {
                showAlert(Alert.AlertType.WARNING, "Chưa chọn file", "Vui lòng chọn file Excel trước khi import.");
                return;
            }

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Nhập mã nhân viên");

            dialog.showAndWait().ifPresent(employeeId -> {
                employeeId = employeeId.trim();
                if (employeeId.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Mã nhân viên không được để trống.");
                } else {
                    importFromExcelFile(selectedFile, employeeId);
                    txtFileName.setText("No file selected");
                    selectedFile = null;
                }
            });
        });
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void loadData() {
        List<MaterialDto> dtos = materialService.getAllMaterialDtos();
        tblMaterials.setItems(FXCollections.observableArrayList(dtos));
        //setupFilterMenus(dtos);
    }

    private void importFromExcelFile(File file, String employeeId) {
        try {
            materialService.importMaterialsFromExcel(file, employeeId);
            loadData();
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Import dữ liệu thành công.");
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể import dữ liệu: " + ex.getMessage());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void setupSearch() {
        List<Warehouse> warehouses = warehouseService.getAllWarehouses();
        cbWarehouses.setItems(FXCollections.observableArrayList(warehouses));

        // Cấu hình hiển thị tên kho trong dropdown
        cbWarehouses.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        cbWarehouses.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        btnSearch.setOnAction(e -> onSearch());
        setupCopyAction();

    }

    private void onSearch() {
        String sapCode = txtFilterSapCode.getText();
        String barcode = txtFilterBarcode.getText();
        LocalDate from = dpFromDate.getValue();
        LocalDate to = dpToDate.getValue();
        Warehouse selectedWarehouse = cbWarehouses.getValue();

        Integer warehouseId = selectedWarehouse != null ? selectedWarehouse.getWarehouseId() : null;

        List<MaterialDto> filtered = materialService.searchMaterials(
                sapCode,
                barcode,
                from != null ? from.atStartOfDay() : null,
                to != null ? to.atTime(23, 59, 59) : null,
                warehouseId
        );
        tblMaterials.setItems(FXCollections.observableArrayList(filtered));
        //setupFilterMenus(filtered);
    }

    private void setupCopyAction() {
        tblMaterials.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode().toString().equals("C")) {
                copyMaterialSelectionToClipboard();
            }
        });
    }

    private void copyMaterialSelectionToClipboard() {
        StringBuilder clipboardString = new StringBuilder();
        ObservableList<TablePosition> positionList = tblMaterials.getSelectionModel().getSelectedCells();

        int prevRow = -1;
        for (TablePosition position : positionList) {
            int row = position.getRow();
            int col = position.getColumn();

            Object cell = tblMaterials.getColumns().get(col).getCellData(row);
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





    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void clearFilters() {
        txtFilterSapCode.clear();
        txtFilterBarcode.clear();
        cbWarehouses.getSelectionModel().clearSelection();
        dpFromDate.setValue(null);
        dpToDate.setValue(null);
        //onSearch();
        tblMaterials.setItems(FXCollections.emptyObservableList());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void applyGeneralFilter(List<String> selectedValues) {
        List<MaterialDto> all = materialService.getAllMaterialDtos();
        List<MaterialDto> filtered = all.stream()
                .filter(m ->
                        selectedValues.contains(m.getSapCode()) ||
                                selectedValues.contains(m.getSpec()) ||
                                selectedValues.contains(m.getRollCode()) ||
                                selectedValues.contains(String.valueOf(m.getQuantity())) ||
                                selectedValues.contains(m.getWarehouseName()) ||
                                selectedValues.contains(String.valueOf(m.getCreatedAt())) ||
                                selectedValues.contains(m.getEmployeeId())
                )
                .collect(Collectors.toList());

        tblMaterials.setItems(FXCollections.observableArrayList(filtered));
    }

    private void setupFilterMenus(List<MaterialDto> data) {
        // Dọn context menu cũ (nếu có)
        colSapCode.setContextMenu(null);
        colSpec.setContextMenu(null);
        colRollCode.setContextMenu(null);
        colWarehouse.setContextMenu(null);
        colEmployeeId.setContextMenu(null);

        // Setup lại filter menu
        FxFilterUtils.setupFilterMenu(colSapCode, data, MaterialDto::getSapCode, this::applyGeneralFilter);
        FxFilterUtils.setupFilterMenu(colSpec, data, MaterialDto::getSpec, this::applyGeneralFilter);
        FxFilterUtils.setupFilterMenu(colRollCode, data, MaterialDto::getRollCode, this::applyGeneralFilter);
        FxFilterUtils.setupFilterMenu(colWarehouse, data, MaterialDto::getWarehouseName, this::applyGeneralFilter);
        FxFilterUtils.setupFilterMenu(colEmployeeId, data, MaterialDto::getEmployeeId, this::applyGeneralFilter);
    }




}
