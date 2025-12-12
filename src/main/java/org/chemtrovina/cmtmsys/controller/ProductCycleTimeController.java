package org.chemtrovina.cmtmsys.controller;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.chemtrovina.cmtmsys.dto.ProductCycleTimeViewDto;
import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.service.base.ProductCycleTimeService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.chemtrovina.cmtmsys.utils.FxAlertUtils;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

@Component
public class ProductCycleTimeController {

    // ===== FXML =====
    @FXML private TextField txtProductCode;
    @FXML private ComboBox<String> cbModelTypeFilter;
    @FXML private ComboBox<Warehouse> cbLines;
    @FXML private Button btnLoad;

    @FXML private Button btnChooseFile, btnImport;
    @FXML private TextField txtFileName;

    // Table
    @FXML private TableView<ProductCycleTimeViewDto> tblCTime;

    @FXML private TableColumn<ProductCycleTimeViewDto, Number> colIndex;
    @FXML private TableColumn<ProductCycleTimeViewDto, String> colProductCode, colModelType, colLine, colCreatedAt;
    @FXML private TableColumn<ProductCycleTimeViewDto, Integer> colArray, colVersion;
    @FXML private TableColumn<ProductCycleTimeViewDto, BigDecimal> colCTSec;
    @FXML private TableColumn<ProductCycleTimeViewDto, Boolean> colActive;

    // ===== STATE =====
    private File selectedFile;

    private final ProductCycleTimeService cycleTimeService;
    private final WarehouseService warehouseService;

    public ProductCycleTimeController(ProductCycleTimeService cycleTimeService, WarehouseService warehouseService) {
        this.cycleTimeService = cycleTimeService;
        this.warehouseService = warehouseService;
    }

    // =======================================================================
    // INIT
    // =======================================================================
    @FXML
    public void initialize() {
        setupModelTypeCombo();
        setupWarehouseCombo();
        setupTable();
        setupButtons();
        setupShortcuts();
    }

    // =======================================================================
    // SETUP UI
    // =======================================================================

    private void setupButtons() {
        btnLoad.setOnAction(e -> loadData());
        btnChooseFile.setOnAction(e -> chooseFile());
        btnImport.setOnAction(e -> importCycleTimes());
    }

    private void setupShortcuts() {
        tblCTime.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == javafx.scene.input.KeyCode.C) {
                FxClipboardUtils.copySelectionToClipboard(tblCTime);
            }
        });
    }

    private void setupModelTypeCombo() {
        cbModelTypeFilter.setItems(FXCollections.observableArrayList("TOP","BOT","SINGLE","BOTTOP","NONE"));
    }

    private void setupWarehouseCombo() {
        List<Warehouse> allLines = warehouseService.getAllWarehouses();
        cbLines.setItems(FXCollections.observableArrayList(allLines));

        cbLines.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        cbLines.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
    }

    private void setupTable() {

        colIndex.setCellValueFactory(row -> Bindings.createIntegerBinding(
                () -> tblCTime.getItems().indexOf(row.getValue()) + 1));

        colProductCode.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        colModelType.setCellValueFactory(new PropertyValueFactory<>("modelType"));
        colLine.setCellValueFactory(new PropertyValueFactory<>("lineName"));
        colArray.setCellValueFactory(new PropertyValueFactory<>("array"));
        colCTSec.setCellValueFactory(new PropertyValueFactory<>("ctSeconds"));
        colVersion.setCellValueFactory(new PropertyValueFactory<>("version"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        FxClipboardUtils.enableCopyShortcut(tblCTime);
    }

    // =======================================================================
    // LOAD DATA
    // =======================================================================

    private void loadData() {
        String productCode = txtProductCode.getText() == null ? "" : txtProductCode.getText().trim();
        String typeStr = cbModelTypeFilter.getValue();

        ModelType modelType = null;
        if (typeStr != null && !typeStr.isBlank()) {
            try { modelType = ModelType.valueOf(typeStr.toUpperCase()); }
            catch (Exception ignored) {}
        }

        Warehouse line = cbLines.getValue();
        String lineName = (line == null ? "" : line.getName());

        var list = cycleTimeService.searchView(productCode, modelType, lineName);
        tblCTime.setItems(FXCollections.observableArrayList(list));
    }

    // =======================================================================
    // IMPORT
    // =======================================================================

    private void chooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn file Excel Cycle Time");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        selectedFile = chooser.showOpenDialog(null);

        txtFileName.setText(selectedFile != null ? selectedFile.getName() : "Chưa chọn file");
    }

    private void importCycleTimes() {
        if (selectedFile == null) {
            FxAlertUtils.warning("Vui lòng chọn file Excel.");
            return;
        }

        try {
            cycleTimeService.importCycleTimesFromExcel(selectedFile);
            FxAlertUtils.info("✅ Import cycle time hoàn tất.");
            loadData();
        } catch (Exception ex) {
            ex.printStackTrace();
            FxAlertUtils.error("❌ Lỗi khi import: " + ex.getMessage());
        }
    }

}
