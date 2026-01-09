package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.chemtrovina.cmtmsys.dto.EmployeeLeaveFilter;
import org.chemtrovina.cmtmsys.dto.LeaveStatisticDeptDto;
import org.chemtrovina.cmtmsys.model.Employee;
import org.chemtrovina.cmtmsys.model.EmployeeLeave;
import org.chemtrovina.cmtmsys.model.enums.LeaveType;
import org.chemtrovina.cmtmsys.repository.base.EmployeeRepository;
import org.chemtrovina.cmtmsys.service.base.EmployeeLeaveService;
import org.chemtrovina.cmtmsys.service.base.EmployeeService;
import org.chemtrovina.cmtmsys.utils.AutoCompleteUtils;
import org.chemtrovina.cmtmsys.utils.FxAlertUtils;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class EmployeeLeaveController {

    @FXML private TextField txtEmployeeName;
    @FXML private ComboBox<LeaveType> cbLeaveType;

    @FXML private DatePicker dpFromDate;
    @FXML private DatePicker dpToDate;

    @FXML private TextField txtReason;
    @FXML private TextArea txtDescription;

    @FXML private Button btnSave;
    @FXML private Button btnClear;

    @FXML private TableView<EmployeeLeave> tblLeave;

    @FXML private TableColumn<EmployeeLeave, Integer> colNo;
    @FXML private TableColumn<EmployeeLeave, String> colEmployee;
    @FXML private TableColumn<EmployeeLeave, LocalDate> colFrom;
    @FXML private TableColumn<EmployeeLeave, LocalDate> colTo;
    @FXML private TableColumn<EmployeeLeave, LeaveType> colType;
    @FXML private TableColumn<EmployeeLeave, String> colReason;
    @FXML private TableColumn<EmployeeLeave, String> colDescription;

    // ===== STATISTIC =====
    // ===== STATISTIC =====
    @FXML private DatePicker dpStatFrom;
    @FXML private DatePicker dpStatTo;
    @FXML private Button btnFilterStatistic;

    @FXML private TableView<LeaveStatisticDeptDto> tblLeaveStatistic;

    @FXML private TableColumn<LeaveStatisticDeptDto, String> colDept;
    @FXML private TableColumn<LeaveStatisticDeptDto, Integer> colLeavePermit;
    @FXML private TableColumn<LeaveStatisticDeptDto, Integer> colLeaveNoPermit;
    @FXML private TableColumn<LeaveStatisticDeptDto, Integer> colLeaveSick;
    @FXML private TableColumn<LeaveStatisticDeptDto, Integer> colLeavePrivate;
    @FXML private TableColumn<LeaveStatisticDeptDto, Integer> colLeaveOther;

    @FXML private TableColumn<LeaveStatisticDeptDto, Integer> colTotal;



    private final EmployeeLeaveService employeeLeaveService;
    private final EmployeeService employeeService;
    private AutoCompleteUtils.AutoCompletionBinding autoCompleteBinding;
    private EmployeeLeave selectedLeave = null;


    @Autowired
    public EmployeeLeaveController( EmployeeLeaveService employeeLeaveService, EmployeeService employeeService ) {
        this.employeeLeaveService = employeeLeaveService;
        this.employeeService = employeeService;
    }

    @FXML
    public void initialize() {

        cbLeaveType.getItems().setAll(LeaveType.values());

        loadLeaveTable();

        setupTable();
        setupActions();
        setupStatisticTable();
        setupEmployeeAutoComplete();
        setupTableSelection();

        FxClipboardUtils.enableCopyShortcut(tblLeaveStatistic);
        FxClipboardUtils.enableCopyShortcut(tblLeave);
    }

    private void setupTable() {

        colNo.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(
                        tblLeave.getItems().indexOf(cell.getValue()) + 1
                )
        );

        colEmployee.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        String.valueOf(cell.getValue().getEmployeeId())
                )
        );

        colFrom.setCellValueFactory(new PropertyValueFactory<>("fromDate"));
        colTo.setCellValueFactory(new PropertyValueFactory<>("toDate"));
        colType.setCellValueFactory(new PropertyValueFactory<>("leaveType"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    private void setupStatisticTable() {

        colDept.setCellValueFactory(
                new PropertyValueFactory<>("departmentName")
        );

        colLeavePermit.setCellValueFactory(
                new PropertyValueFactory<>("leavePermit")
        );

        colLeaveNoPermit.setCellValueFactory(
                new PropertyValueFactory<>("leaveNoPermit")
        );

        colLeaveSick.setCellValueFactory(
                new PropertyValueFactory<>("leaveSick")
        );

        colLeavePrivate.setCellValueFactory(
                new PropertyValueFactory<>("leavePrivate")
        );

        colLeaveOther.setCellValueFactory(
                new PropertyValueFactory<>("leaveOther")
        );

        colTotal.setCellValueFactory(
                new PropertyValueFactory<>("total")
        );
    }

    private void setupActions() {

        btnSave.setOnAction(e -> handleSave());
        btnClear.setOnAction(e -> clearForm());
        btnFilterStatistic.setOnAction(e -> loadStatistic());

        btnSave.setOnAction(e -> {
            handleSave();
            filterLeave(); // reload sau khi save
        });

        btnClear.setOnAction(e -> {
            clearForm();
            loadLeaveTable();
        });

    }

    private void handleSave() {

        String employeeName = txtEmployeeName.getText().trim();
        LeaveType leaveType = cbLeaveType.getValue();
        LocalDate from = dpFromDate.getValue();
        LocalDate to = dpToDate.getValue();

        if (employeeName.isEmpty()) {
            FxAlertUtils.warning("Vui lòng nhập tên nhân viên");
            return;
        }

        Employee employee = employeeService.findByFullName(employeeName);
        if (employee == null) {
            FxAlertUtils.warning("Không tìm thấy nhân viên: " + employeeName);
            return;
        }

        if (leaveType == null) {
            FxAlertUtils.warning("Vui lòng chọn loại nghỉ");
            return;
        }

        EmployeeLeave leave;

        // ===== CREATE or UPDATE =====
        if (selectedLeave == null) {
            leave = new EmployeeLeave();
        } else {
            leave = selectedLeave;
        }

        leave.setEmployeeId(employee.getEmployeeId());
        leave.setFromDate(from);
        leave.setToDate(to);
        leave.setLeaveType(leaveType);
        leave.setReason(txtReason.getText());
        leave.setDescription(txtDescription.getText());

        try {
            if (selectedLeave == null) {
                employeeLeaveService.create(leave);
                tblLeave.getItems().add(leave);
            } else {
                employeeLeaveService.update(leave);
                tblLeave.refresh();
            }

            clearForm();
            resetEditState();

        } catch (Exception ex) {
            FxAlertUtils.error(ex.getMessage());
        }
    }


    private void clearForm() {
        txtEmployeeName.clear();
        cbLeaveType.setValue(null);
        dpFromDate.setValue(null);
        dpToDate.setValue(null);
        txtReason.clear();
        txtDescription.clear();

        resetEditState();
    }

    private void resetEditState() {
        selectedLeave = null;
        tblLeave.getSelectionModel().clearSelection();
        btnSave.setText("Save");
    }

    private void loadStatistic() {

        LocalDate from = dpStatFrom.getValue();
        LocalDate to   = dpStatTo.getValue();

/*        if (from == null || to == null) {
            FxAlertUtils.warning("Vui lòng chọn từ ngày và đến ngày");
            return;
        }*/

        var data = employeeLeaveService.statisticByDepartment(from, to);
        tblLeaveStatistic.getItems().setAll(data);
    }

    private void loadLeaveTable() {
        var data = employeeLeaveService.findByFilter(new EmployeeLeaveFilter());
        tblLeave.getItems().setAll(data);
    }

    private void setupEmployeeAutoComplete() {

        // Lấy danh sách nhân viên
        var employees = employeeService.getAllEmployees();

        // Map sang list tên
        var names = employees.stream()
                .map(Employee::getFullName)
                .filter(n -> n != null && !n.isBlank())
                .distinct()
                .sorted()
                .toList();

        // Gắn autocomplete
        autoCompleteBinding =
                AutoCompleteUtils.setupAutoComplete(txtEmployeeName, names);
    }

    private void filterLeave() {

        EmployeeLeaveFilter filter = new EmployeeLeaveFilter();

        // Employee
        if (!txtEmployeeName.getText().isBlank()) {
            Employee emp = employeeService.findByFullName(
                    txtEmployeeName.getText().trim()
            );
            if (emp != null) {
                filter.setEmployeeId(emp.getEmployeeId());
            }
        }

        filter.setLeaveType(cbLeaveType.getValue());
        filter.setFromDate(dpFromDate.getValue());
        filter.setToDate(dpToDate.getValue());

        var data = employeeLeaveService.findByFilter(filter);
        tblLeave.getItems().setAll(data);
    }

    private void setupTableSelection() {

        tblLeave.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {

                    if (newVal == null) return;

                    selectedLeave = newVal;

                    // Load dữ liệu lên form
                    Employee emp = employeeService.getEmployeeById(newVal.getEmployeeId());
                    if (emp != null) {
                        txtEmployeeName.setText(emp.getFullName());
                    }

                    cbLeaveType.setValue(newVal.getLeaveType());
                    dpFromDate.setValue(newVal.getFromDate());
                    dpToDate.setValue(newVal.getToDate());
                    txtReason.setText(newVal.getReason());
                    txtDescription.setText(newVal.getDescription());

                    btnSave.setText("Update");
                });
    }


}
