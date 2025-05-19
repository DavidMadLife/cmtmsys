package org.chemtrovina.cmtmsys.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.dto.InvoiceDataDto;
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
import org.chemtrovina.cmtmsys.utils.AutoCompleteUtils;
import org.controlsfx.control.textfield.TextFields;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InvoiceController {



    @FXML private DatePicker dpDate;
    @FXML private ComboBox<Invoice> cbInvoiceNo;
    @FXML private Button btnNew;
    @FXML private Button btnSave;
    @FXML private Button btnImportData;
    @FXML private Button btnChooseFile;
    @FXML private Button btnDeleteInvoice;
    @FXML private Text txtFileName;

    @FXML private TableView<InvoiceDetailViewDto> tableView;
    @FXML private TableColumn<InvoiceDetailViewDto, String> colInvoiceNo;
    @FXML private TableColumn<InvoiceDetailViewDto, String> colSAPCode;
    @FXML private TableColumn<InvoiceDetailViewDto, Integer> colQty;
    @FXML private TableColumn<InvoiceDetailViewDto, Integer> colMOQ;
    @FXML private TableColumn<InvoiceDetailViewDto, Integer> colReelQty;
    @FXML private TableColumn<InvoiceDetailViewDto, LocalDate> colDate;

    @FXML private TableView<InvoiceDataDto> tblData;
    @FXML private TableColumn<InvoiceDataDto, String> colSapCode;
    @FXML private TableColumn<InvoiceDataDto, String> colQuantity;


    private InvoiceService invoiceService;
    private MOQService moqService;

    private boolean isDirty = false;
    private boolean isProcessingCancel = false;

    private File selectedFile;


    private final ObservableList<InvoiceDetailViewDto> invoiceDetailDtoList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        initServices();
        initTableView();
        initEventHandlers();
        initComboBox();
        loadInvoiceList();
    }

    private void initServices() {
        DataSource dataSource = DataSourceConfig.getDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        InvoiceRepository invoiceRepository = new InvoiceRepositoryImpl(jdbcTemplate);
        invoiceService = new InvoiceServiceImpl(invoiceRepository);

        MOQRepository moqRepository = new MOQRepositoryImpl(jdbcTemplate);
        moqService = new MOQServiceImpl(moqRepository);
    }

    private void initTableView() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
        colInvoiceNo.setCellValueFactory(new PropertyValueFactory<>("invoiceNo"));
        colSAPCode.setCellValueFactory(new PropertyValueFactory<>("sapCode"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colMOQ.setCellValueFactory(new PropertyValueFactory<>("moq"));
        colReelQty.setCellValueFactory(new PropertyValueFactory<>("reelQty"));

        tableView.setItems(invoiceDetailDtoList);

        tableView.setRowFactory(tv -> {
            TableRow<InvoiceDetailViewDto> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem updateItem = new MenuItem("Update");
            updateItem.setOnAction(e -> {
                InvoiceDetailViewDto selected = row.getItem();
                if (selected != null) {
                    showUpdateDialog(selected);
                    tableView.refresh();
                }
            });

            MenuItem createItem = new MenuItem("Create New Row");
            createItem.setOnAction(e -> {
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
            deleteItem.setOnAction(e -> {
                InvoiceDetailViewDto selected = row.getItem();
                if (selected != null) {
                    // Kiểm tra liên kết đến History
                    int invoiceId = selected.getInvoiceId();
                    String invoiceNo = selected.getInvoiceNo();
                    int historyCount = invoiceService.countHistoryByInvoiceId(invoiceId);

                    if (historyCount > 0) {
                        showAlert("Cannot Delete", "Invoice '" + invoiceNo + "' is referenced in History. You cannot delete any items in this invoice.", Alert.AlertType.WARNING);
                        return;
                    }

                    // Hỏi xác nhận xóa dòng
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirm Delete");
                    alert.setHeaderText("Do you really want to delete this item?");
                    alert.setContentText("SAP Code: " + selected.getSapCode());

                    alert.showAndWait().ifPresent(result -> {
                        if (result == ButtonType.OK) {
                            try {
                                if (invoiceExists(invoiceNo)) {
                                    invoiceService.deleteInvoiceDetail(invoiceId, selected.getSapCode());
                                }
                                invoiceDetailDtoList.remove(selected);

                                // Nếu là dòng cuối cùng
                                if (invoiceDetailDtoList.isEmpty()) {
                                    Alert confirmDeleteInvoice = new Alert(Alert.AlertType.CONFIRMATION);
                                    confirmDeleteInvoice.setTitle("Delete Invoice?");
                                    confirmDeleteInvoice.setHeaderText("This is the last item in the invoice.");
                                    confirmDeleteInvoice.setContentText("Do you want to delete the entire invoice?");

                                    confirmDeleteInvoice.showAndWait().ifPresent(result2 -> {
                                        if (result2 == ButtonType.OK) {
                                            try {
                                                invoiceService.deleteInvoice(invoiceId);
                                                cbInvoiceNo.getItems().removeIf(i -> i.getInvoiceNo().equals(invoiceNo));
                                                cbInvoiceNo.getSelectionModel().clearSelection();
                                                dpDate.setValue(null);
                                                invoiceDetailDtoList.clear();
                                                showAlert("Deleted", "Invoice deleted successfully.", Alert.AlertType.INFORMATION);
                                            } catch (IllegalStateException ex) {
                                                showAlert("Cannot Delete Invoice", ex.getMessage(), Alert.AlertType.WARNING);
                                            } catch (Exception ex) {
                                                showAlert("Error", "Unexpected error during deletion: " + ex.getMessage(), Alert.AlertType.ERROR);
                                            }
                                        }
                                    });
                                }

                                isDirty = false;

                            } catch (Exception ex) {
                                showAlert("Error", "Error deleting item: " + ex.getMessage(), Alert.AlertType.ERROR);
                            }
                        }
                    });
                }
            });


            contextMenu.getItems().addAll(updateItem, createItem, deleteItem);
            row.setContextMenu(contextMenu);
            return row;
        });
    }


    private void initEventHandlers() {
        btnNew.setOnAction(e -> CreateInvoice());
        btnSave.setOnAction(e -> SaveInvoice());
        btnChooseFile.setOnAction(e -> chooseFile());
        btnImportData.setOnAction(e -> importFromExcel());
        btnDeleteInvoice.setOnAction(e -> deleteInvocie());
        btnImportData.setDisable(true);
    }

    private void initComboBox() {
        cbInvoiceNo.setConverter(new StringConverter<Invoice>() {
            @Override
            public String toString(Invoice invoice) {
                return invoice != null ? invoice.getInvoiceNo() : "";
            }

            @Override
            public Invoice fromString(String string) {
                return cbInvoiceNo.getItems().stream()
                        .filter(inv -> inv.getInvoiceNo().equals(string))
                        .findFirst().orElse(null);
            }
        });

        cbInvoiceNo.getSelectionModel().selectedItemProperty().addListener((obs, oldInvoice, newInvoice) -> {
            if (isProcessingCancel) return;

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
                    isProcessingCancel = true;
                    try {
                        if (response == saveBtn) {
                            SaveInvoice();
                            cbInvoiceNo.getSelectionModel().select(newInvoice);
                        } else if (response == discardBtn) {
                            if (oldInvoice != null && !invoiceExists(oldInvoice.getInvoiceNo())) {
                                cbInvoiceNo.getItems().remove(oldInvoice);
                            }
                            invoiceDetailDtoList.clear();
                            isDirty = false;
                            if (newInvoice != null) {
                                loadInvoiceDetails(newInvoice.getInvoiceNo());
                                dpDate.setValue(newInvoice.getInvoiceDate());
                            } else {
                                cbInvoiceNo.getSelectionModel().clearSelection();
                                dpDate.setValue(null);
                            }
                        } else {
                            cbInvoiceNo.getSelectionModel().select(oldInvoice);
                        }
                    } finally {
                        isProcessingCancel = false;
                    }
                });
            } else {
                if (newInvoice != null) {
                    loadInvoiceDetails(newInvoice.getInvoiceNo());
                    dpDate.setValue(newInvoice.getInvoiceDate());
                } else {
                    invoiceDetailDtoList.clear();
                }
            }
        });
    }


    private void loadInvoiceList() {
        List<Invoice> invoices = invoiceService.findAll();
        cbInvoiceNo.setItems(FXCollections.observableArrayList(invoices));
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Load invoice detail by invoice No
    private void loadInvoiceDetails(String invoiceNo) {
        invoiceDetailDtoList.clear(); // clear dữ liệu cũ

        Invoice invoice = invoiceService.findByInvoiceNo(invoiceNo); // Thêm dòng này
        if (invoice == null) return;
        dpDate.setValue(invoice.getInvoiceDate());

        List<InvoiceDetail> details = invoiceService.getInvoiceDetails(invoiceNo);
        if (details == null) {
            return;
        }

        for (InvoiceDetail d : details) {
            InvoiceDetailViewDto dto = new InvoiceDetailViewDto();
            dto.setInvoiceId(d.getInvoiceId());
            dto.setInvoiceNo(invoiceNo);
            dto.setInvoiceDate(invoice.getInvoiceDate()); // hoặc d.getInvoice().getInvoiceDate() nếu có
            dto.setSapCode(d.getSapPN());
            dto.setQuantity(d.getQuantity());
            dto.setMoq(d.getMoq());
            dto.setReelQty(d.getTotalReel());

            invoiceDetailDtoList.add(dto);
        }

    }
    //////////////////////////////////////////////////////////////////////////////////////////////

    //Update dialog
    private void showUpdateDialog(InvoiceDetailViewDto dto) {
        Dialog<InvoiceDetailViewDto> dialog = new Dialog<>();
        dialog.setTitle("Update Invoice Detail");

        List<String> sapCodeSuggestions = moqService.getAllSapCodes(); // giả sử hàm này trả về List<String>


        // Các field nhập liệu
        TextField sapCodeField = new TextField(dto.getSapCode());
        TextField qtyField = new TextField(String.valueOf(dto.getQuantity()));


        AutoCompleteUtils.setupAutoComplete(sapCodeField, sapCodeSuggestions);


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
                    showAlert("Warning", "Please input SAP Code and Quantity available !", Alert.AlertType.ERROR);
                    return null;
                }

                MOQ moq = moqService.getMOQbySAPPN(sapCode);
                if (moq == null || moq.getMoq() == null || moq.getMoq() == 0) {
                    showAlert("Warning", "Not found MOQ for SAP Code: " + sapCode, Alert.AlertType.ERROR);
                    return null;
                }

                int moqValue = moq.getMoq();
                int reelQty = quantity / moqValue;

                if (quantity % moqValue != 0) {

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Uneven Quantity");
                    alert.setHeaderText("Quantity is not evenly divisible by MOQ (" + moqValue + ")");
                    alert.setContentText("Do you want to keep it and round up the reel count, or re-enter quantity?");

                    ButtonType keepButton = new ButtonType("Keep", ButtonBar.ButtonData.OK_DONE);
                    ButtonType retryButton = new ButtonType("Re-enter", ButtonBar.ButtonData.CANCEL_CLOSE);

                    alert.getButtonTypes().setAll(keepButton, retryButton);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == retryButton) {
                        return null;
                    }

                    // Nếu chọn Keep thì làm tròn cuộn (lấy trần)
                    reelQty = (int) Math.ceil((double) quantity / moqValue);
                }

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

    //////////////////////////////////////////////////////////////////////////////////////////////




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

    ///////////////////////////////////////////////////////////////////////////
    private void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file Excel");

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx");
        fileChooser.getExtensionFilters().add(extFilter);

        Stage stage = (Stage) btnChooseFile.getScene().getWindow();

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedFile = file;
            txtFileName.setText(file.getName());
            btnImportData.setDisable(false);
        } else {
            txtFileName.setText("File not selected");
            btnImportData.setDisable(true);
        }
    }

    private void importFromExcel() {
        if (selectedFile == null) {
            showAlert("Error", "No file selected", Alert.AlertType.ERROR);
            return;
        }

        try (FileInputStream fis = new FileInputStream(selectedFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            List<InvoiceDetail> detailList = new ArrayList<>();
            ObservableList<InvoiceDataDto> importedData = FXCollections.observableArrayList();

            String invoiceNo = generateInvoiceNo(LocalDate.now());
            Invoice invoice = new Invoice();
            invoice.setInvoiceNo(invoiceNo);
            invoice.setInvoiceDate(LocalDate.now());
            invoice.setCreatedAt(LocalDate.now());
            invoice.setStatus("New");

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // bỏ header

                String sapCode = row.getCell(0).getStringCellValue().trim();
                int quantity = (int) row.getCell(1).getNumericCellValue();

                MOQ moq = moqService.getMOQbySAPPN(sapCode);
                if (moq == null || moq.getMoq() == null || moq.getMoq() == 0) {
                    showAlert("Error", "Missing MOQ for: " + sapCode, Alert.AlertType.WARNING);
                    continue;
                }

                int moqValue = moq.getMoq();
                int reelQty = (int) Math.ceil((double) quantity / moqValue);

                InvoiceDetail detail = new InvoiceDetail();
                detail.setSapPN(sapCode);
                detail.setQuantity(quantity);
                detail.setMoq(moqValue);
                detail.setTotalReel(reelQty);
                detail.setStatus("New");

                detailList.add(detail);
                importedData.add(new InvoiceDataDto(sapCode, quantity));

            }


            // Lưu invoice và chi tiết vào DB
            invoiceService.saveInvoiceWithDetails(invoice, detailList);

            // Hiển thị invoice vừa import
            cbInvoiceNo.getItems().add(invoice);
            cbInvoiceNo.getSelectionModel().select(invoice);
            dpDate.setValue(invoice.getInvoiceDate());
            invoiceDetailDtoList.clear();
            for (InvoiceDetail detail : detailList) {
                InvoiceDetailViewDto dto = new InvoiceDetailViewDto();
                dto.setInvoiceNo(invoice.getInvoiceNo());
                dto.setInvoiceDate(invoice.getInvoiceDate());
                dto.setSapCode(detail.getSapPN());
                dto.setQuantity(detail.getQuantity());
                dto.setMoq(detail.getMoq());
                dto.setReelQty(detail.getTotalReel());
                dto.setInvoiceId(invoice.getId());
                invoiceDetailDtoList.add(dto);
            }

            // Hiển thị lên tblData
            tblData.setItems(importedData);
            colSapCode.setCellValueFactory(new PropertyValueFactory<>("sapCode"));
            colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

            showAlert("Success", "Import completed successfully", Alert.AlertType.INFORMATION);

        } catch (IOException e) {
            showAlert("Error", "File error: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Error", "Unexpected error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void deleteInvocie() {
        Invoice selectedInvoice = cbInvoiceNo.getSelectionModel().getSelectedItem();
        if (selectedInvoice == null) {
            showAlert("Warning", "Please select an invoice to delete.", Alert.AlertType.WARNING);
            return;
        }

        // Kiểm tra xem invoice đã được tham chiếu trong History chưa
        int historyCount = invoiceService.countHistoryByInvoiceId(selectedInvoice.getId());
        if (historyCount > 0) {
            showAlert("Cannot Delete", "This invoice is referenced in History and cannot be deleted.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete Invoice");
        confirm.setHeaderText("Are you sure you want to delete this invoice?");
        confirm.setContentText("Invoice No: " + selectedInvoice.getInvoiceNo());

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    invoiceService.deleteInvoice(selectedInvoice.getId());
                    cbInvoiceNo.getItems().removeIf(i -> i.getInvoiceNo().equals(selectedInvoice.getInvoiceNo()));
                    cbInvoiceNo.getSelectionModel().clearSelection();
                    dpDate.setValue(null);
                    invoiceDetailDtoList.clear();
                    showAlert("Deleted", "Invoice deleted successfully.", Alert.AlertType.INFORMATION);
                } catch (IllegalStateException ex) {
                    showAlert("Cannot Delete", ex.getMessage(), Alert.AlertType.WARNING);
                } catch (Exception ex) {
                    showAlert("Error", "Unexpected error while deleting invoice: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }
}
