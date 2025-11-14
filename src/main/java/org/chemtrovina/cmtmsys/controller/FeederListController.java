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
import org.chemtrovina.cmtmsys.utils.AutoCompleteUtils;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.chemtrovina.cmtmsys.utils.TableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class FeederListController {

    // region FXML
    @FXML private TextField txtModelCode;
    @FXML private TextField txtModelName;
    @FXML private ComboBox<ModelType> cbModelType;
    @FXML private ComboBox<Warehouse> cbLines;
    @FXML private Button btnLoadFeeders;

    @FXML private TableView<Feeder> tblFeederList;
    @FXML private TableColumn<Feeder, Integer> colNo;
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
    @FXML private Button btnDeleteSelectedFeeders;
    // endregion

    private File selectedFile;
    private ObservableList<Feeder> allFeeders = FXCollections.observableArrayList();

    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final FeederService feederService;

    @Autowired
    public FeederListController(ProductService productService,
                                WarehouseService warehouseService,
                                ModelLineService modelLineService,
                                FeederService feederService) {
        this.productService = productService;
        this.warehouseService = warehouseService;
        this.feederService = feederService;
    }

    // region INITIALIZE
    @FXML
    public void initialize() {
        setupCombos();
        setupTable();
        setupImport();
        setupSearchAndDelete();
        FxClipboardUtils.enableCopyShortcut(tblFeederList);
        TableUtils.centerAlignAllColumns(tblFeederList);
    }
    // endregion


    // region SETUP UI
    private void setupCombos() {
        // Model Type
        cbModelType.setItems(FXCollections.observableArrayList(ModelType.values()));
        cbModelType.setButtonCell(createComboCell());
        cbModelType.setCellFactory(lv -> createComboCell());

        // Lines
        cbLines.setItems(FXCollections.observableArrayList(warehouseService.getAllWarehouses()));
        cbLines.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        cbLines.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        btnLoadFeeders.setOnAction(e -> loadFeederList());
    }

    private ListCell<ModelType> createComboCell() {
        return new ListCell<>() {
            @Override protected void updateItem(ModelType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name());
            }
        };
    }

    private void setupTable() {
        colNo.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(
                tblFeederList.getItems().indexOf(cell.getValue()) + 1).asObject());
        colFeederCode.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getFeederCode()));
        colSapCode.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSapCode()));
        colQty.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getQty()).asObject());
        colMachine.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMachine()));

        // AutoComplete
        List<Product> allProducts = productService.getAllProducts();
        AutoCompleteUtils.setupAutoComplete(txtModelCode,
                allProducts.stream().map(Product::getProductCode).filter(s -> s != null && !s.isBlank()).distinct().toList());
        if (txtModelName != null)
            AutoCompleteUtils.setupAutoComplete(txtModelName,
                    allProducts.stream().map(Product::getName).filter(s -> s != null && !s.isBlank()).distinct().toList());
    }
    // endregion


    // region CORE LOGIC
    private Product getSelectedProduct() {
        String code = txtModelCode.getText().trim();
        String name = txtModelName != null ? txtModelName.getText().trim() : "";
        ModelType type = cbModelType.getValue();

        if ((code.isEmpty() && name.isEmpty()) || type == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin",
                    "Vui lòng nhập Model Code hoặc Model Name và chọn Model Type.");
            return null;
        }

        Product product = productService.getProductByCodeOrNameAndType(code, name, type);
        if (product == null)
            showAlert(Alert.AlertType.ERROR, "Không tìm thấy Model", "Model không tồn tại trong hệ thống.");

        return product;
    }

    private void loadFeederList() {
        Warehouse line = cbLines.getValue();
        if (line == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn Line.");
            return;
        }

        Product product = getSelectedProduct();
        if (product == null) return;

        try {
            List<Feeder> result = feederService.getFeedersByModelAndLine(product.getProductId(), line.getWarehouseId());
            allFeeders = FXCollections.observableArrayList(result);
            tblFeederList.setItems(allFeeders);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách feeder.");
            tblFeederList.setItems(FXCollections.emptyObservableList());
        }
    }
    // endregion


    // region SEARCH + DELETE
    private void setupSearchAndDelete() {
        btnSearchFeeder.setOnAction(e -> {
            Warehouse line = cbLines.getValue();
            if (line == null) {
                showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn Line.");
                return;
            }

            Product product = getSelectedProduct();
            if (product == null) return;

            try {
                List<Feeder> filtered = feederService.searchFeeders(
                        product.getProductId(),
                        line.getWarehouseId(),
                        txtFilterFeederCode.getText().trim(),
                        txtFilterSapCode.getText().trim()
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
            txtModelCode.clear();
            if (txtModelName != null) txtModelName.clear();
            loadFeederList();
        });

        btnDeleteSelectedFeeders.setOnAction(e -> {
            List<Feeder> selected = tblFeederList.getSelectionModel().getSelectedItems();
            if (selected == null || selected.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Không có lựa chọn", "Vui lòng chọn feeder để xóa.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận xóa");
            confirm.setContentText("Bạn có chắc chắn muốn xóa " + selected.size() + " feeder?");
            confirm.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    try {
                        for (Feeder f : selected)
                            feederService.deleteFeederById(f.getFeederId());

                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa feeder.");
                        loadFeederList();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa feeder: " + ex.getMessage());
                    }
                }
            });
        });

        tblFeederList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
    // endregion


    // region IMPORT
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
    // endregion


    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
