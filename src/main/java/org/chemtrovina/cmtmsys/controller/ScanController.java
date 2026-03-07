package org.chemtrovina.cmtmsys.controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.chemtrovina.cmtmsys.dto.HistoryDetailViewDto;
import org.chemtrovina.cmtmsys.dto.HistorySummary;
import org.chemtrovina.cmtmsys.model.Invoice;
import org.chemtrovina.cmtmsys.model.InvoiceDetail;
import org.chemtrovina.cmtmsys.model.MOQ;
import org.chemtrovina.cmtmsys.model.enums.UserRole;
import org.chemtrovina.cmtmsys.security.RequiresRoles;
import org.chemtrovina.cmtmsys.service.base.*;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.chemtrovina.cmtmsys.utils.TableUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RequiresRoles({
        UserRole.ADMIN,
        UserRole.INVENTORY,
        UserRole.SUBLEEDER,
        UserRole.GENERALWAREHOUSE
})

@Component
public class ScanController {

    // =========  FXML =========
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<String> cbInvoiceNo1;
    @FXML private ComboBox<String> cbInvoicePN;
    @FXML private TextField txtSapSelect;


    @FXML private TextField txtScanInput;
    @FXML private TextField txtScanCode;
    @FXML private TextField txtRollCode;
    @FXML private Button btnOnOff, btnKeepGoing, btnCallSuperV, btnSearch, btnClear, btnRefresh, btnScanOddReel;

    @FXML private Text txtScanStatus, txtScanResultTitle;
    @FXML private Pane paneScanResult;

    @FXML private TableView<Invoice> tblInvoiceList;
    @FXML private TableColumn<Invoice, LocalDate> colDate;
    @FXML private TableColumn<Invoice, String> colInvoiceNo;

    @FXML private TableView<HistoryDetailViewDto> tblScanDetails;
    @FXML private TableColumn<HistoryDetailViewDto, String> colSapCode, colMakerCode, colStatus;
    @FXML private TableColumn<HistoryDetailViewDto, Integer> colMOQ, colQty, colQtyScanned, colReelQty;
    @FXML private TableColumn<HistoryDetailViewDto, String> colMaker;
    @FXML private TableColumn<HistoryDetailViewDto, String> colSpec;


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
        colMaker.setCellValueFactory(new PropertyValueFactory<>("maker"));
        colSpec.setCellValueFactory(new PropertyValueFactory<>("spec"));
        txtSapSelect.setDisable(true);
        txtScanCode.setDisable(true);
        btnScanOddReel.setDisable(true);
        txtRollCode.setDisable(true);


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
        btnScanOddReel.setOnAction(e -> showOddReelScanDialog());

    }

    // Supervisor xác nhận mã NG hoặc Over
    private void onCallSupervisor() {
        String currentStatus = txtScanStatus.getText();

/*        if (!"Over".equalsIgnoreCase(currentStatus) && !"Z".equalsIgnoreCase(currentStatus)) {
            showAlert("No Issue", "This item doesn't require supervisor approval.");
            return;
        }*/

        if (selectedInvoice == null) {
            showAlert("No Invoice", "Please select an invoice first.");
            return;
        }

        String sap = txtSapSelect.getText();
        if (sap == null || sap.isBlank()) {
            showAlert("No SAP Code", "Please select a SAP code before approving.");
            return;
        }
        sap = sap.trim();

        // ============================
        //      🛠 SUPERVISOR LOGIC
        // ============================

        if ("Over".equalsIgnoreCase(currentStatus)) {
            // ⚡ Over → xóa bản ghi cuối cùng trong HISTORY
            historyService.deleteLastBySapPNAndInvoiceId(
                    sap,
                    selectedInvoice.getId()
            );
            System.out.println("[SUPERVISOR] Removed last OVER record from history → SAP: " + sap);
        }

        if ("Z".equalsIgnoreCase(currentStatus)) {
            // ⚡ Z → KHÔNG xóa history → chỉ xóa hiển thị view
            for (HistoryDetailViewDto dto : detailList) {
                if (dto.getSapCode().equalsIgnoreCase(sap)) {
                    dto.setStatus("X");  // trở về trạng thái thiếu
                    tblScanDetails.refresh();
                    System.out.println("[SUPERVISOR] Z approved → no history delete");
                    break;
                }
            }
        }

        // ============================
        //      🧹 UPDATE UI
        // ============================

        txtScanStatus.setText("Supervisor OK");
        paneScanResult.setStyle("-fx-background-color:#0099cc; -fx-background-radius:10;");

        btnCallSuperV.setDisable(true);
        btnKeepGoing.setDisable(true);

        // refresh lại bảng và đưa SAP lên đầu
        loadSapListForInvoice(selectedInvoice);
        bringSapToTop(sap);

        Platform.runLater(() -> txtScanCode.requestFocus());
    }


    // Cho phép tiếp tục quét (bỏ qua lỗi)
    private void onKeepGoing() {
        txtScanStatus.setText("Continue");
        paneScanResult.setStyle("-fx-background-color:#4CAF50;");
        btnCallSuperV.setDisable(true);
        btnKeepGoing.setDisable(true);
        txtScanCode.requestFocus();
    }

    private void showOddReelScanDialog() {

        if (selectedInvoice == null) {
            showAlert("No Invoice Selected", "Please select an invoice before scanning odd reel.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Scan Odd Reel");
        dialog.setHeaderText("Scan MakerPN → nhập Quantity");

        ButtonType closeBtn = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeBtn);

        TextField txtRollCode = new TextField();
        txtRollCode.setPromptText("Scan RollCode");

        TextField txtMakerPN = new TextField();
        txtMakerPN.setPromptText("Scan MakerPN");

        TextField txtQty = new TextField();
        txtQty.setPromptText("Quantity");

        GridPane gp = new GridPane();
        gp.setHgap(10);
        gp.setVgap(10);

        gp.add(new Label("RollCode:"), 0, 0);
        gp.add(txtRollCode, 1, 0);

        gp.add(new Label("MakerPN:"), 0, 0);
        gp.add(txtMakerPN, 1, 0);

        gp.add(new Label("Quantity:"), 0, 1);
        gp.add(txtQty, 1, 1);

        dialog.getDialogPane().setContent(gp);

        setupOddReelScanHandlers(dialog, txtRollCode, txtMakerPN, txtQty);

        Platform.runLater(txtMakerPN::requestFocus);

        dialog.showAndWait();
    }
    private void setupOddReelScanHandlers(
            Dialog<?> dialog,
            TextField txtRollCode,
            TextField txtMakerPN,
            TextField txtQty) {

        txtMakerPN.setOnKeyPressed(e -> {

            if (e.getCode() == KeyCode.ENTER) {

                String raw = txtMakerPN.getText();

                if (raw == null || raw.isBlank()) return;

                String makerPN = historyService.extractRealMakerPN(raw);

                if (makerPN == null) {

                    showResult("NG", "#d01029", "Cannot detect MakerPN");

                    txtMakerPN.clear();
                    txtMakerPN.requestFocus();
                    return;
                }

                txtMakerPN.setText(makerPN);

                Platform.runLater(txtQty::requestFocus);
            }
        });

        txtQty.setOnKeyPressed(e -> {

            if (e.getCode() == KeyCode.ENTER) {

                processOddReelInput(
                        txtRollCode.getText(),
                        txtMakerPN.getText(),
                        txtQty.getText()
                );

                dialog.close();
            }
        });

        txtRollCode.setOnKeyPressed(e -> {

            if (e.getCode() == KeyCode.ENTER) {

                if (txtRollCode.getText().isBlank()) return;

                Platform.runLater(txtMakerPN::requestFocus);
            }
        });
    }
    private void processOddReelInput(String rollCode, String rawMakerPN, String qtyStr) {

        if (rawMakerPN == null || rawMakerPN.isBlank()
                || qtyStr == null || qtyStr.isBlank()) {

            showAlert("Missing Data", "MakerPN và Quantity không được để trống.");
            return;
        }

        int qty;

        try {

            qty = Integer.parseInt(qtyStr);

            if (qty <= 0) throw new NumberFormatException();

        } catch (Exception e) {

            showAlert("Invalid Quantity", "Quantity phải là số nguyên dương.");
            return;
        }

        String makerPN = historyService.extractRealMakerPN(rawMakerPN);

        if (makerPN == null) {

            showResult("NG", "#d01029", "Cannot detect MakerPN!");
            return;
        }

        List<MOQ> moqList = moqService.getAllMOQsByMakerPN(makerPN);

        if (moqList == null || moqList.isEmpty()) {

            showResult("NG", "#d01029", "MakerPN not exist in MOQ");
            return;
        }

        MOQ matched = null;

        for (MOQ moq : moqList) {

            InvoiceDetail detail =
                    invoiceDetailService.getInvoiceDetailBySapPNAndInvoiceId(
                            moq.getSapPN(),
                            selectedInvoice.getId()
                    );

            if (detail != null) {

                matched = moq;
                break;
            }
        }

        boolean notExistInInvoice = false;

        if (matched == null) {

            matched = moqList.get(0);
            notExistInInvoice = true;
        }

        saveScanOddReel(matched, qty, notExistInInvoice, rollCode);
    }
    private void saveScanOddReel(MOQ moq, int qty, boolean notExistInInvoice, String rollCode) {

        historyService.createHistoryForScanOddReel(
                moq,
                currentScanId,
                "ODD_REEL",
                rollCode,
                selectedInvoice.getId(),
                qty
        );

        for (HistoryDetailViewDto dto : detailList) {

            if (dto.getSapCode() != null
                    && dto.getSapCode().equalsIgnoreCase(moq.getSapPN())) {

                // 🔹 set makerCode
                dto.setMakerCode(moq.getMakerPN());

                // 🔹 update qty
                int newQty = dto.getQtyScanned() + qty;

                dto.setQtyScanned(newQty);

                // 🔹 tính reelQty
                int moqValue = Math.max(1, dto.getMoq());
                dto.setReelQty(newQty / moqValue);

                if (notExistInInvoice) {

                    dto.setStatus("Z");
                    showResult("Z", "orange", "Odd reel not in invoice");

                } else {

                    if (newQty > dto.getQty()) {

                        dto.setStatus("Over");
                        showResult("Over", "#ff3b3b", "Quantity Over");

                    } else if (newQty == dto.getQty()) {

                        dto.setStatus("O");
                        showResult("OK", "#4CAF50", "Completed");

                    } else {

                        dto.setStatus("X");
                        showResult("Scan", "#0099cc", "Odd reel added");
                    }
                }

                break;
            }
        }

        // 🔹 refresh table sau khi update
        tblScanDetails.refresh();
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

    /*private void loadSapListForInvoice(Invoice invoice) {
        // Lấy chi tiết invoice
        List<InvoiceDetail> details = invoiceService.getInvoiceDetails(invoice.getInvoiceNo());
        detailList.clear();

        // 🔥 Lấy luôn toàn bộ history của invoice này (đã scan trước đó)
        List<HistoryDetailViewDto> historyList =
                historyService.getHistoryDetailsByInvoiceId(invoice.getId());

        for (InvoiceDetail d : details) {

            HistoryDetailViewDto dto = new HistoryDetailViewDto();
            dto.setSapCode(d.getSapPN());
            dto.setQty(d.getQuantity());
            dto.setMoq(d.getMoq());

            // ==========================
            // 🔹 LẤY MAKER / MAKERPN / SPEC TỪ HISTORY (NẾU ĐÃ SCAN)
            // ==========================
            if (historyList != null && !historyList.isEmpty()) {
                historyList.stream()
                        .filter(h -> h.getSapCode() != null
                                && h.getSapCode().equalsIgnoreCase(d.getSapPN()))
                        .findFirst()
                        .ifPresent(h -> {
                            dto.setMakerCode(h.getMakerCode());
                            dto.setMaker(h.getMaker());
                            dto.setSpec(h.getSpec());
                        });
            }

            // Nếu chưa có history nào cho SAP này → để trống
            if (dto.getMakerCode() == null) dto.setMakerCode("");
            if (dto.getMaker() == null) dto.setMaker("");
            if (dto.getSpec() == null) dto.setSpec("");

            // ==========================
            // 🔹 TÍNH QTY SCANNED HIỆN TẠI
            // ==========================
            int scanned = historyService.getTotalScannedQuantityBySapPN(d.getSapPN(), invoice.getId());
            dto.setQtyScanned(scanned);
            dto.setReelQty(scanned / Math.max(1, d.getMoq()));

            // ==========================
            // 🔹 SET STATUS BAN ĐẦU
            // ==========================
            if (scanned == 0) {
                dto.setStatus("X");
            } else if (scanned < d.getQuantity()) {
                dto.setStatus("X");
            } else if (scanned == d.getQuantity()) {
                dto.setStatus("O");
            } else {
                dto.setStatus("Over");
            }

            detailList.add(dto);
        }

        tblScanDetails.setItems(detailList);
        tblScanDetails.refresh();
    }*/

    private void loadSapListForInvoice(Invoice invoice) {

        List<InvoiceDetail> details = invoiceService.getInvoiceDetails(invoice.getInvoiceNo());

        // 🔥 Only 1 query! (tối ưu)
        Map<String, HistorySummary> historyMap =
                historyService.getHistorySummaryByInvoiceId(invoice.getId());

        detailList.clear();

        for (InvoiceDetail d : details) {
            HistoryDetailViewDto dto = new HistoryDetailViewDto();
            dto.setSapCode(d.getSapPN());
            dto.setQty(d.getQuantity());
            dto.setMoq(d.getMoq());

            HistorySummary h = historyMap.get(d.getSapPN());

            // nếu đã scan
            if (h != null) {
                dto.setQtyScanned(h.getTotalScanned());
                dto.setReelQty(h.getTotalScanned() / d.getMoq());
                dto.setMakerCode(h.getMakerPN());
                dto.setMaker(h.getMaker());
                dto.setSpec(h.getSpec());

                if (h.getTotalScanned() == d.getQuantity()) dto.setStatus("O");
                else if (h.getTotalScanned() > d.getQuantity()) dto.setStatus("Over");
                else dto.setStatus("X");
            } else {
                // chưa scan
                dto.setQtyScanned(0);
                dto.setReelQty(0);
                dto.setMakerCode("");
                dto.setMaker("");
                dto.setSpec("");
                dto.setStatus("X");
            }

            detailList.add(dto);
        }

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

                // 🔥 Enable input khi operator hợp lệ
                txtSapSelect.setDisable(false);
                txtScanCode.setDisable(false);
                btnScanOddReel.setDisable(false);
                txtRollCode.setDisable(false);
                txtSapSelect.requestFocus();
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

            String roll = txtRollCode.getText().trim();

            if (roll.isEmpty()) {

                showAlert("Missing RollCode", "Please scan RollCode first.");
                txtRollCode.requestFocus();
                return;
            }


            String makerPN = txtScanCode.getText().trim();
            if (!makerPN.isEmpty()) {
                handleScan(makerPN, sap);
                txtScanCode.clear();
                txtRollCode.clear();
                txtRollCode.requestFocus();
            }
        });

        txtSapSelect.setOnAction(e -> {
            String sap = txtSapSelect.getText().trim();
            bringSapToTop(sap);
            txtRollCode.requestFocus();
        });

        txtRollCode.setOnAction(e -> {

            if (!isScanEnabled) {
                showAlert("Scan Disabled", "Please turn ON scan mode first.");
                return;
            }

            String roll = txtRollCode.getText().trim();

            if (roll.isEmpty()) return;

            boolean duplicated = historyService.isRollCodeDuplicated(roll);

            if (duplicated) {

                showResult("Duplicate", "#d01029", "Roll already scanned!");

                txtRollCode.clear();
                txtRollCode.requestFocus();

                return;
            }

            txtScanCode.requestFocus();
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

        if (isScanEnabled) {
            currentScanId = txtScanInput.getText().trim();

            // 🔥 SCAN MODE ON → enable hết
            txtSapSelect.setDisable(false);
            txtScanCode.setDisable(false);
            btnScanOddReel.setDisable(false);
            txtRollCode.setDisable(false);
            txtScanCode.requestFocus();

        } else {

            // 🔥 SCAN MODE OFF → khóa hết
            txtSapSelect.setDisable(true);
            txtScanCode.setDisable(true);
            btnScanOddReel.setDisable(true);
            txtRollCode.setDisable(true);
        }
    }


    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void clearAll() {
        dpDate.setValue(null);
        cbInvoiceNo1.setValue(null);
        cbInvoicePN.setValue(null);
        txtSapSelect.clear();
        detailList.clear();
        selectedInvoice = null;
        txtSapSelect.setDisable(true);
        txtScanCode.setDisable(true);
        btnScanOddReel.setDisable(true);
        txtScanStatus.setText("None");
        tblInvoiceList.setItems(null);
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
        String rollCode = txtRollCode.getText().trim();

        if (rollCode.isEmpty()) {
            showAlert("Missing Roll Code", "Please scan RollCode first.");
            return;
        }

        String extractedMakerPN = historyService.extractRealMakerPN(makerPN);

        // ⚠️ 1️⃣ Không detect được MakerPN
        if (extractedMakerPN == null) {
            MOQ dummy = new MOQ();
            dummy.setMakerPN(makerPN);
            dummy.setSapPN(selectedSap);
            dummy.setMoq(0);
            historyService.createHistoryForScannedMakePN(dummy, currentScanId, "NG_NO_DETECT", rollCode, selectedInvoice.getId(), "NG");

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
            historyService.createHistoryForScannedMakePN(dummy, currentScanId, "NG_NOT_IN_MOQ",rollCode, selectedInvoice.getId(), "NG");

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

            // ✅ Lưu history NG (audit) - sai SAP
            MOQ dummy = new MOQ();
            dummy.setMakerPN(extractedMakerPN);   // makerPN đã extract
            dummy.setSapPN(selectedSap);          // SAP người dùng đang chọn
            dummy.setMoq(0);                      // không cộng qty

            historyService.createHistoryForScannedMakePN(
                    dummy,
                    currentScanId,
                    "NG_WRONG_SAP",
                    rollCode,
                    selectedInvoice.getId(),
                    "NG"
            );

            System.out.println();

            // ✅ UI
            showResult("MakerPN khác SAP!", "#d01029", "MakerPN not belong to selected SAP!");
            markStatus(selectedSap, "X");
            btnCallSuperV.setDisable(false);
            btnKeepGoing.setDisable(true);
            return;
        }


        // ================================
        // ✅ FIX QUAN TRỌNG — UPDATE MAKER/SPEC TRƯỚC KHI LƯU
        // ================================
        for (HistoryDetailViewDto dto : detailList) {
            if (dto.getSapCode().equalsIgnoreCase(selectedSap)) {
                dto.setMakerCode(matchedMOQ.getMakerPN());
                dto.setMaker(matchedMOQ.getMaker());
                dto.setSpec(matchedMOQ.getSpec());
                break;
            }
        }
        tblScanDetails.refresh();

        // ================================

        // 4️⃣ Hợp lệ → ghi bình thường
        historyService.createHistoryForScannedMakePN(matchedMOQ, currentScanId, "Scan Code: " + matchedMOQ, rollCode, selectedInvoice.getId(), "Scanned");



        // Cập nhật lại bảng hiển thị
        updateScannedQuantity(selectedSap, matchedMOQ.getMoq());

        bringSapToTop(selectedSap);

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

    private void bringSapToTop(String sapCode) {
        if (sapCode == null || sapCode.isBlank()) return;

        HistoryDetailViewDto target = null;

        // tìm dòng cần đưa lên đầu
        for (HistoryDetailViewDto dto : detailList) {
            if (dto.getSapCode().equalsIgnoreCase(sapCode)) {
                target = dto;
                break;
            }
        }

        if (target != null) {
            // remove + add vào vị trí 0
            detailList.remove(target);
            detailList.add(0, target);
            tblScanDetails.refresh();

            // Auto select dòng đầu
            Platform.runLater(() -> {
                tblScanDetails.getSelectionModel().select(0);
                tblScanDetails.scrollTo(0);
            });
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



