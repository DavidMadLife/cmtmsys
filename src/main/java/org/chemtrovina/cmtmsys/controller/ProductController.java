package org.chemtrovina.cmtmsys.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.dto.ProductBomDto;
import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.service.base.ProductBOMService;
import org.chemtrovina.cmtmsys.service.base.ProductService;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.chemtrovina.cmtmsys.utils.FxFilterUtils;
import org.chemtrovina.cmtmsys.utils.TableUtils;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;


import java.io.File;
import java.io.FileInputStream;
import java.util.List;
@Component
public class ProductController {

    @FXML private TextField txtProductCode;
    @FXML private Button btnLoad;
    @FXML private TableView<ProductBomDto> tblProductBOM;

    @FXML
    private TableColumn<ProductBomDto, Number> colIndex;

    @FXML private TableColumn<ProductBomDto, String> colProductCode;
    @FXML private TableColumn<ProductBomDto, String> colSappn;
    @FXML private TableColumn<ProductBomDto, Double> colQuantity;
    @FXML private TableColumn<ProductBomDto, String> colModelType;
    @FXML private TableColumn<ProductBomDto, String> colCreatedDate;
    @FXML private TableColumn<ProductBomDto, String> colUpdatedDate;

    @FXML private TableView<Product> tblProducts;
    @FXML private TableColumn<Product, String> colProductCodeList;
    @FXML private TableColumn<Product, String> colModelTypeList;
    @FXML private TableColumn<Product, String> colDescriptionList;
    @FXML private TableColumn<Product, String> colNameList;



    @FXML private Button btnChooseFile;
    @FXML private Button btnImport;
    @FXML private Button btnUpdateProduct;
    @FXML private Button btnDeleteProduct;
    @FXML private TextField txtFileName;

    @FXML private TextField txtNewProductCode;
    @FXML private TextField txtNewProductName;
    @FXML private ComboBox<String> cbNewModelType;
    @FXML private Button btnCreateProduct;
    @FXML private ComboBox<String> cbModelTypeFilter;



    private File selectedFile;

    private ObservableList<ProductBomDto> originalBomList;


    private final ProductBOMService productBOMService;
    private final ProductService productService;

    @Autowired
    public ProductController(ProductBOMService productBOMService, ProductService productService) {
        this.productBOMService = productBOMService;
        this.productService = productService;
    }


    @FXML
    public void initialize() {
        setupTable();
        setupActions();
        cbNewModelType.setItems(FXCollections.observableArrayList("TOP", "BOT", "SINGLE", "BOTTOP","NONE"));
        cbModelTypeFilter.setItems(FXCollections.observableArrayList("TOP", "BOT", "SINGLE", "BOTTOP", "NONE"));
        btnCreateProduct.setOnAction(e -> onCreateProduct());

        TableUtils.centerAlignAllColumns(tblProductBOM);
    }

    private void setupTable() {
        colIndex.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createIntegerBinding(() ->
                tblProductBOM.getItems().indexOf(cellData.getValue()) + 1));

        colProductCode.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        colSappn.setCellValueFactory(new PropertyValueFactory<>("sappn"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colModelType.setCellValueFactory(new PropertyValueFactory<>("modelType"));
        colCreatedDate.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        colUpdatedDate.setCellValueFactory(new PropertyValueFactory<>("updatedDate"));

        colProductCodeList.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        colModelTypeList.setCellValueFactory(new PropertyValueFactory<>("modelType"));
        colDescriptionList.setCellValueFactory(new PropertyValueFactory<>("description"));
        colNameList.setCellValueFactory(new PropertyValueFactory<>("name"));


        FxClipboardUtils.enableCopyShortcut(tblProducts);
        FxClipboardUtils.enableCopyShortcut(tblProductBOM);

    }

    private void setupActions() {
        btnLoad.setOnAction(e -> onLoadBOM());
        btnChooseFile.setOnAction(e -> onChooseFile());
        btnImport.setOnAction(e -> onImport());
        btnUpdateProduct.setOnAction(e -> onUpdateProduct());
        btnDeleteProduct.setOnAction(e -> onDeleteProduct());
        tblProducts.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();

            // Context menu
            ContextMenu contextMenu = new ContextMenu();

            MenuItem updateItem = new MenuItem("Cập nhật");
            MenuItem deleteItem = new MenuItem("Xoá");

            // Xử lý khi chọn "Cập nhật"
            updateItem.setOnAction(e -> {
                Product selectedProduct = row.getItem();
                if (selectedProduct != null) {
                    txtProductCode.setText(selectedProduct.getProductCode());
                    onUpdateProduct();  // Gọi hàm đã có
                }
            });

            // Xử lý khi chọn "Xoá"
            deleteItem.setOnAction(e -> {
                Product selectedProduct = row.getItem();
                if (selectedProduct != null) {
                    txtProductCode.setText(selectedProduct.getProductCode());
                    onDeleteProduct();  // Gọi hàm đã có
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
        tblProducts.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Product selectedProduct = row.getItem();
                    txtProductCode.setText(selectedProduct.getProductCode());
                    cbModelTypeFilter.setValue(selectedProduct.getModelType().name());
                    onLoadBOM();
                }
            });
            return row;
        });
    }

    private void onLoadBOM() {
        // ✅ Luôn tải danh sách sản phẩm
        loadAllProducts();

        String code = txtProductCode.getText().trim();
        String modelType = cbModelTypeFilter.getValue();

        if (code.isEmpty() || modelType == null) {
            return; // Không cần báo lỗi nữa, vì đã load sản phẩm
        }

        List<ProductBomDto> dtos = productBOMService.getBomDtoByCodeAndModel(code, modelType);

        if (dtos.isEmpty()) {
            showAlert("⚠️ Không tìm thấy BOM tương ứng.");
            return;
        }

        originalBomList = FXCollections.observableArrayList(dtos);
        tblProductBOM.setItems(originalBomList);

        // ✅ Giữ nguyên danh sách sản phẩm
        // tblProducts.getItems().clear();

        FxFilterUtils.setupFilterMenu(
                colSappn,
                originalBomList,
                ProductBomDto::getSappn,
                selectedValues -> {
                    List<ProductBomDto> filtered = originalBomList.stream()
                            .filter(dto -> selectedValues.contains(dto.getSappn()))
                            .toList();
                    tblProductBOM.setItems(FXCollections.observableArrayList(filtered));
                }
        );
    }

    private void loadAllProducts() {
        List<Product> products = productService.getAllProducts(); // Bạn cần có hàm này trong service & repo
        tblProducts.setItems(FXCollections.observableArrayList(products));
    }

    private void onChooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn file Excel");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
        selectedFile = chooser.showOpenDialog(null);
        txtFileName.setText(selectedFile != null ? selectedFile.getName() : "Chưa chọn file");
    }

    private void onImport() {
        if (selectedFile == null) {
            showAlert("Vui lòng chọn file Excel.");
            return;
        }

        try (Workbook wb = new XSSFWorkbook(new FileInputStream(selectedFile))) {
            Sheet sheet = wb.getSheetAt(0);
            JdbcTemplate jdbc = new JdbcTemplate(DataSourceConfig.getDataSource());

            int inserted = 0, updated = 0;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String productCode = getCellString(row.getCell(0));
                String sap = getCellString(row.getCell(1));
                double qty = row.getCell(2).getNumericCellValue();
                String modelTypeRaw = getCellString(row.getCell(3));
                String modelType = (modelTypeRaw == null || modelTypeRaw.isBlank())
                        ? "NONE"
                        : modelTypeRaw.trim().toUpperCase();

                if (productCode.isBlank() || sap.isBlank()) continue;

                // Tìm hoặc tạo mới product
                List<Integer> ids = jdbc.query(
                        "SELECT productId FROM Products WHERE productCode = ?",
                        new Object[]{productCode},
                        (rs, i) -> rs.getInt(1)
                );

                int productId;
                if (ids.isEmpty()) {
                    jdbc.update("""
                    INSERT INTO Products (productCode, modelType, createdDate, updatedDate)
                    VALUES (?, ?, GETDATE(), GETDATE())
                """, productCode, modelType);
                    productId = jdbc.queryForObject("SELECT productId FROM Products WHERE productCode = ?", Integer.class, productCode);
                } else {
                    productId = ids.get(0);
                    // Cập nhật modelType nếu cần
                    jdbc.update("UPDATE Products SET modelType = ?, updatedDate = GETDATE() WHERE productId = ?", modelType, productId);
                }

                // Xử lý BOM (KHÔNG còn có modelType nữa)
                List<Integer> exists = jdbc.query(
                        "SELECT 1 FROM ProductBOM WHERE productId = ? AND sappn = ?",
                        new Object[]{productId, sap},
                        (rs, i) -> rs.getInt(1)
                );

                if (exists.isEmpty()) {
                    jdbc.update("""
                    INSERT INTO ProductBOM (productId, sappn, quantity, createdDate, updatedDate)
                    VALUES (?, ?, ?, GETDATE(), GETDATE())
                """, productId, sap, qty);
                    inserted++;
                } else {
                    jdbc.update("""
                    UPDATE ProductBOM SET quantity = ?, updatedDate = GETDATE()
                    WHERE productId = ? AND sappn = ?
                """, qty, productId, sap);
                    updated++;
                }
            }

            showAlert("✅ Import hoàn tất:\nThêm mới: " + inserted + "\nCập nhật: " + updated);
            onLoadBOM();

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("❌ Lỗi khi import: " + ex.getMessage());
        }
    }


    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue()).replaceAll("\\.0+$", "");
            default -> "";
        };
    }

    @FXML
    private void onCreateProduct() {
        String newCode = txtNewProductCode.getText().trim();
        String modelType = cbNewModelType.getValue();
        String modelName = txtNewProductName.getText().trim();

        if (newCode.isEmpty() || modelType == null || modelName.isEmpty()) {
            showAlert("⚠️ Vui lòng nhập đầy đủ: Mã sản phẩm, Tên sản phẩm và Loại model.");
            return;
        }

        try {
            Product product = new Product();
            product.setProductCode(newCode);
            product.setName(modelName);
            product.setModelType(Enum.valueOf(ModelType.class, modelType));
            product.setDescription(modelName);

            productService.addProduct(product);
            showAlert("✅ Đã tạo sản phẩm mới: " + newCode + " | " + modelName);

            txtNewProductCode.clear();
            txtNewProductName.clear();
            cbNewModelType.getSelectionModel().clearSelection();

        } catch (IllegalArgumentException ex) {
            showAlert("❌ " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("❌ Lỗi khi tạo sản phẩm: " + ex.getMessage());
        }
    }


    private void onUpdateProduct() {
        String code = txtProductCode.getText().trim();
        if (code.isEmpty()) {
            showAlert("⚠️ Vui lòng nhập mã sản phẩm để cập nhật.");
            return;
        }

        Product selected = productService.getProductByCode(code);
        if (selected == null) {
            showAlert("❌ Không tìm thấy sản phẩm có mã: " + code);
            return;
        }

        // ======= Tạo dialog cập nhật =======
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Cập nhật sản phẩm");
        dialog.setHeaderText("Chỉnh sửa thông tin sản phẩm");

        TextField txtCode = new TextField(selected.getProductCode());
        TextField txtName = new TextField(selected.getName() != null ? selected.getName() : "");
        TextField txtDesc = new TextField(selected.getDescription() != null ? selected.getDescription() : "");

        ComboBox<String> cbModel = new ComboBox<>();
        cbModel.setItems(FXCollections.observableArrayList("TOP", "BOT", "SINGLE", "BOTTOP", "NONE"));
        cbModel.setValue(selected.getModelType() != null ? selected.getModelType().toString() : "NONE");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Mã sản phẩm:"), 0, 0);
        grid.add(txtCode, 1, 0);
        grid.add(new Label("Tên sản phẩm (Model Name):"), 0, 1);
        grid.add(txtName, 1, 1);
        grid.add(new Label("Mô tả:"), 0, 2);
        grid.add(txtDesc, 1, 2);
        grid.add(new Label("Loại model:"), 0, 3);
        grid.add(cbModel, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String newCode = txtCode.getText().trim();
                    String newName = txtName.getText().trim();
                    String desc = txtDesc.getText().trim();
                    String modelStr = cbModel.getValue();

                    if (newCode.isEmpty() || newName.isEmpty() || modelStr == null) {
                        showAlert("⚠️ Vui lòng nhập đầy đủ thông tin: Code, Name, Model Type.");
                        return;
                    }

                    // ✅ Cập nhật đối tượng
                    selected.setProductCode(newCode);
                    selected.setName(newName);
                    selected.setDescription(desc);
                    selected.setModelType(ModelType.valueOf(modelStr));

                    // ✅ Gọi service để cập nhật DB
                    productService.updateProduct(selected);

                    showAlert("✅ Đã cập nhật sản phẩm thành công.");

                    // Cập nhật UI
                    txtProductCode.setText(newCode);
                    onLoadBOM(); // Reload BOM nếu có
                    loadAllProducts(); // Refresh danh sách

                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert("❌ Lỗi khi cập nhật: " + ex.getMessage());
                }
            }
        });
    }


    private void onDeleteProduct() {
        String code = txtProductCode.getText().trim();
        if (code.isEmpty()) {
            showAlert("Vui lòng nhập mã sản phẩm để xóa.");
            return;
        }

        Product selected = productService.getProductByCode(code);
        if (selected == null) {
            showAlert("❌ Không tìm thấy sản phẩm có mã: " + code);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Bạn có chắc chắn muốn xóa sản phẩm này?");
        confirm.setContentText("Mã sản phẩm: " + selected.getProductCode());

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    productService.deleteProductWithBOM(selected.getProductId());
                    showAlert("✅ Đã xóa sản phẩm và toàn bộ BOM.");
                    txtProductCode.clear();
                    tblProductBOM.getItems().clear();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert("❌ Lỗi khi xóa: " + ex.getMessage());
                }
            }
        });
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
