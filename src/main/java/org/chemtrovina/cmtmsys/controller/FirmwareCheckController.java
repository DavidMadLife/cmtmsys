package org.chemtrovina.cmtmsys.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.chemtrovina.cmtmsys.dto.FirmwareCheckResultDto;
import org.chemtrovina.cmtmsys.model.FirmwareCheckHistory;
import org.chemtrovina.cmtmsys.model.enums.UserRole;
import org.chemtrovina.cmtmsys.security.RequiresRoles;
import org.chemtrovina.cmtmsys.service.base.FirmwareCheckService;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;




@RequiresRoles({
        UserRole.ADMIN,
        UserRole.INVENTORY,
        UserRole.SUBLEEDER,
        UserRole.EMPLOYEE_MINI,
})

@Component
public class FirmwareCheckController {

    @FXML private TextField inputVersionField;
    @FXML private Button btnCheck;
    @FXML private Label lblPopupVersion;
    @FXML private Label lblResult;
    @FXML private Button btnMockPopup;
    @FXML private ToggleButton btnAuto;
    @FXML private Button btnGet;   // hoặc btnRefresh


    private volatile boolean autoRunning = false;
    private final java.util.Set<Long> handled = java.util.Collections.synchronizedSet(new java.util.HashSet<>());
    private java.util.concurrent.ScheduledExecutorService autoExec;



    @FXML private TableView<FirmwareCheckHistory> historyTable;
    @FXML private TableColumn<FirmwareCheckHistory, Integer> colNo;
    @FXML private TableColumn<FirmwareCheckHistory, String> colInput;
    @FXML private TableColumn<FirmwareCheckHistory, String> colPopup;
    @FXML private TableColumn<FirmwareCheckHistory, String> colResult;
    @FXML private TableColumn<FirmwareCheckHistory, String> colMessage;
    @FXML private TableColumn<FirmwareCheckHistory, Object> colTime;

    private final ObservableList<FirmwareCheckHistory> historyList = FXCollections.observableArrayList();

    private final FirmwareCheckService service;

    @Autowired
    public FirmwareCheckController(FirmwareCheckService service) {
        this.service = service;
    }

    @FXML
    public void initialize() {
        setupTable();
        loadHistory();

        btnCheck.setOnAction(e -> onCheck());

        inputVersionField.setOnAction(e -> onCheck()); // enter để check
        btnMockPopup.setOnAction(e -> showMockExternalPopup());
        btnAuto.setOnAction(e -> {
            /*if (btnAuto.isSelected()) startAutoWatch();
            else stopAutoWatch();*/
        });
        btnGet.setOnAction(e -> onGetLatest());

    }

    private void onGetLatest() {
        loadHistory();
    }




    private void setupTable() {
        colNo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getIndex() >= historyTable.getItems().size()) {
                    setText(null);
                } else {
                    setText(String.valueOf(getTableRow().getIndex() + 1));
                }
            }
        });

        colInput.setCellValueFactory(new PropertyValueFactory<>("inputVersion"));
        colPopup.setCellValueFactory(new PropertyValueFactory<>("popupVersion"));
        colResult.setCellValueFactory(new PropertyValueFactory<>("result"));
        colMessage.setCellValueFactory(new PropertyValueFactory<>("message"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        historyTable.setItems(historyList);
        FxClipboardUtils.enableCopyShortcut(historyTable);
    }

    private void loadHistory() {
        List<FirmwareCheckHistory> latest = service.getLatestHistory(200);
        historyList.setAll(latest);
    }

    private void onCheck() {
        String input = inputVersionField.getText();

        btnCheck.setDisable(true);
        lblResult.setText("Checking...");
        lblPopupVersion.setText("-");

        CompletableFuture
                .supplyAsync(() -> service.checkAndSave(input))
                .whenComplete((res, ex) -> Platform.runLater(() -> {
                    btnCheck.setDisable(false);

                    if (ex != null) {
                        lblResult.setText("ERROR");
                        lblPopupVersion.setText("-");
                        showAlert(Alert.AlertType.ERROR, "Check failed", ex.getMessage());
                        return;
                    }

                    applyResult(res);
                    loadHistory();
                }));
    }

    private void applyResult(FirmwareCheckResultDto res) {
        lblPopupVersion.setText(res.getPopupVersion() == null ? "" : res.getPopupVersion());
        lblResult.setText(res.getResult());

        if ("OK".equals(res.getResult())) {
            showAlert(Alert.AlertType.INFORMATION, "OK", res.getMessage());
        } else if ("NG".equals(res.getResult())) {
            showAlert(Alert.AlertType.ERROR, "NG", res.getMessage());
        } else {
            showAlert(Alert.AlertType.WARNING, res.getResult(), res.getMessage());
        }
    }

    private void showMockExternalPopup() {
        final String version = "24071721"; // hoặc truyền param

        Platform.runLater(() -> {
            Stage stage = new Stage();

            // ✅ title để Python filter window
            stage.setTitle("AitUVCExtTest");

            // optional: luôn nổi
            stage.setAlwaysOnTop(true);

            // nếu muốn giống dialog
            stage.initStyle(StageStyle.UTILITY);
            stage.initModality(Modality.NONE); // external popup, không block app

            // ===== UI =====
            Label lbl = new Label("Firmware Version : " + version);
            lbl.setId("firmwareVersionLabel"); // JavaFX ID (UIA đọc được)

            Button ok = new Button("OK");
            ok.setOnAction(e -> stage.close());

            BorderPane root = new BorderPane();
            root.setPadding(new Insets(15));
            root.setCenter(lbl);
            root.setBottom(ok);
            BorderPane.setAlignment(ok, Pos.CENTER);
            BorderPane.setMargin(ok, new Insets(10, 0, 0, 0));

            Scene scene = new Scene(root, 360, 150);
            stage.setScene(scene);

            // center screen
            stage.centerOnScreen();

            // show
            stage.show();
            stage.toFront();
        });

    }



    private void startAutoWatch() {
        autoRunning = true;
        btnAuto.setText("AUTO: ON");

        autoExec = java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "popup-auto-watch");
            t.setDaemon(true);
            return t;
        });

        autoExec.scheduleAtFixedRate(() -> {
            if (!autoRunning) return;

            // tìm tất cả popup matching title
            List<com.sun.jna.platform.win32.WinDef.HWND> windows =
                    org.chemtrovina.cmtmsys.utils.WindowsPopupFirmwareReader.findFirmwarePopups();


            for (var hwnd : windows) {
                long key = com.sun.jna.Pointer.nativeValue(hwnd.getPointer());

                // debounce: popup này đã xử lý rồi thì skip
                if (handled.contains(key)) continue;

                handled.add(key);

                // tự check (đọc version + lưu history)
                String input = inputVersionField.getText();
                FirmwareCheckResultDto res = service.checkAndSave(input, hwnd);


                Platform.runLater(() -> {
                    applyResult(res);
                    loadHistory();
                });
            }

            // cleanup: tránh set lớn mãi (xóa hwnd đã đóng)
            if (handled.size() > 5000) handled.clear();

        }, 0, 200, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    private void stopAutoWatch() {
        autoRunning = false;
        btnAuto.setText("AUTO: OFF");
        btnAuto.setSelected(false);

        if (autoExec != null) {
            autoExec.shutdownNow();
            autoExec = null;
        }
        handled.clear();
    }



    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}
