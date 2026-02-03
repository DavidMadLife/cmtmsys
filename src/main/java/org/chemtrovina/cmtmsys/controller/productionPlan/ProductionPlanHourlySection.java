package org.chemtrovina.cmtmsys.controller.productionPlan;


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import org.chemtrovina.cmtmsys.dto.HourlyActualRow;
import org.chemtrovina.cmtmsys.dto.PcbPerformanceLogHistoryDTO;
import org.chemtrovina.cmtmsys.model.ProductionPlanDaily;
import org.chemtrovina.cmtmsys.model.ProductionPlanHourly;
import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.service.base.PcbPerformanceLogService;
import org.chemtrovina.cmtmsys.service.base.ProductService;
import org.chemtrovina.cmtmsys.service.base.ProductionPlanDailyService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.chemtrovina.cmtmsys.utils.FxAlertUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.chemtrovina.cmtmsys.utils.TableCellUtils.mergeIdenticalCells;

@Component
public class ProductionPlanHourlySection {

    private final WarehouseService warehouseService;
    private final PcbPerformanceLogService pcbPerformanceLogService;
    private final ProductionPlanDailyService dailyService;
    private final ProductService productService;

    private boolean active = true;

    public ProductionPlanHourlySection(
            WarehouseService warehouseService,
            PcbPerformanceLogService pcbPerformanceLogService,
            ProductionPlanDailyService dailyService,
            ProductService productService
    ) {
        this.warehouseService = warehouseService;
        this.pcbPerformanceLogService = pcbPerformanceLogService;
        this.dailyService = dailyService;
        this.productService = productService;
    }

    private Refs r;

    // ===== auto refresh + idle tracking =====
    private Timeline autoRefresh;
    private volatile long lastUserEventAt = System.currentTimeMillis();

    // idle ms configurable
    private final LongProperty idleMs = new SimpleLongProperty(10_000);
    public LongProperty idleMsProperty() { return idleMs; }
    public long getIdleMs() { return idleMs.get(); }
    public void setIdleMs(long ms) { idleMs.set(Math.max(0, ms)); }

    private Scene trackedScene;
    private EventHandler<Event> anyEventHandler;

    public void init(Refs refs) {
        this.r = Objects.requireNonNull(refs);

        setupHourlyPaneUI();
        setupAutoRefresh();
        setupIdleMsFromViewIfProvided();
    }

    // gọi khi tab đóng
    public void dispose() {
        stopAutoRefresh();
        removeIdleTracking();
    }

    // ==========================================================
    // Setup UI
    // ==========================================================
    private void setupHourlyPaneUI() {
        // line dropdown
        List<String> lineNames = warehouseService.getAllWarehouses().stream()
                .map(Warehouse::getName).toList();

        r.cbLineHourly.setItems(javafx.collections.FXCollections.observableArrayList("Tất cả"));
        r.cbLineHourly.getItems().addAll(lineNames);
        r.cbLineHourly.getSelectionModel().selectFirst();

        // base columns
        r.colHLine.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLine()));
        r.colHModel.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getModel()));
        r.colHProduct.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getProductCode()));
        r.colHModelType.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getModelType()));
        r.colHStage.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStage()));
        r.colHTotal.setCellValueFactory(c -> c.getValue().totalProperty().asObject());

        // slot columns
        TableColumn<HourlyActualRow, Integer>[] sCols = r.slotCols();

        // header icon labels (nếu muốn)
        String[] hourLabels = {
                "🕗 08–10", "🕙 10–12", "🕛 12–14", "🕓 14–16",
                "🕕 16–18", "🕗 18–20", "🕘 20–22", "🌙 22–00",
                "🌑 00–02", "🕑 02–04", "🕓 04–06", "🌅 06–08"
        };
        for (int i = 0; i < sCols.length; i++) {
            if (sCols[i] == null) continue;
            Label lbl = new Label(hourLabels[i]);
            lbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-alignment: center;");
            sCols[i].setGraphic(lbl);
            sCols[i].setText(null);
        }

        // editable plan cells
        for (int i = 0; i < sCols.length; i++) {
            final int idx = i;

            sCols[i].setCellValueFactory(c -> c.getValue().slotProperty(idx).asObject());
            sCols[i].setCellFactory(col -> new TextFieldTableCell<>(new IntegerStringConverter()));

            sCols[i].setOnEditCommit(evt -> {
                HourlyActualRow row = evt.getRowValue();
                if (!"Plan".equals(row.getStage())) return;

                int newQty = Optional.ofNullable(evt.getNewValue()).orElse(0);
                int oldQty = row.slotProperty(idx).get();

                row.slotProperty(idx).set(newQty);
                row.totalProperty().set(row.totalProperty().get() - oldQty + newQty);

                boolean success = dailyService.updateHourlyPlanWithValidation(
                        row.getPlanItemId(),
                        idx,
                        newQty,
                        row.getRunDate()
                );

                if (!success) {
                    FxAlertUtils.warning("Tổng kế hoạch giờ vượt quá kế hoạch ngày!");
                    row.slotProperty(idx).set(oldQty);
                    row.totalProperty().set(row.totalProperty().get());
                }
            });
        }

        r.tblHourly.setEditable(true);

        // merge line
        r.colHLine.setCellFactory(mergeIdenticalCells(HourlyActualRow::getLine));
        r.colHModel.setCellFactory(mergeIdenticalCells(HourlyActualRow::getModel, HourlyActualRow::getLine));
        r.colHProduct.setCellFactory(mergeIdenticalCells(HourlyActualRow::getProductCode, HourlyActualRow::getLine));
        r.colHModelType.setCellFactory(mergeIdenticalCells(HourlyActualRow::getModelType, HourlyActualRow::getLine));

        // stage merge + style
        r.colHStage.setCellFactory(mergeStageWithStyling());

        // buttons
        r.btnLoadHourly.setOnAction(e -> loadHourlyActuals());
    }

    // ==========================================================
    // Auto refresh + idle
    // ==========================================================
    private void setupAutoRefresh() {
        // tick mỗi 5s
        autoRefresh = new Timeline(new KeyFrame(Duration.seconds(5), e -> maybeAutoLoadHourly()));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();

        Platform.runLater(() -> {
            Scene scene = r.tblHourly.getScene();
            if (scene == null) return;

            trackedScene = scene;
            anyEventHandler = e -> lastUserEventAt = System.currentTimeMillis();
            trackedScene.addEventFilter(Event.ANY, anyEventHandler);
        });
    }

    private void stopAutoRefresh() {
        if (autoRefresh != null) {
            autoRefresh.stop();
            autoRefresh = null;
        }
    }

    private void removeIdleTracking() {
        if (trackedScene != null && anyEventHandler != null) {
            trackedScene.removeEventFilter(Event.ANY, anyEventHandler);
        }
        trackedScene = null;
        anyEventHandler = null;
    }

    public void pauseAutoRefresh() {
        if (autoRefresh != null) autoRefresh.pause();
    }

    public void resumeAutoRefresh() {
        if (autoRefresh != null) autoRefresh.play();
    }

    private void maybeAutoLoadHourly() {
        long idle = System.currentTimeMillis() - lastUserEventAt;
        if (idle < getIdleMs()) return;                 // còn thao tác
        if (r.tblHourly.getEditingCell() != null) return; // đang edit
        if (r.dpHourlyDate.getValue() == null) return;

        loadHourlyActuals();
    }

    // ==========================================================
    // Binding idle from view (optional)
    // ==========================================================
    private void setupIdleMsFromViewIfProvided() {
        // 1) Nếu có Spinner<Integer> idleSecondsSpinner: bind seconds -> ms
        if (r.idleSecondsSpinner != null) {
            // default 10s
            if (r.idleSecondsSpinner.getValueFactory() == null) {
                r.idleSecondsSpinner.setValueFactory(
                        new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 3600, 10)
                );
            }

            r.idleSecondsSpinner.valueProperty().addListener((obs, oldV, newV) -> {
                int sec = (newV == null) ? 0 : Math.max(0, newV);
                setIdleMs(sec * 1000L);
            });

            // sync UI -> current
            r.idleSecondsSpinner.getValueFactory().setValue((int) (getIdleMs() / 1000L));
        }

        // 2) Nếu có Slider idleSecondsSlider: bind slider -> ms
        if (r.idleSecondsSlider != null) {
            r.idleSecondsSlider.valueProperty().addListener((obs, ov, nv) -> {
                long sec = (nv == null) ? 0 : Math.max(0, Math.round(nv.doubleValue()));
                setIdleMs(sec * 1000L);
            });
            r.idleSecondsSlider.setValue(getIdleMs() / 1000.0);
        }

        // 3) Nếu có TextField idleSecondsText: parse -> ms
        if (r.idleSecondsText != null) {
            r.idleSecondsText.setText(String.valueOf(getIdleMs() / 1000L));
            r.idleSecondsText.textProperty().addListener((obs, ov, nv) -> {
                try {
                    long sec = Long.parseLong(nv.trim());
                    setIdleMs(sec * 1000L);
                } catch (Exception ignore) {
                    // không alert khi đang gõ
                }
            });
        }
    }

    // ==========================================================
    // Load hourly data (giữ nguyên logic bạn đang có)
    // ==========================================================
    public void loadHourlyActuals() {
        LocalDate day = r.dpHourlyDate.getValue();
        String lineFilter = r.cbLineHourly.getValue();

        if (day == null) {
            FxAlertUtils.warning("Vui lòng chọn ngày.");
            return;
        }

        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end   = start.plusDays(1);

        // Lines cần load
        List<String> lines = (lineFilter == null || "Tất cả".equalsIgnoreCase(lineFilter))
                ? warehouseService.getAllWarehouses().stream().map(Warehouse::getName).toList()
                : List.of(lineFilter);

        // 1) Logs từ AOI
        List<PcbPerformanceLogHistoryDTO> logs = new ArrayList<>();
        for (String ln : lines) {
            logs.addAll(pcbPerformanceLogService.fetchPerformanceGoodModules(ln, start, end));
        }

        if (logs.isEmpty()) {
            r.tblHourly.setItems(javafx.collections.FXCollections.observableArrayList());
            return;
        }

        // 2) Map (ProductCode|ModelType) -> ProductName
        Set<String> productKeys = logs.stream()
                .filter(l -> l.getModelCode() != null && l.getModelType() != null)
                .map(l -> l.getModelCode() + "|" + l.getModelType().name())
                .collect(Collectors.toSet());

        Map<String, String> productNameMap = productService.findProductNamesByCodeAndModelType(productKeys);

        // 3) Daily map (model|line|date -> ProductionPlanDaily)
        Set<String> keysToFetch = logs.stream()
                .map(l -> l.getModelCode() + "|" + l.getWarehouseName() + "|" + l.getCreatedAt().toLocalDate())
                .collect(Collectors.toSet());

        Map<String, ProductionPlanDaily> dailyMap = dailyService.findByModelLineAndDates(keysToFetch);

        Map<String, HourlyActualRow> resultMap = new LinkedHashMap<>();

        // ===================== PLAN =====================
        for (var entry : dailyMap.entrySet()) {
            ProductionPlanDaily d = entry.getValue();
            if (d == null) continue;

            String[] parts = entry.getKey().split("\\|");
            if (parts.length < 3) continue;

            String modelCode = parts[0];
            String lineName  = parts[1];
            LocalDate runDate = LocalDate.parse(parts[2]);

            String baseKey = modelCode + "|" + lineName;

            String planModelType = (d.getModelType() != null && !d.getModelType().isBlank())
                    ? d.getModelType()
                    : "NONE";

            String productKey = modelCode + "|" + planModelType;

            String modelName = (d.getProductName() != null && !d.getProductName().isBlank())
                    ? d.getProductName()
                    : productNameMap.getOrDefault(productKey, modelCode);

            HourlyActualRow planRow = new HourlyActualRow(
                    d.getPlanItemID(),
                    lineName,
                    modelName,
                    modelCode,
                    planModelType,
                    new int[12],
                    "Plan",
                    "AOI",
                    runDate
            );

            List<ProductionPlanHourly> slots = dailyService.getHourlyPlansByDailyId(d.getDailyID());
            for (ProductionPlanHourly slot : slots) {
                int idx = Math.max(0, Math.min(11, slot.getSlotIndex()));
                int q = Math.max(0, slot.getPlanQuantity());
                planRow.slotProperty(idx).set(q);
                planRow.totalProperty().set(planRow.totalProperty().get() + q);
            }

            resultMap.put(baseKey + "|Plan", planRow);
        }

        // ===================== ACTUAL =====================
        for (var log : logs) {
            int good = Math.max(0, log.getTotalModules() - log.getNgModules());
            int idx  = slotIndexTwoHours(log.getCreatedAt().toLocalTime());

            String modelCode = log.getModelCode();
            String lineName  = log.getWarehouseName();
            LocalDate runDate = log.getCreatedAt().toLocalDate();

            String baseKey = modelCode + "|" + lineName;
            String fullKey = baseKey + "|Actual";

            ProductionPlanDaily d = dailyMap.get(modelCode + "|" + lineName + "|" + runDate);

            int planItemId = (d != null) ? d.getPlanItemID() : 0;

            String modelType = (d != null && d.getModelType() != null && !d.getModelType().isBlank())
                    ? d.getModelType()
                    : (log.getModelType() != null ? log.getModelType().name() : "NONE");

            String productKey = modelCode + "|" + modelType;
            String modelName = productNameMap.getOrDefault(productKey, modelCode);

            HourlyActualRow row = resultMap.computeIfAbsent(fullKey, k -> new HourlyActualRow(
                    planItemId,
                    lineName,
                    modelName,
                    modelCode,
                    modelType,
                    new int[12],
                    "Actual",
                    log.getAoi(),
                    runDate
            ));

            row.slotProperty(idx).set(row.slotProperty(idx).get() + good);
            row.totalProperty().set(row.totalProperty().get() + good);
        }

        // ===================== DIFF & COMPLETION =====================
        Map<String, HourlyActualRow> finalMap = new LinkedHashMap<>(resultMap);

        for (var entry : resultMap.entrySet()) {
            String key = entry.getKey();
            if (!key.endsWith("|Plan")) continue;

            String baseKey = key.substring(0, key.length() - "|Plan".length());
            HourlyActualRow planRow   = resultMap.get(key);
            HourlyActualRow actualRow = resultMap.get(baseKey + "|Actual");

            if (planRow != null && actualRow != null) {
                int[] diffSlots = new int[12];
                for (int i = 0; i < 12; i++) {
                    diffSlots[i] = planRow.slotProperty(i).get() - actualRow.slotProperty(i).get();
                }

                HourlyActualRow diffRow = new HourlyActualRow(
                        planRow.getPlanItemId(),
                        planRow.getLine(),
                        planRow.getModel(),
                        planRow.getProductCode(),
                        planRow.getModelType(),
                        diffSlots,
                        "Diff",
                        planRow.getStage(),
                        planRow.getRunDate()
                );
                finalMap.put(baseKey + "|Diff", diffRow);

                HourlyActualRow compRow = new HourlyActualRow(
                        planRow.getPlanItemId(),
                        planRow.getLine(),
                        planRow.getModel(),
                        planRow.getProductCode(),
                        planRow.getModelType(),
                        new int[12],
                        "Completion",
                        planRow.getStage(),
                        planRow.getRunDate()
                );
                finalMap.put(baseKey + "|Completion", compRow);
            }
        }

        // Sort
        List<HourlyActualRow> rows = new ArrayList<>(finalMap.values());
        rows.sort(
                Comparator
                        .comparing(HourlyActualRow::getLine, Comparator.nullsLast(String::compareTo))
                        .thenComparing(HourlyActualRow::getModel, Comparator.nullsLast(String::compareTo))
                        .thenComparingInt(rr -> switch (rr.getStage()) {
                            case "Plan" -> 0;
                            case "Actual" -> 1;
                            case "Diff" -> 2;
                            case "Completion" -> 3;
                            default -> 9;
                        })
        );

        r.tblHourly.setItems(javafx.collections.FXCollections.observableArrayList(rows));
        r.tblHourly.setEditable(true);
    }

    // 08:00..09:59 -> 0, 10:00..11:59 -> 1, ..., 06:00..07:59 -> 11
    private int slotIndexTwoHours(LocalTime time) {
        int totalMinutes = time.getHour() * 60 + time.getMinute();
        int baseMinutes = 8 * 60;
        int index = (totalMinutes - baseMinutes + 24 * 60) % (24 * 60) / 120;
        return Math.min(index, 11);
    }

    // ==========================================================
    // Stage merge + style
    // ==========================================================
    private static void applyStageStyle(TableCell<HourlyActualRow, String> cell, String item) {
        if (item == null || cell.isEmpty()) {
            cell.setStyle("");
            return;
        }
        String style = "-fx-font-weight: bold; -fx-font-size: 16px;";
        switch (item) {
            case "Actual" -> style += " -fx-text-fill: #0077cc;";
            case "Diff" -> style += " -fx-text-fill: red;";
            case "Plan" -> style += " -fx-text-fill: black;";
            case "Completion" -> style += " -fx-text-fill: green;";
            default -> { }
        }
        cell.setStyle(style);
    }

    private static Callback<TableColumn<HourlyActualRow, String>, TableCell<HourlyActualRow, String>>
    mergeStageWithStyling() {
        Callback<TableColumn<HourlyActualRow, String>, TableCell<HourlyActualRow, String>> base =
                mergeIdenticalCells(HourlyActualRow::getStage, HourlyActualRow::getLine);

        return column -> {
            TableCell<HourlyActualRow, String> cell = base.call(column);

            cell.itemProperty().addListener((obs, oldV, newV) -> applyStageStyle(cell, newV));
            cell.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
                if (isEmpty) cell.setStyle("");
                else applyStageStyle(cell, cell.getItem());
            });

            Platform.runLater(() -> applyStageStyle(cell, cell.getItem()));
            return cell;
        };
    }

    // ==========================================================
    // Refs
    // ==========================================================
    public static class Refs {
        public ComboBox<String> cbLineHourly;
        public DatePicker dpHourlyDate;
        public Button btnLoadHourly;
        public TableView<HourlyActualRow> tblHourly;

        public TableColumn<HourlyActualRow, String> colHLine, colHModel, colHModelType, colHProduct, colHStage;
        public TableColumn<HourlyActualRow, Integer> colS1, colS2, colS3, colS4, colS5, colS6, colS7, colS8, colS9, colS10, colS11, colS12, colHTotal;

        // optional UI for idle config:
        public Spinner<Integer> idleSecondsSpinner; // recommended
        public Slider idleSecondsSlider;
        public TextField idleSecondsText;

        @SuppressWarnings("unchecked")
        public TableColumn<HourlyActualRow, Integer>[] slotCols() {
            return new TableColumn[]{
                    colS1, colS2, colS3, colS4, colS5, colS6,
                    colS7, colS8, colS9, colS10, colS11, colS12
            };
        }
    }
}
