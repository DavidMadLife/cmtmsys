package org.chemtrovina.cmtmsys.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.dto.ProductBomDto;
import org.chemtrovina.cmtmsys.model.ProductBOM;
import org.chemtrovina.cmtmsys.repository.Impl.ProductBOMRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.base.ProductBOMRepository;
import org.chemtrovina.cmtmsys.service.Impl.ProductBOMServiceImpl;
import org.chemtrovina.cmtmsys.service.base.ProductBOMService;
import org.springframework.jdbc.core.JdbcTemplate;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

import java.io.File;
import java.io.FileInputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProductController {

    @FXML private TextField txtProductCode;
    @FXML private Button btnLoad;
    @FXML private TableView<ProductBomDto> tblProductBOM;
    @FXML private TableColumn<ProductBomDto, String> colProductCode;
    @FXML private TableColumn<ProductBomDto, String> colSappn;
    @FXML private TableColumn<ProductBomDto, Double> colQuantity;
    @FXML private TableColumn<ProductBomDto, String> colCreatedDate;
    @FXML private TableColumn<ProductBomDto, String> colUpdatedDate;

    @FXML private Button btnChooseFile;
    @FXML private Button btnImport;
    @FXML private TextField txtFileName;

    private File selectedFile;



    private ProductBOMService productBOMService;

    @FXML
    public void initialize() {
        setupService();
        setupTable();
        setupActions();
        btnChooseFile.setOnAction(e -> onChooseFile());
        btnImport.setOnAction(e -> onImportFile());

    }

    private void setupService() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getDataSource());
        ProductBOMRepository repository = new ProductBOMRepositoryImpl(jdbcTemplate);
        this.productBOMService = new ProductBOMServiceImpl(repository);
    }

    private void setupTable() {
        colProductCode.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        colSappn.setCellValueFactory(new PropertyValueFactory<>("sappn"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCreatedDate.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        colUpdatedDate.setCellValueFactory(new PropertyValueFactory<>("updatedDate"));
        tblProductBOM.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tblProductBOM.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode().toString().equals("C")) {
                copySelectionToClipboard();
            }
        });
        // ✅ Cho phép chọn từng ô
        tblProductBOM.getSelectionModel().setCellSelectionEnabled(true);

        // ✅ Cho phép chọn nhiều ô (liên tiếp hoặc rời rạc)
        tblProductBOM.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void setupActions() {
        btnLoad.setOnAction(e -> onSearch());
    }

    private void onSearch() {
        String productCode = txtProductCode.getText().trim();
        if (productCode.isEmpty()) {
            showAlert("Vui lòng nhập mã thành phẩm (ProductCode).");
            return;
        }

        List<ProductBomDto> bomList = productBOMService.getBomDtoByProductCode(productCode);
        tblProductBOM.setItems(FXCollections.observableArrayList(bomList));
    }

    private void copySelectionToClipboard() {
        StringBuilder clipboardString = new StringBuilder();
        ObservableList<TablePosition> positionList = tblProductBOM.getSelectionModel().getSelectedCells();

        int prevRow = -1;
        for (TablePosition position : positionList) {
            int row = position.getRow();
            int col = position.getColumn();

            Object cell = tblProductBOM.getColumns().get(col).getCellData(row);
            if (cell == null) {
                cell = "";
            }

            if (prevRow == row) {
                clipboardString.append('\t');
            } else if (prevRow != -1) {
                clipboardString.append('\n');
            }

            clipboardString.append(cell);
            prevRow = row;
        }

        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(clipboardString.toString());
        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }

    private void onChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file Excel");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedFile = file;
            txtFileName.setText(file.getName());
        }
    }

    private void onImportFile() {
        if (selectedFile == null) {
            showAlert("Vui lòng chọn file Excel trước khi import.");
            return;
        }

        try (FileInputStream fis = new FileInputStream(selectedFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getDataSource());

            int insertCount = 0;
            int updateCount = 0;
            int skipCount = 0;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Bỏ header

                String productCode = getCellString(row.getCell(0)).trim().replaceAll("\\s+", "");
                String sappn = getCellString(row.getCell(1)).trim();
                double quantity = row.getCell(2).getNumericCellValue();

                if (productCode.isEmpty() || sappn.isEmpty()) {
                    System.out.println("⚠ Dòng " + (row.getRowNum() + 1) + " thiếu dữ liệu → Bỏ qua");
                    skipCount++;
                    continue;
                }

                // Tìm productId theo productCode
                List<Integer> result = jdbcTemplate.query(
                        "SELECT productId FROM Products WHERE productCode = ?",
                        new Object[]{productCode},
                        (rs, rowNum) -> rs.getInt("productId")
                );

                Integer productId;

                if (result.isEmpty()) {
                    // Tạo mới product nếu chưa tồn tại
                    jdbcTemplate.update(
                            "INSERT INTO Products (productCode, createdDate, updatedDate) VALUES (?, GETDATE(), GETDATE())",
                            productCode
                    );

                    productId = jdbcTemplate.queryForObject(
                            "SELECT productId FROM Products WHERE productCode = ?",
                            new Object[]{productCode},
                            Integer.class
                    );

                    System.out.println("✅ Đã tạo mới product: " + productCode);
                } else {
                    productId = result.get(0);
                }

                // Kiểm tra BOM đã tồn tại chưa
                List<Integer> bomExists = jdbcTemplate.query(
                        "SELECT 1 FROM ProductBOM WHERE productId = ? AND sappn = ?",
                        new Object[]{productId, sappn},
                        (rs, rowNum) -> rs.getInt(1)
                );

                if (!bomExists.isEmpty()) {
                    // Đã tồn tại → cập nhật quantity và updatedDate
                    jdbcTemplate.update(
                            "UPDATE ProductBOM SET quantity = ?, updatedDate = GETDATE() WHERE productId = ? AND sappn = ?",
                            quantity, productId, sappn
                    );
                    updateCount++;
                    System.out.println("🔁 Update BOM: " + productCode + " - " + sappn);
                } else {
                    // Chưa có → insert mới
                    jdbcTemplate.update(
                            "INSERT INTO ProductBOM (productId, sappn, quantity, createdDate, updatedDate) " +
                                    "VALUES (?, ?, ?, GETDATE(), GETDATE())",
                            productId, sappn, quantity
                    );
                    insertCount++;
                    System.out.println("➕ Insert BOM: " + productCode + " - " + sappn);
                }
            }

            showAlert("✅ Import hoàn tất:\nThêm mới: " + insertCount +
                    "\nCập nhật: " + updateCount +
                    "\nBỏ qua: " + skipCount);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("❌ Lỗi khi import: " + e.getMessage());
        }
    }



    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue()).replaceAll("\\.0+$", "");
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }


    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
