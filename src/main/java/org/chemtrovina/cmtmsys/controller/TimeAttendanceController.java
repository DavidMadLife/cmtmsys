package org.chemtrovina.cmtmsys.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import org.chemtrovina.cmtmsys.dto.TimeAttendanceLogDto;
import org.chemtrovina.cmtmsys.service.base.TimeAttendanceLogService;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class TimeAttendanceController {

    private final TimeAttendanceLogService logService;

    // =========================================================
    // KHU VỰC ĐIỀU KHIỂN (CONTROL PANEL)
    // =========================================================

    // --- LỌC NGÀY ---
    @FXML private DatePicker dpFromDate;
    @FXML private DatePicker dpToDate;
    @FXML private Button btnFilterDate;
    @FXML private Button btnClearDateFilter;
    @FXML private Button btnIn;
    @FXML private Button btnOut;
    @FXML private Label lblScanType;


    // --- SCAN ĐIỂM DANH ---
    @FXML private TextField txtScanInput;
    @FXML private Button btnScan;
    @FXML private Label lblScanStatus;


    // Gán kiểu DTO: TableView<TimeAttendanceLogDto>
    @FXML private TableView<TimeAttendanceLogDto> tblTimeAttendanceLog;
    private String currentScanType = null; // "IN" hoặc "OUT"


    // --- CỘT DỮ LIỆU ---
    // Sử dụng kiểu Object/String/LocalDate vì chưa biết chính xác kiểu Generic của DTO
    @FXML private TableColumn<TimeAttendanceLogDto, Integer> colNo;
    @FXML private TableColumn<TimeAttendanceLogDto, String> colInput;
    @FXML private TableColumn<TimeAttendanceLogDto, String> colFullName;
    @FXML private TableColumn<TimeAttendanceLogDto, String> colCompany;
    @FXML private TableColumn<TimeAttendanceLogDto, String> colGender;
    @FXML private TableColumn<TimeAttendanceLogDto, String> colNote;
    @FXML private TableColumn<TimeAttendanceLogDto, String> colPosition;
    //@FXML private TableColumn<TimeAttendanceLogDto, String> colX;
    @FXML private TableColumn<TimeAttendanceLogDto, LocalDate> colDateOfBirth;
    @FXML private TableColumn<TimeAttendanceLogDto, LocalDate> colEntryDate;
    //@FXML private TableColumn<TimeAttendanceLogDto, String> colCodeNow;
    @FXML private TableColumn<TimeAttendanceLogDto, LocalDate> colScanDate;
    //@FXML private TableColumn<TimeAttendanceLogDto, String> colScanTime;
    @FXML private TableColumn<TimeAttendanceLogDto, String> colIn;
    @FXML private TableColumn<TimeAttendanceLogDto, String> colOut;
    @FXML private TableColumn<TimeAttendanceLogDto, String> colDepartment;
    @FXML private TableColumn<TimeAttendanceLogDto, String> colShift;


    // =========================================================
    // DEPENDENCY INJECTION (Spring)
    // =========================================================

    @Autowired
    public TimeAttendanceController(TimeAttendanceLogService logService) {
        this.logService = logService;
    }


    // =========================================================
    // KHỞI TẠO
    // =========================================================

    @FXML
    public void initialize() {
        setupTableColumns();
        setupActions();
        loadInitialData();
        FxClipboardUtils.enableCopyShortcut(tblTimeAttendanceLog);

        btnIn.setOnAction(e -> {
            currentScanType = "IN";
            lblScanType.setText("IN");
            lblScanType.setStyle("-fx-text-fill: green; -fx-font-size: 22px; -fx-font-weight: bold;");

            btnIn.setStyle("-fx-font-size: 18px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
            btnOut.setStyle("-fx-font-size: 18px; -fx-background-color: #9E9E9E; -fx-text-fill: white;");

            btnIn.setDisable(true);
            btnOut.setDisable(false);
        });

        btnOut.setOnAction(e -> {
            currentScanType = "OUT";
            lblScanType.setText("OUT");
            lblScanType.setStyle("-fx-text-fill: red; -fx-font-size: 22px; -fx-font-weight: bold;");

            btnOut.setStyle("-fx-font-size: 18px; -fx-background-color: #F44336; -fx-text-fill: white;");
            btnIn.setStyle("-fx-font-size: 18px; -fx-background-color: #9E9E9E; -fx-text-fill: white;");

            btnOut.setDisable(true);
            btnIn.setDisable(false);
        });

    }

    private void setupTableColumns() {

        colNo.setCellValueFactory(new PropertyValueFactory<>("no"));
        colInput.setCellValueFactory(new PropertyValueFactory<>("mscnId1"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colCompany.setCellValueFactory(new PropertyValueFactory<>("company"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("positionName"));
        colDateOfBirth.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        colEntryDate.setCellValueFactory(new PropertyValueFactory<>("entryDate"));
        /*colCodeNow.setCellValueFactory(new PropertyValueFactory<>("codeNow"));*/
        colScanDate.setCellValueFactory(new PropertyValueFactory<>("scanDate"));

        /*// Time
        colScanTime.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(
                        cell.getValue().getScanTime() != null ?
                                cell.getValue().getScanTime().toString() : ""
                )
        );*/

        colIn.setCellValueFactory(new PropertyValueFactory<>("in"));
        colOut.setCellValueFactory(new PropertyValueFactory<>("out"));

        colDepartment.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        colShift.setCellValueFactory(new PropertyValueFactory<>("shiftName"));
    }


    private void setupActions() {
        btnFilterDate.setOnAction(e -> handleDateFilter());
        btnClearDateFilter.setOnAction(e -> handleClearDateFilter());
        btnScan.setOnAction(e -> handleScanInput());

        // Xử lý sự kiện nhấn Enter trong ô Scan
        txtScanInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleScanInput();
                event.consume();
            }
        });
    }

    // =========================================================
    // LOGIC DỮ LIỆU
    // =========================================================

    private void loadInitialData() {
        // Mặc định tải dữ liệu của ngày hôm nay
        LocalDate today = LocalDate.now();
        dpFromDate.setValue(today);
        dpToDate.setValue(today);
        loadLogs(today, today);
    }

    private void handleDateFilter() {
        LocalDate from = dpFromDate.getValue();
        LocalDate to = dpToDate.getValue();

        if (from == null || to == null || from.isAfter(to)) {
            lblScanStatus.setText("Lỗi: Khoảng ngày không hợp lệ.");
            lblScanStatus.setStyle("-fx-text-fill: red;");
            return;
        }

        lblScanStatus.setText("Đang tải dữ liệu...");
        lblScanStatus.setStyle("-fx-text-fill: gray;");
        loadLogs(from, to);
    }

    private void handleClearDateFilter() {
        dpFromDate.setValue(null);
        dpToDate.setValue(null);
        // Tải lại dữ liệu của 7 ngày gần nhất hoặc toàn bộ (tùy quy mô DB)
        loadLogs(LocalDate.now().minusDays(7), LocalDate.now());
        lblScanStatus.setText("Đã xóa lọc.");
        lblScanStatus.setStyle("-fx-text-fill: green;");
    }

    private void loadLogs(LocalDate from, LocalDate to) {
        try {
            List<TimeAttendanceLogDto> logs = logService.getLogDtosByDateRange(from, to);
            tblTimeAttendanceLog.setItems(FXCollections.observableArrayList(logs));
            lblScanStatus.setText("Tải thành công " + logs.size() + " bản ghi.");
            lblScanStatus.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            lblScanStatus.setText("Lỗi tải dữ liệu: " + e.getMessage());
            System.out.println(e.getMessage());
            lblScanStatus.setStyle("-fx-text-fill: red;");
        }
    }

    private void handleScanInput() {

        String input = txtScanInput.getText().trim();
        if (input.isEmpty()) return;


        if (currentScanType == null) {
            lblScanStatus.setText("Vui lòng chọn IN hoặc OUT trước.");
            lblScanStatus.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            TimeAttendanceLogDto dto = logService.processScan(input, currentScanType);

            tblTimeAttendanceLog.getItems().add(0, dto);

            lblScanStatus.setText("OK: " + dto.getFullName());
            lblScanStatus.setStyle("-fx-text-fill: green;");

        } catch (Exception e) {
            lblScanStatus.setText("Lỗi: " + e.getMessage());
            lblScanStatus.setStyle("-fx-text-fill: red;");
            System.out.println(e.getMessage());
        }

        txtScanInput.clear();
        txtScanInput.requestFocus();
    }



}