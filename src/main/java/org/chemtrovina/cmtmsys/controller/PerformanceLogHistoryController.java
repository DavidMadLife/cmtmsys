package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import org.chemtrovina.cmtmsys.dto.MaterialUsage;
import org.chemtrovina.cmtmsys.dto.PcbPerformanceLogHistoryDTO;
import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.service.base.MaterialConsumeDetailLogService;
import org.chemtrovina.cmtmsys.service.base.PcbPerformanceLogService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.chemtrovina.cmtmsys.utils.FxAlertUtils;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class PerformanceLogHistoryController {

    // ======================================================================
    // ✔ FXML
    // ======================================================================
    @FXML private TextField txtModelCode, txtCarrierLookup;
    @FXML private ComboBox<ModelType> cbModelType;
    @FXML private ComboBox<String> cbWarehouse;
    @FXML private DatePicker dpStartDate, dpEndDate;
    @FXML private Button btnSearch, btnClearFilter, btnLookupCarrier;
    @FXML private Label lblCarrierInfo;

    // Log Table
    @FXML private TableView<PcbPerformanceLogHistoryDTO> tblLogs;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, String> colModelCode;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, String> colCarrierId;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, String> colAoi;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, Integer> colTotal, colNg;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, Double> colPerformance;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, String> colFileName;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, String> colWarehouse;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, LocalDateTime> colCreatedAt;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, Double> colTimeDiff;


    // Material Table
    @FXML private TableView<MaterialUsage> tblMaterials;
    @FXML private TableColumn<MaterialUsage, String> colSapCode, colRollCode, colWarehouseName, colSpec, colLot;
    @FXML private TableColumn<MaterialUsage, Integer> colQuantity;
    @FXML private TableColumn<MaterialUsage, LocalDateTime> colCreated;

    // ======================================================================
    // ✔ SERVICES
    // ======================================================================
    private final PcbPerformanceLogService logService;
    private final MaterialConsumeDetailLogService consumeDetailService;
    private final WarehouseService warehouseService;

    private final ObservableList<PcbPerformanceLogHistoryDTO> logList = FXCollections.observableArrayList();
    private List<Warehouse> warehouseCache;  // ✔ Cache warehouse tránh query DB nhiều lần

    // ======================================================================
    // ✔ CONSTRUCTOR
    // ======================================================================
    @Autowired
    public PerformanceLogHistoryController(
            PcbPerformanceLogService logService,
            MaterialConsumeDetailLogService consumeDetailService,
            WarehouseService warehouseService) {

        this.logService = logService;
        this.consumeDetailService = consumeDetailService;
        this.warehouseService = warehouseService;
    }

    // ======================================================================
    // 🚀 INITIALIZE
    // ======================================================================
    @FXML
    public void initialize() {
        loadWarehouseCache();
        setupComboBoxes();
        setupLogTable();
        setupMaterialTable();
        setupEvents();
        FxClipboardUtils.enableCopyShortcut(tblLogs);
        FxClipboardUtils.enableCopyShortcut(tblMaterials);
    }

    // ======================================================================
    // ✔ LOAD WAREHOUSE ONE TIME ONLY
    // ======================================================================
    private void loadWarehouseCache() {
        warehouseCache = warehouseService.getAllWarehouses();
        cbWarehouse.setItems(
                FXCollections.observableArrayList(
                        warehouseCache.stream().map(Warehouse::getName).toList()
                )
        );
    }

    // ======================================================================
    // ✔ UI SETUP
    // ======================================================================
    private void setupComboBoxes() {
        cbModelType.setItems(FXCollections.observableArrayList(ModelType.values()));
    }

    private void setupLogTable() {
        tblLogs.setItems(logList);

        colModelCode.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getModelCode()));
        colCarrierId.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getCarrierId()));
        colAoi.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getAoi()));
        colTotal.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getTotalModules()));
        colNg.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getNgModules()));
        colPerformance.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getPerformance()));
        colFileName.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getLogFileName()));
        colWarehouse.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getWarehouseName()));
        colCreatedAt.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getCreatedAt()));
        colTimeDiff.setCellValueFactory(d ->
                new ReadOnlyObjectWrapper<>(d.getValue().getTimeDiffSeconds()));

    }

    private void setupMaterialTable() {
        colRollCode.setCellValueFactory(new PropertyValueFactory<>("rollCode"));
        colSapCode.setCellValueFactory(new PropertyValueFactory<>("sapCode"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantityUsed"));
        colWarehouseName.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        colSpec.setCellValueFactory(new PropertyValueFactory<>("spec"));
        colLot.setCellValueFactory(new PropertyValueFactory<>("lot"));
        colCreated.setCellValueFactory(new PropertyValueFactory<>("created"));
    }

    // ======================================================================
    // ✔ EVENTS
    // ======================================================================
    private void setupEvents() {
        btnSearch.setOnAction(e -> performSearch());
        btnClearFilter.setOnAction(e -> clearFilters());
        btnLookupCarrier.setOnAction(e -> lookupCarrier());

        tblLogs.getSelectionModel().selectedItemProperty().addListener((obs, old, now) -> {
            if (now != null) loadMaterialsForLog(now);
        });

        tblLogs.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) clearFilters();
        });
    }

    // ======================================================================
    // 🔍 SEARCH
    // ======================================================================
    private void performSearch() {
        String modelCode = txtModelCode.getText().trim();
        ModelType modelType = cbModelType.getValue();

        LocalDateTime from = dpStartDate.getValue() != null ? dpStartDate.getValue().atStartOfDay() : null;
        LocalDateTime to = dpEndDate.getValue() != null ? dpEndDate.getValue().atTime(LocalTime.MAX) : null;

        Integer warehouseId = findWarehouseId(cbWarehouse.getValue());

        List<PcbPerformanceLogHistoryDTO> results = logService.searchLogs(
                modelCode.isEmpty() ? null : modelCode,
                modelType,
                from,
                to,
                warehouseId
        );

        logList.setAll(results);

        if (results.isEmpty()) {
            tblMaterials.getItems().clear();
            lblCarrierInfo.setText("");
            return;
        }

        // Auto-select first log
        tblLogs.getSelectionModel().selectFirst();
        loadMaterialsForLog(results.get(0));
    }

    // ======================================================================
    // 🔎 LOOKUP CARRIER
    // ======================================================================
    private void lookupCarrier() {
        String carrierId = txtCarrierLookup.getText().trim();
        if (carrierId.isEmpty()) {
            FxAlertUtils.warning("⚠️ Nhập Carrier ID để tra cứu.");
            return;
        }

        List<PcbPerformanceLogHistoryDTO> logs = logService.getLogsByCarrierId(carrierId);

        if (logs.isEmpty()) {
            lblCarrierInfo.setText("❌ Không tìm thấy dữ liệu cho: " + carrierId);
            tblMaterials.getItems().clear();
            return;
        }

        PcbPerformanceLogHistoryDTO log = logs.get(0);

        lblCarrierInfo.setText(
                "✅ Model: " + log.getModelCode() +
                        " | AOI: " + log.getAoi() +
                        " | Line: " + log.getWarehouseName()
        );

        loadMaterialsForLog(log);

        highlightCarrierInTable(carrierId);
    }

    private void highlightCarrierInTable(String carrierId) {
        tblLogs.getSelectionModel().clearSelection();

        tblLogs.getItems().stream()
                .filter(row -> row.getCarrierId().equalsIgnoreCase(carrierId))
                .findFirst()
                .ifPresent(row -> tblLogs.getSelectionModel().select(row));
    }

    // ======================================================================
    // 📦 MATERIAL TABLE LOAD
    // ======================================================================
    private void loadMaterialsForLog(PcbPerformanceLogHistoryDTO dto) {
        List<MaterialUsage> rows = consumeDetailService.getMaterialUsageBySourceLog(dto.getLogId());
        tblMaterials.setItems(FXCollections.observableArrayList(rows));
    }

    // ======================================================================
    // 🧹 CLEAR FILTERS
    // ======================================================================
    private void clearFilters() {
        txtModelCode.clear();
        cbModelType.getSelectionModel().clearSelection();
        cbWarehouse.getSelectionModel().clearSelection();
        dpStartDate.setValue(null);
        dpEndDate.setValue(null);
        lblCarrierInfo.setText("");

        logList.clear();
        tblMaterials.getItems().clear();
    }

    // ======================================================================
    // ❤️ UTILS
    // ======================================================================
    private Integer findWarehouseId(String name) {
        if (name == null || name.isBlank()) return null;

        return warehouseCache.stream()
                .filter(w -> w.getName().equals(name))
                .map(Warehouse::getWarehouseId)
                .findFirst()
                .orElse(null);
    }
}
