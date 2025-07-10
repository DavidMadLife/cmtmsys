package org.chemtrovina.cmtmsys.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.converter.IntegerStringConverter;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.dto.DailyPlanDisplayRow;
import org.chemtrovina.cmtmsys.dto.DailyPlanRowDto;
import org.chemtrovina.cmtmsys.repository.Impl.MaterialConsumeLogRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.Impl.MaterialRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.Impl.ProductionPlanDailyRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.Impl.WarehouseRepositoryImpl;
import org.chemtrovina.cmtmsys.service.Impl.ProductionPlanDailyServiceImpl;
import org.chemtrovina.cmtmsys.service.Impl.WarehouseServiceImpl;
import org.chemtrovina.cmtmsys.service.base.ProductionPlanDailyService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class DailyPlanController {

    @FXML private ComboBox<String> cbLineFilter;
    @FXML private DatePicker dpWeekDate;
    @FXML private Button btnLoadDailyPlans;
    @FXML private Button btnSaveActuals;
    @FXML private Button btnRollbackMaterial;


    @FXML private TableView<DailyPlanDisplayRow> tblDailyPlans;
    @FXML private TableColumn<DailyPlanDisplayRow, String> colLine;
    @FXML private TableColumn<DailyPlanDisplayRow, String> colModel;
    @FXML private TableColumn<DailyPlanDisplayRow, String> colProductCode;
    @FXML private TableColumn<DailyPlanDisplayRow, String> colType;

    @FXML private TableColumn<DailyPlanDisplayRow, Integer> colD1;
    @FXML private TableColumn<DailyPlanDisplayRow, Integer> colD2;
    @FXML private TableColumn<DailyPlanDisplayRow, Integer> colD3;
    @FXML private TableColumn<DailyPlanDisplayRow, Integer> colD4;
    @FXML private TableColumn<DailyPlanDisplayRow, Integer> colD5;
    @FXML private TableColumn<DailyPlanDisplayRow, Integer> colD6;
    @FXML private TableColumn<DailyPlanDisplayRow, Integer> colD7;
    @FXML private TableColumn<DailyPlanDisplayRow, Integer> colTotal;





    private final ProductionPlanDailyService dailyService;
    private final WarehouseService warehouseService;

    @Autowired
    public DailyPlanController(ProductionPlanDailyService dailyService, WarehouseService warehouseService) {
        this.dailyService = dailyService;
        this.warehouseService = warehouseService;
    }
    @FXML
    public void initialize() {

        setupTableColumns();
        setupEvents();

        // Tạm thời hardcode danh sách line
        loadLines();

        btnSaveActuals.setOnAction(e -> saveDailyPlans()); // Giả sử nút này dùng để lưu cả actual & plan
        btnRollbackMaterial.setOnAction(e -> rollbackSelectedMaterial());

        tblDailyPlans.getSelectionModel().setCellSelectionEnabled(true);
        tblDailyPlans.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tblDailyPlans.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode().toString().equals("C")) {
                copySelectionToClipboard(tblDailyPlans);
            }
        });


    }

    private void setupEvents() {
        btnLoadDailyPlans.setOnAction(e -> loadDailyPlans());
        //btnSaveActuals.setOnAction(e -> saveDailyPlans());
    }

    private void setupTableColumns() {
        // Merge-like columns
        setupMergedTextColumn(colLine, DailyPlanDisplayRow::getLine, DailyPlanDisplayRow::getGroupKey);
        setupMergedTextColumn(colModel, DailyPlanDisplayRow::getModel, DailyPlanDisplayRow::getGroupKey);
        setupMergedTextColumn(colProductCode, DailyPlanDisplayRow::getProductCode, DailyPlanDisplayRow::getGroupKey);

        colType.setCellValueFactory(c -> c.getValue().typeProperty());

        setupEditableColumn(colD1, 0);
        setupEditableColumn(colD2, 1);
        setupEditableColumn(colD3, 2);
        setupEditableColumn(colD4, 3);
        setupEditableColumn(colD5, 4);
        setupEditableColumn(colD6, 5);
        setupEditableColumn(colD7, 6);

        colTotal.setCellValueFactory(c -> c.getValue().totalProperty().asObject());
    }



    private void setupEditableColumn(TableColumn<DailyPlanDisplayRow, Integer> col, int dayIndex) {
        col.setCellValueFactory(c -> c.getValue().dayProperty(dayIndex).asObject()); // <-- THÊM DÒNG NÀY

        col.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        col.setEditable(true); // <-- Cần set editable cho từng cột

        col.setOnEditCommit(event -> {
            DailyPlanDisplayRow row = event.getRowValue();
            int newValue = event.getNewValue() != null ? event.getNewValue() : 0;

            row.dayProperty(dayIndex).set(newValue);

            // Update lại tổng
            int newTotal = 0;
            for (int i = 0; i < 7; i++) {
                newTotal += row.getDay(i);
            }
            row.totalProperty().set(newTotal);

            // Ngày tương ứng
            LocalDate runDate = dpWeekDate.getValue()
                    .with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1)
                    .plusDays(dayIndex);

            if (row.getType().equals("Diff")) return;

            if (row.getType().equals("Plan")) {
                dailyService.updateDailyPlan(row.getPlanItemId(), runDate, newValue);
            } else if (row.getType().equals("Actual")) {
                dailyService.updateActual(row.getPlanItemId(), runDate, newValue);
            }

            loadDailyPlans();
        });
    }



    private void loadLines() {
        List<String> lineNames = warehouseService.getAllWarehouses()
                .stream()
                .map(w -> w.getName())
                .toList();

        cbLineFilter.setItems(FXCollections.observableArrayList(lineNames));
    }


    private void loadDailyPlans() {
        String line = cbLineFilter.getValue();
        LocalDate selectedDate = dpWeekDate.getValue();

        if (line == null || selectedDate == null) {
            showAlert("Vui lòng chọn Line và ngày trong tuần.");
            return;
        }

        int weekNo = selectedDate.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        int year = selectedDate.getYear();

        LocalDate monday = selectedDate.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
        updateDayColumnHeaders(monday);

        List<DailyPlanRowDto> rawData = dailyService.getDailyPlanView(line, weekNo, year);
        List<DailyPlanDisplayRow> displayData = convertToDisplayRows(rawData, line);

        tblDailyPlans.setItems(FXCollections.observableArrayList(displayData));
        tblDailyPlans.setEditable(true); // Chỉ xem, không chỉnh sửa ở đây
    }


    private List<DailyPlanDisplayRow> convertToDisplayRows(List<DailyPlanRowDto> dtos, String line) {
        List<DailyPlanDisplayRow> rows = new ArrayList<>();

        for (DailyPlanRowDto dto : dtos) {
            DailyPlanDisplayRow plan = new DailyPlanDisplayRow(dto.getPlanItemId(),line, dto.getModelCode(), dto.getSapCode(), "Plan", dto.getStock(),
                    dto.getDayPlan(1), dto.getDayPlan(2), dto.getDayPlan(3),
                    dto.getDayPlan(4), dto.getDayPlan(5), dto.getDayPlan(6), dto.getDayPlan(7));

            DailyPlanDisplayRow actual = new DailyPlanDisplayRow(dto.getPlanItemId(),line, dto.getModelCode(), dto.getSapCode(), "Actual", dto.getStock(),
                    dto.getDayActual(1), dto.getDayActual(2), dto.getDayActual(3),
                    dto.getDayActual(4), dto.getDayActual(5), dto.getDayActual(6), dto.getDayActual(7));

            DailyPlanDisplayRow diff = new DailyPlanDisplayRow(dto.getPlanItemId(), line, dto.getModelCode(), dto.getSapCode(), "Diff", dto.getStock(),
                    dto.getDayActual(1) - dto.getDayPlan(1),
                    dto.getDayActual(2) - dto.getDayPlan(2),
                    dto.getDayActual(3) - dto.getDayPlan(3),
                    dto.getDayActual(4) - dto.getDayPlan(4),
                    dto.getDayActual(5) - dto.getDayPlan(5),
                    dto.getDayActual(6) - dto.getDayPlan(6),
                    dto.getDayActual(7) - dto.getDayPlan(7));


            rows.add(plan);
            rows.add(actual);
            rows.add(diff);
        }

        return rows;
    }
    private void setupMergedTextColumn(TableColumn<DailyPlanDisplayRow, String> col,
                                       java.util.function.Function<DailyPlanDisplayRow, String> valueGetter,
                                       java.util.function.Function<DailyPlanDisplayRow, String> groupKeyGetter) {

        col.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= tblDailyPlans.getItems().size()) {
                    setText(null);
                    return;
                }

                DailyPlanDisplayRow current = tblDailyPlans.getItems().get(getIndex());

                // Dòng đầu tiên hoặc khác group thì hiển thị
                if (getIndex() == 0 || !groupKeyGetter.apply(current)
                        .equals(groupKeyGetter.apply(tblDailyPlans.getItems().get(getIndex() - 1)))) {
                    setText(valueGetter.apply(current));
                } else {
                    setText(""); // Không hiển thị nếu trùng nhóm
                }
            }
        });
    }

    private void saveDailyPlans() {
        ObservableList<DailyPlanDisplayRow> displayRows = tblDailyPlans.getItems();
        LocalDate selectedDate = dpWeekDate.getValue();

        if (selectedDate == null || displayRows == null || displayRows.isEmpty()) {
            showAlert("Không có dữ liệu để lưu.");
            return;
        }

        LocalDate monday = selectedDate.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);

        // Lấy nhóm theo Plan
        var planRows = displayRows.stream()
                .filter(row -> "Plan".equals(row.getType()))
                .toList();

        var actualRows = displayRows.stream()
                .filter(row -> "Actual".equals(row.getType()))
                .toList();

        for (DailyPlanDisplayRow planRow : planRows) {
            int planItemId = planRow.getPlanItemId();
            int plannedWeeklyQty = dailyService.getPlannedWeeklyQuantityByPlanItemId(planItemId);

            // Tính tổng Plan
            int totalPlanQty = 0;
            for (int i = 0; i < 7; i++) {
                totalPlanQty += planRow.getDay(i);
            }

            if (totalPlanQty > plannedWeeklyQty) {
                showAlert("Kế hoạch ngày vượt quá kế hoạch tuần của model " + planRow.getModel());
                return;
            }

            // Lưu kế hoạch ngày
            for (int i = 0; i < 7; i++) {
                LocalDate runDate = monday.plusDays(i);
                int planQty = planRow.getDay(i);
                dailyService.updateDailyPlan(planItemId, runDate, planQty);
            }
        }

        for (DailyPlanDisplayRow actualRow : actualRows) {
            int planItemId = actualRow.getPlanItemId();
            int plannedWeeklyQty = dailyService.getPlannedWeeklyQuantityByPlanItemId(planItemId);

            int totalActualQty = 0;
            for (int i = 0; i < 7; i++) {
                totalActualQty += actualRow.getDay(i);
            }

            if (totalActualQty > plannedWeeklyQty) {
                showAlert("Số lượng thực tế vượt quá kế hoạch tuần của model " + actualRow.getModel());
                return;
            }

            for (int i = 0; i < 7; i++) {
                LocalDate runDate = monday.plusDays(i);
                int actualQty = actualRow.getDay(i);

                dailyService.updateActual(planItemId, runDate, actualQty);

                try {
                    // Chỉ trừ nếu chưa có log
                    dailyService.consumeMaterialByActual(planItemId, runDate, actualQty);
                } catch (RuntimeException e) {
                    showAlert("Lỗi trừ liệu: " + e.getMessage());
                    return;
                }
            }
        }

        showAlert("Đã lưu kế hoạch ngày và số lượng thực tế thành công!");
    }


    private void updateDayColumnHeaders(LocalDate startOfWeek) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        colD1.setText(startOfWeek.plusDays(0).format(formatter));
        colD2.setText(startOfWeek.plusDays(1).format(formatter));
        colD3.setText(startOfWeek.plusDays(2).format(formatter));
        colD4.setText(startOfWeek.plusDays(3).format(formatter));
        colD5.setText(startOfWeek.plusDays(4).format(formatter));
        colD6.setText(startOfWeek.plusDays(5).format(formatter));
        colD7.setText(startOfWeek.plusDays(6).format(formatter));

        int weekNo = startOfWeek.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        colTotal.setText("Total W" + weekNo);
    }


    private void rollbackSelectedMaterial() {
        DailyPlanDisplayRow selected = tblDailyPlans.getSelectionModel().getSelectedItem();
        if (selected == null || !selected.getType().equals("Actual")) {
            showAlert("Vui lòng chọn một dòng có loại 'Actual' để hoàn tác.");
            return;
        }

        LocalDate selectedDate = dpWeekDate.getValue();
        if (selectedDate == null) {
            showAlert("Vui lòng chọn ngày trong tuần.");
            return;
        }

        for (int i = 0; i < 7; i++) {
            LocalDate runDate = selectedDate.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1).plusDays(i);
            int actualQty = selected.getDay(i);
            if (actualQty > 0) {
                try {
                    dailyService.rollbackConsumeMaterial(selected.getPlanItemId(), runDate);
                } catch (Exception ex) {
                    showAlert("Lỗi hoàn tác: " + ex.getMessage());
                    return;
                }
            }
        }

        showAlert("Đã hoàn tác trừ liệu cho model: " + selected.getModel());
    }


    private void copySelectionToClipboard(TableView<?> table) {
        StringBuilder clipboardString = new StringBuilder();
        ObservableList<TablePosition> positionList = table.getSelectionModel().getSelectedCells();

        int prevRow = -1;
        for (TablePosition position : positionList) {
            int row = position.getRow();
            int col = position.getColumn();

            Object cell = table.getColumns().get(col).getCellData(row);
            if (cell == null) cell = "";

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


    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
