package org.chemtrovina.cmtmsys.controller.productionPlan;


import javafx.collections.FXCollections;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.util.converter.IntegerStringConverter;
import javafx.scene.control.cell.TextFieldTableCell;
import org.chemtrovina.cmtmsys.dto.DailyPlanDisplayRow;
import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.service.base.ProductionPlanDailyService;
import org.chemtrovina.cmtmsys.service.base.ProductionPlanService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.chemtrovina.cmtmsys.utils.FxAlertUtils;
import org.chemtrovina.cmtmsys.utils.FxFilterUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.function.Function;

import static org.chemtrovina.cmtmsys.utils.TableCellUtils.mergeIdenticalCells;

@Component
public class ProductionPlanDailySection {

    private final ProductionPlanService productionPlanService;
    private final ProductionPlanDailyService dailyService;
    private final WarehouseService warehouseService;

    public ProductionPlanDailySection(
            ProductionPlanService productionPlanService,
            ProductionPlanDailyService dailyService,
            WarehouseService warehouseService
    ) {
        this.productionPlanService = productionPlanService;
        this.dailyService = dailyService;
        this.warehouseService = warehouseService;
    }

    private Refs r;

    // state dùng cho filter menu
    private List<DailyPlanDisplayRow> masterRows = new ArrayList<>();

    public void init(Refs refs) {
        this.r = Objects.requireNonNull(refs);

        setupLineFilter();
        setupColumnsAndMerging();
        setupEditableColumns();
        setupActions();
    }

    // ==========================================================
    // Public API (để Weekly gọi khi plan thay đổi)
    // ==========================================================
    public void loadDailyPlans() {
        if (r == null) return;

        String selectedLine = r.cbLineFilter.getValue();
        LocalDate selectedDate = r.dpWeekDate.getValue();
        if (selectedDate == null) {
            FxAlertUtils.warning("Vui lòng chọn ngày trong tuần.");
            return;
        }

        // Monday của tuần
        LocalDate monday = selectedDate.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
        updateDayColumnHeaders(monday);

        // A) backfill actual từ AOI -> cập nhật ProductionPlanDaily.actualQuantity
        dailyService.backfillActualFromPerformanceByGoodModules(
                (selectedLine != null && !"Tất cả".equalsIgnoreCase(selectedLine)) ? selectedLine : null,
                monday,
                true
        );

        // B) lấy data để vẽ bảng
        int weekNo = selectedDate.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        int year   = selectedDate.getYear();

        List<String> linesToLoad = (selectedLine == null || "Tất cả".equalsIgnoreCase(selectedLine))
                ? productionPlanService.getLinesWithPlan(weekNo, year)
                : List.of(selectedLine);

        List<DailyPlanDisplayRow> allDisplayRows = new ArrayList<>();

        for (String line : linesToLoad) {
            var rawData = dailyService.getDailyPlanView(line, weekNo, year);

            var displayData = rawData.stream().flatMap(dto -> {
                int totalPlan   = dto.getTotalPlan();
                int totalActual = dto.getTotalActual();
                String modelType = dto.getModelType();

                var plan = new DailyPlanDisplayRow(dto.getPlanItemId(), line, dto.getModelName(), dto.getSapCode(),
                        "Plan", dto.getStock(),
                        dto.getDayPlan(1), dto.getDayPlan(2), dto.getDayPlan(3), dto.getDayPlan(4),
                        dto.getDayPlan(5), dto.getDayPlan(6), dto.getDayPlan(7));
                plan.setModelType(modelType);

                var actual = new DailyPlanDisplayRow(dto.getPlanItemId(), line, dto.getModelName(), dto.getSapCode(),
                        "Actual", dto.getStock(),
                        dto.getDayActual(1), dto.getDayActual(2), dto.getDayActual(3), dto.getDayActual(4),
                        dto.getDayActual(5), dto.getDayActual(6), dto.getDayActual(7));
                actual.setModelType(modelType);

                var diff = new DailyPlanDisplayRow(dto.getPlanItemId(), line, dto.getModelName(), dto.getSapCode(),
                        "Diff", dto.getStock(),
                        dto.getDayActual(1) - dto.getDayPlan(1),
                        dto.getDayActual(2) - dto.getDayPlan(2),
                        dto.getDayActual(3) - dto.getDayPlan(3),
                        dto.getDayActual(4) - dto.getDayPlan(4),
                        dto.getDayActual(5) - dto.getDayPlan(5),
                        dto.getDayActual(6) - dto.getDayPlan(6),
                        dto.getDayActual(7) - dto.getDayPlan(7));
                diff.setModelType(modelType);

                double completion = (totalPlan == 0) ? 0 : (totalActual * 100.0 / totalPlan);
                var completionRow = new DailyPlanDisplayRow(dto.getPlanItemId(), line, dto.getModelName(), dto.getSapCode(),
                        "Completion", 0, 0,0,0,0,0,0,0);
                completionRow.setModelType(modelType);
                completionRow.setCompletionRate(completion);

                return Arrays.asList(plan, actual, diff, completionRow).stream();
            }).toList();

            allDisplayRows.addAll(displayData);
        }

        masterRows = allDisplayRows;
        r.tblDailyPlans.setItems(FXCollections.observableArrayList(allDisplayRows));
        r.tblDailyPlans.setEditable(true);

        setupColumnFilters();
    }

    public void saveDailyPlans() {
        var displayRows = r.tblDailyPlans.getItems();
        LocalDate selectedDate = r.dpWeekDate.getValue();

        if (selectedDate == null || displayRows == null || displayRows.isEmpty()) {
            FxAlertUtils.warning("Không có dữ liệu để lưu.");
            return;
        }

        LocalDate monday = selectedDate.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);

        var planRows   = displayRows.stream().filter(r -> "Plan".equals(r.getType())).toList();
        var actualRows = displayRows.stream().filter(r -> "Actual".equals(r.getType())).toList();

        // PLAN
        for (var planRow : planRows) {
            int weekly = dailyService.getPlannedWeeklyQuantityByPlanItemId(planRow.getPlanItemId());
            int total  = planRow.totalProperty().get();

            if (total > weekly) {
                FxAlertUtils.warning("Kế hoạch ngày vượt quá kế hoạch tuần của model " + planRow.getModel());
                return;
            }

            for (int i = 0; i < 7; i++) {
                dailyService.updateDailyPlan(planRow.getPlanItemId(), monday.plusDays(i), planRow.getDay(i));
            }
        }

        // ACTUAL + consume material
        for (var actualRow : actualRows) {
            int weekly = dailyService.getPlannedWeeklyQuantityByPlanItemId(actualRow.getPlanItemId());
            int total  = actualRow.totalProperty().get();

            if (total > weekly) {
                FxAlertUtils.warning("Số lượng thực tế vượt kế hoạch tuần của model " + actualRow.getModel());
                return;
            }

            for (int i = 0; i < 7; i++) {
                var date = monday.plusDays(i);
                int actual = actualRow.getDay(i);

                dailyService.updateActual(actualRow.getPlanItemId(), date, actual);

                try {
                    dailyService.consumeMaterialByActual(actualRow.getPlanItemId(), date, actual);
                } catch (Exception e) {
                    FxAlertUtils.warning("Lỗi trừ liệu: " + e.getMessage());
                    return;
                }
            }
        }

        FxAlertUtils.info("Đã lưu kế hoạch ngày và số lượng thực tế thành công!");
    }

    public void rollbackSelectedMaterial() {
        var selected = r.tblDailyPlans.getSelectionModel().getSelectedItem();

        if (selected == null || !"Actual".equals(selected.getType())) {
            FxAlertUtils.warning("Vui lòng chọn một dòng loại 'Actual' để hoàn tác.");
            return;
        }

        LocalDate selectedDate = r.dpWeekDate.getValue();
        if (selectedDate == null) {
            FxAlertUtils.warning("Vui lòng chọn ngày.");
            return;
        }

        for (int i = 0; i < 7; i++) {
            var runDate = selectedDate.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1).plusDays(i);

            if (selected.getDay(i) > 0) {
                try {
                    dailyService.rollbackConsumeMaterial(selected.getPlanItemId(), runDate);
                } catch (Exception e) {
                    FxAlertUtils.warning("Lỗi hoàn tác: " + e.getMessage());
                    return;
                }
            }
        }

        FxAlertUtils.info("Đã hoàn tác trừ liệu cho model: " + selected.getModel());
    }

    // ==========================================================
    // Setup parts
    // ==========================================================
    private void setupLineFilter() {
        List<String> lineNames = warehouseService.getAllWarehouses()
                .stream().map(Warehouse::getName).toList();

        List<String> allOptions = new ArrayList<>();
        allOptions.add("Tất cả");
        allOptions.addAll(lineNames);

        r.cbLineFilter.setItems(FXCollections.observableArrayList(allOptions));
        r.cbLineFilter.getSelectionModel().selectFirst();
    }

    private void setupActions() {
        r.btnLoadDailyPlans.setOnAction(e -> loadDailyPlans());
        r.btnSaveActuals.setOnAction(e -> {
            saveDailyPlans();
            loadDailyPlans(); // để refresh diff + completion
        });
        r.btnRollbackMaterial.setOnAction(e -> rollbackSelectedMaterial());
    }

    private void setupColumnsAndMerging() {
        r.colType.setCellValueFactory(c -> c.getValue().typeProperty());
        r.colDailyLine.setCellValueFactory(c -> c.getValue().lineProperty());
        r.colModel.setCellValueFactory(c -> c.getValue().modelProperty());
        r.colDailyProductCode.setCellValueFactory(c -> c.getValue().productCodeProperty());
        r.colDailyModelType.setCellValueFactory(c -> c.getValue().modelTypeProperty());

        r.colTotal.setCellValueFactory(c -> c.getValue().totalProperty().asObject());

        // merge
        r.colDailyLine.setCellFactory(mergeIdenticalCells(DailyPlanDisplayRow::getLine));
        r.colModel.setCellFactory(mergeIdenticalCells(DailyPlanDisplayRow::getModel, DailyPlanDisplayRow::getLine));
        r.colDailyProductCode.setCellFactory(mergeIdenticalCells(DailyPlanDisplayRow::getProductCode, DailyPlanDisplayRow::getLine));
        r.colDailyModelType.setCellFactory(mergeIdenticalCells(DailyPlanDisplayRow::getModelType, DailyPlanDisplayRow::getLine));

        // style Type column
        r.colType.setCellFactory(column -> new TableCell<DailyPlanDisplayRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(item);

                switch (item) {
                    case "Actual" -> setStyle("-fx-text-fill: #0077cc; -fx-font-weight: bold;");
                    case "Diff" -> setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    case "Completion" -> setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    default -> setStyle("");
                }
            }
        });
    }

    private void setupEditableColumns() {
        setupEditableColumn(r.colD1, 0);
        setupEditableColumn(r.colD2, 1);
        setupEditableColumn(r.colD3, 2);
        setupEditableColumn(r.colD4, 3);
        setupEditableColumn(r.colD5, 4);
        setupEditableColumn(r.colD6, 5);
        setupEditableColumn(r.colD7, 6);
    }

    private void setupEditableColumn(TableColumn<DailyPlanDisplayRow, Integer> col, int dayIndex) {
        col.setCellValueFactory(c -> c.getValue().dayProperty(dayIndex).asObject());
        col.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        col.setEditable(true);

        col.setOnEditCommit(event -> {
            var row = event.getRowValue();

            int value = Optional.ofNullable(event.getNewValue()).orElse(0);
            row.dayProperty(dayIndex).set(value);

            LocalDate base = r.dpWeekDate.getValue();
            if (base == null) return;

            LocalDate runDate = base.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1).plusDays(dayIndex);

            if ("Diff".equals(row.getType()) || "Completion".equals(row.getType())) return;

            if ("Plan".equals(row.getType())) {
                dailyService.updateDailyPlan(row.getPlanItemId(), runDate, value);
            } else if ("Actual".equals(row.getType())) {
                dailyService.updateActual(row.getPlanItemId(), runDate, value);
            }

            // refresh table (để diff + completion update)
            loadDailyPlans();
        });
    }

    // ==========================================================
    // Filters
    // ==========================================================
    private void setupColumnFilters() {
        var src = masterRows;

        FxFilterUtils.setupFilterMenu(r.colDailyLine, src, DailyPlanDisplayRow::getLine,
                selected -> applyGeneralFilter(selected, DailyPlanDisplayRow::getLine));

        FxFilterUtils.setupFilterMenu(r.colModel, src, DailyPlanDisplayRow::getModel,
                selected -> applyGeneralFilter(selected, DailyPlanDisplayRow::getModel));

        FxFilterUtils.setupFilterMenu(r.colDailyProductCode, src, DailyPlanDisplayRow::getProductCode,
                selected -> applyGeneralFilter(selected, DailyPlanDisplayRow::getProductCode));

        FxFilterUtils.setupFilterMenu(r.colDailyModelType, src, DailyPlanDisplayRow::getModelType,
                selected -> applyGeneralFilter(selected, DailyPlanDisplayRow::getModelType));

        FxFilterUtils.setupFilterMenu(r.colType, src, DailyPlanDisplayRow::getType,
                selected -> applyGeneralFilter(selected, DailyPlanDisplayRow::getType));
    }

    private void applyGeneralFilter(List<String> selected, Function<DailyPlanDisplayRow, String> extractor) {
        List<DailyPlanDisplayRow> filtered = masterRows.stream()
                .filter(row -> selected.contains(extractor.apply(row)))
                .toList();

        r.tblDailyPlans.setItems(FXCollections.observableArrayList(filtered));
    }

    // ==========================================================
    // Helpers
    // ==========================================================
    private void updateDayColumnHeaders(LocalDate start) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");

        r.colD1.setText(start.plusDays(0).format(fmt));
        r.colD2.setText(start.plusDays(1).format(fmt));
        r.colD3.setText(start.plusDays(2).format(fmt));
        r.colD4.setText(start.plusDays(3).format(fmt));
        r.colD5.setText(start.plusDays(4).format(fmt));
        r.colD6.setText(start.plusDays(5).format(fmt));
        r.colD7.setText(start.plusDays(6).format(fmt));

        int weekNo = start.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        r.colTotal.setText("Total W" + weekNo);
    }

    // ==========================================================
    // Refs holder
    // ==========================================================
    public static class Refs {
        public ComboBox<String> cbLineFilter;
        public DatePicker dpWeekDate;
        public Button btnLoadDailyPlans, btnSaveActuals, btnRollbackMaterial;

        public TableView<DailyPlanDisplayRow> tblDailyPlans;

        public TableColumn<DailyPlanDisplayRow, String> colDailyLine, colModel, colDailyProductCode, colType, colDailyModelType;
        public TableColumn<DailyPlanDisplayRow, Integer> colD1, colD2, colD3, colD4, colD5, colD6, colD7, colTotal;
    }
}

