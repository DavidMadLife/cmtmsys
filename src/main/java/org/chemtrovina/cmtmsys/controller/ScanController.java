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
    @FXML private Button btnRefresh;
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
    @FXML private TableColumn<HistoryDetailViewDto, String> colInvoice;


    private InvoiceService invoiceService;
    private MOQService moqService;
    private HistoryService historyService;
    private InvoiceDetailService invoiceDetailService ;

    private boolean isScanEnabled = false;
    private int selectedInvoiceId;
    private String currentScanId;
    private String lastAcceptedMakerPN = null;
    private String lastScannedMakerPN;

    private Invoice selectedInvoice;




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

        InvoiceDetailRepository invoiceDetailRepository = new InvoiceDetailRepositoryImpl(jdbcTemplate);
        invoiceDetailService = new InvoiceDetailServiceImpl(invoiceDetailRepository);

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
                selectedInvoice = newValue;
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

        btnOnOff.setDisable(true); // mặc định ban đầu
        txtScanInput.textProperty().addListener((observable, oldValue, newValue) -> {
            btnOnOff.setDisable(newValue.trim().isEmpty());
        });

        btnKeepGoing.setOnAction(event -> onKeptGoing());


        btnCallSuperV.setOnAction(event -> onCallSuperV());

        btnRefresh.setOnAction(event -> onRefresh());

    }

    @FXML
    private void onRefresh() {
        if (selectedInvoice != null) {
            loadHistoryDetails(selectedInvoice);
        } else {
            showAlert("No invoice selected", "Please select an invoice to refresh data.");
        }
    }





    //Call Super-V
    private void onCallSuperV() {
        lastAcceptedMakerPN = null;

        // Lấy danh sách hiện tại
        ObservableList<HistoryDetailViewDto> currentItems = tblScanDetails.getItems();

        // Tìm dòng tương ứng với lastScannedMakerPN
        HistoryDetailViewDto matchedDto = null;
        for (HistoryDetailViewDto dto : currentItems) {
            if (dto.getMakerCode().equalsIgnoreCase(lastScannedMakerPN)) {
                matchedDto = dto;
                break;
            }
        }

        if (matchedDto != null) {
            // Giảm quantity
            int newQty = matchedDto.getQty() - matchedDto.getMoq();

            // Nếu còn dữ liệu thì cập nhật lại số lượng
            if (newQty > 0) {
                matchedDto.setQty(newQty);
                matchedDto.setReelQty(newQty / matchedDto.getMoq());
                tblScanDetails.refresh();
            } else {
                // Nếu hết thì xoá dòng đó khỏi bảng
                currentItems.remove(matchedDto);
            }

            // Xoá bản ghi history tương ứng trong DB
            historyService.deleteLastByMakerPNAndInvoiceId(lastScannedMakerPN, selectedInvoiceId);

        }

        // Reset UI
        txtScanStatus.setText("None");
        txtScanStatus.setStyle("-fx-background-color: gray; -fx-text-fill: white;");
        paneScanResult.setStyle("-fx-background-color: lightgray;");
        btnKeepGoing.setDisable(true);
        btnCallSuperV.setDisable(true);
        txtScanCode.setDisable(false);
        loadHistoryDetails(selectedInvoice);
    }


    //Keep Going
    private void onKeptGoing() {
        lastAcceptedMakerPN = lastScannedMakerPN; // Sử dụng biến tạm đã lưu
        txtScanStatus.setText("Good");
        txtScanStatus.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        paneScanResult.setStyle("-fx-background-color: #23dc28;");
        btnKeepGoing.setDisable(true);
        btnCallSuperV.setDisable(true);
        loadHistoryDetails(selectedInvoice);
        txtScanCode.setDisable(false);
        txtScanCode.requestFocus();

    }



    //Scan
    private void handleScanCode(String makerPN) {
        MOQ moq = moqService.getMOQbyMakerPN(makerPN);

        if (moq == null) {
            showAlert("Wrong makerPN", "Not found.");
            return;
        }

        // Gán lại để dùng nếu NG
        lastScannedMakerPN = makerPN;

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
                int newQty = dto.getQty() + moq.getMoq();
                dto.setQty(newQty);
                dto.setReelQty(newQty / moq.getMoq());
                tblScanDetails.refresh();
                found = true;
                break;
            }
        }

        // 3. Nếu chưa có dòng nào cho makerPN này, thêm mới
        if (!found) {
            HistoryDetailViewDto dto = new HistoryDetailViewDto();
            dto.setId(0);
            dto.setMakerCode(makerPN);
            dto.setSapCode(moq.getSapPN());
            dto.setMaker(moq.getMaker());
            dto.setMoq(moq.getMoq());
            dto.setQty(moq.getMoq());
            dto.setReelQty(1);
            dto.setInvoice("");
            tblScanDetails.getItems().add(dto);
        }

        // 4. Kiểm tra NG hay GOOD
        boolean isGood = isValidScan(makerPN);
        if (!isGood) {
            updateScanResultUI(false);
            txtScanCode.setDisable(true);

            return; // NG -> chờ xử lý
        }

        if (isGood) {
            updateScanResultUI(true);
            checkQuantityAndUpdateStatus(moq.getSapPN());

        }

        lastAcceptedMakerPN = makerPN;

        txtScanCode.clear();
        tblScanDetails.refresh();
    }



    //Check quantity
    /*private void checkQuantityAndUpdateStatus(String sapPN) {
        // 1. Tính tổng số lượng đã quét từ bảng History
        int totalScannedQty = historyService.getTotalScannedQuantityBySapPN(sapPN, selectedInvoiceId);

        // 2. Lấy chi tiết Invoice để so sánh
        InvoiceDetail invoiceDetail = invoiceDetailService.getInvoiceDetailBySapPNAndInvoiceId(sapPN, selectedInvoiceId);

        if (invoiceDetail != null) {
            int expectedQty = invoiceDetail.getQuantity();

            if (totalScannedQty > expectedQty) {
                // Dư -> NG ngay lập tức
                updateInvoiceColumnStatus(sapPN, "X", "#D01029");
                updateScanResultUI(false); // NG UI
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
        *//*else {

            updateInvoiceColumnStatus(sapPN, "Z", "#caaa12");
            updateScanResultUI(false);
            txtScanStatus.setText("Not Exíst");
            txtScanStatus.setStyle("-fx-background-color: #ffffff; -fx-text-fill: white;");
            paneScanResult.setStyle("-fx-background-color: #d1d624;");
            btnKeepGoing.setDisable(true);
            txtScanCode.setDisable(true);
            btnCallSuperV.setDisable(false);
        }*//*
    }*/

    private void checkQuantityAndUpdateStatus(String sapPN) {
        // 1. Lấy chi tiết Invoice để so sánh
        InvoiceDetail invoiceDetail = invoiceDetailService.getInvoiceDetailBySapPNAndInvoiceId(sapPN, selectedInvoiceId);

        // 2. Nếu không tìm thấy mã SAP trong invoice -> "Z"
        if (invoiceDetail == null) {
            updateInvoiceColumnStatus(sapPN, "Z", "#CAAA12"); // Z màu vàng
            updateScanResultUI(false); // Đổi UI NG
            txtScanStatus.setText("Not Exist");
            txtScanStatus.setStyle("-fx-background-color: #CAAA12; -fx-text-fill: black;");
            paneScanResult.setStyle("-fx-background-color: #CAAA12;");
            btnKeepGoing.setDisable(true);
            txtScanCode.setDisable(true);
            btnCallSuperV.setDisable(false);
            btnRefresh.setDisable(true);
            return;
        }

        // 3. Nếu tìm thấy, xử lý như bình thường
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
            btnKeepGoing.setDisable(false);
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
                    // Tránh chia cho 0
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
                        "" // sẽ cập nhật trạng thái sau
                ));
            }
        }

        // Kiểm tra trạng thái O/X cho từng mã và cập nhật vào invoice field
        for (HistoryDetailViewDto dto : groupedMap.values()) {
            int totalScannedQty = historyService.getTotalScannedQuantityBySapPN(dto.getSapCode(), selectedInvoice.getId());
            InvoiceDetail invoiceDetail = invoiceDetailService.getInvoiceDetailBySapPNAndInvoiceId(dto.getSapCode(), selectedInvoice.getId());

            String status = "X";
            if (invoiceDetail != null && totalScannedQty == invoiceDetail.getQuantity()) {
                status = "O";
            }

            dto.setInvoice(status);
        }

        // Gán dữ liệu vào bảng
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
                    if ("O".equals(item)) {
                        setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
                    } else if ("X".equals(item)) {
                        setStyle("-fx-background-color: #D01029; -fx-text-fill: white; -fx-font-weight: bold;");
                    }
                }
            }
        });


    }


    //UI
    // Cập nhật kết quả quét UI
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


    //Valid Scan
    private boolean isValidScan(String makerPN) {
        if (lastAcceptedMakerPN == null) return true;
        return makerPN.equalsIgnoreCase(lastAcceptedMakerPN);
    }


    private void onClear() {
        // Reset DatePicker
        dpDate.setValue(null);  // Xóa ngày đã chọn trong DatePicker

        // Reset ComboBox
        cbInvoiceNo1.setValue(null);  // Xóa lựa chọn trong ComboBox
        tblInvoiceList.getItems().clear();
        txtScanStatus.setText("None");
        txtScanStatus.setStyle("-fx-background-color: gray; -fx-text-fill: white;");
        paneScanResult.setStyle("-fx-background-color: lightgray;");
        tblScanDetails.getItems().clear();
        btnKeepGoing.setDisable(true);
        btnCallSuperV.setDisable(true);
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

    private String determineStatus(String sapPN) {
        int totalScannedQty = historyService.getTotalScannedQuantityBySapPN(sapPN, selectedInvoiceId);
        InvoiceDetail invoiceDetail = invoiceDetailService.getInvoiceDetailBySapPNAndInvoiceId(sapPN, selectedInvoiceId);

        if (invoiceDetail != null && totalScannedQty == invoiceDetail.getQuantity()) {
            return "O"; // Đủ
        } else {
            return "X"; // Thiếu hoặc dư
        }
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
