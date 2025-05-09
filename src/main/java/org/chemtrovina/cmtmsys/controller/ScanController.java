package org.chemtrovina.cmtmsys.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.dto.HistoryDetailViewDto;
import org.chemtrovina.cmtmsys.model.History;
import org.chemtrovina.cmtmsys.model.Invoice;
import org.chemtrovina.cmtmsys.model.MOQ;
import org.chemtrovina.cmtmsys.repository.Impl.HistoryRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.Impl.InvoiceRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.Impl.MOQRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.base.HistoryRepository;
import org.chemtrovina.cmtmsys.repository.base.InvoiceRepository;
import org.chemtrovina.cmtmsys.repository.base.MOQRepository;
import org.chemtrovina.cmtmsys.service.Impl.HistoryServiceImpl;
import org.chemtrovina.cmtmsys.service.Impl.InvoiceServiceImpl;
import org.chemtrovina.cmtmsys.service.Impl.MOQServiceImpl;
import org.chemtrovina.cmtmsys.service.base.HistoryService;
import org.chemtrovina.cmtmsys.service.base.InvoiceService;
import org.chemtrovina.cmtmsys.service.base.MOQService;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ScanController {

    // Layout containers
    @FXML private HBox hbTopFilter;
    @FXML private HBox hbDate;
    @FXML private HBox hbInvoice;
    @FXML private HBox hbScanInput;
    @FXML private Pane paneScanResult;
    @FXML private Pane paneScanTitle;

    // Text nodes
    @FXML private Text txtDate;
    @FXML private Text txtInvoiceLabel;
    @FXML private Text txtID;
    @FXML private Text txtScanStatus;
    @FXML private Text txtScanResultTitle;

    // Controls
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<String> cbInvoiceNo1;
    @FXML private TextField txtScanInput;
    @FXML private Button btnOnOff;
    @FXML private Button btnKeepGoing;
    @FXML private Button btnCallSuperV;
    @FXML private Button btnSearch;
    @FXML private Button btnClear;
    @FXML private TextField txtScanCode;

    // TableViews and Columns
    @FXML private TableView<Invoice> tblInvoiceList;
    @FXML private TableColumn<Invoice, LocalDate> colDate;
    @FXML private TableColumn<Invoice, String> colInvoiceNo;


    @FXML private TableView<HistoryDetailViewDto> tblScanDetails;

    @FXML private TableColumn<HistoryDetailViewDto, String> colMakerCode;
    @FXML private TableColumn<HistoryDetailViewDto, String> colSapCode;
    @FXML private TableColumn<HistoryDetailViewDto, String> colMaker;
    @FXML private TableColumn<HistoryDetailViewDto, Integer> colMOQ;
    @FXML private TableColumn<HistoryDetailViewDto, Integer> colQty;
    @FXML private TableColumn<HistoryDetailViewDto, Integer> colReelQty;
    @FXML private TableColumn<HistoryDetailViewDto, Boolean> colInvoice;


    private InvoiceService invoiceService;
    private MOQService moqService;
    private HistoryService historyService;

    private boolean isScanEnabled = false;
    private int selectedInvoiceId;
    private String currentScanId;



    @FXML
    public void initialize() {



        // Setup service
        DataSource dataSource = DataSourceConfig.getDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        InvoiceRepository invoiceRepository = new InvoiceRepositoryImpl(jdbcTemplate);
        invoiceService = new InvoiceServiceImpl(invoiceRepository);

        MOQRepository moqRepository = new MOQRepositoryImpl(jdbcTemplate);
        moqService = new MOQServiceImpl(moqRepository);

        HistoryRepository historyRepository = new HistoryRepositoryImpl(jdbcTemplate);
        historyService = new HistoryServiceImpl(historyRepository, moqRepository);

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


        List<String> invoiceNos = invoiceService.getAllInvoiceNos();
        cbInvoiceNo1.setItems(FXCollections.observableArrayList(invoiceNos));

        tblInvoiceList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedInvoiceId = newValue.getId();
                updateFiltersFromInvoice(newValue);
                loadHistoryDetails(newValue);
            }
        });


        btnSearch.setOnAction(event -> onSearch());
        btnClear.setOnAction(event -> onClear());
        btnOnOff.setOnAction(event -> {
            isScanEnabled = !isScanEnabled;
            btnOnOff.setText(isScanEnabled ? "Off" : "On");
            txtScanCode.setDisable(!isScanEnabled);
        });

        txtScanCode.setOnAction(event -> {
            if (!isScanEnabled) {
                showAlert("Scan Disabled", "Please turn on button.");
                return;
            }

            if (selectedInvoiceId == 0) {
                showAlert("No Invoice", "Please select a invoice first.");
                return;
            }

            String makerPN = txtScanCode.getText().trim();
            if (makerPN.isEmpty()) return;

            handleScanCode(makerPN);
            txtScanCode.clear();
        });

        //btnOnOff.setDisable(true);
        btnOnOff.setOnAction(event -> {
            isScanEnabled = !isScanEnabled;
            btnOnOff.setText(isScanEnabled ? "Off" : "On");
            txtScanCode.setDisable(!isScanEnabled);

            if (isScanEnabled) {
                currentScanId = txtScanInput.getText().trim(); // Lưu ID tại thời điểm nhấn On
            }
        });



    }


    //Scan
    private void handleScanCode(String makerPN) {
        MOQ moq = moqService.getMOQbyMakerPN(makerPN);

        if (moq == null) {
            showAlert("Wrong makerPN", "Not found.");
            return;
        }

        // 1. Tạo bản ghi history mới
        History history = new History();
        history.setInvoiceId(selectedInvoiceId);
        history.setMakerPN(makerPN);
        history.setSapPN(moq.getSapPN());
        history.setMaker(moq.getMaker());
        history.setQuantity(moq.getMoq());
        history.setMSL(moq.getMsql());
        history.setStatus("Scanned");
        history.setEmployeeId(currentScanId);
        history.setScanCode("ScanCode");
        history.setDate(LocalDate.now());
        history.setTime(LocalTime.now());

        historyService.addHistory(history);

        // 2. Kiểm tra và cập nhật DTO hiển thị
        boolean found = false;
        for (HistoryDetailViewDto dto : tblScanDetails.getItems()) {
            if (dto.getMakerCode().equalsIgnoreCase(makerPN)) {
                // Cộng dồn qty theo MOQ
                int newQty = dto.getQty() + moq.getMoq();
                dto.setQty(newQty);
                dto.setReelQty(newQty / moq.getMoq());
                tblScanDetails.refresh(); // Cập nhật lại bảng
                found = true;
                break;
            }
        }

        // 3. Nếu chưa có dòng nào cho makerPN này, thêm mới dòng DTO
        if (!found) {
            HistoryDetailViewDto dto = new HistoryDetailViewDto();
            dto.setId(0); // Nếu không cần ID
            dto.setMakerCode(makerPN);
            dto.setSapCode(moq.getSapPN());
            dto.setMaker(moq.getMaker());
            dto.setMoq(moq.getMoq());
            dto.setQty(moq.getMoq()); // Lần đầu = MOQ
            dto.setReelQty(1);
            dto.setInvoice(true);

            tblScanDetails.getItems().add(dto);
        }
        boolean isGood = isValidScan(makerPN);
        updateScanResultUI(isGood);

        if (!isGood) {
            return; // Dừng xử lý nếu mã NG, chờ người dùng chọn KeepGoing
        }


        txtScanCode.clear();
    }




    private void loadHistoryDetails(Invoice selectedInvoice) {
        // Lấy tất cả lịch sử theo invoice
        List<HistoryDetailViewDto> rawList = historyService.getHistoryDetailsByInvoiceId(selectedInvoice.getId());

        // Gom nhóm theo MakerPN
        Map<String, HistoryDetailViewDto> groupedMap = new LinkedHashMap<>();

        for (HistoryDetailViewDto dto : rawList) {
            String makerCode = dto.getMakerCode();
            int moq = dto.getMoq();

            if (groupedMap.containsKey(makerCode)) {
                HistoryDetailViewDto existing = groupedMap.get(makerCode);
                int newReelQty = existing.getReelQty() + 1;
                int newQty = moq * newReelQty;

                existing.setQty(newQty);
                existing.setReelQty(newReelQty);
            } else {
                int qty = moq;
                int reelQty = 1;

                if (moq == 0) {
                    // Tránh chia cho 0 — hoặc log cảnh báo
                    qty = 0;
                    reelQty = 0;
                }

                groupedMap.put(makerCode, new HistoryDetailViewDto(
                        0,
                        dto.getMakerCode(),
                        dto.getSapCode(),
                        dto.getMaker(),
                        moq,
                        qty,
                        reelQty,
                        false
                ));
            }
        }


        // Gán dữ liệu vào bảng
        tblScanDetails.setItems(FXCollections.observableArrayList(groupedMap.values()));


    }

    //UI
    private void updateScanResultUI(boolean isGood) {
        if (isGood) {
            txtScanStatus.setText("Good");
            txtScanStatus.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            paneScanResult.setStyle("-fx-background-color: #23dc28;");
            btnKeepGoing.setDisable(false);
        } else {
            txtScanStatus.setText("NG");
            txtScanStatus.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
            paneScanResult.setStyle("-fx-background-color: #d01029;");
            btnKeepGoing.setDisable(true);
        }
    }

    //Valid Scan
    private boolean isValidScan(String makerPN) {
        ObservableList<HistoryDetailViewDto> currentItems = tblScanDetails.getItems();

        if (!currentItems.isEmpty()) {
            String lastScannedMakerPN = currentItems.get(currentItems.size() - 1).getMakerCode();
            return makerPN.equalsIgnoreCase(lastScannedMakerPN);
        }

        return true; // Nếu danh sách rỗng thì mặc định là hợp lệ
    }






    private void onClear() {
        // Reset DatePicker
        dpDate.setValue(null);  // Xóa ngày đã chọn trong DatePicker

        // Reset ComboBox
        cbInvoiceNo1.setValue(null);  // Xóa lựa chọn trong ComboBox
        tblInvoiceList.getItems().clear();
    }


    private void updateFiltersFromInvoice(Invoice selectedInvoice) {
        // Cập nhật DatePicker với giá trị ngày của invoice đã chọn
        dpDate.setValue(selectedInvoice.getInvoiceDate());

        // Cập nhật ComboBox với InvoiceNo của invoice đã chọn
        cbInvoiceNo1.setValue(selectedInvoice.getInvoiceNo());
    }


    private void onSearch() {
        LocalDate selectedDate = dpDate.getValue();
        String selectedInvoiceNo = cbInvoiceNo1.getValue();

        List<Invoice> searchResults;

        if (selectedDate != null && selectedInvoiceNo != null) {
            searchResults = invoiceService.getInvoicesByDateAndInvoiceNo(selectedDate, selectedInvoiceNo);
        } else if (selectedDate != null) {
            searchResults = invoiceService.getInvoicesByDate(selectedDate);
        } else if (selectedInvoiceNo != null) {
            searchResults = invoiceService.getInvoicesByInvoiceNo(selectedInvoiceNo);
        } else {
            searchResults = invoiceService.findAll();  // Nếu không có filter nào, lấy tất cả invoices
        }

        tblInvoiceList.setItems(FXCollections.observableArrayList(searchResults));
    }

    //Alert
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
