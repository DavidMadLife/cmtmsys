package org.chemtrovina.cmtmsys.controller;


import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.chemtrovina.cmtmsys.controller.product.*;
import org.chemtrovina.cmtmsys.dto.ProductBomDto;
import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.model.enums.UserRole;
import org.chemtrovina.cmtmsys.security.RequiresRoles;
import org.chemtrovina.cmtmsys.service.base.ProductBOMService;
import org.chemtrovina.cmtmsys.service.base.ProductService;
import org.chemtrovina.cmtmsys.utils.AutoCompleteUtils;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.chemtrovina.cmtmsys.utils.TableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@RequiresRoles({
        UserRole.ADMIN,
        UserRole.INVENTORY,
        UserRole.SUBLEEDER
})

@Component
public class ProductController {

    /* ========= FXML ========= */
    @FXML private TextField txtProductCode;
    @FXML private TextField txtProductName;
    @FXML private ComboBox<String> cbModelTypeFilter;
    @FXML private Button btnLoad;
    @FXML private TableView<ProductBomDto> tblProductBOM;

    @FXML private TableView<Product> tblProducts;

    @FXML private Button btnChooseFile;
    @FXML private Button btnImport;
    @FXML private TextField txtFileName;

    @FXML private TextField txtNewProductCode;
    @FXML private TextField txtNewProductName;
    @FXML private ComboBox<String> cbNewModelType;
    @FXML private Button btnCreateProduct;

    @FXML private Button btnUpdateProduct;
    @FXML private Button btnDeleteProduct;

    @FXML private TableColumn<ProductBomDto, Number>  colIndex;
    @FXML private TableColumn<ProductBomDto, String>  colProductCode;
    @FXML private TableColumn<ProductBomDto, String>  colSappn;
    @FXML private TableColumn<ProductBomDto, Double>  colQuantity;
    @FXML private TableColumn<ProductBomDto, String>  colModelType;
    @FXML private TableColumn<ProductBomDto, String>  colCreatedDate;
    @FXML private TableColumn<ProductBomDto, String>  colUpdatedDate;

    @FXML private TableColumn<Product, String> colProductCodeList;
    @FXML private TableColumn<Product, String> colModelTypeList;
    @FXML private TableColumn<Product, String> colDescriptionList;
    @FXML private TableColumn<Product, String> colNameList;


    /* ========= HELPERS ========= */
    private final ProductBomLoader bomLoader;
    private final ProductExcelImporter excelImporter;
    private final ProductCrudHandler crudHandler;

    /* ========= STATE ========= */
    private File selectedFile;
    private List<Product> allProducts;

    /* ========= SERVICES ========= */
    private final ProductBOMService productBOMService;
    private final ProductService productService;


    @Autowired
    public ProductController(ProductBOMService productBOMService, ProductService productService) {
        this.productBOMService = productBOMService;
        this.productService = productService;

        // Khởi tạo helper
        this.bomLoader           = new ProductBomLoader(productBOMService);
        this.excelImporter       = new ProductExcelImporter(productService, productBOMService);
        this.crudHandler         = new ProductCrudHandler(productService);
    }


    @FXML
    public void initialize() {
        setupTable();
        setupActions();

        cbNewModelType.setItems(FXCollections.observableArrayList("TOP","BOT","SINGLE","BOTTOP","NONE"));
        cbModelTypeFilter.setItems(FXCollections.observableArrayList("TOP","BOT","SINGLE","BOTTOP","NONE"));

        loadAllProducts();
        //autoCompleteManager.setupAutoComplete(txtProductCode, txtProductName, cbModelTypeFilter, allProducts);

        TableUtils.centerAlignAllColumns(tblProductBOM);

        setupAutoCompleteModels();
    }


    /* ===========================
     * TABLE SETUP
     * =========================== */
    private void setupTable() {
        FxClipboardUtils.enableCopyShortcut(tblProductBOM);
        FxClipboardUtils.enableCopyShortcut(tblProducts);

        tblProducts.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();

            // Double click: load BOM
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    Product p = row.getItem();
                    txtProductCode.setText(p.getProductCode());
                    cbModelTypeFilter.setValue(p.getModelType().name());
                    onLoadBOM();
                }
            });

            // Context menu
            MenuItem update = new MenuItem("Cập nhật");
            update.setOnAction(e -> {
                if (!row.isEmpty()) {
                    crudHandler.updateProductDialog(row.getItem(), txtProductCode,
                            () -> {
                                loadAllProducts();
                                onLoadBOM();
                            });
                }
            });

            MenuItem delete = new MenuItem("Xoá");
            delete.setOnAction(e -> {
                if (!row.isEmpty()) {
                    crudHandler.deleteProduct(row.getItem(),
                            () -> {
                                loadAllProducts();
                                tblProductBOM.getItems().clear();
                            });
                }
            });

            ContextMenu menu = new ContextMenu(update, delete);

            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(menu)
            );

            return row;
        });

        colIndex.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createIntegerBinding(
                        () -> tblProductBOM.getItems().indexOf(cellData.getValue()) + 1
                )
        );
        colProductCode.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        colSappn.setCellValueFactory(new PropertyValueFactory<>("sappn"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colModelType.setCellValueFactory(new PropertyValueFactory<>("modelType"));
        colCreatedDate.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        colUpdatedDate.setCellValueFactory(new PropertyValueFactory<>("updatedDate"));

        // Products table
        colProductCodeList.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        colModelTypeList.setCellValueFactory(new PropertyValueFactory<>("modelType"));
        colDescriptionList.setCellValueFactory(new PropertyValueFactory<>("description"));
        colNameList.setCellValueFactory(new PropertyValueFactory<>("name"));
    }


    /* ===========================
     * BUTTON ACTIONS
     * =========================== */
    private void setupActions() {
        btnLoad.setOnAction(e -> onLoadBOM());

        btnChooseFile.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Chọn file Excel");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
            selectedFile = fc.showOpenDialog(null);
            txtFileName.setText(selectedFile != null ? selectedFile.getName() : "Chưa chọn file");
        });

        btnImport.setOnAction(e -> {
            if (selectedFile != null) {
                excelImporter.importExcel(selectedFile,
                        () -> {
                            loadAllProducts();
                            onLoadBOM();
                        });
            }
        });

        btnCreateProduct.setOnAction(e -> {
            crudHandler.createProduct(
                    txtNewProductCode,
                    txtNewProductName,
                    cbNewModelType,
                    () -> {
                        loadAllProducts();
                        //autoCompleteManager.setupAutoComplete(txtProductCode, txtProductName, cbModelTypeFilter, allProducts);
                    }
            );
        });

        btnUpdateProduct.setOnAction(e -> {
            Product p = productService.getProductByCode(txtProductCode.getText());
            if (p != null) {
                crudHandler.updateProductDialog(p, txtProductCode,
                        () -> {
                            loadAllProducts();
                            onLoadBOM();
                        });
            }
        });

        btnDeleteProduct.setOnAction(e -> {
            Product p = productService.getProductByCode(txtProductCode.getText());
            if (p != null) {
                crudHandler.deleteProduct(p,
                        () -> {
                            loadAllProducts();
                            tblProductBOM.getItems().clear();
                        });
            }
        });
    }

    private boolean syncing = false;

    private void setupAutoCompleteModels() {
        if (allProducts == null || allProducts.isEmpty()) return;

        // ===== List gợi ý =====
        List<String> codes = allProducts.stream()
                .map(Product::getProductCode)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .toList();

        List<String> names = allProducts.stream()
                .map(Product::getName)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .toList();

        AutoCompleteUtils.setupAutoComplete(txtProductCode, codes);
        AutoCompleteUtils.setupAutoComplete(txtProductName, names);

        // ===== Index nhanh: code/name -> list product =====
        var byCode = allProducts.stream()
                .filter(p -> p.getProductCode() != null && !p.getProductCode().isBlank())
                .collect(java.util.stream.Collectors.groupingBy(p -> p.getProductCode().trim().toLowerCase()));

        var byName = allProducts.stream()
                .filter(p -> p.getName() != null && !p.getName().isBlank())
                .collect(java.util.stream.Collectors.groupingBy(p -> p.getName().trim().toLowerCase()));

        // ===== Helper chọn đúng record (ưu tiên type đang chọn) =====
        java.util.function.BiFunction<List<Product>, String, Product> pickBest = (list, typeStr) -> {
            if (list == null || list.isEmpty()) return null;

            // ưu tiên đúng type đang filter
            if (typeStr != null && !typeStr.isBlank()) {
                Product typeMatch = list.stream()
                        .filter(p -> p.getModelType() != null)
                        .filter(p -> p.getModelType().name().equalsIgnoreCase(typeStr))
                        .findFirst()
                        .orElse(null);
                if (typeMatch != null) return typeMatch;
            }

            // fallback: lấy record đầu
            return list.get(0);
        };

        // ===== Khi gõ CODE -> fill NAME + TYPE =====
        txtProductCode.textProperty().addListener((obs, oldVal, newVal) -> {
            if (syncing) return;
            if (newVal == null || newVal.isBlank()) return;

            String key = newVal.trim().toLowerCase();
            List<Product> list = byCode.get(key);
            if (list == null || list.isEmpty()) return;

            String currentType = cbModelTypeFilter.getValue(); // có thể null
            Product found = pickBest.apply(list, currentType);
            if (found == null) return;

            syncing = true;
            try {
                // fill NAME
                if (found.getName() != null) {
                    txtProductName.setText(found.getName());
                }

                // fill TYPE nếu combobox đang trống hoặc khác
                if (found.getModelType() != null) {
                    String mt = found.getModelType().name();
                    if (cbModelTypeFilter.getValue() == null || !cbModelTypeFilter.getValue().equalsIgnoreCase(mt)) {
                        cbModelTypeFilter.setValue(mt);
                    }
                }
            } finally {
                syncing = false;
            }
        });

        // ===== Khi gõ NAME -> fill CODE + TYPE =====
        txtProductName.textProperty().addListener((obs, oldVal, newVal) -> {
            if (syncing) return;
            if (newVal == null || newVal.isBlank()) return;

            String key = newVal.trim().toLowerCase();
            List<Product> list = byName.get(key);
            if (list == null || list.isEmpty()) return;

            String currentType = cbModelTypeFilter.getValue();
            Product found = pickBest.apply(list, currentType);
            if (found == null) return;

            syncing = true;
            try {
                if (found.getProductCode() != null) {
                    txtProductCode.setText(found.getProductCode());
                }

                if (found.getModelType() != null) {
                    String mt = found.getModelType().name();
                    if (cbModelTypeFilter.getValue() == null || !cbModelTypeFilter.getValue().equalsIgnoreCase(mt)) {
                        cbModelTypeFilter.setValue(mt);
                    }
                }
            } finally {
                syncing = false;
            }
        });

        // ===== Khi đổi ModelTypeFilter -> nếu đã có code/name thì chọn lại record đúng type =====
        cbModelTypeFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (syncing) return;

            String code = txtProductCode.getText();
            String name = txtProductName.getText();

            // ưu tiên lấy theo code nếu có
            if (code != null && !code.isBlank()) {
                String key = code.trim().toLowerCase();
                List<Product> list = byCode.get(key);
                Product found = pickBest.apply(list, newVal);

                if (found != null) {
                    syncing = true;
                    try {
                        if (found.getName() != null) txtProductName.setText(found.getName());
                    } finally {
                        syncing = false;
                    }
                }
                return;
            }

            // fallback theo name
            if (name != null && !name.isBlank()) {
                String key = name.trim().toLowerCase();
                List<Product> list = byName.get(key);
                Product found = pickBest.apply(list, newVal);

                if (found != null) {
                    syncing = true;
                    try {
                        if (found.getProductCode() != null) txtProductCode.setText(found.getProductCode());
                    } finally {
                        syncing = false;
                    }
                }
            }
        });
    }




    /* ===========================
     * BUSINESS ACTIONS
     * =========================== */
    private void loadAllProducts() {
        allProducts = productService.getAllProducts();
        tblProducts.setItems(FXCollections.observableArrayList(allProducts));
    }

    private void onLoadBOM() {
        String code = txtProductCode.getText() != null ? txtProductCode.getText().trim() : "";
        String type = cbModelTypeFilter.getValue(); // có thể null

        // 1) Load BOM như cũ
        bomLoader.loadBom(
                txtProductCode,
                txtProductName,
                cbModelTypeFilter,
                allProducts,
                tblProductBOM
        );

        // 2) Nếu không có BOM -> focus model trong tblProducts
        if (tblProductBOM.getItems() == null || tblProductBOM.getItems().isEmpty()) {
            focusProductInList(code, type);

            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Thông báo");
            a.setHeaderText(null);
            a.setContentText("Model này chưa có BOM. Đã focus model trong danh sách Products.");
            a.showAndWait();
        }
    }

    private void focusProductInList(String productCode, String modelType) {
        if (productCode == null || productCode.isBlank()) return;

        // đảm bảo tblProducts có data
        if (tblProducts.getItems() == null || tblProducts.getItems().isEmpty()) {
            loadAllProducts();
        }

        var items = tblProducts.getItems();
        if (items == null || items.isEmpty()) return;

        // ưu tiên match theo code + type (nếu có)
        int idx = -1;

        if (modelType != null && !modelType.isBlank()) {
            for (int i = 0; i < items.size(); i++) {
                Product p = items.get(i);
                if (p == null) continue;

                boolean matchCode = p.getProductCode() != null
                        && p.getProductCode().equalsIgnoreCase(productCode);

                boolean matchType = p.getModelType() != null
                        && p.getModelType().name().equalsIgnoreCase(modelType);

                if (matchCode && matchType) {
                    idx = i;
                    break;
                }
            }
        }

        // fallback: match theo code thôi
        if (idx < 0) {
            for (int i = 0; i < items.size(); i++) {
                Product p = items.get(i);
                if (p == null) continue;

                if (p.getProductCode() != null && p.getProductCode().equalsIgnoreCase(productCode)) {
                    idx = i;
                    break;
                }
            }
        }

        if (idx >= 0) {
            tblProducts.getSelectionModel().clearAndSelect(idx);
            tblProducts.scrollTo(idx);
            tblProducts.requestFocus();
        }
    }


}
