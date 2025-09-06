package org.chemtrovina.cmtmsys.controller;

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
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

@Component
public class ProductCycleTimeController {

    @FXML private TextField txtProductCode;
    @FXML private ComboBox<String> cbModelTypeFilter;
    @FXML private ComboBox<Warehouse> cbLines;
    @FXML private Button btnLoad;

    @FXML private Button btnChooseFile;
    @FXML private TextField txtFileName;
    @FXML private Button btnImport;

    @FXML private TableView<ProductCycleTimeViewDto> tblCTime;
    @FXML
    private TableColumn<ProductCycleTimeViewDto, Number> colIndex;
    @FXML private TableColumn<ProductCycleTimeViewDto, String> colProductCode;
    @FXML private TableColumn<ProductCycleTimeViewDto, String> colModelType;
    @FXML private TableColumn<ProductCycleTimeViewDto, String> colLine;
    @FXML private TableColumn<ProductCycleTimeViewDto, Integer> colArray;
    @FXML private TableColumn<ProductCycleTimeViewDto, BigDecimal> colCTSec;
    @FXML private TableColumn<ProductCycleTimeViewDto, Integer> colVersion;
    @FXML private TableColumn<ProductCycleTimeViewDto, Boolean> colActive;
    @FXML private TableColumn<ProductCycleTimeViewDto, String> colCreatedAt;

    private File selectedFile;

    private final ProductCycleTimeService cycleTimeService;
    private final WarehouseService warehouseService;

    @Autowired
    public ProductCycleTimeController(ProductCycleTimeService cycleTimeService, WarehouseService warehouseService) {
        this.cycleTimeService = cycleTimeService;
        this.warehouseService = warehouseService;
    }

    @FXML
    public void initialize() {
        cbModelTypeFilter.setItems(FXCollections.observableArrayList("TOP","BOT","SINGLE","BOTTOP","NONE"));
        setupTable();
        btnLoad.setOnAction(e -> onLoad());
        btnChooseFile.setOnAction(e -> onChooseFile());
        btnImport.setOnAction(e -> onImport());
        setupComboBoxes();

        tblCTime.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == javafx.scene.input.KeyCode.C) {
                FxClipboardUtils.copySelectionToClipboard(tblCTime); // true = kèm header
            }
        });



    }

    private void setupTable() {
        colIndex.setCellValueFactory(cd -> javafx.beans.binding.Bindings.createIntegerBinding(
                () -> tblCTime.getItems().indexOf(cd.getValue()) + 1));

        colProductCode.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        colModelType.setCellValueFactory(new PropertyValueFactory<>("modelType"));
        colLine.setCellValueFactory(new PropertyValueFactory<>("lineName"));
        colArray.setCellValueFactory(new PropertyValueFactory<>("array"));
        colCTSec.setCellValueFactory(new PropertyValueFactory<>("ctSeconds"));
        colVersion.setCellValueFactory(new PropertyValueFactory<>("version"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        tblCTime.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblCTime.getSelectionModel().setCellSelectionEnabled(true);
        tblCTime.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void setupComboBoxes() {
        // ModelType cho filter
        cbModelTypeFilter.setItems(FXCollections.observableArrayList("TOP","BOT","SINGLE","BOTTOP","NONE"));

        // Nạp list line từ service
        List<Warehouse> all = warehouseService.getAllWarehouses();
        cbLines.setItems(FXCollections.observableArrayList(all));

        // hiển thị Name trong list dropdown
        cbLines.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getName());
            }
        });

        // hiển thị Name trong ô đã chọn
        cbLines.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getName());
            }
        });

        // tuỳ chọn: tự load khi chọn line
        //cbLines.valueProperty().addListener((obs, old, val) -> onLoad());
    }


    private void onLoad() {
        String code = safe(txtProductCode.getText());
        String typeStr = cbModelTypeFilter.getValue();

        Warehouse selected = cbLines.getValue();
        String lineLike = (selected == null) ? "" : selected.getName();

        ModelType type = null;
        if (typeStr != null && !typeStr.isBlank()) {
            try { type = ModelType.valueOf(typeStr.trim().toUpperCase()); }
            catch (IllegalArgumentException ignored) { type = null; }
        }

        var list = cycleTimeService.searchView(code, type, lineLike);
        tblCTime.setItems(FXCollections.observableArrayList(list));
    }


    private void onChooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn file Excel Cycle Time");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
        selectedFile = chooser.showOpenDialog(null);
        txtFileName.setText(selectedFile != null ? selectedFile.getName() : "Chưa chọn file");
    }

    private void onImport() {
        if (selectedFile == null) {
            info("Vui lòng chọn file Excel.");
            return;
        }
        try {
            cycleTimeService.importCycleTimesFromExcel(selectedFile); // gọi service
            info("✅ Import cycle time hoàn tất.");
            onLoad();
        } catch (Exception ex) {
            ex.printStackTrace();
            info("❌ Lỗi khi import: " + ex.getMessage());
        }
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }
    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Thông báo"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}

