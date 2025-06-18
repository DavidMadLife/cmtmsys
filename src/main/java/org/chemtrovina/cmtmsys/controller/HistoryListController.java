package org.chemtrovina.cmtmsys.controller;


import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.model.History;
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
import org.chemtrovina.cmtmsys.utils.AutoCompleteUtils;
import org.springframework.jdbc.core.JdbcTemplate;


import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    @FXML private TextField invoiceNoField;
    @FXML private TextField makerField;
    @FXML private TextField pnField;
    @FXML private TextField sapField;
    @FXML private DatePicker dateTimePicker;
    @FXML private TextField mslField;
    @FXML private TextField invoicePNField;

    @FXML private CheckBox invoiceNoCheckBox;
    @FXML private CheckBox makerCheckBox;
    @FXML private CheckBox pnCheckBox;
    @FXML private CheckBox sapCheckBox;
    @FXML private CheckBox dateCheckBox;
    @FXML private CheckBox mslCheckBox;
    @FXML private CheckBox invoicePNCheckBox;

    @FXML private Button searchBtn;
    @FXML private Button clearBtn;
    @FXML private Button importExcelBtn;

    @FXML private Label totalQuantityLabel;
    @FXML private Label totalReelQuantityLabel;


    private HistoryService historyService;
    private InvoiceService invoiceService;
    private MOQService moqService;
    private final ObservableList<History> historyObservableList = FXCollections.observableArrayList(); // Inject nếu dùng DI
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @FXML
    public void initialize() {

        historyDateTableView.setRowFactory(tv -> {
            TableRow<History> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem updateItem = new MenuItem("Update");
            MenuItem deleteItem = new MenuItem("Delete");
            contextMenu.getItems().addAll(updateItem, deleteItem);

            // Listener cần remove sau
            ChangeListener<History> listener = (obs, oldItem, newItem) -> {
                if (newItem != null) {
                    updateItem.setOnAction(e -> showUpdateDialog(newItem));
                    deleteItem.setOnAction(e -> showDeleteConfirm(newItem));
                }
            };

            row.itemProperty().addListener(listener);

            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );

            // Khi row bị "detached" khỏi scene, remove listener
            row.sceneProperty().addListener((sceneObs, oldScene, newScene) -> {
                if (newScene == null) {
                    row.itemProperty().removeListener(listener); // ✅ cleanup
                }
            });

            return row;
        });




        // Gán các cột với property trong HistoryEntrance
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

        DataSource dataSource = DataSourceConfig.getDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        HistoryRepository historyRepository = new HistoryRepositoryImpl(jdbcTemplate);
        MOQRepository moqRepository = new MOQRepositoryImpl(jdbcTemplate);
        moqService = new MOQServiceImpl(moqRepository);
        InvoiceRepository invoiceRepository = new InvoiceRepositoryImpl(jdbcTemplate);
        historyService = new HistoryServiceImpl(historyRepository,moqRepository, invoiceRepository);
                invoiceService = new InvoiceServiceImpl(invoiceRepository);


        searchBtn.setOnAction(e -> onSearch());

        clearBtn.setOnAction(e -> {
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
        });


        importExcelBtn.setOnAction(e -> onExportExcel());

        setupAutoCompleteFields();
        setupSearchShortcut();

        startAutoGC();


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
        // Lắng nghe sự kiện Ctrl + F
        historyDateTableView.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.F) {
                openSearchDialog(); // Mở dialog tìm kiếm
            }
        });
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

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
                String sapCode = txtSapCode.getText().trim();
                String makerCode = txtMakerCode.getText().trim();
                String invoiceNo = txtInvoiceNo.getText().trim();
                String invoicePN = txtInvoicePN.getText().trim();
                String msl = txtMsl.getText().trim();

                searchFromData(invoiceNo, makerCode, sapCode, msl, invoicePN); // Tìm kiếm trực tiếp từ dữ liệu
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void searchFromData(String invoiceNo, String makerCode, String sapCode, String msl, String invoicePN) {
        // Sử dụng hàm searchHistory của HistoryService để tìm kiếm từ cơ sở dữ liệu
        List<History> result = historyService.searchHistory(invoiceNo, makerCode, null, sapCode, null, msl, invoicePN);

        // Cập nhật TableView với kết quả tìm kiếm
        ObservableList<History> observableList = FXCollections.observableArrayList(result);
        historyDateTableView.setItems(observableList);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void onSearch() {
/*
        List<String> invoiceNoSuggestions = invoiceService.getAllInvoiceNos();
        //AutoCompleteUtils.setupAutoComplete(invoiceNoField, invoiceNoSuggestions);

        List<String> sapCodeSuggestions = moqService.getAllSapCodes();
        //AutoCompleteUtils.setupAutoComplete(sapField, sapCodeSuggestions);

        List<String> makerSuggestions = moqService.getAllMakers();
        //AutoCompleteUtils.setupAutoComplete(makerField, makerSuggestions);

        List<String> makerPNSuggestions = moqService.getAllMakerPNs();
        //AutoCompleteUtils.setupAutoComplete(pnField, makerPNSuggestions);

        List<String> mslSuggestions = moqService.getAllMSLs();
        //AutoCompleteUtils.setupAutoComplete(mslField, mslSuggestions);

        List<String> invoicePNSuggestions = invoiceService.getAllInvoicePNs();*/

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

    private void showUpdateDialog(History selected) {
        Dialog<History> dialog = new Dialog<>();
        dialog.setTitle("Update History Information");
        dialog.setHeaderText("Update History");

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        TextField makerField = new TextField(selected.getMaker());
        TextField makerPNField = new TextField(selected.getMakerPN());
        TextField sapPNField = new TextField(selected.getSapPN());
        TextField quantityField = new TextField(String.valueOf(selected.getQuantity()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Maker:"), 0, 0);
        grid.add(makerField, 1, 0);
        grid.add(new Label("Maker P/N:"), 0, 1);
        grid.add(makerPNField, 1, 1);
        grid.add(new Label("SAP P/N:"), 0, 2);
        grid.add(sapPNField, 1, 2);
        grid.add(new Label("Quantity:"), 0, 3);
        grid.add(quantityField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                try {
                    int updatedQuantity = Integer.parseInt(quantityField.getText().trim());
                    selected.setMaker(makerField.getText().trim());
                    selected.setMakerPN(makerPNField.getText().trim());
                    selected.setSapPN(sapPNField.getText().trim());
                    selected.setQuantity(updatedQuantity);
                    return selected;
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Wrong format", "Quantity must be Integer.");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updated -> {
            historyService.updateHistory(updated);
            onSearch(); // Refresh table
            showAlert(Alert.AlertType.INFORMATION, "Success", "Update successful.");
        });
    }

    private void showDeleteConfirm(History selected) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Accpet to delete");
        confirm.setHeaderText("Are you accepted?");
        confirm.setContentText("ScanCode: " + selected.getScanCode());

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                historyService.deleteById(selected.getId());
                onSearch(); // Refresh lại view
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Delete success.");
            }
        });
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
                centerStyle.setAlignment(HorizontalAlignment.CENTER); // Căn giữa ngang
                centerStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Căn giữa dọc

                // Header
                Row headerRow = sheet.createRow(0);
                String[] headers = {"No", "Invoice No", "Invoice PN", "Maker", "Part No", "SAP", "Date", "Time", "Quantity", "MSL"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                }

                // Data rows
                for (int i = 0; i < dataToExport.size(); i++) {
                    History h = dataToExport.get(i);
                    Row row = sheet.createRow(i + 1);

                    Cell cell0 = row.createCell(0);
                    cell0.setCellValue(i + 1);
                    cell0.setCellStyle(centerStyle);

                    // Add Invoice No
                    Cell cell1 = row.createCell(1);
                    cell1.setCellValue(h.getInvoiceNo());
                    cell1.setCellStyle(centerStyle);

                    // Add Invoice PN
                    Cell cell2 = row.createCell(2);
                    cell2.setCellValue(h.getInvoicePN());
                    cell2.setCellStyle(centerStyle);

                    // Add Maker
                    Cell cell3 = row.createCell(3);
                    cell3.setCellValue(h.getMaker());
                    cell3.setCellStyle(centerStyle);

                    // Add Maker P/N
                    Cell cell4 = row.createCell(4);
                    cell4.setCellValue(h.getMakerPN());
                    cell4.setCellStyle(centerStyle);

                    // Add SAP P/N
                    Cell cell5 = row.createCell(5);
                    cell5.setCellValue(h.getSapPN());
                    cell5.setCellStyle(centerStyle);

                    // Add Date
                    Cell cell6 = row.createCell(6);
                    cell6.setCellValue(h.getDate().toString());
                    cell6.setCellStyle(centerStyle);

                    // Add Time
                    Cell cell7 = row.createCell(7);
                    cell7.setCellValue(h.getTime().toString());
                    cell7.setCellStyle(centerStyle);

                    // Add Quantity
                    Cell cell8 = row.createCell(8);
                    cell8.setCellValue(h.getQuantity());
                    cell8.setCellStyle(centerStyle);

                    // Add MSL
                    Cell cell9 = row.createCell(9);
                    cell9.setCellValue(h.getMSL());
                    cell9.setCellStyle(centerStyle);
                }

                // Auto-size columns
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                // Save
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }

                showAlert(Alert.AlertType.INFORMATION, "Success", "Export successful.");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Errors", "Can't create file: " + e.getMessage());
            }
        }
    }




    //Alert
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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
}
