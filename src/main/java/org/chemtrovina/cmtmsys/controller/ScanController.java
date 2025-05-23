package org.chemtrovina.cmtmsys.controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.dto.HistoryDetailViewDto;
import org.chemtrovina.cmtmsys.model.History;
import org.chemtrovina.cmtmsys.model.Invoice;
import org.chemtrovina.cmtmsys.model.InvoiceDetail;
import org.chemtrovina.cmtmsys.model.MOQ;
import org.chemtrovina.cmtmsys.repository.Impl.HistoryRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.Impl.InvoiceDetailRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.Impl.InvoiceRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.Impl.MOQRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.base.HistoryRepository;
import org.chemtrovina.cmtmsys.repository.base.InvoiceDetailRepository;
import org.chemtrovina.cmtmsys.repository.base.InvoiceRepository;
import org.chemtrovina.cmtmsys.repository.base.MOQRepository;
import org.chemtrovina.cmtmsys.service.Impl.HistoryServiceImpl;
import org.chemtrovina.cmtmsys.service.Impl.InvoiceDetailServiceImpl;
import org.chemtrovina.cmtmsys.service.Impl.InvoiceServiceImpl;
import org.chemtrovina.cmtmsys.service.Impl.MOQServiceImpl;
import org.chemtrovina.cmtmsys.service.base.HistoryService;
import org.chemtrovina.cmtmsys.service.base.InvoiceDetailService;
import org.chemtrovina.cmtmsys.service.base.InvoiceService;
import org.chemtrovina.cmtmsys.service.base.MOQService;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ScanController {

    // ======================== FXML COMPONENTS ========================
    // Layout containers
    @FXML private HBox hbTopFilter, hbDate, hbInvoice, hbScanInput;
    @FXML private Pane paneScanResult, paneScanTitle;

    // Text nodes
    @FXML private Text txtDate, txtInvoiceLabel, txtID, txtScanStatus, txtScanResultTitle;

    // Controls
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<String> cbInvoiceNo1;
    @FXML private TextField txtScanInput, txtScanCode;
    @FXML private Button btnOnOff, btnKeepGoing, btnCallSuperV, btnSearch, btnClear, btnRefresh, btnScanOddReel;

    // TableViews and Columns
    @FXML private TableView<Invoice> tblInvoiceList;
    @FXML private TableColumn<Invoice, LocalDate> colDate;
    @FXML private TableColumn<Invoice, String> colInvoiceNo;

    @FXML private TableView<HistoryDetailViewDto> tblScanDetails;
    @FXML private TableColumn<HistoryDetailViewDto, String> colMakerCode, colSapCode, colMaker, colInvoice;
    @FXML private TableColumn<HistoryDetailViewDto, Integer> colMOQ, colQty, colReelQty;

    // ======================== SERVICES ========================
    private InvoiceService invoiceService;
    private MOQService moqService;
    private HistoryService historyService;
    private InvoiceDetailService invoiceDetailService;

    // ======================== VARIABLES ========================
    private boolean isScanEnabled = false;
    private int selectedInvoiceId;
    private String currentScanId;
    private String lastAcceptedMakerPN = null;
    private String lastScannedMakerPN;
    private Invoice selectedInvoice;
    private PauseTransition idleTimer;


    // ======================== INIT ========================
    @FXML
    public void initialize() {
        setupServices();
        setupTableColumns();
        setupInvoiceComboBox();
        setupScanInputHandlers();
        setupEventHandlers();
        setupButton();
        setupIdleTimer();
        setupActivityListeners();
    }

    // ======================== SETUP ========================
    private void setupServices() {
        DataSource dataSource = DataSourceConfig.getDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        InvoiceRepository invoiceRepository = new InvoiceRepositoryImpl(jdbcTemplate);
        invoiceService = new InvoiceServiceImpl(invoiceRepository);

        MOQRepository moqRepository = new MOQRepositoryImpl(jdbcTemplate);
        moqService = new MOQServiceImpl(moqRepository);

        HistoryRepository historyRepository = new HistoryRepositoryImpl(jdbcTemplate);
        historyService = new HistoryServiceImpl(historyRepository, moqRepository);

        InvoiceDetailRepository invoiceDetailRepository = new InvoiceDetailRepositoryImpl(jdbcTemplate);
        invoiceDetailService = new InvoiceDetailServiceImpl(invoiceDetailRepository);
    }

    private void setupTableColumns(){
        //Table Invoice
        colDate.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
        colInvoiceNo.setCellValueFactory(new PropertyValueFactory<>("invoiceNo"));

        //Table History
        colMakerCode.setCellValueFactory(new PropertyValueFactory<>("makerCode"));
        colSapCode.setCellValueFactory(new PropertyValueFactory<>("sapCode"));
        colMaker.setCellValueFactory(new PropertyValueFactory<>("maker"));
        colMOQ.setCellValueFactory(new PropertyValueFactory<>("moq"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colReelQty.setCellValueFactory(new PropertyValueFactory<>("reelQty"));
        colInvoice.setCellValueFactory(new PropertyValueFactory<>("invoice"));
    }

    private void setupButton(){
        btnKeepGoing.setDisable(true);
        btnCallSuperV.setDisable(true);
        btnScanOddReel.setDisable(true);
    }

    private void setupInvoiceComboBox(){
        loadInvoiceNosToComboBox();

        cbInvoiceNo1.setOnShowing(event -> loadInvoiceNosToComboBox());

        tblInvoiceList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedInvoice = newValue;
                selectedInvoiceId = newValue.getId();
                updateFiltersFromInvoice(newValue);
                loadHistoryDetails(newValue);
                lastAcceptedMakerPN = null;
            }
        });
    }

    private void setupScanInputHandlers() {
        txtScanCode.setOnAction(event -> {
            if (!isScanEnabled) {
                showAlert("Scan Disabled", "Please turn on button.");
                return;
            }

            if (selectedInvoiceId == 0) {
                showAlert("No Invoice", "Please select an invoice first.");
                return;
            }

            String makerPN = txtScanCode.getText().trim();
            if (!makerPN.isEmpty()) {
                handleScanCode(makerPN);
                txtScanCode.clear();
            }
        });
    }

    private void setupEventHandlers() {
        btnSearch.setOnAction(event -> onSearch());
        btnClear.setOnAction(event -> onClear());
        btnRefresh.setOnAction(event -> onRefresh());
        btnKeepGoing.setOnAction(event -> onKeptGoing());
        btnCallSuperV.setOnAction(event -> onCallSuperV());

        btnOnOff.setDisable(true); // ban đầu
        btnOnOff.setOnAction(event -> toggleScanMode());

        // Lắng nghe nội dung thay đổi để bật nút On/Off
        txtScanInput.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean isEmpty = newVal.trim().isEmpty();
            btnOnOff.setDisable(isEmpty);
        });



        // Khi scan xong (nhấn Enter), bật chế độ scan và focus
        txtScanInput.setOnAction(event -> {
            String input = txtScanInput.getText().trim();

            if (!input.isEmpty() && !isScanEnabled) {
                toggleScanMode();
            }

            updateScanCodeState();
            txtScanCode.requestFocus();
        });
    }

    private void setupIdleTimer() {
        idleTimer = new PauseTransition(Duration.minutes(30));
        idleTimer.setOnFinished(event -> onIdleTimeout());
        idleTimer.play(); // Start initially
    }

    private void setupActivityListeners() {
        txtScanInput.setOnKeyTyped(event -> resetIdleTimer());
        txtScanInput.setOnMouseClicked(event -> resetIdleTimer());
        btnOnOff.setOnMouseClicked(event -> resetIdleTimer());
        btnScanOddReel.setOnAction(event -> showOddReelScanDialog());

    }

    // ======================== UI HELPER ========================
    private void updateScanCodeState() {
        boolean hasScanInput = !txtScanInput.getText().trim().isEmpty();
        txtScanCode.setDisable(!(hasScanInput && isScanEnabled));
        btnScanOddReel.setDisable(!(hasScanInput && isScanEnabled));
    }

    private void toggleScanMode() {
        isScanEnabled = !isScanEnabled;
        btnOnOff.setText(isScanEnabled ? "Off" : "On");

        if (isScanEnabled) {
            currentScanId = txtScanInput.getText().trim();
        }

        updateScanCodeState();
    }

    private void resetScanUI() {
        txtScanStatus.setText("None");
        txtScanStatus.setStyle("-fx-background-color: gray; -fx-text-fill: white;");
        paneScanResult.setStyle("-fx-background-color: lightgray;");
        btnKeepGoing.setDisable(true);
        btnCallSuperV.setDisable(true);
        txtScanCode.setDisable(false);
    }

    private void setScanStatusGood() {
        txtScanStatus.setText("Good");
        txtScanStatus.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        paneScanResult.setStyle("-fx-background-color: #23dc28;");
        btnKeepGoing.setDisable(true);
        btnCallSuperV.setDisable(true);
    }

    private void resetIdleTimer() {
        idleTimer.stop();
        idleTimer.playFromStart();
    }

    private void onIdleTimeout() {
        btnOnOff.setDisable(true);
        txtScanInput.clear();
        txtScanCode.clear();
        System.out.println("Idle timeout! Scan disabled.");
    }

    private void onClear() {
        resetFilters();
        resetInvoiceTable();
        resetUI();
    }
    private void resetFilters() {
        dpDate.setValue(null);
        cbInvoiceNo1.setValue(null);
    }

    private void resetInvoiceTable() {
        tblInvoiceList.getItems().clear();
    }

    private void resetUI() {
        txtScanStatus.setText("None");
        txtScanStatus.setStyle("-fx-background-color: gray; -fx-text-fill: white;");
        paneScanResult.setStyle("-fx-background-color: lightgray;");
        tblScanDetails.getItems().clear();
        btnKeepGoing.setDisable(true);
        btnCallSuperV.setDisable(true);
    }

    private void updateScanResultUI(boolean isGood) {
        if (isGood) {
            // Mã quét hợp lệ (Good)
            txtScanStatus.setText("Good");
            txtScanStatus.setStyle("-fx-background-color: #ffffff; -fx-text-fill: white;");
            paneScanResult.setStyle("-fx-background-color: #23dc28;");

            // Ẩn cả 2 nút khi mã là Good
            btnKeepGoing.setDisable(true);
            btnCallSuperV.setDisable(true);
        } else {
            // Mã quét không hợp lệ (NG)
            txtScanStatus.setText("NG");
            txtScanStatus.setStyle("-fx-background-color: #ffffff; -fx-text-fill: white;");
            paneScanResult.setStyle("-fx-background-color: #d01029;");

            // Hiển thị cả 2 nút khi mã là NG
            btnKeepGoing.setDisable(false);
            btnCallSuperV.setDisable(false);
        }
    }

    // ======================== ACTIONS ========================

    @FXML
    private void onRefresh() {
        if (selectedInvoice != null) {
            refreshHistoryTable();
        } else {
            showAlert("No invoice selected", "Please select an invoice to refresh data.");
        }
    }

    private void onCallSuperV() {
        lastAcceptedMakerPN = null;

        HistoryDetailViewDto matchedDto = findMatchedDto(lastScannedMakerPN);
        if (matchedDto != null) {
            adjustOrRemoveDto(matchedDto);
            historyService.deleteLastByMakerPNAndInvoiceId(lastScannedMakerPN, selectedInvoiceId);
        }

        resetScanUI();
        refreshHistoryTable();
    }

    private void onKeptGoing() {
        lastAcceptedMakerPN = lastScannedMakerPN;
        setScanStatusGood();
        refreshHistoryTable();
        txtScanCode.setDisable(false);
        txtScanCode.requestFocus();
    }
    // ======================== HELPER ========================

    private void loadInvoiceNosToComboBox() {
        List<String> invoiceNos = invoiceService.getAllInvoiceNos();
        cbInvoiceNo1.setItems(FXCollections.observableArrayList(invoiceNos));
    }

    private void updateFiltersFromInvoice(Invoice selectedInvoice) {
        dpDate.setValue(selectedInvoice.getInvoiceDate());
        cbInvoiceNo1.setValue(selectedInvoice.getInvoiceNo());
    }

    private HistoryDetailViewDto findMatchedDto(String makerPN) {
        for (HistoryDetailViewDto dto : tblScanDetails.getItems()) {
            if (dto.getMakerCode().equalsIgnoreCase(makerPN)) {
                return dto;
            }
        }
        return null;
    }

    private void adjustOrRemoveDto(HistoryDetailViewDto dto) {
        int newQty = dto.getQty() - dto.getMoq();
        if (newQty > 0) {
            dto.setQty(newQty);
            dto.setReelQty(newQty / dto.getMoq());
            tblScanDetails.refresh();
        } else {
            tblScanDetails.getItems().remove(dto);
        }
    }

    private void loadHistoryDetails(Invoice selectedInvoice) {
        // Lấy tất cả lịch sử theo invoice
        List<HistoryDetailViewDto> rawList = historyService.getHistoryDetailsByInvoiceId(selectedInvoice.getId());

        // Gom nhóm theo MakerPN
        Map<String, HistoryDetailViewDto> groupedMap = new LinkedHashMap<>();

        for (HistoryDetailViewDto dto : rawList) {
            String makerCode = dto.getMakerCode();
            int moq = dto.getMoq();
            int qty = dto.getQty();

            System.out.println(qty);

            if (groupedMap.containsKey(makerCode)) {
                HistoryDetailViewDto existing = groupedMap.get(makerCode);
                existing.setQty(existing.getQty() + qty);         // cộng dồn qty thật
                System.out.println(existing.getQty());
                existing.setReelQty(existing.getReelQty() + 1);   // tăng reel count
            } else {
                groupedMap.put(makerCode, new HistoryDetailViewDto(
                        0,
                        dto.getMakerCode(),
                        dto.getSapCode(),
                        dto.getMaker(),
                        moq,
                        qty,
                        1,     // reel đầu tiên
                        ""     // sẽ cập nhật trạng thái sau
                ));
            }
        }


        // Kiểm tra trạng thái O/X cho từng mã và cập nhật vào invoice field
        for (HistoryDetailViewDto dto : groupedMap.values()) {
            int totalScannedQty = historyService.getTotalScannedQuantityBySapPN(dto.getSapCode(), selectedInvoice.getId());
            InvoiceDetail invoiceDetail = invoiceDetailService.getInvoiceDetailBySapPNAndInvoiceId(dto.getSapCode(), selectedInvoice.getId());

            String status;
            if (invoiceDetail == null) {
                status = "Z"; // Không tồn tại trong invoiceDetail
            } else if (totalScannedQty == invoiceDetail.getQuantity()) {
                status = "O"; // Đủ
            } else {
                status = "X"; // Thiếu
            }
            dto.setInvoice(status);
        }

        tblScanDetails.setItems(FXCollections.observableArrayList(groupedMap.values()));
        tblScanDetails.refresh();

        colInvoice.setCellFactory(column -> new TableCell<HistoryDetailViewDto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "O":
                            setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;"); // xanh lá
                            break;
                        case "X":
                            setStyle("-fx-background-color: #D01029; -fx-text-fill: white; -fx-font-weight: bold;"); // đỏ
                            break;
                        case "Z":
                            setStyle("-fx-background-color: orange; -fx-text-fill: #ffffff; -fx-font-weight: bold;"); // cam
                            break;
                        default:
                            setStyle(""); // fallback
                            break;
                    }
                }
            }
        });

    }


    private void saveScanOddReel(String makerPN, int quantity) {
        historyService.createHistoryForScanOddReel(makerPN, currentScanId, "Scan Code", selectedInvoiceId, quantity);
    }


    private void saveScanToHistory(String makerPN) {
        System.out.println(currentScanId.toString());
        historyService.createHistoryForScannedMakePN(makerPN, currentScanId, "Scan Code", selectedInvoiceId);
    }


    private void refreshHistoryTable() {
        loadHistoryDetails(selectedInvoice);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateTableScanSummary(String makerPN, MOQ moq, int customQty) {
        boolean found = false;

        for (HistoryDetailViewDto dto : tblScanDetails.getItems()) {
            if (dto.getMakerCode().equalsIgnoreCase(makerPN)) {
                int newQty = dto.getQty() + customQty;
                dto.setQty(newQty);
                System.out.println(newQty);
                dto.setReelQty(newQty / moq.getMoq());
                found = true;
                break;
            }
        }

        if (!found) {
            HistoryDetailViewDto dto = new HistoryDetailViewDto();
            dto.setId(0);
            dto.setMakerCode(makerPN);
            dto.setSapCode(moq.getSapPN());
            dto.setMaker(moq.getMaker());
            dto.setMoq(moq.getMoq());
            dto.setQty(customQty);
            dto.setReelQty(customQty / moq.getMoq());
            dto.setInvoice("");
            tblScanDetails.getItems().add(dto);
        }

        tblScanDetails.refresh();
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    //Scan
    private void handleScanCode(String rawInput) {

        String extractedMakerPN = historyService.extractRealMakerPN(rawInput);
        if (extractedMakerPN == null) {
            showAlert("Scan NG", "Không nhận diện được MakerPN từ chuỗi: " + rawInput);
            return;
        }

        // Bước 2: lấy MOQ theo makerPN chuẩn
        MOQ moq = moqService.getMOQbyMakerPN(extractedMakerPN);
        if (moq == null) {
            showAlert("Wrong makerPN", "Not found.");
            return;
        }

        saveScanToHistory(extractedMakerPN);
        lastScannedMakerPN = extractedMakerPN;
        String sapPN = moq.getSapPN();
        InvoiceDetail invoiceDetail = invoiceDetailService.getInvoiceDetailBySapPNAndInvoiceId(sapPN, selectedInvoiceId);

        updateTableScanSummary(extractedMakerPN, moq, moq.getMoq());

        if (invoiceDetail == null) {
            handleNotExistInInvoice(sapPN);
            return;
        }

        boolean isGood = isValidScan(extractedMakerPN);
        if (!isGood) {
            updateScanResultUI(false);
            txtScanCode.setDisable(true);
            return;
        }

        updateScanResultUI(true);
        checkQuantityAndUpdateStatus(sapPN);

        lastAcceptedMakerPN = extractedMakerPN;
        txtScanCode.clear();
        tblScanDetails.refresh();

    }


    private void checkQuantityAndUpdateStatus(String sapPN) {
        InvoiceDetail invoiceDetail = invoiceDetailService.getInvoiceDetailBySapPNAndInvoiceId(sapPN, selectedInvoiceId);
        int expectedQty = invoiceDetail.getQuantity();
        int totalScannedQty = historyService.getTotalScannedQuantityBySapPN(sapPN, selectedInvoiceId);

        if (totalScannedQty > expectedQty) {
            // Dư -> NG ngay lập tức
            updateInvoiceColumnStatus(sapPN, "X", "#D01029");
            updateScanResultUI(false);
            txtScanStatus.setText("Over");
            txtScanStatus.setStyle("-fx-background-color: red; -fx-text-fill: white;");
            paneScanResult.setStyle("-fx-background-color: red;");
            txtScanCode.setDisable(true);
            btnKeepGoing.setDisable(true);
            btnCallSuperV.setDisable(false);
            return;
        }

        if (totalScannedQty == expectedQty) {
            // Đủ
            updateInvoiceColumnStatus(sapPN, "O", "#4CAF50");
            btnKeepGoing.setDisable(true);
        } else {
            // Thiếu
            updateInvoiceColumnStatus(sapPN, "X", "#D01029");
            btnKeepGoing.setDisable(true);
        }
    }


    private void updateInvoiceColumnStatus(String sapPN, String status, String color) {
        for (HistoryDetailViewDto dto : tblScanDetails.getItems()) {
            if (dto.getSapCode().equalsIgnoreCase(sapPN)) {
                dto.setInvoice(status); // Cập nhật trạng thái
                tblScanDetails.refresh();
                colInvoice.setStyle("-fx-background-color: " + color + ";");
                break;
            }
        }
    }


    //Valid Scan
    private boolean isValidScan(String makerPN) {
        if (lastAcceptedMakerPN == null) return true;
        return makerPN.equalsIgnoreCase(lastAcceptedMakerPN);
    }

    private void handleNotExistInInvoice(String sapPN) {
        updateInvoiceColumnStatus(sapPN, "Z", "#CAAA12");
        txtScanStatus.setText("Not Exist");
        txtScanStatus.setStyle("-fx-background-color: #CAAA12; -fx-text-fill: black;");
        paneScanResult.setStyle("-fx-background-color: #CAAA12;");
        txtScanCode.setDisable(true);
        btnKeepGoing.setDisable(true);
        btnCallSuperV.setDisable(false);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void showOddReelScanDialog() {
        if (selectedInvoice == null) {
            showAlert("No Invoice Selected", "Please select an invoice before scanning odd reel.");
            return;
        }

        Dialog<Pair<String, String>> dialog = createOddReelDialog();
        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(pair -> processOddReelInput(pair.getKey().trim(), pair.getValue().trim()));
    }

    private Dialog<Pair<String, String>> createOddReelDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Scan Odd Reel");
        dialog.setHeaderText("Nhập MakerPN (Scan Code) và Quantity");

        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        TextField txtMakerPN = new TextField();
        txtMakerPN.setPromptText("MakerPN");

        TextField txtQuantity = new TextField();
        txtQuantity.setPromptText("Quantity");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("MakerPN:"), 0, 0);
        grid.add(txtMakerPN, 1, 0);
        grid.add(new Label("Quantity:"), 0, 1);
        grid.add(txtQuantity, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> dialogButton == submitButtonType
                ? new Pair<>(txtMakerPN.getText(), txtQuantity.getText())
                : null);

        return dialog;
    }

    private void processOddReelInput(String makerPN, String qtyStr) {
        if (makerPN.isEmpty() || qtyStr.isEmpty()) {
            showAlert("Missing Data", "Both MakerPN and Quantity are required.");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyStr);
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Invalid Quantity", "Quantity must be a positive number.");
            return;
        }

        MOQ moq = moqService.getMOQbyMakerPN(makerPN);
        if (moq == null) {
            showAlert("Invalid MakerPN", "Không tìm thấy MakerPN trong MOQ.");
            return;
        }

        String extractedMakerPN = historyService.extractRealMakerPN(makerPN);
        saveScanOddReel(makerPN, qty);
        lastScannedMakerPN = extractedMakerPN;

        String sapPN = moq.getSapPN();
        InvoiceDetail invoiceDetail = invoiceDetailService.getInvoiceDetailBySapPNAndInvoiceId(sapPN, selectedInvoiceId);
        updateTableScanSummary(makerPN, moq, qty);

        if (invoiceDetail == null) {
            handleNotExistInInvoice(sapPN);
            return;
        }

        boolean isGood = isValidScan(extractedMakerPN);
        updateScanResultUI(isGood);
        txtScanCode.setDisable(!isGood);

        if (isGood) {
            checkQuantityAndUpdateStatus(sapPN);
            lastAcceptedMakerPN = extractedMakerPN;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Search
    private void onSearch() {
        List<Invoice> searchResults = searchInvoices(dpDate.getValue(), cbInvoiceNo1.getValue());
        tblInvoiceList.setItems(FXCollections.observableArrayList(searchResults));
    }


    private List<Invoice> searchInvoices(LocalDate date, String invoiceNo) {
        if (date != null && invoiceNo != null) {
            return invoiceService.getInvoicesByDateAndInvoiceNo(date, invoiceNo);
        } else if (date != null) {
            return invoiceService.getInvoicesByDate(date);
        } else if (invoiceNo != null) {
            return invoiceService.getInvoicesByInvoiceNo(invoiceNo);
        } else {
            return invoiceService.findAll();
        }
    }

}
