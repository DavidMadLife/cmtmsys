package org.chemtrovina.cmtmsys.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.dto.InvoiceDetailViewDto;
import org.chemtrovina.cmtmsys.model.Invoice;
import org.chemtrovina.cmtmsys.model.InvoiceDetail;
import org.chemtrovina.cmtmsys.model.MOQ;
import org.chemtrovina.cmtmsys.repository.Impl.InvoiceRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.Impl.MOQRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.base.InvoiceRepository;
import org.chemtrovina.cmtmsys.repository.base.MOQRepository;
import org.chemtrovina.cmtmsys.service.Impl.InvoiceServiceImpl;
import org.chemtrovina.cmtmsys.service.Impl.MOQServiceImpl;
import org.chemtrovina.cmtmsys.service.base.InvoiceService;
import org.chemtrovina.cmtmsys.service.base.MOQService;
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
    private MOQService moqService;

    private boolean isDirty = false;
    private boolean isProcessingCancel = false;


    private final ObservableList<InvoiceDetailViewDto> invoiceDetailDtoList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {


        // Setup service
        DataSource dataSource = DataSourceConfig.getDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        InvoiceRepository invoiceRepository = new InvoiceRepositoryImpl(jdbcTemplate);
        invoiceService = new InvoiceServiceImpl(invoiceRepository);
        MOQRepository moqRepository = new MOQRepositoryImpl(jdbcTemplate);
        moqService = new MOQServiceImpl(moqRepository);


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

        //Table action
        tableView.setRowFactory(tv -> {
            TableRow<InvoiceDetailViewDto> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem updateItem = new MenuItem("Update");
            updateItem.setOnAction(event -> {
                InvoiceDetailViewDto selected = row.getItem();
                if (selected != null) {
                    showUpdateDialog(selected);
                    tableView.refresh(); // Cập nhật lại dòng đã sửa
                }
            });

            MenuItem createItem = new MenuItem("Create New Row");
            createItem.setOnAction(event -> {
                Invoice invoice = cbInvoiceNo.getSelectionModel().getSelectedItem();
                LocalDate date = dpDate.getValue();
                if (invoice != null && date != null) {
                    InvoiceDetailViewDto newDto = new InvoiceDetailViewDto();
                    newDto.setInvoiceNo(invoice.getInvoiceNo());
                    newDto.setInvoiceDate(date);
                    invoiceDetailDtoList.add(newDto);
                    isDirty = true;
                } else {
                    showAlert("Error", "Please create or select an invoice first.", Alert.AlertType.ERROR);
                }
            });

            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(event -> {
                InvoiceDetailViewDto selected = row.getItem();
                if (selected != null) {
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Confirm Delete");
                    confirmAlert.setHeaderText("Do you really want to delete this item?");
                    confirmAlert.setContentText("SAP Code: " + selected.getSapCode());

                    confirmAlert.showAndWait().ifPresent(result -> {
                        if (result == ButtonType.OK) {
                            // Lấy ID hoặc thông tin cần thiết để xóa từ DTO
                            int invoiceId = selected.getInvoiceId();
                            System.out.println("Invoice id: " + invoiceId);
                            String sapCode = selected.getSapCode();

                            // Xóa bản ghi trong DB
                            if (invoiceExists(selected.getInvoiceNo())) {
                                invoiceService.deleteInvoiceDetail(invoiceId, sapCode); // gọi phương thức xóa từ service
                            }

                            // Xóa trên UI
                            invoiceDetailDtoList.remove(selected); // xóa bản ghi khỏi TableView
                            isDirty = true;
                        }
                    });
                }
            });


            contextMenu.getItems().addAll(updateItem, createItem, deleteItem);

            row.setContextMenu(contextMenu);
            return row;
        });



        cbInvoiceNo.getSelectionModel().selectedItemProperty().addListener((obs, oldInvoice, newInvoice) -> {
            if (isProcessingCancel) {
                return; // Không làm gì khi đang xử lý Cancel
            }

            if (isDirty) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Unsaved Changes");
                alert.setHeaderText("You have unsaved changes.");
                alert.setContentText("Do you want to save before switching invoices?");

                ButtonType saveBtn = new ButtonType("Save");
                ButtonType discardBtn = new ButtonType("Discard");
                ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(saveBtn, discardBtn, cancelBtn);

                alert.showAndWait().ifPresent(response -> {
                    isProcessingCancel = true; // Bắt đầu xử lý cancel
                    try {
                        if (response == saveBtn) {
                            SaveInvoice();
                            cbInvoiceNo.getSelectionModel().select(newInvoice);
                        } else if (response == discardBtn) {
                            // Xóa invoice ảo nếu chưa tồn tại trong DB
                            if (oldInvoice != null && !invoiceExists(oldInvoice.getInvoiceNo())) {
                                cbInvoiceNo.getItems().remove(oldInvoice); // Xóa invoice ảo khỏi UI
                            }
                            invoiceDetailDtoList.clear(); // Xóa danh sách chi tiết cũ
                            isDirty = false;

                            // Nếu newInvoice không null, load dữ liệu mới
                            if (newInvoice != null) {
                                loadInvoiceDetails(newInvoice.getInvoiceNo());
                                dpDate.setValue(newInvoice.getInvoiceDate());
                            } else {
                                cbInvoiceNo.getSelectionModel().clearSelection();
                                dpDate.setValue(null);

                            }
                        } else {
                            // Quay lại invoice cũ
                            cbInvoiceNo.getSelectionModel().select(oldInvoice);
                        }
                    } finally {
                        isProcessingCancel = false; // Kết thúc xử lý cancel
                    }
                });
            } else {
                // Không có thay đổi, chỉ cần load dữ liệu của invoice đã chọn
                if (newInvoice != null) {
                    loadInvoiceDetails(newInvoice.getInvoiceNo());
                    dpDate.setValue(newInvoice.getInvoiceDate());
                } else {
                    invoiceDetailDtoList.clear();
                }
            }
        });






        // Load toàn bộ invoice từ DB và đưa vào ComboBox
        List<Invoice> invoices = invoiceService.findAll(); // phương thức này cần có trong InvoiceService
        cbInvoiceNo.setItems(FXCollections.observableArrayList(invoices));
    }

    //Load invoice detail by invoice No
    private void loadInvoiceDetails(String invoiceNo) {
        invoiceDetailDtoList.clear(); // clear dữ liệu cũ

        List<InvoiceDetail> details = invoiceService.getInvoiceDetails(invoiceNo);
        if (details == null) {
            return;
        }

        for (InvoiceDetail d : details) {
            InvoiceDetailViewDto dto = new InvoiceDetailViewDto();
            dto.setInvoiceId(d.getInvoiceId());
            dto.setInvoiceNo(invoiceNo);
            dto.setInvoiceDate(dpDate.getValue()); // hoặc d.getInvoice().getInvoiceDate() nếu có
            dto.setSapCode(d.getSapPN());
            dto.setQuantity(d.getQuantity());
            dto.setMoq(d.getMoq());
            dto.setReelQty(d.getTotalReel());

            invoiceDetailDtoList.add(dto);
        }

    }


    //Update dialog
    private void showUpdateDialog(InvoiceDetailViewDto dto) {
        Dialog<InvoiceDetailViewDto> dialog = new Dialog<>();
        dialog.setTitle("Update Invoice Detail");

        // Các field nhập liệu
        TextField sapCodeField = new TextField(dto.getSapCode());
        TextField qtyField = new TextField(String.valueOf(dto.getQuantity()));

        // Các field tự động tính - không cho chỉnh sửa
        TextField moqField = new TextField();
        moqField.setEditable(false);
        TextField reelQtyField = new TextField();
        reelQtyField.setEditable(false);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("SAP Code:"), 0, 0);
        grid.add(sapCodeField, 1, 0);
        grid.add(new Label("Quantity:"), 0, 1);
        grid.add(qtyField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String sapCode = sapCodeField.getText().trim();
                int quantity = parseIntSafe(qtyField.getText());

                if (sapCode.isEmpty() || quantity <= 0) {
                    showAlert("Warning", "Vui lòng nhập SAP Code và Quantity hợp lệ", Alert.AlertType.ERROR);
                    return null;
                }

                MOQ moq = moqService.getMOQbySAPPN(sapCode);
                if (moq == null || moq.getMoq() == null || moq.getMoq() == 0) {
                    showAlert("Lỗi", "Không tìm thấy MOQ hợp lệ cho SAP Code: " + sapCode, Alert.AlertType.ERROR);
                    return null;
                }

                int moqValue = moq.getMoq();
                int reelQty = quantity / moqValue;

                moqField.setText(String.valueOf(moqValue));
                reelQtyField.setText(String.valueOf(reelQty));

                dto.setSapCode(sapCode);
                dto.setQuantity(quantity);
                dto.setMoq(moqValue);
                dto.setReelQty(reelQty);
                isDirty = true;
            }
            return null;
        });

        dialog.showAndWait();
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
        isDirty = true;

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
            d.setSapPN(dto.getSapCode());
            d.setQuantity(dto.getQuantity());
            d.setMoq(dto.getMoq());
            d.setTotalReel(dto.getReelQty());
            d.setStatus("New");
            return d;
        }).toList();

        /*try {
            invoiceService.saveInvoiceWithDetails(invoice, details);
            showAlert("Success", "Saved invoice successfully", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Failed to save invoice: " + e.getMessage(), Alert.AlertType.ERROR);
        }*/

        try {
            if (invoiceExists(invoice.getInvoiceNo())) {
                // Nếu đã có Invoice -> chỉ update InvoiceDetail
                invoiceService.updateInvoiceDetails(invoice.getInvoiceNo(), details);
            } else {
                // Nếu chưa có Invoice -> tạo mới cả invoice và detail
                invoiceService.saveInvoiceWithDetails(invoice, details);
            }

            showAlert("Success", "Saved invoice successfully", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Failed to save invoice: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        isDirty = false;

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
            invoiceNo = prefix + "-" + String.format("%03d", count);

            if (!invoiceExists(invoiceNo) && !invoiceExistsInUI(invoiceNo)) {
                break;
            }
            count++;
        }

        return invoiceNo;
    }

    // Check trùng invoice trong DB
    private boolean invoiceExists(String invoiceNo) {
        try {
            Invoice existingInvoice = invoiceService.findByInvoiceNo(invoiceNo);
            return existingInvoice != null;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    // ✅ Check trùng invoiceNo trên giao diện (comboBox cbInvoiceNo)
    private boolean invoiceExistsInUI(String invoiceNo) {
        return cbInvoiceNo.getItems().stream()
                .anyMatch(inv -> invoiceNo.equals(inv.getInvoiceNo()));
    }


    private int parseIntSafe(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
