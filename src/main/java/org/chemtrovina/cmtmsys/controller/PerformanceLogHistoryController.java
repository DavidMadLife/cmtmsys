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
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.service.base.MaterialConsumeDetailLogService;
import org.chemtrovina.cmtmsys.service.base.PcbPerformanceLogService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.chemtrovina.cmtmsys.utils.FxAlertUtils;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class PerformanceLogHistoryController {

    // ============================================================================
    // 🧩 1️⃣ FXML FIELDS
    // ============================================================================
    @FXML private TextField txtModelCode;
    @FXML private ComboBox<ModelType> cbModelType;
    @FXML private ComboBox<String> cbWarehouse;

    @FXML private DatePicker dpStartDate, dpEndDate;
    @FXML private Button btnSearch;
    @FXML private Button btnClearFilter;



    // Bảng log hiệu suất
    @FXML private TableView<PcbPerformanceLogHistoryDTO> tblLogs;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, String> colModelCode;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, String> colCarrierId;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, String> colAoi;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, Integer> colTotal;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, Integer> colNg;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, Double> colPerformance;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, String> colFileName;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, String> colWarehouse;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, LocalDateTime> colCreatedAt;

    // Bảng vật tư sử dụng
    @FXML private TableView<MaterialUsage> tblMaterials;
    @FXML private TableColumn<MaterialUsage, String> colSapCode;
    @FXML private TableColumn<MaterialUsage, String> colRollCode;
    @FXML private TableColumn<MaterialUsage, Integer> colQuantity;
    @FXML private TableColumn<MaterialUsage, String> colWarehouseName;
    @FXML private TableColumn<MaterialUsage, String> colSpec;
    @FXML private TableColumn<MaterialUsage, String> colLot;
    @FXML private TableColumn<MaterialUsage, String> colMaker; // 🆕
    @FXML private TableColumn<MaterialUsage, LocalDateTime> colCreated;

    @FXML private TextField txtCarrierLookup;
    @FXML private Button btnLookupCarrier;
    @FXML private Label lblCarrierInfo;


    // ============================================================================
    // ⚙️ 2️⃣ SERVICES & STATE
    // ============================================================================
    private final PcbPerformanceLogService logService;
    private final MaterialConsumeDetailLogService consumeDetailService;
    private final WarehouseService warehouseService;
    private final ObservableList<PcbPerformanceLogHistoryDTO> logList = FXCollections.observableArrayList();

    // ============================================================================
    // 🏗️ 3️⃣ CONSTRUCTOR
    // ============================================================================
    @Autowired
    public PerformanceLogHistoryController(PcbPerformanceLogService logService,
                                           MaterialConsumeDetailLogService consumeDetailService,
                                           WarehouseService warehouseService) {
        this.logService = logService;
        this.consumeDetailService = consumeDetailService;
        this.warehouseService = warehouseService;
    }

    // ============================================================================
    // 🚀 4️⃣ INITIALIZATION
    // ============================================================================
    @FXML
    public void initialize() {
        setupComboBoxes();
        setupLogTable();
        setupWarehouseCombo();
        setupMaterialTable();
        FxClipboardUtils.enableCopyShortcut(tblLogs);
        FxClipboardUtils.enableCopyShortcut(tblMaterials);
        setupEvents();
    }

    // ============================================================================
    // 🧱 5️⃣ UI SETUP
    // ============================================================================
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
    }

    private void setupMaterialTable() {
        colSapCode.setCellValueFactory(new PropertyValueFactory<>("sapCode"));
        colRollCode.setCellValueFactory(new PropertyValueFactory<>("rollCode"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantityUsed"));
        colWarehouseName.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        colSpec.setCellValueFactory(new PropertyValueFactory<>("spec"));
        colLot.setCellValueFactory(new PropertyValueFactory<>("lot"));
        colCreated.setCellValueFactory(new PropertyValueFactory<>("created"));
    }

    private void setupWarehouseCombo() {
        cbWarehouse.setItems(FXCollections.observableArrayList(
                warehouseService.getAllWarehouses().stream()
                        .map(w -> w.getName()) // ✅ chỉ lấy tên
                        .toList()
        ));
    }




    // ============================================================================
    // 🎯 6️⃣ EVENT HANDLERS
    // ============================================================================
    private void setupEvents() {
        btnSearch.setOnAction(e -> performSearch());
        btnClearFilter.setOnAction(e -> clearFilters());
        tblLogs.getSelectionModel().selectedItemProperty().addListener((obs, old, now) -> {
            if (now != null) loadMaterialsFor(now);
        });

        btnLookupCarrier.setOnAction(e -> lookupCarrier());

        tblLogs.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                clearFilters();
            }
        });


    }

    // ============================================================================
    // 🔍 7️⃣ CORE LOGIC
    // ============================================================================
    private void performSearch() {
        String modelCode = txtModelCode.getText().trim();
        ModelType modelType = cbModelType.getValue();

        LocalDate start = dpStartDate.getValue();
        LocalDate end = dpEndDate.getValue();

        LocalDateTime from = (start != null) ? start.atStartOfDay() : null;
        LocalDateTime to = (end != null) ? end.atTime(LocalTime.MAX) : null;

        Integer warehouseId = null;
        String selectedName = cbWarehouse.getValue();
        if (selectedName != null && !selectedName.isBlank()) {
            warehouseId = warehouseService.getAllWarehouses().stream()
                    .filter(w -> w.getName().equals(selectedName))
                    .map(w -> w.getWarehouseId())
                    .findFirst()
                    .orElse(null);
        }


        List<PcbPerformanceLogHistoryDTO> results = logService.searchLogs(
                modelCode.isEmpty() ? null : modelCode,
                modelType,
                from,
                to,
                warehouseId   // 🆕 Thêm tham số này
        );

        logList.setAll(results);

        if (!results.isEmpty()) {
            tblLogs.getSelectionModel().select(0);
            loadMaterialsFor(results.get(0));
        } else {
            tblMaterials.setItems(FXCollections.emptyObservableList());
        }
    }

    private void lookupCarrier() {
        String carrierId = txtCarrierLookup.getText().trim();
        if (carrierId.isEmpty()) {
            FxAlertUtils.warning("⚠️ Vui lòng nhập Carrier ID để tra cứu.");
            return;
        }

        // Lấy thông tin log theo Carrier ID
        List<PcbPerformanceLogHistoryDTO> logs = logService.getLogsByCarrierId(carrierId);

        if (logs.isEmpty()) {
            lblCarrierInfo.setText("❌ Không tìm thấy dữ liệu cho Carrier ID: " + carrierId);
            tblMaterials.setItems(FXCollections.emptyObservableList());
            return;
        }

        PcbPerformanceLogHistoryDTO log = logs.get(0);
        lblCarrierInfo.setText("✅ Model: " + log.getModelCode() + " | AOI: " + log.getAoi() + " | Line: " + log.getWarehouseName());

        // Hiển thị vật tư đã dùng
        List<MaterialUsage> materials = consumeDetailService.getMaterialUsageBySourceLog(log.getLogId());
        tblMaterials.setItems(FXCollections.observableArrayList(materials));

        // Highlight log tương ứng trong bảng
        tblLogs.getSelectionModel().clearSelection();
        tblLogs.getItems().stream()
                .filter(item -> item.getCarrierId().equalsIgnoreCase(carrierId))
                .findFirst()
                .ifPresent(item -> tblLogs.getSelectionModel().select(item));
    }



    private void loadMaterialsFor(PcbPerformanceLogHistoryDTO dto) {
        if (dto.getLogId() == 0) {
            tblMaterials.setItems(FXCollections.emptyObservableList());
            return;
        }

        List<MaterialUsage> rows = consumeDetailService.getMaterialUsageBySourceLog(dto.getLogId());
        tblMaterials.setItems(FXCollections.observableArrayList(rows));
    }
    private void clearFilters() {
        // 🧹 Xóa các trường lọc
        txtModelCode.clear();
        cbModelType.getSelectionModel().clearSelection();
        cbWarehouse.getSelectionModel().clearSelection();
        dpStartDate.setValue(null);
        dpEndDate.setValue(null);

        // 🧩 Làm trống bảng kết quả
        logList.clear();
        tblMaterials.getItems().clear();

        // 🧠 Reset label tra cứu nếu có
        lblCarrierInfo.setText("");
    }

}
