package org.chemtrovina.cmtmsys.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.dto.InvoiceDetailViewDto;
import org.chemtrovina.cmtmsys.model.Invoice;
import org.chemtrovina.cmtmsys.model.InvoiceDetail;
import org.chemtrovina.cmtmsys.repository.Impl.InvoiceRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.base.InvoiceRepository;
import org.chemtrovina.cmtmsys.service.Impl.InvoiceServiceImpl;
import org.chemtrovina.cmtmsys.service.base.InvoiceService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;

public class InvoiceController {



    @FXML private DatePicker dpDate;
    @FXML private ComboBox<Invoice> cbInvoiceNo;
    @FXML private Button btnNew;
    @FXML private Button btnSave;

    @FXML private TableView<InvoiceDetailViewDto> tableView;
    @FXML private TableColumn<InvoiceDetailViewDto, String> colInvoiceNo;
    @FXML private TableColumn<InvoiceDetailViewDto, String> colSAPCode;
    @FXML private TableColumn<InvoiceDetailViewDto, Integer> colQty;
    @FXML private TableColumn<InvoiceDetailViewDto, Integer> colMOQ;
    @FXML private TableColumn<InvoiceDetailViewDto, Integer> colReelQty;
    @FXML private TableColumn<InvoiceDetailViewDto, LocalDate> colDate;

    private InvoiceService invoiceService;

    private final ObservableList<InvoiceDetailViewDto> invoiceDetailDtoList = FXCollections.observableArrayList();




    @FXML
    public void initialize() {


        // Setup service
        DataSource dataSource = DataSourceConfig.getDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        InvoiceRepository invoiceRepository = new InvoiceRepositoryImpl(jdbcTemplate);
        invoiceService = new InvoiceServiceImpl(invoiceRepository);

        // Cấu hình TableView
        colDate.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
        colInvoiceNo.setCellValueFactory(new PropertyValueFactory<>("invoiceNo"));
        colSAPCode.setCellValueFactory(new PropertyValueFactory<>("sapCode"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colMOQ.setCellValueFactory(new PropertyValueFactory<>("moq"));
        colReelQty.setCellValueFactory(new PropertyValueFactory<>("reelQty"));

        tableView.setItems(invoiceDetailDtoList);

        btnNew.setOnAction(event -> CreateInvoice());
        btnSave.setOnAction(event -> SaveInvoice());

        cbInvoiceNo.setConverter(new StringConverter<Invoice>() {
            @Override
            public String toString(Invoice invoice) {
                return invoice != null ? invoice.getInvoiceNo() : "";
            }

            @Override
            public Invoice fromString(String string) {
                // Tùy bạn có cần xử lý khi người dùng nhập trực tiếp không
                return cbInvoiceNo.getItems().stream()
                        .filter(inv -> inv.getInvoiceNo().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

    }

    //Create new
    private void CreateInvoice() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create new Invoice");
        dialog.setHeaderText("Are you want to create new Invoice?");
        dialog.setContentText("Input quantity of Item:");

        dialog.showAndWait().ifPresent(input -> {
            try {
                int detailCount = Integer.parseInt(input);
                if (detailCount <= 0) {
                    showAlert("Failed","Quantity must be greater than 0.", Alert.AlertType.ERROR);
                    return;
                }

                // Tạo invoice mới
                LocalDate today = LocalDate.now();
                String invoiceNo = generateInvoiceNo(today);
                Invoice invoice = new Invoice();
                invoice.setInvoiceNo(invoiceNo);
                invoice.setInvoiceDate(today);

                // Gán vào giao diện
                dpDate.setValue(today);
                cbInvoiceNo.getItems().add(invoice);
                cbInvoiceNo.getSelectionModel().select(invoice);

                // Khởi tạo danh sách dòng rỗng
                invoiceDetailDtoList.clear();
                for (int i = 0; i < detailCount; i++) {
                    InvoiceDetailViewDto dto = new InvoiceDetailViewDto();
                    dto.setInvoiceNo(invoiceNo);
                    dto.setInvoiceDate(today);
                    invoiceDetailDtoList.add(dto);
                }

            } catch (NumberFormatException e) {
                showAlert("Failed","Please enter a valid number.", Alert.AlertType.ERROR);
            }
        });
    }


    //Save invoice
    private void SaveInvoice() {
        Invoice invoice = cbInvoiceNo.getSelectionModel().getSelectedItem();
        LocalDate date = dpDate.getValue();

        if (invoice == null || date == null || invoiceDetailDtoList.isEmpty()) {
            showAlert("Error", "Missing invoice data", Alert.AlertType.ERROR);
            return;
        }

        invoice.setInvoiceDate(date);
        invoice.setCreatedAt(LocalDate.now());
        invoice.setStatus("New");

        List<InvoiceDetail> details = invoiceDetailDtoList.stream().map(dto -> {
            InvoiceDetail d = new InvoiceDetail();
            d.setSapPN("dto.getSapCode()");
            d.setQuantity(dto.getQuantity());
            d.setMoq(dto.getMoq());
            d.setTotalReel(dto.getReelQty());
            d.setStatus("New");
            return d;
        }).toList();

        try {
            invoiceService.saveInvoiceWithDetails(invoice, details);
            showAlert("Success", "Saved invoice successfully", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Failed to save invoice: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    //Alert
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }


    //Generate Invoice No
    private String generateInvoiceNo(LocalDate date) {
        String prefix = date.format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));
        int count = 1;
        String invoiceNo;
        while (true) {
            // Tạo mã invoice tạm thời
            invoiceNo = prefix + "-" + String.format("%03d", count);

            // Kiểm tra nếu mã invoice đã tồn tại
            if (!invoiceExists(invoiceNo)) {
                break;
            }
            count++;
        }

        return invoiceNo;
    }

    //Existing Invoice No
    private boolean invoiceExists(String invoiceNo) {
        try {
            Invoice existingInvoice = invoiceService.findByInvoiceNo(invoiceNo);
            return existingInvoice != null;  // Nếu không null thì đã tồn tại
        } catch (EmptyResultDataAccessException e) {
            // Nếu không có kết quả, nghĩa là chưa tồn tại
            return false;
        }
    }


}
