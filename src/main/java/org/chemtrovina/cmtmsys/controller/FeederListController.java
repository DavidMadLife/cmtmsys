package org.chemtrovina.cmtmsys.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.chemtrovina.cmtmsys.model.*;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.service.base.*;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class FeederListController {

    @FXML private TextField txtModelCode;
    @FXML private ComboBox<ModelType> cbModelType;
    @FXML private ComboBox<Warehouse> cbLines;
    @FXML private Button btnLoadFeeders;

    @FXML private TableView<Feeder> tblFeederList;
    @FXML private TableColumn<Feeder, String> colFeederCode;
    @FXML private TableColumn<Feeder, String> colSapCode;
    @FXML private TableColumn<Feeder, Integer> colQty;
    @FXML private TableColumn<Feeder, String> colMachine;

    @FXML private TextField txtFilterFeederCode;
    @FXML private TextField txtFilterSapCode;
    @FXML private Button btnSearchFeeder;
    @FXML private Button btnClearSearch;
    @FXML private Button btnChooseFile;
    @FXML private Text txtSelectedFileName;
    @FXML private Button btnImportFeederList;

    private File selectedFile;
    private ObservableList<Feeder> allFeeders = FXCollections.observableArrayList();

    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final ModelLineService modelLineService;
    private final FeederService feederService;

    @Autowired
    public FeederListController(
            ProductService productService,
            WarehouseService warehouseService,
            ModelLineService modelLineService,
            FeederService feederService
    ) {
        this.productService = productService;
        this.warehouseService = warehouseService;
        this.modelLineService = modelLineService;
        this.feederService = feederService;
    }

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupModelTypeComboBox();
        setupTable();
        setupImport();
        setupSearch();
        enableClipboardSupport();


    }

    private void setupModelTypeComboBox() {
        cbModelType.setItems(FXCollections.observableArrayList(ModelType.values()));
        cbModelType.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(ModelType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name());
            }
        });
        cbModelType.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(ModelType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name());
            }
        });
    }

    private void setupComboBoxes() {
        cbLines.setItems(FXCollections.observableArrayList(warehouseService.getAllWarehouses()));

        cbLines.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        cbLines.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        btnLoadFeeders.setOnAction(e -> loadFeederList());
    }

    private void setupTable() {
        colFeederCode.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getFeederCode()));
        colSapCode.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getSapCode()));
        colQty.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getQty()).asObject());
        colMachine.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getMachine()));
        tblFeederList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadFeederList() {
        String modelCode = txtModelCode.getText().trim();
        ModelType modelType = cbModelType.getValue();
        Warehouse selectedLine = cbLines.getValue();

        if (modelCode.isEmpty() || modelType == null || selectedLine == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đầy đủ Model Code, Model Type và Line.");
            return;
        }

        Product product = productService.getProductByCodeAndType(modelCode, modelType);
        if (product == null) {
            showAlert(Alert.AlertType.ERROR, "Không tìm thấy Model", "Model không tồn tại trong hệ thống.");
            return;
        }

        try {
            List<Feeder> result = feederService.getFeedersByModelAndLine(product.getProductId(), selectedLine.getWarehouseId());
            allFeeders = FXCollections.observableArrayList(result);
            tblFeederList.setItems(allFeeders);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách feeder.");
            tblFeederList.setItems(FXCollections.emptyObservableList());
        }
    }

    private void setupSearch() {
        btnSearchFeeder.setOnAction(e -> {
            String modelCode = txtModelCode.getText().trim();
            ModelType modelType = cbModelType.getValue();
            Warehouse selectedLine = cbLines.getValue();

            if (modelCode.isEmpty() || modelType == null || selectedLine == null) {
                showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập Model Code, Model Type và Line.");
                return;
            }

            Product product = productService.getProductByCodeAndType(modelCode, modelType);
            if (product == null) {
                showAlert(Alert.AlertType.ERROR, "Không tìm thấy Model", "Model không tồn tại trong hệ thống.");
                return;
            }

            String feederCode = txtFilterFeederCode.getText().trim();
            String sapCode = txtFilterSapCode.getText().trim();

            try {
                List<Feeder> filtered = feederService.searchFeeders(
                        product.getProductId(),
                        selectedLine.getWarehouseId(),
                        feederCode,
                        sapCode
                );
                tblFeederList.setItems(FXCollections.observableArrayList(filtered));
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tìm kiếm dữ liệu.");
            }
        });

        btnClearSearch.setOnAction(e -> {
            txtFilterFeederCode.clear();
            txtFilterSapCode.clear();
            loadFeederList();
        });
    }

    private void setupImport() {
        btnChooseFile.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Chọn file Excel");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
            selectedFile = fileChooser.showOpenDialog(btnChooseFile.getScene().getWindow());
            txtSelectedFileName.setText(selectedFile != null ? selectedFile.getName() : "Chưa chọn file");
        });

        btnImportFeederList.setOnAction(e -> {
            if (selectedFile == null) {
                showAlert(Alert.AlertType.WARNING, "Chưa chọn file", "Vui lòng chọn file để import.");
                return;
            }

            try {
                feederService.importFeedersFromExcel(selectedFile);
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Import feeder thành công.");
                loadFeederList();
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi import", ex.getMessage());
            }

            txtSelectedFileName.setText("Chưa chọn file");
            selectedFile = null;
        });
    }

    private void enableClipboardSupport() {

        tblFeederList.getSelectionModel().setCellSelectionEnabled(true);
        tblFeederList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tblFeederList.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode().toString().equals("C")) {
                FxClipboardUtils.copySelectionToClipboard(tblFeederList);
            }
        });
    }


    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
