package org.chemtrovina.cmtmsys.controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.chemtrovina.cmtmsys.dto.HistoryDetailViewDto;
import org.chemtrovina.cmtmsys.model.Invoice;
import org.chemtrovina.cmtmsys.model.InvoiceDetail;
import org.chemtrovina.cmtmsys.model.MOQ;
import org.chemtrovina.cmtmsys.service.base.*;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.chemtrovina.cmtmsys.utils.TableUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ScanController {

    // =========  FXML =========
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<String> cbInvoiceNo1;
    @FXML private ComboBox<String> cbInvoicePN;
    @FXML private TextField txtSapSelect;


    @FXML private TextField txtScanInput;
    @FXML private TextField txtScanCode;
    @FXML private Button btnOnOff, btnKeepGoing, btnCallSuperV, btnSearch, btnClear, btnRefresh, btnScanOddReel;

    @FXML private Text txtScanStatus, txtScanResultTitle;
    @FXML private Pane paneScanResult;

    @FXML private TableView<Invoice> tblInvoiceList;
    @FXML private TableColumn<Invoice, LocalDate> colDate;
    @FXML private TableColumn<Invoice, String> colInvoiceNo;

    @FXML private TableView<HistoryDetailViewDto> tblScanDetails;
    @FXML private TableColumn<HistoryDetailViewDto, String> colSapCode, colMakerCode, colStatus;
    @FXML private TableColumn<HistoryDetailViewDto, Integer> colMOQ, colQty, colQtyScanned, colReelQty;

    // =========  SERVICE =========
    private final InvoiceService invoiceService;
    private final MOQService moqService;
    private final HistoryService historyService;
    private final InvoiceDetailService invoiceDetailService;

    public ScanController(InvoiceService invoiceService, MOQService moqService,
                          HistoryService historyService, InvoiceDetailService invoiceDetailService) {
        this.invoiceService = invoiceService;
        this.moqService = moqService;
        this.historyService = historyService;
        this.invoiceDetailService = invoiceDetailService;
    }

    // =========  STATE =========
    private boolean isScanEnabled = false;
    private String currentScanId;
    private Invoice selectedInvoice;
    private PauseTransition idleTimer;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ObservableList<HistoryDetailViewDto> detailList = FXCollections.observableArrayList();

    // =========  INITIALIZE =========
    @FXML
    public void initialize() {
        setupTableColumns();
        setupButtons();
        setupShortcuts();
        setupInvoiceCombos();
        setupScanHandlers();
        startIdleTimer();
        startAutoGC();

        setupAutoCompleteSapField();   // ✅ thêm dòng này
        setupInvoiceTableClick();

        FxClipboardUtils.enableCopyShortcut(tblInvoiceList);
        FxClipboardUtils.enableCopyShortcut(tblScanDetails);
        /*TableUtils.centerAlignAllColumns(tblScanDetails);
        TableUtils.centerAlignAllColumns(tblInvoiceList);*/
    }

    // =========  TABLE & BUTTON SETUP =========
    private void setupTableColumns() {
        // invoice list
        colDate.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
        colInvoiceNo.setCellValueFactory(new PropertyValueFactory<>("invoiceNo"));

        // scan details
        colSapCode.setCellValueFactory(new PropertyValueFactory<>("sapCode"));
        colMakerCode.setCellValueFactory(new PropertyValueFactory<>("makerCode"));
        colMOQ.setCellValueFactory(new PropertyValueFactory<>("moq"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));             // required
        colQtyScanned.setCellValueFactory(new PropertyValueFactory<>("qtyScanned"));// scanned
        colReelQty.setCellValueFactory(new PropertyValueFactory<>("reelQty"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        tblScanDetails.setItems(detailList);
        //tblScanDetails.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // màu cột trạng thái
        colStatus.setCellFactory(col -> new TableCell<HistoryDetailViewDto, String>() {
            private final Label label = new Label();

            {
                label.setMaxWidth(Double.MAX_VALUE);
                label.setAlignment(javafx.geometry.Pos.CENTER);
                label.setStyle("-fx-font-weight:bold; -fx-padding:3 5; -fx-background-radius:4;");
                setGraphic(label);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    label.setText("");
                    label.setStyle("-fx-background-color:transparent;");
                    return;
                }

                label.setText(item);

                String bgColor;
                switch (item) {
                    case "O" -> bgColor = "#4CAF50";   // Xanh lá
                    case "X" -> bgColor = "#d01029";   // Đỏ
                    case "Z" -> bgColor = "orange";    // Cam
                    case "Over" -> bgColor = "#ff3b3b"; // Đỏ tươi
                    default -> bgColor = "#b0b0b0";     // Xám
                }

                // ⚡ ép màu nền và chữ (theme không override được vì Label nằm trong cell)
                label.setStyle(String.format(
                        "-fx-background-color:%s; -fx-text-fill:white; -fx-font-weight:bold; " +
                                "-fx-padding:3 5; -fx-background-radius:4; -fx-border-color:#1e3a5f; -fx-border-width:0.3;",
                        bgColor
                ));
            }
        });



    }

    // ✅ Auto-complete cho ô nhập SAP code
    private void setupAutoCompleteSapField() {
        List<String> allSapCodes = invoiceDetailService.getAllSapPNs(); // cần thêm hàm này trong service
        org.chemtrovina.cmtmsys.utils.AutoCompleteUtils.setupAutoComplete(txtSapSelect, allSapCodes);
    }

    // ✅ Click chọn invoice trong bảng -> load details
    private void setupInvoiceTableClick() {
        tblInvoiceList.setRowFactory(tv -> {
            TableRow<Invoice> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    selectedInvoice = row.getItem();
                    dpDate.setValue(selectedInvoice.getInvoiceDate());
                    cbInvoiceNo1.setValue(selectedInvoice.getInvoiceNo());
                    cbInvoicePN.setValue(selectedInvoice.getInvoicePN());
                    loadSapListForInvoice(selectedInvoice);
                }
            });
            return row;
        });
    }


    private void setupButtons() {
        btnKeepGoing.setDisable(true);
        btnCallSuperV.setDisable(true);
        btnScanOddReel.setDisable(true);

        btnSearch.setOnAction(e -> searchInvoices());
        btnClear.setOnAction(e -> clearAll());
        btnRefresh.setOnAction(e -> refreshDetails());
        btnOnOff.setOnAction(e -> toggleScanMode());

        btnCallSuperV.setOnAction(e -> onCallSupervisor());
        btnKeepGoing.setOnAction(e -> onKeepGoing());

    }

    // Supervisor xác nhận mã NG hoặc Over
    private void onCallSupervisor() {
        String currentStatus = txtScanStatus.getText();

        if (!"Over".equalsIgnoreCase(currentStatus) && !"Z".equalsIgnoreCase(currentStatus)) {
            showAlert("No Issue", "This item doesn't require supervisor approval.");
            return;
        }

        if (selectedInvoice != null && txtSapSelect.getText() != null && !txtSapSelect.getText().isBlank()) {
            String sapCode = txtSapSelect.getText().trim();

            if ("Over".equalsIgnoreCase(currentStatus)) {
                // ✅ Nếu Over thì rollback bản ghi cuối cùng đúng SAP
                historyService.deleteLastBySapPNAndInvoiceId(sapCode, selectedInvoice.getId());
                System.out.println("[SUPERVISOR] Rollback last OVER scan for " + sapCode);
            } else if ("Z".equalsIgnoreCase(currentStatus)) {
                // ⚠️ Nếu Z thì không rollback (bản ghi sai SAP đã log riêng)
                System.out.println("[SUPERVISOR] Z status → no rollback (record kept for trace)");
            }
        }

        // ✅ Reset giao diện
        txtScanStatus.setText("Supervisor OK");
        paneScanResult.setStyle("-fx-background-color:#0099cc;");
        btnCallSuperV.setDisable(true);
        btnKeepGoing.setDisable(true);

        // ✅ Refresh lại bảng hiển thị
        if (selectedInvoice != null)
            loadSapListForInvoice(selectedInvoice);
    }


    // Cho phép tiếp tục quét (bỏ qua lỗi)
    private void onKeepGoing() {
        txtScanStatus.setText("Continue");
        paneScanResult.setStyle("-fx-background-color:#4CAF50;");
        btnCallSuperV.setDisable(true);
        btnKeepGoing.setDisable(true);
        txtScanCode.requestFocus();
    }


    private void setupShortcuts() {
        tblScanDetails.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.C)
                FxClipboardUtils.copySelectionToClipboard(tblScanDetails);
        });
    }

    // =========  COMBO SETUP =========
    private void setupInvoiceCombos() {
        loadInvoiceNos();
        loadInvoicePNs();

        cbInvoiceNo1.setOnAction(e -> {
            String no = cbInvoiceNo1.getValue();
            if (no != null) {
                selectedInvoice = invoiceService.getInvoiceByInvoiceNo(no);
                if (selectedInvoice != null) {
                    dpDate.setValue(selectedInvoice.getInvoiceDate());
                    cbInvoicePN.setValue(selectedInvoice.getInvoicePN());
                    loadSapListForInvoice(selectedInvoice);
                }
            }
        });

        cbInvoicePN.setOnAction(e -> {
            String pn = cbInvoicePN.getValue();
            if (pn != null) {
                Invoice inv = invoiceService.getInvoicesByInvoicePN(pn);
                if (inv != null) {
                    cbInvoiceNo1.setValue(inv.getInvoiceNo());
                    dpDate.setValue(inv.getInvoiceDate());
                    selectedInvoice = inv;
                    loadSapListForInvoice(inv);
                }
            }
        });

        txtSapSelect.setOnAction(e -> txtScanCode.requestFocus());
    }

    private void loadInvoiceNos() {
        List<String> list = invoiceService.getAllInvoiceNos();
        cbInvoiceNo1.setItems(FXCollections.observableArrayList(list));
    }

    private void loadInvoicePNs() {
        List<String> list = invoiceService.getAllInvoicePNs();
        cbInvoicePN.setItems(FXCollections.observableArrayList(list));
    }

    private void loadSapListForInvoice(Invoice invoice) {
        List<InvoiceDetail> details = invoiceService.getInvoiceDetails(invoice.getInvoiceNo());
        detailList.clear();
        List<String> sapCodes = new ArrayList<>();

        for (InvoiceDetail d : details) {
            HistoryDetailViewDto dto = new HistoryDetailViewDto();
            dto.setSapCode(d.getSapPN());
            dto.setQty(d.getQuantity());

            // ✅ Lấy số lượng đã quét
            int scanned = historyService.getTotalScannedQuantityBySapPN(d.getSapPN(), invoice.getId());
            dto.setQtyScanned(scanned);

            dto.setMoq(d.getMoq());

            // ✅ Tính reel qty = qtyScanned / moq
            int reel = scanned / Math.max(1, d.getMoq());
            dto.setReelQty(reel);

            // ✅ Tự xác định trạng thái ban đầu
            if (scanned == 0) {
                dto.setStatus("X"); // chưa quét
            } else if (scanned < d.getQuantity()) {
                dto.setStatus("X"); // thiếu
            } else if (scanned == d.getQuantity()) {
                dto.setStatus("O"); // đủ
            } else {
                dto.setStatus("Over"); // dư
            }

            detailList.add(dto);
            sapCodes.add(d.getSapPN());
        }


        // ✅ Refresh bảng để hiển thị ngay màu
        tblScanDetails.refresh();

    }


    // =========  SCAN HANDLERS =========
    private void setupScanHandlers() {
        txtScanInput.textProperty().addListener((o, old, val) ->
                btnOnOff.setDisable(val == null || val.isBlank()));

        // ✅ Enter để bật chế độ scan
        txtScanInput.setOnAction(e -> {
            if (!isScanEnabled) {
                String opId = txtScanInput.getText().trim();
                if (opId.isEmpty()) {
                    showAlert("Missing Operator ID", "Please enter your Operator ID before scanning.");
                    return;
                }
                currentScanId = opId;
                isScanEnabled = true;
                btnOnOff.setText("Off");
                txtScanCode.requestFocus();
                showResult("READY", "#2196F3", "Scan mode is now active.");
            }
        });


        txtScanCode.setOnAction(e -> {
            if (!isScanEnabled) {
                showAlert("Scan Disabled", "Please turn ON scan mode first.");
                return;
            }
            if (selectedInvoice == null) {
                showAlert("No Invoice Selected", "Please select an invoice first.");
                return;
            }
            String sap = txtSapSelect.getText().trim();
            if (sap.isEmpty()) {
                showAlert("No SAP Entered", "Please type or select a SAP code before scanning MakerPN.");
                return;
            }

            String makerPN = txtScanCode.getText().trim();
            if (!makerPN.isEmpty()) {
                handleScan(makerPN, sap);
                txtScanCode.clear();
            }
        });
    }

    // =========  IDLE TIMER & GC =========
    private void startIdleTimer() {
        idleTimer = new PauseTransition(Duration.minutes(30));
        idleTimer.setOnFinished(e -> disableScan());
        idleTimer.playFromStart();
        txtScanInput.setOnKeyTyped(e -> resetIdle());
        txtScanCode.setOnKeyTyped(e -> resetIdle());
    }

    private void disableScan() {
        isScanEnabled = false;
        btnOnOff.setText("On");
        txtScanInput.clear();
        txtScanCode.clear();
        showAlert("Idle Timeout", "Scan disabled due to inactivity.");
    }

    private void resetIdle() {
        idleTimer.stop();
        idleTimer.playFromStart();
    }

    private void startAutoGC() {
        scheduler.scheduleAtFixedRate(() -> {
            System.gc();
            System.out.println("GC @ " + java.time.LocalTime.now());
        }, 20, 20, TimeUnit.SECONDS);
    }

    // =========  COMMON UI OPS =========
    private void toggleScanMode() {
        isScanEnabled = !isScanEnabled;
        btnOnOff.setText(isScanEnabled ? "Off" : "On");
        if (isScanEnabled)
            currentScanId = txtScanInput.getText().trim();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void clearAll() {
        cbInvoiceNo1.setValue(null);
        cbInvoicePN.setValue(null);
        txtSapSelect.clear();
        detailList.clear();
        selectedInvoice = null;
        txtScanStatus.setText("None");
        paneScanResult.setStyle("-fx-background-color: lightgray;");
    }

    private void searchInvoices() {
        List<Invoice> result = invoiceService.findAll();
        tblInvoiceList.setItems(FXCollections.observableArrayList(result));
    }

    private void refreshDetails() {
        if (selectedInvoice != null)
            loadSapListForInvoice(selectedInvoice);
    }

    // =========  SCAN LOGIC =========
    private void handleScan(String makerPN, String selectedSap) {
        String extractedMakerPN = historyService.extractRealMakerPN(makerPN);

        // ⚠️ 1️⃣ Không detect được MakerPN
        if (extractedMakerPN == null) {
            MOQ dummy = new MOQ();
            dummy.setMakerPN(makerPN);
            dummy.setSapPN(selectedSap);
            dummy.setMoq(0);
            historyService.createHistoryForScannedMakePN(dummy, currentScanId, "NG_NO_DETECT", selectedInvoice.getId());

            showResult("NG", "#d01029", "Cannot detect MakerPN!");
            markStatus(selectedSap, "Z");
            btnCallSuperV.setDisable(false);
            btnKeepGoing.setDisable(true);
            return;
        }

        // ⚠️ 2️⃣ MakerPN không có trong bảng MOQ
        List<MOQ> moqList = moqService.getAllMOQsByMakerPN(extractedMakerPN);
        if (moqList == null || moqList.isEmpty()) {
            MOQ dummy = new MOQ();
            dummy.setMakerPN(extractedMakerPN);
            dummy.setSapPN(selectedSap);
            dummy.setMoq(0);
            historyService.createHistoryForScannedMakePN(dummy, currentScanId, "NG_NOT_IN_MOQ", selectedInvoice.getId());

            showResult("NG", "#d01029", "MakerPN not found in MOQ table!");
            markStatus(selectedSap, "Z");
            btnCallSuperV.setDisable(false);
            btnKeepGoing.setDisable(true);
            return;
        }

        // ⚠️ 3️⃣ MakerPN có trong MOQ nhưng sai SAP
        MOQ matchedMOQ = moqList.stream()
                .filter(m -> m.getSapPN().equalsIgnoreCase(selectedSap))
                .findFirst()
                .orElse(null);

        if (matchedMOQ == null) {
            showResult("Z", "#CAAA12", "MakerPN not belong to selected SAP!");
            markStatus(selectedSap, "Z");
            btnCallSuperV.setDisable(false);
            btnKeepGoing.setDisable(true);
            return;
        }

        // ✅ 4️⃣ Hợp lệ → ghi bình thường
        historyService.createHistoryForScannedMakePN(matchedMOQ, currentScanId, "Scan Code", selectedInvoice.getId());

        // Cập nhật lại bảng hiển thị
        updateScannedQuantity(selectedSap, matchedMOQ.getMoq());

        // Lấy dữ liệu hiện tại sau update để xác định over
        for (HistoryDetailViewDto dto : detailList) {
            if (dto.getSapCode().equalsIgnoreCase(selectedSap)) {
                int required = dto.getQty();
                int scanned = dto.getQtyScanned();

                if (scanned > required) {
                    // ⚠️ Dư số lượng
                    dto.setStatus("Over");
                    tblScanDetails.refresh();
                    showResult("Over", "#ff3b3b",
                            String.format("Over quantity! (%d/%d)", scanned, required));
                    btnCallSuperV.setDisable(false);
                    btnKeepGoing.setDisable(true);
                    return;
                } else {
                    // ✅ OK — quét đúng
                    evaluateStatus(selectedSap, matchedMOQ.getMoq());
                    showResult("O", "#4CAF50",
                            String.format("Scan OK (%d/%d)", scanned, required));
                    btnCallSuperV.setDisable(true);
                    btnKeepGoing.setDisable(true);
                    return;
                }
            }
        }
    }


    // =========  TABLE & STATUS UPDATE =========
    private void updateScannedQuantity(String sapCode, int addQty) {
        for (HistoryDetailViewDto dto : detailList) {
            if (dto.getSapCode().equalsIgnoreCase(sapCode)) {
                int newScanned = dto.getQtyScanned() + addQty;
                dto.setQtyScanned(newScanned);
                dto.setReelQty(newScanned / Math.max(1, dto.getMoq()));
                tblScanDetails.refresh();
                return;
            }
        }
    }

    private void evaluateStatus(String sapCode, int moqValue) {
        for (HistoryDetailViewDto dto : detailList) {
            if (!dto.getSapCode().equalsIgnoreCase(sapCode)) continue;

            int required = dto.getQty();
            int scanned = dto.getQtyScanned();

            // ✅ Cập nhật lại Reel Qty
            int reel = scanned / Math.max(1, dto.getMoq());
            dto.setReelQty(reel);

            String status;
            String color;
            String label;

            if (scanned > required) {
                status = "Over";
                color = "#ff3b3b";
                label = String.format("Over quantity! (%d/%d)", scanned, required);
            }
            else if (scanned == required) {
                status = "O";
                color = "#4CAF50";
                label = String.format("Enough quantity (%d/%d)", scanned, required);
            }
            else if (scanned > 0) {
                status = "X";
                color = "#d01029";
                label = String.format("Scanned %d/%d (not enough yet)", scanned, required);
            }
            else {
                status = "X";
                color = "#999999";
                label = "Not scanned";
            }

            dto.setStatus(status);
            tblScanDetails.refresh();
            showResult(status, color, label);
            return;
        }
    }




    private void markStatus(String sapCode, String status) {
        for (HistoryDetailViewDto dto : detailList) {
            if (dto.getSapCode().equalsIgnoreCase(sapCode)) {
                dto.setStatus(status);
                tblScanDetails.refresh();
                break;
            }
        }
    }

    // =========  RESULT UI =========
    private void showResult(String text, String color, String message) {
        txtScanStatus.setText(text);
        paneScanResult.setStyle("-fx-background-color:" + color + "; -fx-background-radius:10;");
        System.out.println("[SCAN RESULT] " + message);

        // ✅ bật/tắt nút dựa theo kết quả
        if ("Over".equalsIgnoreCase(text) || "Z".equalsIgnoreCase(text)) {
            btnCallSuperV.setDisable(false);
            btnKeepGoing.setDisable(true);
        } else {
            btnCallSuperV.setDisable(true);
            btnKeepGoing.setDisable(true);
        }
    }


    // =========  CLEANUP =========
    public void shutdown() {
        scheduler.shutdown();
        if (idleTimer != null) idleTimer.stop();
    }
}



