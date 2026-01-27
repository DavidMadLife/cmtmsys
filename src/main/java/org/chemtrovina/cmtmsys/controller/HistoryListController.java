package org.chemtrovina.cmtmsys.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chemtrovina.cmtmsys.model.History;
import org.chemtrovina.cmtmsys.model.enums.UserRole;
import org.chemtrovina.cmtmsys.security.RequiresRoles;
import org.chemtrovina.cmtmsys.service.base.HistoryService;
import org.chemtrovina.cmtmsys.service.base.InvoiceService;
import org.chemtrovina.cmtmsys.service.base.MOQService;
import org.chemtrovina.cmtmsys.utils.AutoCompleteUtils;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RequiresRoles({
        UserRole.ADMIN,
        UserRole.GENERALWAREHOUSE,
        UserRole.INVENTORY
})

@Component
public class HistoryListController {
    @FXML private TableView<History> historyDateTableView;
    @FXML private TableColumn<History, String> dateColumn;
    @FXML private TableColumn<History, String> makerColumn;
    @FXML private TableColumn<History, String> makerPNColumn;
    @FXML private TableColumn<History, String> sapPNColumn;
    @FXML private TableColumn<History, Integer> quantityColumn;
    @FXML private TableColumn<History, String> invoiceNoColumn;
    @FXML private TableColumn<History, String> invoicePNColumn;
    @FXML private TableColumn<History, String> mslColumn, specColumn;
    @FXML private TableColumn<History, String> timeColumn;
    @FXML private TableColumn<History, String> employeeIdColumn; // thêm dòng này
    @FXML private TableColumn<History, String> statusColumn;



    @FXML private TextField invoiceNoField, makerField, pnField, sapField, mslField, invoicePNField;
    @FXML private DatePicker dateTimePicker;
    @FXML private CheckBox invoiceNoCheckBox, makerCheckBox, pnCheckBox, sapCheckBox, dateCheckBox, mslCheckBox, invoicePNCheckBox;
    @FXML private Button searchBtn, clearBtn, importExcelBtn;
    @FXML private Label totalQuantityLabel, totalReelQuantityLabel;

    private final ObservableList<History> historyObservableList = FXCollections.observableArrayList();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final HistoryService historyService;
    private final InvoiceService invoiceService;
    private final MOQService moqService;

    @Autowired
    public HistoryListController(HistoryService historyService, InvoiceService invoiceService, MOQService moqService) {
        this.historyService = historyService;
        this.invoiceService = invoiceService;
        this.moqService = moqService;
    }

    @FXML
    public void initialize() {
        setupTable();
        setupAutoCompleteFields();
        setupSearchShortcut();
        searchBtn.setOnAction(e -> onSearch());
        clearBtn.setOnAction(e -> clearFields());
        importExcelBtn.setOnAction(e -> onExportExcel());
        //startAutoGC();

        FxClipboardUtils.enableCopyShortcut(historyDateTableView);

        historyDateTableView.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.F) {
                openSearchDialog();
            }
        });

        setupContextMenu();

    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Xóa dòng này");

        deleteItem.setOnAction(e -> {
            History selected = historyDateTableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Xác nhận");
                alert.setHeaderText("Bạn có chắc muốn xoá dòng này không?");

                alert.showAndWait().ifPresent(result -> {
                    if (result == ButtonType.OK) {
                        historyService.deleteById(selected.getId()); // Đảm bảo có id trong model
                        historyObservableList.remove(selected);
                        historyDateTableView.getItems().remove(selected);
                    }
                });
            }
        });

        contextMenu.getItems().add(deleteItem);

        historyDateTableView.setRowFactory(tv -> {
            TableRow<History> row = new TableRow<>();
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    row.contextMenuProperty().set(contextMenu);
                } else {
                    row.contextMenuProperty().set(null);
                }
            });
            return row;
        });




    }

    private void setupTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date")); // cần có field date dạng String
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        makerColumn.setCellValueFactory(new PropertyValueFactory<>("maker"));
        makerPNColumn.setCellValueFactory(new PropertyValueFactory<>("makerPN"));
        sapPNColumn.setCellValueFactory(new PropertyValueFactory<>("sapPN"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        invoiceNoColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceNo"));
        invoicePNColumn.setCellValueFactory(new PropertyValueFactory<>("invoicePN"));
        mslColumn.setCellValueFactory(new PropertyValueFactory<>("MSL"));
        specColumn.setCellValueFactory(new PropertyValueFactory<>("spec"));
        employeeIdColumn.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        historyDateTableView.setEditable(true);
        statusColumn.setEditable(true);

        statusColumn.setCellFactory(col -> new ComboBoxTableCell<History, String>(
                "Scanned", "NG", "DUPLICATE", "NOT_EXIST", "NONE"
        ) {
            @Override
            public void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(status);

                switch (status) {
                    case "NG" -> {
                        setStyle("-fx-background-color: #ffe5e5; -fx-text-fill: #b00020; -fx-font-weight: bold;");
                    }
                    default -> {
                        setStyle("-fx-background-color: #e9fbe9; -fx-text-fill: #0b6b0b;");
                    }
                }
            }
        });


        statusColumn.setOnEditCommit(e -> {
            History h = e.getRowValue();
            String newStatus = e.getNewValue();

            h.setStatus(newStatus);

            // tận dụng updateHistory()
            historyService.updateHistory(h);

            historyDateTableView.refresh();
        });





    }

    private void setupAutoCompleteFields() {
        AutoCompleteUtils.setupAutoComplete(invoiceNoField, invoiceService.getAllInvoiceNos());
        AutoCompleteUtils.setupAutoComplete(sapField, moqService.getAllSapCodes());
        AutoCompleteUtils.setupAutoComplete(makerField, moqService.getAllMakers());
        AutoCompleteUtils.setupAutoComplete(pnField, moqService.getAllMakerPNs());
        AutoCompleteUtils.setupAutoComplete(mslField, moqService.getAllMSLs());
        AutoCompleteUtils.setupAutoComplete(invoicePNField, invoiceService.getAllInvoicePNs());
    }

    private void setupSearchShortcut() {
        historyDateTableView.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.F) {
                openSearchDialog();
            }
        });
    }

    private void openSearchDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Search");
        dialog.setHeaderText("Search by SAP Code or Maker Code");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField txtSapCode = new TextField();
        TextField txtMakerCode = new TextField();
        TextField txtInvoiceNo = new TextField();
        TextField txtInvoicePN = new TextField();
        TextField txtMsl = new TextField();

        grid.add(new Label("SAP Code:"), 0, 0);
        grid.add(txtSapCode, 1, 0);
        grid.add(new Label("Maker Code:"), 0, 1);
        grid.add(txtMakerCode, 1, 1);
        grid.add(new Label("Invoice No:"), 0, 2);
        grid.add(txtInvoiceNo, 1, 2);
        grid.add(new Label("Invoice PN:"), 0, 3);
        grid.add(txtInvoicePN, 1, 3);
        grid.add(new Label("MSL:"), 0, 4);
        grid.add(txtMsl, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                searchFromData(txtInvoiceNo.getText().trim(), txtMakerCode.getText().trim(), txtSapCode.getText().trim(), txtMsl.getText().trim(), txtInvoicePN.getText().trim());
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void searchFromData(String invoiceNo, String makerCode, String sapCode, String msl, String invoicePN) {
        List<History> result = historyService.searchHistory(invoiceNo, makerCode, null, sapCode, null, msl, invoicePN);
        ObservableList<History> observableList = FXCollections.observableArrayList(result);
        historyDateTableView.setItems(observableList);
    }

    private void onSearch() {
        String invoiceNo = invoiceNoCheckBox.isSelected() ? invoiceNoField.getText() : null;
        String maker = makerCheckBox.isSelected() ? makerField.getText() : null;
        String pn = pnCheckBox.isSelected() ? pnField.getText() : null;
        String sap = sapCheckBox.isSelected() ? sapField.getText() : null;
        LocalDate date = dateCheckBox.isSelected() ? dateTimePicker.getValue() : null;
        String MSL = mslCheckBox.isSelected() ? mslField.getText() : null;
        String invoicePN = invoicePNCheckBox.isSelected() ? invoicePNField.getText() : null;

        List<History> result = historyService.searchHistory(invoiceNo, maker, pn, sap, date, MSL, invoicePN);
        ObservableList<History> observableList = FXCollections.observableArrayList(result);
        historyDateTableView.setItems(observableList);
    }

    private void clearFields() {
        invoiceNoField.clear();
        makerField.clear();
        pnField.clear();
        sapField.clear();
        dateTimePicker.setValue(null);

        invoiceNoCheckBox.setSelected(false);
        makerCheckBox.setSelected(false);
        pnCheckBox.setSelected(false);
        sapCheckBox.setSelected(false);
        dateCheckBox.setSelected(false);

        historyDateTableView.getItems().clear();
    }

    private void onExportExcel() {
        List<History> dataToExport = historyDateTableView.getItems();
        if (dataToExport.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Alert", "No data to be exported.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Please choose location to export Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(historyDateTableView.getScene().getWindow());

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("History Scan");
                CellStyle centerStyle = workbook.createCellStyle();
                centerStyle.setAlignment(HorizontalAlignment.CENTER);
                centerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

                String[] headers = {"No", "Invoice No", "Invoice PN", "Maker", "Part No", "SAP", "Date", "Time", "Quantity", "MSL"};
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }

                for (int i = 0; i < dataToExport.size(); i++) {
                    History h = dataToExport.get(i);
                    Row row = sheet.createRow(i + 1);
                    row.createCell(0).setCellValue(i + 1);
                    row.createCell(1).setCellValue(h.getInvoiceNo());
                    row.createCell(2).setCellValue(h.getInvoicePN());
                    row.createCell(3).setCellValue(h.getMaker());
                    row.createCell(4).setCellValue(h.getMakerPN());
                    row.createCell(5).setCellValue(h.getSapPN());
                    row.createCell(6).setCellValue(h.getDate().toString());
                    row.createCell(7).setCellValue(h.getTime().toString());
                    row.createCell(8).setCellValue(h.getQuantity());
                    row.createCell(9).setCellValue(h.getMSL());
                }

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }
                showAlert(Alert.AlertType.INFORMATION, "Success", "Export successful.");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Errors", "Can't create file: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

/*    private void startAutoGC() {
        scheduler.scheduleAtFixedRate(() -> {
            System.gc();
            System.out.println("Triggered GC at: " + java.time.LocalTime.now());
        }, 20, 20, TimeUnit.SECONDS);
    }*/
}
