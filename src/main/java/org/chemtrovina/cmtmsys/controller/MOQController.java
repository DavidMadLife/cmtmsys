package org.chemtrovina.cmtmsys.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.model.MOQ;
import org.chemtrovina.cmtmsys.repository.Impl.MOQRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.base.MOQRepository;
import org.chemtrovina.cmtmsys.service.Impl.MOQServiceImpl;
import org.chemtrovina.cmtmsys.service.base.MOQService;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MOQController {
    @FXML
    private TableView<MOQ> moqTableView;
    @FXML private TableColumn<MOQ, Integer> noColumn;
    @FXML private TableColumn<MOQ, String> makerColumn;
    @FXML private TableColumn<MOQ, String> makerPNColumn;
    @FXML private TableColumn<MOQ, String> sapPNColumn;
    @FXML private TableColumn<MOQ, Integer> moqColumn;
    @FXML private TableColumn<MOQ, String> mslColumn;

    @FXML private TextField makerField;
    @FXML private TextField pnField;
    @FXML private TextField sapField;
    @FXML private TextField moqField;
    @FXML private TextField mslField;

    @FXML private CheckBox makerCheckBox;
    @FXML private CheckBox pnCheckBox;
    @FXML private CheckBox sapCheckBox;
    @FXML private CheckBox moqCheckBox;
    @FXML private CheckBox mslCheckBox;

    @FXML private Button chooseFileBtn;
    @FXML private Button btnImportData;
    @FXML private Button btnSearch;
    @FXML private Text fileNameLabel;

    private File selectedFile;

    private final List<MOQ> allData = new ArrayList<>(); // để lưu toàn bộ dữ liệu từ Excel

    private MOQService moqService;
    private final ObservableList<MOQ> moqObservableList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Số thứ tự (không có trong DB)
        noColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getIndex() >= moqTableView.getItems().size()) {
                    setText(null);
                } else {
                    setText(String.valueOf(getTableRow().getIndex() + 1));
                }
            }
        });
        makerColumn.setCellValueFactory(new PropertyValueFactory<>("maker"));
        makerPNColumn.setCellValueFactory(new PropertyValueFactory<>("makerPN"));
        sapPNColumn.setCellValueFactory(new PropertyValueFactory<>("sapPN"));
        moqColumn.setCellValueFactory(new PropertyValueFactory<>("moq"));
        mslColumn.setCellValueFactory(new PropertyValueFactory<>("msql"));

        DataSource dataSource = DataSourceConfig.getDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        MOQRepository moqRepository = new MOQRepositoryImpl(jdbcTemplate);
        moqService = new MOQServiceImpl(moqRepository);

        chooseFileBtn.setOnAction(e -> chooseFile());
        btnImportData.setOnAction(e -> importDataFromExcel());
        btnSearch.setOnAction(e -> onSearch());

        moqTableView.setRowFactory(tv -> {
            TableRow<MOQ> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem updateItem = new MenuItem("Update");
            updateItem.setOnAction(event -> {
                MOQ selected = row.getItem();
                if (selected != null) {
                    showUpdateDialog(selected); // Gọi dialog để sửa
                }
            });

            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(event -> {
                MOQ selected = row.getItem();
                if (selected != null) {
                    deleteMOQ(selected); // Gọi hàm xóa
                }
            });

            contextMenu.getItems().addAll(updateItem, deleteItem);

            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );

            return row;
        });

    }

    private void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file Excel");

        // Chỉ cho phép chọn file Excel (.xlsx)
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx");
        fileChooser.getExtensionFilters().add(extFilter);

        // Lấy cửa sổ hiện tại để hiển thị FileChooser
        Stage stage = (Stage) chooseFileBtn.getScene().getWindow();

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedFile = file;
            fileNameLabel.setText(file.getName());
        } else {
            fileNameLabel.setText("File not selected");
        }
    }
    private void importDataFromExcel() {
        if (selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "No File Selected", "Please select an Excel file first.");
            return;
        }

        moqService.saveImportedData(selectedFile);

        // Lấy lại toàn bộ dữ liệu sau khi import (nếu muốn hiển thị)
        List<MOQ> importedData = moqService.searchMOQ(null, null, null, null, null);

        if (importedData.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Import Result", "No data found or inserted.");
        } else {
            allData.clear();
            allData.addAll(importedData);

            moqObservableList.clear();
            moqObservableList.addAll(importedData);

            moqTableView.setItems(moqObservableList);

            showAlert(Alert.AlertType.INFORMATION, "Import Successful", "Successfully imported " + importedData.size() + " items.");
        }
    }

    private void onSearch() {
        String maker = makerCheckBox.isSelected() ? makerField.getText() : null;
        String pn = pnCheckBox.isSelected() ? pnField.getText() : null;
        String sap = sapCheckBox.isSelected() ? sapField.getText() : null;
        String moq = moqCheckBox.isSelected() ? moqField.getText() : null;
        String msl = mslCheckBox.isSelected() ? mslField.getText() : null;

        List<MOQ> results = moqService.searchMOQ(maker, pn, sap, moq, msl);
        ObservableList<MOQ> observableList = FXCollections.observableArrayList(results);
        moqTableView.setItems(observableList);

    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showUpdateDialog(MOQ moq) {
        Dialog<MOQ> dialog = new Dialog<>();
        dialog.setTitle("Update MOQ Entry");
        dialog.setHeaderText("Update entry for: " + moq.getMakerPN());

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        // Tạo form nhập
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField makerField = new TextField(moq.getMaker());
        TextField makerPNField = new TextField(moq.getMakerPN());
        TextField sapPNField = new TextField(moq.getSapPN());
        TextField moqField = new TextField(String.valueOf(moq.getMoq()));
        TextField mslField = new TextField(moq.getMsql());

        grid.add(new Label("Maker:"), 0, 0);
        grid.add(makerField, 1, 0);
        grid.add(new Label("Maker P/N:"), 0, 1);
        grid.add(makerPNField, 1, 1);
        grid.add(new Label("SAP P/N:"), 0, 2);
        grid.add(sapPNField, 1, 2);
        grid.add(new Label("MOQ:"), 0, 3);
        grid.add(moqField, 1, 3);
        grid.add(new Label("MSL:"), 0, 4);
        grid.add(mslField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Khi nhấn "Update"
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                try {
                    int newMoq = Integer.parseInt(moqField.getText());

                    moq.setMaker(makerField.getText());
                    moq.setMakerPN(makerPNField.getText());
                    moq.setSapPN(sapPNField.getText());
                    moq.setMoq(newMoq);
                    moq.setMsql(mslField.getText());

                    return moq;
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "MOQ must be a number!");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedMoq -> {
            moqService.updateImportedData(updatedMoq);
            moqTableView.refresh();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Entry updated successfully.");
        });
    }


    private void deleteMOQ(MOQ moq) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete MOQ");
        confirm.setHeaderText("Are you sure you want to delete this entry?");
        confirm.setContentText("MakerPN: " + moq.getMakerPN());
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                moqService.deleteById(moq.getId()); // gọi Service xoá DB
                moqObservableList.remove(moq); // xoá trong bảng UI
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Entry deleted successfully.");
                onSearch();
            }
        });
    }



}
