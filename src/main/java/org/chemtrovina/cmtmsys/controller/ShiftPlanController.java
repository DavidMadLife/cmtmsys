package org.chemtrovina.cmtmsys.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import org.chemtrovina.cmtmsys.dto.ShiftPlanRow;
import org.chemtrovina.cmtmsys.model.Employee;
import org.chemtrovina.cmtmsys.model.ShiftTypeEmployee;
import org.chemtrovina.cmtmsys.model.ShiftPlanEmployee;
import org.chemtrovina.cmtmsys.service.base.EmployeeService;
import org.chemtrovina.cmtmsys.service.base.ShiftPlanEmployeeService;
import org.chemtrovina.cmtmsys.service.base.ShiftTypeEmployeeService;
import org.chemtrovina.cmtmsys.utils.FxAlertUtils;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.chemtrovina.cmtmsys.utils.FxFilterUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Component
public class ShiftPlanController {


    // ================= FILTER =================
    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;
    @FXML private Label txtYearInfo;
    @FXML private Button btnFilter;
    @FXML private Button btnClear;

    // ================= BATCH ASSIGN =================
    @FXML private ComboBox<String> cbShiftCode;
    @FXML private TextField txtNote;
    @FXML private DatePicker dpBatchFrom;
    @FXML private DatePicker dpBatchTo;
    @FXML private Button btnBatchAssign;
    @FXML private Button btnApplyFilter;
    // ===== FILTER DETAIL =====
    @FXML private ComboBox<LocalDate> cbFilterDay;
    @FXML private ComboBox<String> cbFilterShift;



    // ================= TABLE =================
    @FXML private TableView<ShiftPlanRow> tblShiftPlan;
    @FXML private TableColumn<ShiftPlanRow, Boolean> colSelect;
    @FXML private TableColumn<ShiftPlanRow, Number> colIndex;
    @FXML private TableColumn<ShiftPlanRow, String> colMSCNID1;
    @FXML private TableColumn<ShiftPlanRow, String> colMSCNID2;
    @FXML private TableColumn<ShiftPlanRow, String> colFullName2;
    @FXML private TableColumn<ShiftPlanRow, String> colManager2;

    // ================= SERVICES =================
    private final EmployeeService employeeService;
    private final ShiftPlanEmployeeService shiftPlanEmployeeService;
    private final ShiftTypeEmployeeService shiftTypeEmployeeService;

    // ================= STATE =================
    private List<Employee> employees = new ArrayList<>();
    private List<LocalDate> displayedDates = new ArrayList<>();
    private List<String> shiftCodes = new ArrayList<>();
    private List<ShiftPlanRow> originalRows = new ArrayList<>();
    private final List<TableColumn<ShiftPlanRow, ?>> dynamicColumns = new ArrayList<>();


    private static final String[] VIET_WEEKDAY = {"", "T2", "T3", "T4", "T5", "T6", "T7", "CN"};

    public ShiftPlanController(EmployeeService employeeService,
                               ShiftPlanEmployeeService shiftPlanEmployeeService,
                               ShiftTypeEmployeeService shiftTypeEmployeeService) {

        this.employeeService = employeeService;
        this.shiftPlanEmployeeService = shiftPlanEmployeeService;
        this.shiftTypeEmployeeService = shiftTypeEmployeeService;
    }

    // ===========================================================
    @FXML
    public void initialize() {
        // Log INFO/DEBUG không cần thiết, loại bỏ.

        setupShiftPlanTable();
        setupTickAllColumn();

        employees = employeeService.getAllEmployees();

        try {
            shiftCodes = shiftTypeEmployeeService.getAll()
                    .stream().map(ShiftTypeEmployee::getShiftCode).toList();
        } catch (Exception ex) {
            FxAlertUtils.error("Lỗi khi tải danh sách Mã Ca: {}"); // 🔥 Ghi lỗi tải data ban đầu
        }


        cbShiftCode.setItems(FXCollections.observableArrayList(shiftCodes));
        cbFilterShift.setItems(FXCollections.observableArrayList(shiftCodes));

        // Disable filter detail when chưa load ngày
        cbFilterDay.setDisable(true);
        cbFilterShift.setDisable(true);
        btnApplyFilter.setDisable(true);

        btnFilter.setOnAction(e -> filterShiftPlan());
        btnClear.setOnAction(e -> clearFilter());
        btnBatchAssign.setOnAction(e -> assignBatch());
        btnApplyFilter.setOnAction(e -> applyDayShiftFilter());

        FxClipboardUtils.enableCopyShortcut(tblShiftPlan);
        tblShiftPlan.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }

    // ... (các hàm setup không đổi) ...
    private void setupShiftPlanTable() {

        tblShiftPlan.setEditable(true);
        tblShiftPlan.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case A -> {
                    if (event.isControlDown()) {
                        tblShiftPlan.getItems().forEach(r -> r.setSelected(true));
                        tblShiftPlan.refresh();
                    }
                }
            }
        });


        colSelect.setCellValueFactory(c -> c.getValue().selectedProperty());
        colSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colSelect));

        colIndex.setCellValueFactory(row ->
                Bindings.createIntegerBinding(
                        () -> tblShiftPlan.getItems().indexOf(row.getValue()) + 1));

        colMSCNID1.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMSCNID1()));
        colMSCNID2.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMSCNID2()));
        colFullName2.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colManager2.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getManagerName()));


    }

    private void setupTickAllColumn() {
        CheckBox headerCheck = new CheckBox();
        headerCheck.setOnAction(e -> {
            boolean checked = headerCheck.isSelected();
            for (ShiftPlanRow row : tblShiftPlan.getItems()) {
                row.setSelected(checked);
            }
            tblShiftPlan.refresh();
        });

        // Gán checkbox vào header
        colSelect.setGraphic(headerCheck);
    }

    private void setupStaticColumnFilters() {
        FxFilterUtils.setupFilterMenu(
                colMSCNID1,
                originalRows,
                ShiftPlanRow::getMSCNID1,
                selected -> applyFilter("MSCNID1", selected)
        );

        FxFilterUtils.setupFilterMenu(
                colMSCNID2,
                originalRows,
                ShiftPlanRow::getMSCNID2,
                selected -> applyFilter("MSCNID2", selected)
        );

        FxFilterUtils.setupFilterMenu(
                colFullName2,
                originalRows,
                ShiftPlanRow::getFullName,
                selected -> applyFilter("FullName", selected)
        );

        FxFilterUtils.setupFilterMenu(
                colManager2,
                originalRows,
                ShiftPlanRow::getManagerName,
                selected -> applyFilter("ManagerName", selected)
        );
    }
    private void applyFilter(String field, List<String> selectedValues) {
        // Log INFO/DEBUG không cần thiết, loại bỏ.

        List<ShiftPlanRow> filtered = originalRows.stream()
                .filter(row -> switch (field) {
                    case "MSCNID1" -> selectedValues.contains(row.getMSCNID1());
                    case "MSCNID2" -> selectedValues.contains(row.getMSCNID2());
                    case "FullName" -> selectedValues.contains(row.getFullName());
                    case "ManagerName" -> selectedValues.contains(row.getManagerName());
                    default -> true;
                })
                .toList();

        tblShiftPlan.setItems(FXCollections.observableArrayList(filtered));
    }



    // ===========================================================
    private void filterShiftPlan() {

        LocalDate from = dpFrom.getValue();
        LocalDate to = dpTo.getValue();

        if (from == null || to == null || to.isBefore(from)) {
            FxAlertUtils.warning("Lỗi ngày khoảng ngày không hợp lệ.");
            return;
        }

        txtYearInfo.setText("Năm: " + from.getYear());

        displayedDates.clear();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1))
            displayedDates.add(d);

        createDynamicColumns();
        loadShiftPlanData(from, to);

        // Cập nhật dữ liệu cho ComboBox lọc
        cbFilterDay.setItems(FXCollections.observableArrayList(displayedDates));
        cbFilterDay.setDisable(false);

        cbFilterShift.setValue(null);
        cbFilterShift.setDisable(false);

        btnApplyFilter.setDisable(false);
    }

    private void applyDayShiftFilter() {

        LocalDate filterDay = cbFilterDay.getValue();
        String filterShift = cbFilterShift.getValue();

        if (filterDay == null) {
            FxAlertUtils.warning("Lỗi Validate: Ngày lọc chi tiết không được chọn."); // 🔥 Ghi WARN cho lỗi Validate
            FxAlertUtils.warning("Vui lòng chọn ngày.");
            return;
        }

        // Chỉ hiển thị 1 ngày duy nhất
        showOnlyOneDay(filterDay);

        // Nếu không chọn ca → chỉ hiển thị đúng ngày
        if (filterShift == null || filterShift.isEmpty()) {
            tblShiftPlan.setItems(FXCollections.observableArrayList(originalRows));
            return;
        }

        // Nếu có ca → lọc row phù hợp
        List<ShiftPlanRow> filtered = originalRows.stream()
                .filter(row -> filterShift.equals(row.getShiftForDate(filterDay)))
                .toList();

        tblShiftPlan.setItems(FXCollections.observableArrayList(filtered));
    }


    private void showOnlyOneDay(LocalDate day) {

        // Xóa toàn bộ dynamic columns
        tblShiftPlan.getColumns().removeIf(c ->
                c.getId() != null &&
                        (c.getId().startsWith("dynamicDay") || c.getId().startsWith("week_separator"))
        );

        // Tạo mới 1 cột duy nhất
        String weekday = VIET_WEEKDAY[day.getDayOfWeek().getValue()];
        String dayMonth = "%02d/%02d".formatted(day.getDayOfMonth(), day.getMonthValue());

        TableColumn<ShiftPlanRow, String> parent = new TableColumn<>(weekday);
        parent.setId("dynamicDay_" + day);

        TableColumn<ShiftPlanRow, String> child = new TableColumn<>(dayMonth);
        child.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getShiftForDate(day)));
        child.setCellFactory(col -> {
            ComboBoxTableCell<ShiftPlanRow, String> cell =
                    new ComboBoxTableCell<>(FXCollections.observableArrayList(shiftCodes));

            cell.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !cell.isEmpty()) {
                    tblShiftPlan.edit(cell.getIndex(), col); // mở edit mode
                }
            });

            return cell;
        });


        child.setOnEditCommit(evt -> {
            ShiftPlanRow row = evt.getRowValue();
            LocalDate date = day;
            String newShift = evt.getNewValue();

            try {
                shiftPlanEmployeeService.saveOrUpdate(
                        row.getEmployeeId(),
                        date,
                        newShift,
                        null
                );
            } catch (Exception ex) {
                FxAlertUtils.error("Lỗi cập nhật ca: " + ex.getMessage());
                // Refresh để revert lại giá trị cũ
                tblShiftPlan.refresh();
                return;
            }

            row.setShiftForDate(day, newShift);
            tblShiftPlan.refresh();
        });

        child.setPrefWidth(65);
        child.setStyle("-fx-alignment:center;");

        parent.getColumns().add(child);

        // Add đúng vị trí sau cột cố định
        tblShiftPlan.getColumns().add(parent);
    }


    // ===========================================================
    private void createDynamicColumns() {
        // Log INFO/DEBUG không cần thiết, loại bỏ.

        // Xóa toàn bộ dynamic columns + separator trước
        tblShiftPlan.getColumns().removeIf(c ->
                c.getId() != null &&
                        (c.getId().startsWith("dynamicDay") || c.getId().startsWith("week_separator"))
        );
        dynamicColumns.clear(); // Reset danh sách parent columns

        for (LocalDate date : displayedDates) {

            String weekday = VIET_WEEKDAY[date.getDayOfWeek().getValue()];
            String dayMonth = "%02d/%02d".formatted(date.getDayOfMonth(), date.getMonthValue());

            // Parent column
            TableColumn<ShiftPlanRow, String> parent = new TableColumn<>(weekday);
            parent.setId("dynamicDay_" + date);

            // Child column
            TableColumn<ShiftPlanRow, String> child = new TableColumn<>(dayMonth);
            child.setCellValueFactory(cell ->
                    new SimpleStringProperty(cell.getValue().getShiftForDate(date)));
            child.setCellFactory(col -> {
                ComboBoxTableCell<ShiftPlanRow, String> cell =
                        new ComboBoxTableCell<>(FXCollections.observableArrayList(shiftCodes));

                cell.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !cell.isEmpty()) {
                        tblShiftPlan.edit(cell.getIndex(), col); // mở edit mode
                    }
                });

                return cell;
            });


            child.setOnEditCommit(evt -> {
                ShiftPlanRow row = evt.getRowValue();
                String newShift = evt.getNewValue();

                try {
                    shiftPlanEmployeeService.saveOrUpdate(
                            row.getEmployeeId(),
                            date,
                            newShift,
                            null
                    );
                } catch (Exception ex) {
                    FxAlertUtils.error("Lỗi cập nhật ca: " + ex.getMessage());
                    // Refresh để revert lại giá trị cũ
                    tblShiftPlan.refresh();
                    return;
                }

                row.setShiftForDate(date, newShift);
                tblShiftPlan.refresh();
            });

            child.setPrefWidth(55);
            child.setStyle("-fx-alignment:center;");

            parent.getColumns().add(child);

            // Thêm parent column
            tblShiftPlan.getColumns().add(parent);
            dynamicColumns.add(parent);

            // ============================================
            // Thêm separator ngay sau Chủ Nhật (DayOfWeek = 7)
            // ============================================
            if (date.getDayOfWeek().getValue() == 7) {

                TableColumn<ShiftPlanRow, String> separator = new TableColumn<>();
                separator.setId("week_separator_" + date);
                separator.setPrefWidth(5);
                separator.setSortable(false);
                separator.setReorderable(false);
                separator.setEditable(false);
                separator.setStyle("-fx-background-color: #5e5e5e;");

                // Luôn add sau cột parent cuối cùng
                int insertIndex = tblShiftPlan.getColumns().indexOf(parent) + 1;
                tblShiftPlan.getColumns().add(insertIndex, separator);
            }
        }
    }


    // ===========================================================
    private void loadShiftPlanData(LocalDate from, LocalDate to) {
        // Log INFO/DEBUG không cần thiết, loại bỏ.

        List<ShiftPlanRow> rows = new ArrayList<>();

        for (Employee emp : employees) {
            try {
                List<ShiftPlanEmployee> plans =
                        shiftPlanEmployeeService.getByEmployeeAndDateRange(
                                emp.getEmployeeId(), from, to);

                rows.add(new ShiftPlanRow(emp, displayedDates, plans));
            } catch (Exception ex) {
                FxAlertUtils.error("Lỗi tải lịch ca cho NV ID {}: {}"); // 🔥 Ghi lỗi
            }
        }

        originalRows = rows;

        tblShiftPlan.setItems(FXCollections.observableArrayList(rows));

        setupStaticColumnFilters();
    }

    // ===========================================================
    private void assignBatch() {

        LocalDate from = dpBatchFrom.getValue();
        LocalDate to = dpBatchTo.getValue();
        String shiftCode = cbShiftCode.getValue();
        String note = txtNote.getText();

        if (shiftCode == null || shiftCode.isEmpty()) {
            FxAlertUtils.warning("Thiếu ca vui lòng chọn ca.");
            return;
        }

        if (from == null || to == null || to.isBefore(from)) {
            FxAlertUtils.warning("Ngày không hợp lệ vui lòng chọn khoảng ngày đúng.");
            return;
        }

        for (ShiftPlanRow row : tblShiftPlan.getItems()) {
            if (!row.isSelected()) continue;

            int empId = row.getEmployeeId();

            for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
                try {
                    shiftPlanEmployeeService.saveOrUpdate(empId, d, shiftCode, note);

                    if (displayedDates.contains(d))
                        row.setShiftForDate(d, shiftCode);
                } catch (Exception ex) {
                    FxAlertUtils.error("Lỗi phân ca hàng loạt cho NV ID {} vào ngày {}: {}"); // 🔥 Ghi lỗi
                }
            }
        }

        tblShiftPlan.refresh();
        FxAlertUtils.info("Hoàn tất phân ca hàng loạt thành công!");
    }

    // ===========================================================
    private void clearFilter() {

        dpFrom.setValue(null);
        dpTo.setValue(null);
        txtYearInfo.setText("Năm: -");

        displayedDates.clear();

        tblShiftPlan.getColumns().removeIf(c ->
                c.getId() != null &&
                        (c.getId().startsWith("dynamicDay") || c.getId().startsWith("week_separator"))
        );

        tblShiftPlan.getItems().clear();
        dynamicColumns.clear();

        // reset filter detail
        cbFilterDay.getItems().clear();
        cbFilterShift.setValue(null);

        cbFilterDay.setDisable(true);
        cbFilterShift.setDisable(true);
        btnApplyFilter.setDisable(true);
    }
}