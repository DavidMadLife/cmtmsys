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
import org.chemtrovina.cmtmsys.repository.Impl.ProductBOMRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.Impl.ProductRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.base.ProductBOMRepository;
import org.chemtrovina.cmtmsys.repository.base.ProductRepository;
import org.chemtrovina.cmtmsys.service.Impl.ProductBOMServiceImpl;
import org.chemtrovina.cmtmsys.service.Impl.ProductServiceImpl;
import org.chemtrovina.cmtmsys.service.base.ProductBOMService;
import org.chemtrovina.cmtmsys.service.base.ProductService;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.chemtrovina.cmtmsys.utils.FxFilterUtils;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
@Component
public class ProductController {

    @FXML private TextField txtProductCode;
    @FXML private Button btnLoad;
    @FXML private TableView<ProductBomDto> tblProductBOM;
    @FXML private TableColumn<ProductBomDto, String> colProductCode;
    @FXML private TableColumn<ProductBomDto, String> colSappn;
    @FXML private TableColumn<ProductBomDto, Double> colQuantity;
    @FXML private TableColumn<ProductBomDto, String> colModelType;
    @FXML private TableColumn<ProductBomDto, String> colCreatedDate;
    @FXML private TableColumn<ProductBomDto, String> colUpdatedDate;

    @FXML private Button btnChooseFile;
    @FXML private Button btnImport;
    @FXML private Button btnUpdateProduct;
    @FXML private Button btnDeleteProduct;
    @FXML private TextField txtFileName;

    @FXML private TextField txtNewProductCode;
    @FXML private ComboBox<String> cbNewModelType;
    @FXML private Button btnCreateProduct;


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
        cbNewModelType.setItems(FXCollections.observableArrayList("TOP", "BOT", "NONE"));
        btnCreateProduct.setOnAction(e -> onCreateProduct());
        tblProductBOM.getSelectionModel().setCellSelectionEnabled(true);
        tblProductBOM.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void setupTable() {
        colProductCode.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        colSappn.setCellValueFactory(new PropertyValueFactory<>("sappn"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colModelType.setCellValueFactory(new PropertyValueFactory<>("modelType"));
        colCreatedDate.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        colUpdatedDate.setCellValueFactory(new PropertyValueFactory<>("updatedDate"));

        tblProductBOM.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblProductBOM.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == javafx.scene.input.KeyCode.C) {
                FxClipboardUtils.copySelectionToClipboard(tblProductBOM);
            }
        });

    }

    private void setupActions() {
        btnLoad.setOnAction(e -> onLoadBOM());
        btnChooseFile.setOnAction(e -> onChooseFile());
        btnImport.setOnAction(e -> onImport());
        btnUpdateProduct.setOnAction(e -> onUpdateProduct());
        btnDeleteProduct.setOnAction(e -> onDeleteProduct());
    }

    private void onLoadBOM() {
        String code = txtProductCode.getText().trim();
        if (code.isEmpty()) {
            showAlert("Vui lòng nhập mã sản phẩm.");
            return;
        }


        List<ProductBomDto> dtos = productBOMService.getBomDtoByProductCode(code);
        originalBomList = FXCollections.observableArrayList(dtos);
        tblProductBOM.setItems(originalBomList);
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
                String modelType = getCellString(row.getCell(3)).toUpperCase();

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

                // Xử lý BOM
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

    private void onCreateProduct() {
        String newCode = txtNewProductCode.getText().trim();
        String modelType = cbNewModelType.getValue();

        if (newCode.isEmpty() || modelType == null) {
            showAlert("Vui lòng nhập mã sản phẩm và chọn loại model.");
            return;
        }

        JdbcTemplate jdbc = new JdbcTemplate(DataSourceConfig.getDataSource());

        try {
            List<Integer> exists = jdbc.query(
                    "SELECT 1 FROM Products WHERE productCode = ? AND modelType = ?",
                    new Object[]{newCode, modelType},
                    (rs, i) -> rs.getInt(1)
            );

            if (!exists.isEmpty()) {
                showAlert("❌ Mã sản phẩm với loại model này đã tồn tại.");
                return;
            }

            jdbc.update(
                    "INSERT INTO Products (productCode, modelType, createdDate, updatedDate) VALUES (?, ?, GETDATE(), GETDATE())",
                    newCode, modelType
            );

            showAlert("✅ Đã tạo sản phẩm mới: " + newCode + " | " + modelType);
            txtNewProductCode.clear();
            cbNewModelType.getSelectionModel().clearSelection();

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("❌ Lỗi khi tạo sản phẩm: " + ex.getMessage());
        }
    }

    private void onUpdateProduct() {
        String code = txtProductCode.getText().trim();
        if (code.isEmpty()) {
            showAlert("Vui lòng nhập mã sản phẩm để cập nhật.");
            return;
        }

        Product selected = productService.getProductByCode(code);
        if (selected == null) {
            showAlert("❌ Không tìm thấy sản phẩm có mã: " + code);
            return;
        }

        // Tạo dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Cập nhật sản phẩm");
        dialog.setHeaderText("Chỉnh sửa thông tin sản phẩm");

        TextField txtCode = new TextField(selected.getProductCode());
        TextField txtDesc = new TextField(selected.getDescription() != null ? selected.getDescription() : "");
        ComboBox<String> cbModel = new ComboBox<>();
        cbModel.setItems(FXCollections.observableArrayList("TOP", "BOT", "NONE"));
        cbModel.setValue(selected.getModelType() != null ? selected.getModelType().toString() : "NONE");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Mã sản phẩm:"), 0, 0);
        grid.add(txtCode, 1, 0);
        grid.add(new Label("Mô tả:"), 0, 1);
        grid.add(txtDesc, 1, 1);
        grid.add(new Label("Loại model:"), 0, 2);
        grid.add(cbModel, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String newCode = txtCode.getText().trim();
                    String desc = txtDesc.getText().trim();
                    String modelStr = cbModel.getValue();

                    if (newCode.isEmpty() || modelStr == null) {
                        showAlert("Vui lòng nhập đầy đủ thông tin.");
                        return;
                    }

                    selected.setProductCode(newCode);
                    selected.setDescription(desc);
                    selected.setModelType(ModelType.valueOf(modelStr));

                    productService.updateProduct(selected);

                    showAlert("✅ Đã cập nhật sản phẩm.");
                    txtProductCode.setText(newCode);  // Cập nhật lại textbox
                    onLoadBOM(); // Reload bảng BOM nếu cần

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
