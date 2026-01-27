package org.chemtrovina.cmtmsys.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.chemtrovina.cmtmsys.dto.AttendanceSummaryDto;
import org.chemtrovina.cmtmsys.model.Employee;
import org.chemtrovina.cmtmsys.model.ShiftPlanEmployee;
import org.chemtrovina.cmtmsys.model.ShiftTypeEmployee;
import org.chemtrovina.cmtmsys.model.enums.ScanAction;
import org.chemtrovina.cmtmsys.model.enums.UserRole;
import org.chemtrovina.cmtmsys.security.RequiresRoles;
import org.chemtrovina.cmtmsys.service.base.EmployeeService;
import org.chemtrovina.cmtmsys.service.base.ShiftPlanEmployeeService;
import org.chemtrovina.cmtmsys.service.base.ShiftTypeEmployeeService;
import org.chemtrovina.cmtmsys.service.base.TimeAttendanceLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RequiresRoles({
        UserRole.ADMIN,
        UserRole.EMPLOYEE,
        UserRole.EMPLOYEE_MINI
})

@Component
public class AttendanceScanController {

    // =========================
    // FXML
    // =========================
    @FXML private Label lblCurrentShift;
    @FXML private Label lblClock;

    @FXML private StackPane scanBox;
    @FXML private TextField txtScanInput;
    @FXML private Label lblScanStatus;

    @FXML private Label lblDayPresent;
    @FXML private Label lblDayAbsent;
    @FXML private Label lblNightPresent;
    @FXML private Label lblNightAbsent;

    // Nếu bạn muốn hiển thị "CHƯA CÓ CA" trên UI -> thêm fx:id này trong FXML
    @FXML private Label lblNaPresent;
    @FXML private Label lblNaAbsent;

    @FXML private Button btnViewAbsent;

    @FXML private Label lblLastResult;

    // =========================
    // SERVICES (DI)
    // =========================
    private final TimeAttendanceLogService logService;
    private final EmployeeService employeeService;
    private final ShiftPlanEmployeeService shiftPlanEmployeeService;
    private final ShiftTypeEmployeeService shiftTypeEmployeeService;

    @Autowired
    public AttendanceScanController(TimeAttendanceLogService logService,
                                    EmployeeService employeeService,
                                    ShiftPlanEmployeeService shiftPlanEmployeeService,
                                    ShiftTypeEmployeeService shiftTypeEmployeeService) {
        this.logService = logService;
        this.employeeService = employeeService;
        this.shiftPlanEmployeeService = shiftPlanEmployeeService;
        this.shiftTypeEmployeeService = shiftTypeEmployeeService;
    }

    // =========================
    // STATE
    // =========================
    private final DateTimeFormatter CLOCK_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final DateTimeFormatter TIME_FMT  = DateTimeFormatter.ofPattern("HH:mm");

    private final LocalDate today = LocalDate.now();

    // Popup auto close
    private Stage toastStage;
    private Timeline toastTimer;

    // =========================
    // INIT
    // =========================
    @FXML
    public void initialize() {

        // 1) Clock realtime
        startClock();

        // 2) Auto focus scan input
        Platform.runLater(() -> {
            requestScanFocus();
            refreshSummary(today);
            updateCurrentShiftBanner();
        });

        // 3) Scan by ENTER
        txtScanInput.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleScan();
                e.consume();
            }
        });

        // 4) Button open tab absent
        btnViewAbsent.setOnAction(e -> openAbsentTab());

        // 5) Nếu click vào vùng scanBox cũng focus
        scanBox.setOnMouseClicked(e -> requestScanFocus());
    }

    // =========================
    // CLOCK
    // =========================
    private void startClock() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> {
                    lblClock.setText(LocalTime.now().format(CLOCK_FMT));
                }),
                new KeyFrame(Duration.seconds(1))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    // =========================
    // SCAN HANDLER
    // =========================
    private void handleScan() {
        String raw = txtScanInput.getText();
        String input = raw == null ? "" : raw.trim();

        if (input.isBlank()) {
            requestScanFocus();
            return;
        }

        try {
            // 1) Find employee (support MSCNID1 or MSCNID2)
            Employee emp = findEmployeeByCard(input);
            if (emp == null) {
                setScanStateRed("❌ Không tồn tại mã: " + input);
                showToast("Không tìm thấy nhân viên", "Mã: " + input, "❌ Không tồn tại", 3.5);
                setLastResult("N/A", "N/A", "❌ Không tồn tại", LocalTime.now());
                clearAndFocus();
                return;
            }

            // 2) ✅ AUTO scan: tự quyết định IN/OUT theo ca + ngày
            // YÊU CẦU: bạn phải implement method này trong service
            var dto = logService.processScanAuto(input);

            // 3) workDate (rất quan trọng cho ca đêm OUT)
            LocalDate workDate = dto.getScanDate() != null ? dto.getScanDate() : LocalDate.now();

            // 4) Lấy ca theo workDate
            ShiftPlanEmployee plan = getTodayPlan(emp.getEmployeeId(), workDate);

            ShiftTypeEmployee shiftType = null;
            if (plan != null && plan.getShiftCode() != null && !plan.getShiftCode().isBlank()) {
                shiftType = shiftTypeEmployeeService.getByCode(plan.getShiftCode());
            }

            // 5) Xác định action vừa scan là IN hay OUT
            ScanAction action = (dto.getIn() != null && !dto.getIn().isBlank())
                    ? ScanAction.IN
                    : ScanAction.OUT;

            // 6) Tính trạng thái hiển thị
            LocalTime now = LocalTime.now();
            ScanUiStatus status = evaluateStatus(shiftType, now, action);

            // 7) Update UI
            applyUiStatus(status);

            String shiftLabel = shiftType != null
                    ? (shiftType.getShiftName() + " (" + fmtTime(shiftType.getStartTime()) + " – " + fmtTime(shiftType.getEndTime()) + ")")
                    : "CHƯA CÓ CA";

            lblScanStatus.setText(status.badge + " " + status.message);

            lblLastResult.setText(
                    emp.getFullName()
                            + " | " + shiftLabel
                            + " | " + (action == ScanAction.IN ? "IN" : "OUT")
                            + " | " + status.message
                            + " | " + now.format(TIME_FMT)
            );

            showToast(
                    emp.getFullName(),
                    shiftLabel + " | " + (action == ScanAction.IN ? "IN" : "OUT"),
                    status.badge + " " + status.message,
                    3.5
            );

            // 8) Refresh summary theo workDate (đúng cho ca đêm OUT)
            refreshSummary(workDate);

        } catch (Exception ex) {

            setScanStateRed("❌ " + ex.getMessage());
            showToast("Lỗi scan", "", "❌ " + ex.getMessage(), 4.0);
            setLastResult("N/A", "N/A", "❌ " + ex.getMessage(), LocalTime.now());

        } finally {
            clearAndFocus();
            refreshSummary(LocalDate.now());
        }
    }

    private Employee findEmployeeByCard(String input) {
        // ưu tiên service đang có
        Employee emp = employeeService.getByMscnId1(input);
        if (emp != null) return emp;
        return employeeService.getByMscnId2(input);
    }

    private ShiftPlanEmployee getTodayPlan(int employeeId, LocalDate date) {
        List<ShiftPlanEmployee> plans = shiftPlanEmployeeService.findByShiftDate(date);
        if (plans == null || plans.isEmpty()) return null;

        return plans.stream()
                .filter(p -> p.getEmployeeId() == employeeId)
                .max(Comparator.comparing(ShiftPlanEmployee::getShiftPlanId)) // nếu có trùng, lấy bản mới nhất
                .orElse(null);
    }

    // =========================
    // STATUS EVALUATION
    // =========================
    private static class ScanUiStatus {
        final String badge;   // ✅ / ⚠️ / ❌
        final String message; // text hiển thị
        final String colorHex; // background scan box
        ScanUiStatus(String badge, String message, String colorHex) {
            this.badge = badge;
            this.message = message;
            this.colorHex = colorHex;
        }
    }

    private ScanUiStatus evaluateStatus(ShiftTypeEmployee shift, LocalTime scanTime, ScanAction action) {

        if (shift == null) {
            return new ScanUiStatus("❌", "Chưa có ca / cần HR xử lý", "#e12c2c");
        }

        LocalTime start = shift.getStartTime();
        LocalTime end = shift.getEndTime();

        if (start == null || end == null) {
            return new ScanUiStatus("❌", "Ca thiếu giờ / cần HR xử lý", "#e12c2c");
        }

        // ===== IN =====
        if (action == ScanAction.IN) {
            if (scanTime.isAfter(start)) {
                long lateMinutes = java.time.Duration.between(start, scanTime).toMinutes();
                return new ScanUiStatus("⚠️", "Đi trễ " + lateMinutes + " phút", "#f2c94c");
            }
            return new ScanUiStatus("✅", "IN đúng giờ", "#54ed54");
        }

        // ===== OUT =====
        // Ca thường: OUT trước end => về sớm
        // Ca đêm: logic OUT chỉ check time (vì OUT xảy ra sau 00:00)
        boolean overnight = Boolean.TRUE.equals(shift.getIsOvernight());

        if (!overnight) {
            if (scanTime.isBefore(end)) {
                long early = java.time.Duration.between(scanTime, end).toMinutes();
                return new ScanUiStatus("⚠️", "Về sớm " + early + " phút", "#f2c94c");
            }
            return new ScanUiStatus("✅", "OUT đúng giờ", "#54ed54");
        } else {
            // ca đêm: chỉ so time với end (vd 04:00)
            if (scanTime.isBefore(end)) {
                // 01:00 < 04:00 => OK
                return new ScanUiStatus("✅", "OUT ca đêm OK", "#54ed54");
            }
            // 06:00 > 04:00 => coi như OT hoặc “quá giờ” (tuỳ nghiệp vụ)
            return new ScanUiStatus("✅", "OUT (sau giờ kết thúc)", "#54ed54");
        }
    }

    private void applyUiStatus(ScanUiStatus s) {
        // đổi nền scanBox theo trạng thái
        scanBox.setStyle(
                "-fx-background-radius: 14;" +
                        "-fx-padding: 10;" +
                        "-fx-background-color: " + s.colorHex + ";"
        );
    }

    private void setScanStateRed(String msg) {
        scanBox.setStyle(
                "-fx-background-radius: 14;" +
                        "-fx-padding: 10;" +
                        "-fx-background-color: #e12c2c;"
        );
        lblScanStatus.setText(msg);
    }

    // =========================
    // SUMMARY
    // =========================
    private void refreshSummary(LocalDate date) {
        AttendanceSummaryDto s = logService.getAttendanceSummary(date);

        lblDayPresent.setText(String.valueOf(s.getDayPresent()));
        lblDayAbsent.setText(String.valueOf(s.getDayAbsent()));

        lblNightPresent.setText(String.valueOf(s.getNightPresent()));
        lblNightAbsent.setText(String.valueOf(s.getNightAbsent()));

        // Nếu bạn thêm card "CHƯA CÓ CA" thì controller tự set
        if (lblNaPresent != null) lblNaPresent.setText(String.valueOf(s.getNaPresent()));
        if (lblNaAbsent != null)  lblNaAbsent.setText(String.valueOf(s.getNaAbsent()));
    }

    // banner trên top: chỉ hiển thị giờ hiện tại + text chung
    private void updateCurrentShiftBanner() {
        // Nếu bạn muốn chính xác theo “shift hiện tại” của kiosk, cần mapping giờ->shift.
        // Tạm hiển thị theo thời gian hiện tại.
        LocalTime now = LocalTime.now();
        String label = "CA HIỆN TẠI: " + (now.isBefore(LocalTime.NOON) ? "CA NGÀY" : "CA ĐÊM");
        lblCurrentShift.setText(label);
    }

    // =========================
    // OPEN TAB ABSENT
    // =========================
    private void openAbsentTab() {
        try {
            // Bạn sửa đúng path fxml tab DS vắng của bạn
            MainController.getInstance().openTab(
                    "DS Người Vắng",
                    "/org/chemtrovina/cmtmsys/view/AbsentListView.fxml"
            );
        } catch (Exception e) {
            // fallback: show error
            showToast("Không mở được tab", "", "❌ " + e.getMessage(), 4.0);
        }
    }

    // =========================
    // TOAST (POPUP AUTO CLOSE)
    // =========================
    private void showToast(String line1, String line2, String line3, double seconds) {
        Platform.runLater(() -> {
            if (toastStage == null) {
                toastStage = new Stage(StageStyle.UNDECORATED);
                toastStage.initModality(Modality.NONE);
                toastStage.setAlwaysOnTop(true);
            }

            Label l1 = new Label(line1);
            Label l2 = new Label(line2);
            Label l3 = new Label(line3);

            l1.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
            l2.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
            l3.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

            javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(6, l1, l2, l3);
            root.setStyle(
                    "-fx-background-color: rgba(0,0,0,0.86);" +
                            "-fx-background-radius: 14;" +
                            "-fx-padding: 14;"
            );

            Scene scene = new Scene(root);
            scene.setFill(null);
            toastStage.setScene(scene);

            // đặt popup gần giữa màn hình (hoặc cạnh scanBox)
            toastStage.sizeToScene();
            toastStage.centerOnScreen();
            toastStage.show();

            if (toastTimer != null) toastTimer.stop();
            toastTimer = new Timeline(new KeyFrame(Duration.seconds(seconds), e -> toastStage.hide()));
            toastTimer.play();
        });
    }

    // =========================
    // UTIL
    // =========================
    private void requestScanFocus() {
        txtScanInput.requestFocus();
        txtScanInput.selectAll();
    }

    private void clearAndFocus() {
        txtScanInput.clear();
        requestScanFocus();
    }

    private void setLastResult(String fullName, String shift, String status, LocalTime time) {
        lblLastResult.setText(fullName + " | " + shift + " | " + status + " | " + time.format(TIME_FMT));
    }

    private String fmtTime(LocalTime t) {
        return t == null ? "N/A" : t.format(TIME_FMT);
    }
}
