package org.chemtrovina.cmtmsys.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.util.converter.DefaultStringConverter;
import org.chemtrovina.cmtmsys.dto.AbsentEmployeeDto;
import org.chemtrovina.cmtmsys.dto.TimeAttendanceLogDto;
import org.chemtrovina.cmtmsys.model.enums.AttendanceTimeStatus;
import org.chemtrovina.cmtmsys.model.enums.ScanAction;
import org.chemtrovina.cmtmsys.service.base.EmployeeService;
import org.chemtrovina.cmtmsys.service.base.ShiftPlanEmployeeService;
import org.chemtrovina.cmtmsys.service.base.TimeAttendanceLogService;
import org.chemtrovina.cmtmsys.utils.FxAlertUtils;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TimeAttendanceController {

    // =========================================================
    // SERVICES (DI)
    // =========================================================
    private final TimeAttendanceLogService logService;
    private final EmployeeService employeeService;
    private final ShiftPlanEmployeeService shiftPlanEmployeeService;

    @Autowired
    public TimeAttendanceController(TimeAttendanceLogService logService,
                                    EmployeeService employeeService,
                                    ShiftPlanEmployeeService shiftPlanEmployeeService) {
        this.logService = logService;
        this.employeeService = employeeService;
        this.shiftPlanEmployeeService = shiftPlanEmployeeService;
    }

    // =========================================================
    // FILTER / CONTROL PANEL
    // =========================================================
    @FXML private DatePicker dpScanDate;
    @FXML private Button btnFilterDate;
    @FXML private Button btnClearDateFilter;

    // =========================================================
    // SCAN AREA
    // =========================================================
    @FXML private TextField txtScanInput;
    @FXML private Button btnScan;
    @FXML private Button btnIn;
    @FXML private Button btnOut;
    @FXML private Label lblScanType;
    @FXML private Label lblScanStatus;

    private String currentScanType; // IN / OUT
    // ================= CACHE DATA =================
    private LocalDate currentDate;

    private List<TimeAttendanceLogDto> cachedLogs;
    private List<AbsentEmployeeDto> cachedAbsents;


    // =========================================================
    // SUMMARY
    // =========================================================
    @FXML
    private Label lblTotalEmployee;
    @FXML private Label lblTotalIn;
    @FXML private Label lblAbsentCount;

    // =========================================================
    // MAIN TABLE (TIME LOG)
    // =========================================================
    @FXML private TableView<TimeAttendanceLogDto> tblTimeAttendanceLog;

    @FXML private TableColumn<TimeAttendanceLogDto, Integer> colNo;
    @FXML private TableColumn<TimeAttendanceLogDto, String> colInput;
    @FXML private TableColumn<TimeAttendanceLogDto, String> colFullName;
    @FXML private TableColumn<TimeAttendanceLogDto, String> colCompany;
    @FXML private TableColumn<TimeAttendanceLogDto, String> colDepartment;
    @FXML private TableColumn<TimeAttendanceLogDto, String> colGender;
    @FXML private TableColumn<TimeAttendanceLogDto, String> colPosition;
    @FXML private TableColumn<TimeAttendanceLogDto, String> colNote;
    @FXML private TableColumn<TimeAttendanceLogDto, String> colShift;
    @FXML private TableColumn<TimeAttendanceLogDto, String> colIn;
    @FXML private TableColumn<TimeAttendanceLogDto, String> colOut;
    @FXML private TableColumn<TimeAttendanceLogDto, LocalDate> colDateOfBirth;
    @FXML private TableColumn<TimeAttendanceLogDto, LocalDate> colEntryDate;
    @FXML private TableColumn<TimeAttendanceLogDto, LocalDate> colScanDate;
    @FXML private TableColumn<TimeAttendanceLogDto, String> colY;

    @FXML private TableColumn<TimeAttendanceLogDto, String> colJobTitle;
    @FXML private TableColumn<TimeAttendanceLogDto, String> colManager;
    @FXML private TableColumn<TimeAttendanceLogDto, String> colPhoneNumber;

    @FXML private TableView<AbsentEmployeeDto> tblAbsentEmployees;

    @FXML private TableColumn<AbsentEmployeeDto, Integer> colAbsentNo;
    @FXML private TableColumn<AbsentEmployeeDto, String> colAbsentCode;
    @FXML private TableColumn<AbsentEmployeeDto, String> colAbsentName;
    @FXML private TableColumn<AbsentEmployeeDto, String> colAbsentDept;
    @FXML private TableColumn<AbsentEmployeeDto, String> colAbsentShift;
    @FXML private TableColumn<AbsentEmployeeDto, String> colAbsentNote;


    // =========================================================
    // INITIALIZE
    // =========================================================
    @FXML
    public void initialize() {
        setupTableColumns();
        setupActions();
        //loadInitialData();
        setupAbsentTableColumns();
        FxClipboardUtils.enableCopyShortcut(tblTimeAttendanceLog);
        FxClipboardUtils.enableCopyShortcut(tblAbsentEmployees);
    }

    // =========================================================
    // SETUP UI
    // =========================================================
    private void setupTableColumns() {
        colNo.setCellValueFactory(new PropertyValueFactory<>("no"));
        colInput.setCellValueFactory(new PropertyValueFactory<>("mscnId1"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colCompany.setCellValueFactory(new PropertyValueFactory<>("company"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("positionName"));

        colJobTitle.setCellValueFactory(new PropertyValueFactory<>("jobTitle"));
        colManager.setCellValueFactory(new PropertyValueFactory<>("managerName"));

        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colShift.setCellValueFactory(new PropertyValueFactory<>("shiftName"));
        colIn.setCellValueFactory(new PropertyValueFactory<>("in"));
        colOut.setCellValueFactory(new PropertyValueFactory<>("out"));
        colDateOfBirth.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        colEntryDate.setCellValueFactory(new PropertyValueFactory<>("entryDate"));

        colPhoneNumber.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));


        colScanDate.setCellValueFactory(new PropertyValueFactory<>("scanDate"));
        colY.setCellValueFactory(new PropertyValueFactory<>("shiftName"));

        tblTimeAttendanceLog.setEditable(true);





        tblTimeAttendanceLog.setEditable(true);
        colIn.setOnEditCommit(event -> {
            TimeAttendanceLogDto dto = event.getRowValue();
            String newTime = event.getNewValue();

            try {
                logService.manualFixAttendance(
                        dto.getEmployeeId(),
                        dto.getScanDate(),
                        newTime,
                        ScanAction.IN
                );

                dto.setIn(newTime);
                tblTimeAttendanceLog.refresh();

            } catch (Exception e) {
                FxAlertUtils.error(e.getMessage());
                tblTimeAttendanceLog.refresh();
            }
        });

        colOut.setOnEditCommit(event -> {
            TimeAttendanceLogDto dto = event.getRowValue();
            String newTime = event.getNewValue();

            try {
                logService.manualFixAttendance(
                        dto.getEmployeeId(),
                        dto.getScanDate(),
                        newTime,
                        ScanAction.OUT
                );

                dto.setOut(newTime);
                tblTimeAttendanceLog.refresh();

            } catch (Exception e) {
                FxAlertUtils.error(e.getMessage());
                tblTimeAttendanceLog.refresh();
            }
        });


        colIn.setEditable(true);

        colIn.setCellFactory(col -> new TextFieldTableCell<>(
                new DefaultStringConverter()
        ) {

            @Override
            public void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                TimeAttendanceLogDto dto =
                        getTableView().getItems().get(getIndex());

                setText(value);

                if (dto.getInStatus() == AttendanceTimeStatus.LATE) {
                    setStyle("""
                -fx-background-color: #ee2828;
                -fx-text-fill: black;
            """);
                } else {
                    setStyle("""
                -fx-background-color: #43bc43;
                -fx-text-fill: black;
            """);
                }
            }
        });


        colOut.setEditable(true);
        colOut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                TimeAttendanceLogDto dto =
                        getTableView().getItems().get(getIndex());

                setText(value);

                if (dto.getOutStatus() == AttendanceTimeStatus.EARLY) {
                    setStyle("""
                -fx-background-color: #e12c2c;
                -fx-text-fill: black;
            """);
                } else {
                    setStyle("""
                -fx-background-color: #54ed54;
                -fx-text-fill: black;
            """);
                }
            }
        });colOut.setEditable(true);

        colOut.setCellFactory(col -> new TextFieldTableCell<>(
                new DefaultStringConverter()
        ) {

            @Override
            public void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                TimeAttendanceLogDto dto =
                        getTableView().getItems().get(getIndex());

                setText(value);

                if (dto.getOutStatus() == AttendanceTimeStatus.EARLY) {
                    setStyle("""
                -fx-background-color: #e12c2c;
                -fx-text-fill: black;
            """);
                } else {
                    setStyle("""
                -fx-background-color: #54ed54;
                -fx-text-fill: black;
            """);
                }
            }
        });

    }
    private void setupAbsentTableColumns() {
        colAbsentNo.setCellValueFactory(new PropertyValueFactory<>("no"));
        colAbsentCode.setCellValueFactory(new PropertyValueFactory<>("employeeCode"));
        colAbsentName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colAbsentDept.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        colAbsentShift.setCellValueFactory(new PropertyValueFactory<>("shiftCode"));
        colAbsentNote.setCellValueFactory(new PropertyValueFactory<>("note"));

        tblAbsentEmployees.setEditable(true);
        colAbsentNote.setEditable(true);

        colAbsentNote.setCellFactory(TextFieldTableCell.forTableColumn());

        colAbsentNote.setOnEditCommit(event -> {
            AbsentEmployeeDto dto = event.getRowValue();
            String newNote = event.getNewValue();

            dto.setNote(newNote);

            try {
                shiftPlanEmployeeService.updateNote(
                        dto.getEmployeeId(),
                        dpScanDate.getValue(), // LocalDate đang xem
                        newNote
                );
            } catch (Exception e) {
                FxAlertUtils.error(e.getMessage());
                tblAbsentEmployees.refresh();
            }
        });


    }

    private void setupActions() {
        btnFilterDate.setOnAction(e -> handleDateFilter());
        btnClearDateFilter.setOnAction(e -> handleClearDateFilter());
        btnScan.setOnAction(e -> handleScanInput());

        txtScanInput.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleScanInput();
                e.consume();
            }
        });

        btnIn.setOnAction(e -> switchScanType("IN"));
        btnOut.setOnAction(e -> switchScanType("OUT"));
    }

    // =========================================================
    // LOAD DATA
    // =========================================================
    private void loadInitialData() {
        LocalDate today = LocalDate.now();
        dpScanDate.setValue(today);
        loadLogsByDate(today);
        loadAbsentEmployees(today);
        updateAttendanceSummary(today);
    }

    private void loadLogsByDate(LocalDate date) {
        try {
            var logs = logService.getLogDtosByDateRange(date, date);

            tblTimeAttendanceLog.setItems(FXCollections.observableArrayList(logs));

            lblScanStatus.setText("Tải thành công " + logs.size() + " bản ghi (" + date + ")");
            lblScanStatus.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            lblScanStatus.setText("Lỗi tải dữ liệu: " + e.getMessage());
            lblScanStatus.setStyle("-fx-text-fill: red;");
        }
    }

    private void loadAbsentEmployees(LocalDate date) {


        var absents = logService.getAbsentEmployees(date);



        tblAbsentEmployees.setItems(
                FXCollections.observableArrayList(absents)
        );

        lblAbsentCount.setText(String.valueOf(absents.size()));
    }


    // =========================================================
    // FILTER
    // =========================================================
    private void handleDateFilter() {
        if (dpScanDate.getValue() == null) {
            FxAlertUtils.warning("Vui lòng chọn ngày.");
            return;
        }
        LocalDate date = dpScanDate.getValue();
        loadLogsByDate(date);
        loadAbsentEmployees(date);    // ⭐
        updateAttendanceSummary(date);
    }


    private void handleClearDateFilter() {
        LocalDate today = LocalDate.now();
        dpScanDate.setValue(today);
        loadLogsByDate(today);
        updateAttendanceSummary(today);

        FxAlertUtils.info("Đã reset về hôm nay.");
    }


    // =========================================================
    // SCAN LOGIC
    // =========================================================
    private void handleScanInput() {
        String input = txtScanInput.getText().trim();
        if (input.isEmpty()) return;

        if (currentScanType == null) {
            FxAlertUtils.warning("Vui lòng chọn IN hoặc OUT trước.");
            return;
        }

        try {
            TimeAttendanceLogDto dto =
                    logService.processScan(input, currentScanType);

            tblTimeAttendanceLog.getItems().add(0, dto);

            LocalDate date = dpScanDate.getValue();
            //loadAbsentEmployees(date);    // ⭐
            //updateAttendanceSummary(date);

            //FxAlertUtils.info("OK: " + dto.getFullName());
        } catch (Exception e) {
            FxAlertUtils.error("Lỗi: " + e.getMessage());
        }

        txtScanInput.clear();
        txtScanInput.requestFocus();
    }

    private void switchScanType(String type) {
        currentScanType = type;
        lblScanType.setText(type);

        boolean isIn = "IN".equals(type);

        btnIn.setDisable(isIn);
        btnOut.setDisable(!isIn);

        lblScanType.setStyle(isIn
                ? "-fx-text-fill: green; -fx-font-size: 22px; -fx-font-weight: bold;"
                : "-fx-text-fill: red; -fx-font-size: 22px; -fx-font-weight: bold;");
    }

    // =========================================================
    // SUMMARY LOGIC
    // =========================================================

    private void updateAttendanceSummary(LocalDate date) {
        var employees = employeeService.getAllEmployeeDtos();
        var logs = logService.getLogDtosByDateRange(date, date);

        Set<Integer> inEmployeeIds = logs.stream()
                .filter(l -> l.getIn() != null && !l.getIn().isBlank())
                .map(TimeAttendanceLogDto::getEmployeeId)
                .collect(Collectors.toSet());

        long totalIn = employees.stream()
                .filter(e -> inEmployeeIds.contains(e.getEmployeeId()))
                .count();

        lblTotalEmployee.setText(String.valueOf(employees.size()));
        lblTotalIn.setText(String.valueOf(totalIn));
        lblAbsentCount.setText(String.valueOf(employees.size() - totalIn));
    }

}
