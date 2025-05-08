package org.chemtrovina.cmtmsys.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.dto.HistoryDetailViewDto;
import org.chemtrovina.cmtmsys.model.Invoice;
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
import java.util.List;

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
    @FXML private Button btnError;
    @FXML private Button btnSearch;

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

    }

    private void loadInvoices(LocalDate date, String invoiceNo) {
        try {
            var invoices = invoiceService.findByDateAndInvoiceNo(date, invoiceNo);
            tblInvoiceList.getItems().setAll(invoices);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Failed to load Invoice", e.getMessage());
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
