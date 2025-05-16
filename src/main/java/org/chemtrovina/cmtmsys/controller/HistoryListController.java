package org.chemtrovina.cmtmsys.controller;


import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

public class HistoryListController {

    @FXML private TableView<History> historyDateTableView;
    @FXML private TableColumn<History, String> dateColumn;
    @FXML private TableColumn<History, String> makerColumn;
    @FXML private TableColumn<History, String> makerPNColumn;
    @FXML private TableColumn<History, String> sapPNColumn;
    @FXML private TableColumn<History, Integer> quantityColumn;
    @FXML private TableColumn<History, String> invoiceNoColumn;
    @FXML private TableColumn<History, String> mslColumn;
    @FXML private TableColumn<History, String> timeColumn;

    @FXML private TextField invoiceNoField;
    @FXML private TextField makerField;
    @FXML private TextField pnField;
    @FXML private TextField sapField;
    @FXML private DatePicker dateTimePicker;
    @FXML private TextField mslField;

    @FXML private CheckBox invoiceNoCheckBox;
    @FXML private CheckBox makerCheckBox;
    @FXML private CheckBox pnCheckBox;
    @FXML private CheckBox sapCheckBox;
    @FXML private CheckBox dateCheckBox;
    @FXML private CheckBox mslCheckBox;

    @FXML private Button searchBtn;
    @FXML private Button clearBtn;
    @FXML private Button importExcelBtn;


    private HistoryService historyService;
    private InvoiceService invoiceService;
    private MOQService moqService;
    private final ObservableList<History> historyObservableList = FXCollections.observableArrayList(); // Inject nếu dùng DI


    @FXML
    public void initialize() {

        historyDateTableView.setRowFactory(tv -> {
            TableRow<History> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem updateItem = new MenuItem("Update");
            updateItem.setOnAction(event -> showUpdateDialog(row.getItem()));

            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(event -> showDeleteConfirm(row.getItem()));

            contextMenu.getItems().addAll(updateItem, deleteItem);

            // Chỉ hiển thị menu nếu row không rỗng
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );

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
        mslColumn.setCellValueFactory(new PropertyValueFactory<>("MSL"));

        DataSource dataSource = DataSourceConfig.getDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        HistoryRepository historyRepository = new HistoryRepositoryImpl(jdbcTemplate);
        MOQRepository moqRepository = new MOQRepositoryImpl(jdbcTemplate);
        moqService = new MOQServiceImpl(moqRepository);
        historyService = new HistoryServiceImpl(historyRepository,moqRepository);
        InvoiceRepository invoiceRepository = new InvoiceRepositoryImpl(jdbcTemplate);
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


    }

    private void onSearch() {

        List<String> invoiceNoSuggestions = invoiceService.getAllInvoiceNos();
        AutoCompleteUtils.setupAutoComplete(invoiceNoField, invoiceNoSuggestions);

        List<String> sapCodeSuggestions = moqService.getAllSapCodes();
        AutoCompleteUtils.setupAutoComplete(sapField, sapCodeSuggestions);

        List<String> makerSuggestions = moqService.getAllMakers();
        AutoCompleteUtils.setupAutoComplete(makerField, makerSuggestions);

        List<String> makerPNSuggestions = moqService.getAllMakerPNs();
        AutoCompleteUtils.setupAutoComplete(pnField, makerPNSuggestions);

        List<String> mslSuggestions = moqService.getAllMSLs();
        AutoCompleteUtils.setupAutoComplete(mslField, mslSuggestions);

        String invoiceNo = invoiceNoCheckBox.isSelected() ? invoiceNoField.getText() : null;
        String maker = makerCheckBox.isSelected() ? makerField.getText() : null;
        String pn = pnCheckBox.isSelected() ? pnField.getText() : null;
        String sap = sapCheckBox.isSelected() ? sapField.getText() : null;
        LocalDate date = dateCheckBox.isSelected() ? dateTimePicker.getValue() : null;
        String MSL = mslCheckBox.isSelected() ? mslField.getText() : null;

        List<History> result = historyService.searchHistory(invoiceNo, maker, pn, sap, date, MSL);

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
                centerStyle.setVerticalAlignment(VerticalAlignment.CENTER); // (tuỳ chọn) Căn giữa dọc

                // Header
                Row headerRow = sheet.createRow(0);
                String[] headers = {"No", "Maker", "Part No", "SAP", "Date", "Time", "Quantity", "MSL"};
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

                    Cell cell1 = row.createCell(1);
                    cell1.setCellValue(h.getMaker());
                    cell1.setCellStyle(centerStyle);

                    Cell cell2 = row.createCell(2);
                    cell2.setCellValue(h.getMakerPN());
                    cell2.setCellStyle(centerStyle);

                    Cell cell3 = row.createCell(3);
                    cell3.setCellValue(h.getSapPN());
                    cell3.setCellStyle(centerStyle);

                    Cell cell4 = row.createCell(4);
                    cell4.setCellValue(h.getDate().toString());
                    cell4.setCellStyle(centerStyle);

                    Cell cell5 = row.createCell(5);
                    cell5.setCellValue(h.getTime().toString());
                    cell5.setCellStyle(centerStyle);

                    Cell cell6 = row.createCell(6);
                    cell6.setCellValue(h.getQuantity());
                    cell6.setCellStyle(centerStyle);

                    Cell cell7 = row.createCell(7);
                    cell7.setCellValue(h.getMSL());
                    cell7.setCellStyle(centerStyle);
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
                showAlert(Alert.AlertType.ERROR, "Errors", "Can't to create file: " + e.getMessage());
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


}
