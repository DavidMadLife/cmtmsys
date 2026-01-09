package org.chemtrovina.cmtmsys.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.chemtrovina.cmtmsys.dto.EmployeeScanViewDto;
import org.chemtrovina.cmtmsys.model.enums.ScanAction;
import org.chemtrovina.cmtmsys.service.base.TimeAttendanceLogService;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmployeeAttendanceController {

    @Autowired
    private TimeAttendanceLogService logService;

    @FXML private TextField txtScanInput;
    @FXML private Label lblScanType;
    @FXML private Label lblStatus;
    @FXML private TableView<EmployeeScanViewDto> tblScanned;
    @FXML
    private Label lblDate;

    @FXML private TableColumn<EmployeeScanViewDto, Number> colNo;
    @FXML private TableColumn<EmployeeScanViewDto, String> colCode;
    @FXML private TableColumn<EmployeeScanViewDto, String> colName;
    @FXML private TableColumn<EmployeeScanViewDto, String> colTime;
    @FXML
    private TableColumn<EmployeeScanViewDto, String> colScanType;




    private ScanAction currentAction;

    @FXML
    public void initialize() {
        setupTable();
        setScanType(ScanAction.IN);

        // ENTER để scan
        txtScanInput.setOnAction(e -> handleScan());
        loadTodayScanned();
        txtScanInput.requestFocus();
        updateDate();
        startDateAutoRefresh();
        FxClipboardUtils.enableCopyShortcut(tblScanned);

    }

    private void setupTable() {
        colNo.setCellValueFactory(new PropertyValueFactory<>("no"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("employeeCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("scanTime"));
        colScanType.setCellValueFactory(
                new PropertyValueFactory<>("scanType")
        );

        colScanType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(item);
                setStyle("""
            -fx-font-weight: bold;
            -fx-alignment: CENTER;
        """);

                if ("IN".equalsIgnoreCase(item)) {
                    setStyle(getStyle() + "-fx-text-fill: #2E7D32;");
                } else {
                    setStyle(getStyle() + "-fx-text-fill: #C62828;");
                }
            }
        });

        tblScanned.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case DELETE:
                case BACK_SPACE:
                    deleteSelectedRow();
                    event.consume();
                    break;
            }
        });



    }

    private void deleteSelectedRow() {
        EmployeeScanViewDto selected =
                tblScanned.getSelectionModel().getSelectedItem();

        if (selected == null) {
            lblStatus.setText("⚠ Chưa chọn dòng");
            lblStatus.setStyle("""
            -fx-font-size: 26px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            -fx-background-color: #F57C00;
            -fx-background-radius: 10;
        """);
            return;
        }

        // ===== CONFIRM TRƯỚC KHI XÓA =====
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Bạn có chắc muốn xóa dòng này?");
        confirm.setContentText(
                "Mã NV: " + selected.getEmployeeCode() + "\n" +
                        "Họ tên: " + selected.getFullName() + "\n" +
                        "Loại: " + selected.getScanType() + "\n" +
                        "Giờ: " + selected.getScanTime()
        );

        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            // Người dùng chọn NO / đóng dialog
            txtScanInput.requestFocus();
            return;
        }

        // ===== THỰC SỰ XÓA =====
        try {
            System.out.println(selected.getId());
            logService.delete(selected.getId());

            loadTodayScanned();

            lblStatus.setText("🗑 Đã xóa");
            lblStatus.setStyle("""
            -fx-font-size: 28px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            -fx-background-color: #455A64;
            -fx-background-radius: 10;
        """);

        } catch (Exception e) {
            lblStatus.setText("❌ Không xóa được");
            lblStatus.setStyle("""
            -fx-font-size: 28px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            -fx-background-color: #C62828;
            -fx-background-radius: 10;
        """);
        } finally {
            txtScanInput.requestFocus();
        }
    }

    private void handleScan() {
        String input = txtScanInput.getText().trim();
        if (input.isEmpty()) return;

        try {
            logService.processScan(input, currentAction.name());

            lblStatus.setText("✔ OK");
            lblStatus.setStyle("""
            -fx-font-size: 32px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            -fx-background-color: #2E7D32;
            -fx-background-radius: 10;
        """);

            loadTodayScanned();
        } catch (Exception e) {
            lblStatus.setText("❌ " + e.getMessage());
            lblStatus.setStyle("""
            -fx-font-size: 36px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            -fx-background-color: #C62828;
            -fx-background-radius: 10;
        """);
        }

        txtScanInput.clear();
        txtScanInput.requestFocus();
    }


    private void loadTodayScanned() {
        tblScanned.setItems(
                FXCollections.observableArrayList(
                        logService.getTodayScannedForEmployeeView()
                )
        );
    }

    // ====== Action đổi IN / OUT ======
    @FXML
    private void switchToIn() {
        setScanType(ScanAction.IN);
    }

    @FXML
    private void switchToOut() {
        setScanType(ScanAction.OUT);
    }


    private void updateDate() {
        lblDate.setText(
                java.time.LocalDate.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );
    }

    private void setScanType(ScanAction action) {
        this.currentAction = action;

        if (action == ScanAction.IN) {
            lblScanType.setText("IN");
            lblScanType.setStyle("""
            -fx-font-size: 42px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            -fx-background-color: #2E7D32;
            -fx-background-radius: 20;
        """);
        } else {
            lblScanType.setText("OUT");
            lblScanType.setStyle("""
            -fx-font-size: 42px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            -fx-background-color: #C62828;
            -fx-background-radius: 20;
        """);
        }
    }


    private void startDateAutoRefresh() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.minutes(1),
                        e -> updateDate()
                )
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
    }


}

