package org.chemtrovina.cmtmsys.controller;


import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.chemtrovina.cmtmsys.controller.product.*;
import org.chemtrovina.cmtmsys.dto.ProductBomDto;
import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.service.base.ProductBOMService;
import org.chemtrovina.cmtmsys.service.base.ProductService;
import org.chemtrovina.cmtmsys.utils.AutoCompleteUtils;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.chemtrovina.cmtmsys.utils.TableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

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

    private void setupAutoCompleteModels() {
        if (allProducts == null) return;

        // Lọc null + chuẩn hoá
        List<String> codes = allProducts.stream()
                .map(Product::getProductCode)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .toList();

        List<String> names = allProducts.stream()
                .map(Product::getName)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .toList();

        // thiết lập autocomplete
        AutoCompleteUtils.setupAutoComplete(txtProductCode, codes);
        AutoCompleteUtils.setupAutoComplete(txtProductName, names);

        // ===== Khi chọn CODE → fill NAME + TYPE =====
        txtProductCode.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) return;

            Product found = allProducts.stream()
                    .filter(p -> newVal.equalsIgnoreCase(p.getProductCode()))
                    .findFirst()
                    .orElse(null);

            if (found != null) {
                txtProductName.setText(found.getName());
                if (found.getModelType() != null) {
                    cbModelTypeFilter.setValue(found.getModelType().name());
                }
            }
        });

        // ===== Khi chọn NAME → fill CODE + TYPE =====
        txtProductName.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) return;

            Product found = allProducts.stream()
                    .filter(p -> newVal.equalsIgnoreCase(
                            p.getName() != null ? p.getName() : ""))
                    .findFirst()
                    .orElse(null);

            if (found != null) {
                txtProductCode.setText(found.getProductCode());
                if (found.getModelType() != null) {
                    cbModelTypeFilter.setValue(found.getModelType().name());
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
        bomLoader.loadBom(
                txtProductCode,
                txtProductName,
                cbModelTypeFilter,
                allProducts,
                tblProductBOM
        );
    }
}
