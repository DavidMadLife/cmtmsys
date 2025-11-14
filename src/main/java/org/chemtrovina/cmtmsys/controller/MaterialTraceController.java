package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.chemtrovina.cmtmsys.model.*;
import org.chemtrovina.cmtmsys.service.base.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class MaterialTraceController {

    @FXML private TextField txtCarrierId;
    @FXML private Button btnSearch;
    @FXML private TableView<TraceRow> tblTrace;
    @FXML private TableColumn<TraceRow, String> colRollCode, colSapCode, colFeederCode, colCreatedAt;
    @FXML private TableColumn<TraceRow, Integer> colConsumedQty;
    @FXML private TextArea txtSummary;

    @Autowired private PcbPerformanceLogService pcbLogService;
    @Autowired private MaterialConsumeDetailLogService consumeDetailService;
    @Autowired private MaterialService materialService;
    @Autowired private FeederAssignmentMaterialService assignmentMaterialService;
    @Autowired private FeederAssignmentService feederAssignmentService;
    @Autowired private FeederService feederService;
    @Autowired private ProductService productService;
    @Autowired private WarehouseService warehouseService;

    @FXML
    public void initialize() {
        colRollCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRollCode()));
        colSapCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSapCode()));
        colFeederCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFeederCode()));
        colConsumedQty.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getConsumedQty()).asObject());
        colCreatedAt.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCreatedAt().toString()));

        btnSearch.setOnAction(e -> handleSearch());
    }

    private void handleSearch() {
        String carrierId = txtCarrierId.getText().trim();
        if (carrierId.isEmpty()) {
            txtSummary.setText("⚠️ Vui lòng nhập Carrier ID để tra cứu.");
            return;
        }

        // 1️⃣ Tìm log AOI theo CarrierId
        PcbPerformanceLog log = pcbLogService.getByCarrierId(carrierId);
        if (log == null) {
            txtSummary.setText("❌ Không tìm thấy log cho Carrier: " + carrierId);
            tblTrace.getItems().clear();
            return;
        }

        // 2️⃣ Lấy danh sách chi tiết tiêu hao từ SourceLogId
        var detailLogs = consumeDetailService.getDetailsBySourceLog(log.getLogId());
        if (detailLogs == null || detailLogs.isEmpty()) {
            txtSummary.setText("📭 Không có vật tư nào được trừ cho Carrier: " + carrierId);
            tblTrace.getItems().clear();
            return;
        }

        // 3️⃣ Map vật tư và feeder
        List<TraceRow> traceRows = new ArrayList<>();
        for (MaterialConsumeDetailLog d : detailLogs) {
            Material mat = materialService.getMaterialById(d.getMaterialId());
            if (mat == null) continue;

            String rollCode = mat.getRollCode();
            String sapCode = mat.getSapCode();
            String feederCode = "N/A";

            // tìm feeder đã gắn cuộn này
            FeederAssignmentMaterial assignMat = assignmentMaterialService.getLatestByMaterialId(mat.getMaterialId());
            if (assignMat != null) {
                FeederAssignment assign = feederAssignmentService.getById(assignMat.getAssignmentId());
                if (assign != null) {
                    Feeder feeder = feederService.getFeederById(assign.getFeederId());
                    if (feeder != null) {
                        feederCode = feeder.getFeederCode();
                    }
                }
            }

            traceRows.add(new TraceRow(
                    rollCode,
                    sapCode,
                    feederCode,
                    d.getConsumedQty(),
                    d.getCreatedAt()
            ));
        }

        // 4️⃣ Hiển thị kết quả
        tblTrace.setItems(FXCollections.observableArrayList(traceRows));

        String modelName = productService.getProductById(log.getProductId()).getProductCode();
        String lineName = warehouseService.getWarehouseById(log.getWarehouseId()).getName();

        txtSummary.setText(String.format(
                """
                ✅ Kết quả tra cứu:
                Model: %s
                Line: %s
                Total Modules: %d
                NG Modules: %d
                Hiệu suất: %.2f %%
                Số cuộn bị trừ: %d
                """,
                modelName, lineName, log.getTotalModules(),
                log.getNgModules(), log.getPerformance(),
                traceRows.size()
        ));
    }

    // ======================= DTO hiển thị Table ==========================
    public static class TraceRow {
        private final String rollCode;
        private final String sapCode;
        private final String feederCode;
        private final int consumedQty;
        private final LocalDateTime createdAt;

        public TraceRow(String rollCode, String sapCode, String feederCode, int consumedQty, LocalDateTime createdAt) {
            this.rollCode = rollCode;
            this.sapCode = sapCode;
            this.feederCode = feederCode;
            this.consumedQty = consumedQty;
            this.createdAt = createdAt;
        }

        public String getRollCode() { return rollCode; }
        public String getSapCode() { return sapCode; }
        public String getFeederCode() { return feederCode; }
        public int getConsumedQty() { return consumedQty; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }
}
