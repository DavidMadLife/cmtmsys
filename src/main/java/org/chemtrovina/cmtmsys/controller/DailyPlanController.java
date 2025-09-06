package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import org.chemtrovina.cmtmsys.dto.DailyPlanDisplayRow;
import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.service.base.ProductionPlanDailyService;
import org.chemtrovina.cmtmsys.service.base.ProductionPlanService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.chemtrovina.cmtmsys.utils.FxFilterUtils;
import org.chemtrovina.cmtmsys.utils.TableCellUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

import static org.chemtrovina.cmtmsys.utils.TableCellUtils.mergeIdenticalCells;

@Component
public class DailyPlanController {

    @FXML private ComboBox<String> cbLineFilter;
    @FXML private DatePicker dpWeekDate;
    @FXML private Button btnLoadDailyPlans, btnSaveActuals, btnRollbackMaterial;
    @FXML private TableView<DailyPlanDisplayRow> tblDailyPlans;

    @FXML private TableColumn<DailyPlanDisplayRow, String> colDailyLine, colModel, colDailyModelType, colDailyProductCode, colType;
    @FXML private TableColumn<DailyPlanDisplayRow, Integer> colD1, colD2, colD3, colD4, colD5, colD6, colD7, colTotal;

    // thêm field
    private List<DailyPlanDisplayRow> masterRows = new ArrayList<>();


    private final ProductionPlanService productionPlanService;
    private final ProductionPlanDailyService dailyService;
    private final WarehouseService warehouseService;

    @Autowired
    public DailyPlanController(ProductionPlanService planService, ProductionPlanDailyService dailyService, WarehouseService warehouseService) {
        this.productionPlanService = planService;
        this.dailyService = dailyService;
        this.warehouseService = warehouseService;
    }

    @FXML
    public void initialize() {
        List<String> lineNames = warehouseService.getAllWarehouses().stream().map(Warehouse::getName).collect(Collectors.toList());
        List<String> allOptions = new ArrayList<>();
        allOptions.add("Tất cả");
        allOptions.addAll(lineNames);
        cbLineFilter.setItems(FXCollections.observableArrayList(allOptions));
        cbLineFilter.getSelectionModel().selectFirst();

        btnLoadDailyPlans.setOnAction(e -> loadDailyPlans());
        btnSaveActuals.setOnAction(e -> {
            saveDailyPlans();
            loadDailyPlans();
        });
        btnRollbackMaterial.setOnAction(e -> rollbackSelectedMaterial());

        tblDailyPlans.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode().toString().equals("C")) {
                copySelectionToClipboard(tblDailyPlans);
            }
        });

        colType.setCellValueFactory(c -> c.getValue().typeProperty());
        colDailyLine.setCellValueFactory(c -> c.getValue().lineProperty());
        colModel.setCellValueFactory(c -> c.getValue().modelProperty());
        colDailyProductCode.setCellValueFactory(c -> c.getValue().productCodeProperty());
        colDailyModelType.setCellValueFactory(c -> c.getValue().modelTypeProperty());

        colDailyLine.setCellFactory(mergeIdenticalCells(DailyPlanDisplayRow::getLine));
        colModel.setCellFactory(mergeIdenticalCells(DailyPlanDisplayRow::getModel));
        colDailyProductCode.setCellFactory(mergeIdenticalCells(DailyPlanDisplayRow::getProductCode));
        colDailyModelType.setCellFactory(mergeIdenticalCells(DailyPlanDisplayRow::getModelType));

        setupEditableColumn(colD1, 0); setupEditableColumn(colD2, 1);
        setupEditableColumn(colD3, 2); setupEditableColumn(colD4, 3);
        setupEditableColumn(colD5, 4); setupEditableColumn(colD6, 5);
        setupEditableColumn(colD7, 6);
        colTotal.setCellValueFactory(c -> c.getValue().totalProperty().asObject());

        tblDailyPlans.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tblDailyPlans.getSelectionModel().setCellSelectionEnabled(true);
        tblDailyPlans.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);



    }

    private void setupEditableColumn(TableColumn<DailyPlanDisplayRow, Integer> col, int dayIndex) {
        col.setCellValueFactory(c -> c.getValue().dayProperty(dayIndex).asObject());
        col.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        col.setEditable(true);
        col.setOnEditCommit(event -> {
            var row = event.getRowValue();
            int value = Optional.ofNullable(event.getNewValue()).orElse(0);
            row.dayProperty(dayIndex).set(value);
            row.totalProperty().set(row.totalProperty().get());
            LocalDate runDate = dpWeekDate.getValue().with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1).plusDays(dayIndex);
            if ("Diff".equals(row.getType())) return;
            if ("Plan".equals(row.getType())) dailyService.updateDailyPlan(row.getPlanItemId(), runDate, value);
            else if ("Actual".equals(row.getType())) dailyService.updateActual(row.getPlanItemId(), runDate, value);
        });
    }

    private void applyGeneralFilter(List<String> selected, java.util.function.Function<DailyPlanDisplayRow, String> extractor) {
        List<DailyPlanDisplayRow> filtered = masterRows.stream()
                .filter(row -> selected.contains(extractor.apply(row)))
                .toList();
        tblDailyPlans.setItems(FXCollections.observableArrayList(filtered));
    }


    private void setupColumnFilters() {
        // hủy menu cũ (nếu util của bạn gán đè thì có thể bỏ bước này)
        // colDailyLine.setContextMenu(null); ...

        var src = masterRows;  // luôn lấy từ nguồn gốc

        FxFilterUtils.setupFilterMenu(colDailyLine, src, DailyPlanDisplayRow::getLine,
                selected -> applyGeneralFilter(selected, DailyPlanDisplayRow::getLine));

        FxFilterUtils.setupFilterMenu(colModel, src, DailyPlanDisplayRow::getModel,
                selected -> applyGeneralFilter(selected, DailyPlanDisplayRow::getModel));

        FxFilterUtils.setupFilterMenu(colDailyProductCode, src, DailyPlanDisplayRow::getProductCode,
                selected -> applyGeneralFilter(selected, DailyPlanDisplayRow::getProductCode));

        FxFilterUtils.setupFilterMenu(colDailyModelType, src, DailyPlanDisplayRow::getModelType,
                selected -> applyGeneralFilter(selected, DailyPlanDisplayRow::getModelType));

        FxFilterUtils.setupFilterMenu(colType, src, DailyPlanDisplayRow::getType,
                selected -> applyGeneralFilter(selected, DailyPlanDisplayRow::getType));
    }



    private void loadDailyPlans() {
        String selectedLine = cbLineFilter.getValue();
        LocalDate selectedDate = dpWeekDate.getValue();
        if (selectedDate == null) {
            showAlert("Vui lòng chọn ngày trong tuần.");
            return;
        }

        int weekNo = selectedDate.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        int year = selectedDate.getYear();
        LocalDate monday = selectedDate.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
        updateDayColumnHeaders(monday);

        List<String> linesToLoad = (selectedLine == null || selectedLine.equalsIgnoreCase("Tất cả"))
                ? productionPlanService.getLinesWithPlan(weekNo, year)
                : List.of(selectedLine);

        List<DailyPlanDisplayRow> allDisplayRows = new ArrayList<>();

        for (String line : linesToLoad) {
            var rawData = dailyService.getDailyPlanView(line, weekNo, year);
            var displayData = rawData.stream().flatMap(dto -> {
                int totalPlan = dto.getTotalPlan();
                int totalActual = dto.getTotalActual();
                String modelType = dto.getModelType();

                var plan = new DailyPlanDisplayRow(dto.getPlanItemId(), line, dto.getModelCode(), dto.getSapCode(), "Plan", dto.getStock(),
                        dto.getDayPlan(1), dto.getDayPlan(2), dto.getDayPlan(3), dto.getDayPlan(4), dto.getDayPlan(5), dto.getDayPlan(6), dto.getDayPlan(7));
                plan.setModelType(modelType);

                var actual = new DailyPlanDisplayRow(dto.getPlanItemId(), line, dto.getModelCode(), dto.getSapCode(), "Actual", dto.getStock(),
                        dto.getDayActual(1), dto.getDayActual(2), dto.getDayActual(3), dto.getDayActual(4), dto.getDayActual(5), dto.getDayActual(6), dto.getDayActual(7));
                actual.setModelType(modelType);

                var diff = new DailyPlanDisplayRow(dto.getPlanItemId(), line, dto.getModelCode(), dto.getSapCode(), "Diff", dto.getStock(),
                        dto.getDayActual(1) - dto.getDayPlan(1), dto.getDayActual(2) - dto.getDayPlan(2), dto.getDayActual(3) - dto.getDayPlan(3),
                        dto.getDayActual(4) - dto.getDayPlan(4), dto.getDayActual(5) - dto.getDayPlan(5), dto.getDayActual(6) - dto.getDayPlan(6), dto.getDayActual(7) - dto.getDayPlan(7));
                diff.setModelType(modelType);

                double completion = (totalPlan == 0) ? 0 : (totalActual * 100.0 / totalPlan);
                var completionRow = new DailyPlanDisplayRow(dto.getPlanItemId(), line, dto.getModelCode(), dto.getSapCode(), "Completion", 0,
                        0, 0, 0, 0, 0, 0, 0);
                completionRow.setModelType(modelType);
                completionRow.setCompletionRate(completion);

                return Arrays.asList(plan, actual, diff, completionRow).stream();
            }).toList();

            allDisplayRows.addAll(displayData);
        }

        masterRows = allDisplayRows;

        tblDailyPlans.setItems(FXCollections.observableArrayList(allDisplayRows));
        tblDailyPlans.setEditable(true);

        setupColumnFilters();
    }

    private void rollbackSelectedMaterial() {
        var selected = tblDailyPlans.getSelectionModel().getSelectedItem();
        if (selected == null || !"Actual".equals(selected.getType())) {
            showAlert("Vui lòng chọn một dòng loại 'Actual' để hoàn tác.");
            return;
        }
        LocalDate selectedDate = dpWeekDate.getValue();
        for (int i = 0; i < 7; i++) {
            var runDate = selectedDate.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1).plusDays(i);
            if (selected.getDay(i) > 0) {
                try {
                    dailyService.rollbackConsumeMaterial(selected.getPlanItemId(), runDate);
                } catch (Exception e) {
                    showAlert("Lỗi hoàn tác: " + e.getMessage());
                    return;
                }
            }
        }
        showAlert("Đã hoàn tác trừ liệu cho model: " + selected.getModel());
    }

    private void saveDailyPlans() {
        var displayRows = tblDailyPlans.getItems();
        LocalDate selectedDate = dpWeekDate.getValue();
        if (selectedDate == null || displayRows.isEmpty()) {
            showAlert("Không có dữ liệu để lưu.");
            return;
        }
        LocalDate monday = selectedDate.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
        var planRows = displayRows.stream().filter(r -> "Plan".equals(r.getType())).toList();
        var actualRows = displayRows.stream().filter(r -> "Actual".equals(r.getType())).toList();

        for (var planRow : planRows) {
            int weekly = dailyService.getPlannedWeeklyQuantityByPlanItemId(planRow.getPlanItemId());
            int total = planRow.totalProperty().get();
            if (total > weekly) {
                showAlert("Kế hoạch ngày vượt quá kế hoạch tuần của model " + planRow.getModel());
                return;
            }
            for (int i = 0; i < 7; i++) dailyService.updateDailyPlan(planRow.getPlanItemId(), monday.plusDays(i), planRow.getDay(i));
        }

        for (var actualRow : actualRows) {
            int weekly = dailyService.getPlannedWeeklyQuantityByPlanItemId(actualRow.getPlanItemId());
            int total = actualRow.totalProperty().get();
            if (total > weekly) {
                showAlert("Số lượng thực tế vượt kế hoạch tuần của model " + actualRow.getModel());
                return;
            }
            for (int i = 0; i < 7; i++) {
                var date = monday.plusDays(i);
                int actual = actualRow.getDay(i);
                dailyService.updateActual(actualRow.getPlanItemId(), date, actual);
                try {
                    dailyService.consumeMaterialByActual(actualRow.getPlanItemId(), date, actual);
                } catch (Exception e) {
                    showAlert("Lỗi trừ liệu: " + e.getMessage());
                    return;
                }
            }
        }

        showAlert("Đã lưu kế hoạch ngày và số lượng thực tế thành công!");
    }

    private void updateDayColumnHeaders(LocalDate start) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        colD1.setText(start.plusDays(0).format(fmt));
        colD2.setText(start.plusDays(1).format(fmt));
        colD3.setText(start.plusDays(2).format(fmt));
        colD4.setText(start.plusDays(3).format(fmt));
        colD5.setText(start.plusDays(4).format(fmt));
        colD6.setText(start.plusDays(5).format(fmt));
        colD7.setText(start.plusDays(6).format(fmt));
        int weekNo = start.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        colTotal.setText("Total W" + weekNo);
    }

    private void copySelectionToClipboard(TableView<?> table) {
        StringBuilder sb = new StringBuilder();
        var positions = table.getSelectionModel().getSelectedCells();
        int prevRow = -1;
        for (var pos : positions) {
            int row = pos.getRow(), col = pos.getColumn();
            Object cell = table.getColumns().get(col).getCellData(row);
            if (cell == null) cell = "";
            if (prevRow == row) sb.append('\t');
            else if (prevRow != -1) sb.append('\n');
            sb.append(cell);
            prevRow = row;
        }
        final var content = new javafx.scene.input.ClipboardContent();
        content.putString(sb.toString());
        javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
