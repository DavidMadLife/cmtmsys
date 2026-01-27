package org.chemtrovina.cmtmsys.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.dto.InvoiceDataDto;
import org.chemtrovina.cmtmsys.dto.InvoiceDetailViewDto;
import org.chemtrovina.cmtmsys.model.Invoice;
import org.chemtrovina.cmtmsys.model.InvoiceDetail;
import org.chemtrovina.cmtmsys.model.MOQ;
import org.chemtrovina.cmtmsys.model.enums.UserRole;
import org.chemtrovina.cmtmsys.repository.Impl.InvoiceRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.Impl.MOQRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.base.InvoiceRepository;
import org.chemtrovina.cmtmsys.repository.base.MOQRepository;
import org.chemtrovina.cmtmsys.security.RequiresRoles;
import org.chemtrovina.cmtmsys.service.Impl.InvoiceServiceImpl;
import org.chemtrovina.cmtmsys.service.Impl.MOQServiceImpl;
import org.chemtrovina.cmtmsys.service.base.InvoiceService;
import org.chemtrovina.cmtmsys.service.base.MOQService;
import org.chemtrovina.cmtmsys.utils.AutoCompleteUtils;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.controlsfx.control.textfield.TextFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiresRoles({
        UserRole.ADMIN,
        UserRole.GENERALWAREHOUSE,
        UserRole.INVENTORY
})

@Component
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
    @FXML private TableColumn<InvoiceDetailViewDto, String> colInvoicePN;


    @FXML private Text txtTotalQuantity;
    @FXML private Text txtTotalReelQty;


    @FXML private TableView<InvoiceDataDto> tblData;
    @FXML private TableColumn<InvoiceDataDto, String> colSapCode;
    @FXML private TableColumn<InvoiceDataDto, String> colQuantity;




    private boolean isDirty = false;
    private boolean isProcessingCancel = false;

    private File selectedFile;


    private final ObservableList<InvoiceDetailViewDto> invoiceDetailDtoList = FXCollections.observableArrayList();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final InvoiceService invoiceService;
    private final MOQService moqService;

    @Autowired
    public InvoiceController(InvoiceService invoiceService, MOQService moqService) {
        this.invoiceService = invoiceService;
        this.moqService = moqService;
    }


    @FXML
    public void initialize() {
        initTableView();
        initEventHandlers();
        initComboBox();
        loadInvoiceList();
        startAutoGC();
        FxClipboardUtils.enableCopyShortcut(tblData);
        FxClipboardUtils.enableCopyShortcut(tableView);

        tblData.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.F) {
                openSearchDialog();
            }
        });

        tableView.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.F) {
                openSearchDialog();
            }
        });
    }

    private void initTableView() {
        tableView.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.F) {
                openSearchDialog();
            }
        });


        colInvoicePN.setCellValueFactory(new PropertyValueFactory<>("invoicePN"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
        colInvoiceNo.setCellValueFactory(new PropertyValueFactory<>("invoiceNo"));
        colSAPCode.setCellValueFactory(new PropertyValueFactory<>("sapCode"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colMOQ.setCellValueFactory(new PropertyValueFactory<>("moq"));
        colReelQty.setCellValueFactory(new PropertyValueFactory<>("reelQty"));

        tableView.setItems(invoiceDetailDtoList);

        tableView.setRowFactory(tv -> {
            TableRow<InvoiceDetailViewDto> row = new TableRow<>();

            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null) {
                    row.setContextMenu(null); // không tạo context nếu không có item
                } else {
                    ContextMenu contextMenu = new ContextMenu();

                    MenuItem updateItem = new MenuItem("Update");
                    updateItem.setOnAction(e -> {
                        showUpdateDialog(newItem);
                        tableView.refresh();
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
                    deleteItem.setOnAction(e -> handleDelete(newItem));

                    contextMenu.getItems().addAll(updateItem, createItem, deleteItem);
                    row.setContextMenu(contextMenu);
                }
            });

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
        cbInvoiceNo.getItems().clear(); // thêm dòng này trước khi set list
        List<Invoice> invoices = invoiceService.findAll();
        cbInvoiceNo.setItems(FXCollections.observableArrayList(invoices));
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Load invoice detail by invoice No
    private void loadInvoiceDetails(String invoiceNo) {
        invoiceDetailDtoList.clear();
        tblData.getItems().clear();

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
            dto.setInvoicePN(invoice.getInvoicePN());
            dto.setInvoiceDate(invoice.getInvoiceDate()); // hoặc d.getInvoice().getInvoiceDate() nếu có
            dto.setSapCode(d.getSapPN());
            dto.setQuantity(d.getQuantity());
            dto.setMoq(d.getMoq());
            dto.setReelQty(d.getTotalReel());

            invoiceDetailDtoList.add(dto);
        }

        updateTotals();

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


        AutoCompleteUtils.AutoCompletionBinding binding = AutoCompleteUtils.setupAutoComplete(sapCodeField, sapCodeSuggestions);


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
        dialog.setOnHidden(e -> binding.dispose());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////




    //Create new
    private void CreateInvoice() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New Invoice");
        dialog.setHeaderText("Enter InvoicePN and Number of Items");

        Label labelPN = new Label("InvoicePN:");
        TextField txtPN = new TextField();

        Label labelQty = new Label("Item Quantity:");
        TextField txtQty = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(labelPN, 0, 0);
        grid.add(txtPN, 1, 0);
        grid.add(labelQty, 0, 1);
        grid.add(txtQty, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String invoicePN = txtPN.getText().trim();
                String qtyInput = txtQty.getText().trim();

                if (invoicePN.isEmpty() || qtyInput.isEmpty()) {
                    showAlert("Input Error", "Please enter both InvoicePN and quantity.", Alert.AlertType.ERROR);
                    return;
                }

                try {
                    int detailCount = Integer.parseInt(qtyInput);
                    if (detailCount <= 0) {
                        showAlert("Input Error", "Quantity must be greater than 0.", Alert.AlertType.ERROR);
                        return;
                    }

                    LocalDate today = LocalDate.now();
                    String invoiceNo = generateInvoiceNo(today);

                    Invoice invoice = new Invoice();
                    invoice.setInvoiceNo(invoiceNo);
                    invoice.setInvoicePN(invoicePN);
                    invoice.setInvoiceDate(today);

                    dpDate.setValue(today);
                    cbInvoiceNo.getItems().add(invoice);
                    cbInvoiceNo.getSelectionModel().select(invoice);

                    invoiceDetailDtoList.clear();
                    for (int i = 0; i < detailCount; i++) {
                        InvoiceDetailViewDto dto = new InvoiceDetailViewDto();
                        dto.setInvoiceNo(invoiceNo);
                        dto.setInvoicePN(invoicePN); // Gán vào DTO
                        dto.setInvoiceDate(today);
                        invoiceDetailDtoList.add(dto);
                    }

                    isDirty = true;

                } catch (NumberFormatException e) {
                    showAlert("Input Error", "Please enter a valid number for quantity.", Alert.AlertType.ERROR);
                }
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

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter InvoicePN");
        dialog.setHeaderText("Please input InvoicePN for this import:");
        dialog.setContentText("InvoicePN:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().trim().isEmpty()) {
            showAlert("Error", "InvoicePN is required to import.", Alert.AlertType.ERROR);
            return;
        }
        String invoicePN = result.get().trim();

        try (FileInputStream fis = new FileInputStream(selectedFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            List<InvoiceDetail> detailList = new ArrayList<>();
            ObservableList<InvoiceDataDto> importedData = FXCollections.observableArrayList();

            String invoiceNo = generateInvoiceNo(LocalDate.now());
            Invoice invoice = new Invoice();
            invoice.setInvoiceNo(invoiceNo);
            invoice.setInvoicePN(invoicePN);
            invoice.setInvoiceDate(LocalDate.now());
            invoice.setCreatedAt(LocalDate.now());
            invoice.setStatus("New");

            for (Row row : sheet) {
                if (row == null || row.getRowNum() == 0) continue;

                Cell sapCell = row.getCell(0);
                Cell qtyCell = row.getCell(1);

                if (sapCell == null || qtyCell == null) continue;

                String sapCode = "";
                try {
                    if (sapCell.getCellType() == CellType.STRING) {
                        sapCode = sapCell.getStringCellValue().trim();
                    } else if (sapCell.getCellType() == CellType.NUMERIC) {
                        sapCode = String.valueOf((int) sapCell.getNumericCellValue());
                    } else {
                        continue;
                    }
                } catch (Exception ex) {
                    System.out.println("Lỗi đọc SAP Code tại dòng " + (row.getRowNum() + 1));
                    continue;
                }

                int quantity = 0;
                try {
                    if (qtyCell.getCellType() == CellType.NUMERIC) {
                        quantity = (int) qtyCell.getNumericCellValue();
                    } else if (qtyCell.getCellType() == CellType.STRING) {
                        String qtyStr = qtyCell.getStringCellValue().replace(",", "").trim();
                        quantity = Integer.parseInt(qtyStr);
                    } else {
                        continue;
                    }
                } catch (Exception ex) {
                    System.out.println("Lỗi đọc Quantity tại dòng " + (row.getRowNum() + 1));
                    continue;
                }

                MOQ moq = moqService.getMOQbySAPPN(sapCode);
                if (moq == null || moq.getMoq() == null || moq.getMoq() == 0) {
                    showAlert("Warning", "Missing MOQ for: " + sapCode, Alert.AlertType.WARNING);
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

            if (detailList.isEmpty()) {
                showAlert("Error", "No valid data found to import.", Alert.AlertType.ERROR);
                return;
            }

            invoiceService.saveInvoiceWithDetails(invoice, detailList);

            cbInvoiceNo.getItems().add(invoice);
            cbInvoiceNo.getSelectionModel().select(invoice);
            dpDate.setValue(invoice.getInvoiceDate());
            invoiceDetailDtoList.clear();

            for (InvoiceDetail detail : detailList) {
                InvoiceDetailViewDto dto = new InvoiceDetailViewDto();
                dto.setInvoiceNo(invoice.getInvoiceNo());
                dto.setInvoicePN(invoicePN);
                dto.setInvoiceDate(invoice.getInvoiceDate());
                dto.setSapCode(detail.getSapPN());
                dto.setQuantity(detail.getQuantity());
                dto.setMoq(detail.getMoq());
                dto.setReelQty(detail.getTotalReel());
                dto.setInvoiceId(invoice.getId());
                invoiceDetailDtoList.add(dto);
            }

            tblData.getItems().clear();
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

    private void clearUIState() {
        cbInvoiceNo.getSelectionModel().clearSelection();
        dpDate.setValue(null);
        invoiceDetailDtoList.clear();
        tblData.getItems().clear();
        txtFileName.setText("");
        btnImportData.setDisable(true);
    }

    private void handleDelete(InvoiceDetailViewDto selected) {
        if (selected == null) return;

        int invoiceId = selected.getInvoiceId();
        String invoiceNo = selected.getInvoiceNo();
        int historyCount = invoiceService.countHistoryByInvoiceId(invoiceId);

        if (historyCount > 0) {
            showAlert("Cannot Delete", "Invoice '" + invoiceNo + "' is referenced in History. You cannot delete any items in this invoice.", Alert.AlertType.WARNING);
            return;
        }

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
                                    clearUIState(); // gọi hàm dọn toàn bộ UI
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


    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void startAutoGC() {
        scheduler.scheduleAtFixedRate(() -> {
            System.gc();
            System.out.println("Triggered GC at: " + java.time.LocalTime.now());
        }, 20, 20, TimeUnit.SECONDS);
        long heapSize = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.println("Heap used: " + heapSize / 1024 / 1024 + " MB");

    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void updateTotals() {
        int totalQuantity = 0;
        int totalReelQty = 0;

        // Duyệt qua các chi tiết trong bảng và tính tổng
        for (InvoiceDetailViewDto dto : invoiceDetailDtoList) {
            totalQuantity += dto.getQuantity();
            totalReelQty += dto.getReelQty();
        }

        // Cập nhật vào Text trong giao diện
        txtTotalQuantity.setText("Total Quantity: " + totalQuantity);
        txtTotalReelQty.setText("Total Reel Qty: " + totalReelQty);
    }



    public void shutdown() {
        scheduler.shutdown();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void openSearchDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Advanced Search");
        dialog.setHeaderText("Search Invoices by Criteria");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField txtInvoicePN = new TextField();
        TextField txtSAPCode = new TextField();
        TextField txtMakerPN = new TextField();
        DatePicker dpSearchDate = new DatePicker();

        grid.add(new Label("InvoicePN:"), 0, 0);
        grid.add(txtInvoicePN, 1, 0);
        grid.add(new Label("SAP Code:"), 0, 1);
        grid.add(txtSAPCode, 1, 1);
        grid.add(new Label("MakerPN:"), 0, 2);
        grid.add(txtMakerPN, 1, 2);
        grid.add(new Label("Invoice Date:"), 0, 3);
        grid.add(dpSearchDate, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String invoicePN = txtInvoicePN.getText().trim();
                String sapCode = txtSAPCode.getText().trim();
                String makerPN = txtMakerPN.getText().trim();
                LocalDate date = dpSearchDate.getValue();

                List<InvoiceDetailViewDto> result = invoiceService.searchByFields(invoicePN, sapCode, makerPN, date);

                cbInvoiceNo.getSelectionModel().clearSelection();
                dpDate.setValue(null);
                invoiceDetailDtoList.clear();
                invoiceDetailDtoList.addAll(result);
            }
        });
        updateTotals();
    }

}
