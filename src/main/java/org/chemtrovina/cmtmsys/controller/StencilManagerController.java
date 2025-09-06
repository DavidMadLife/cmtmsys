package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chemtrovina.cmtmsys.dto.StencilViewDto;
import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.model.Stencil;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.service.base.ProductService;
import org.chemtrovina.cmtmsys.service.base.StencilService;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.chemtrovina.cmtmsys.utils.TableColumnUtils.*;

@Component
public class StencilManagerController {

    @FXML private TableView<StencilViewDto> tblStencils;
    @FXML private TableColumn<StencilViewDto, Number> colSTT;
    @FXML private TableColumn<StencilViewDto, String> colBarcode, colStencilNo, colProductCode, colProductName, colModelType, colVersion, colSize, colStatus, colWarehouse, colNote;
    @FXML private TableColumn<StencilViewDto, Number> colArray;
    @FXML private TableColumn<StencilViewDto, LocalDate> colReceived;
    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbProduct, cbStatus, cbWarehouse;
    @FXML private Label lblTotal;
    @FXML private Button btnRefresh, btnClearFilters, btnImportExcel;

    private List<StencilViewDto> masterList = new ArrayList<>();
    private final StencilService stencilService;
    private final ProductService productService;

    @Autowired
    public StencilManagerController(StencilService stencilService, ProductService productService) {
        this.stencilService = stencilService;
        this.productService = productService;
    }

    @FXML
    public void initialize() {
        setupColumns();
        loadFromDatabase();
        setupSearchFilters();
        enableClipboardSupport();
        btnImportExcel.setOnAction(e -> handleImportExcel());
    }

    private void setupColumns() {
        colSTT.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createIntegerBinding(() -> tblStencils.getItems().indexOf(cellData.getValue()) + 1));
        setStringColumn(colBarcode, StencilViewDto::getBarcode);
        setStringColumn(colStencilNo, StencilViewDto::getStencilNo);
        setStringColumn(colProductCode, StencilViewDto::getProductCode);
        setStringColumn(colProductName, StencilViewDto::getProductName);
        setStringColumn(colModelType, StencilViewDto::getModelType);
        setStringColumn(colVersion, StencilViewDto::getVersionLabel);
        setStringColumn(colSize, StencilViewDto::getSize);
        setIntegerColumn(colArray, StencilViewDto::getArrayCount);
        setStringColumn(colStatus, StencilViewDto::getStatus);
        setStringColumn(colWarehouse, StencilViewDto::getWarehouse);
        setObjectColumn(colReceived, StencilViewDto::getReceivedDate);
        setStringColumn(colNote, StencilViewDto::getNote);
    }

    private void loadFromDatabase() {
        masterList = stencilService.getAllStencilViews();
        applyFilters();
    }

    private void setupSearchFilters() {
        tfSearch.setOnAction(e -> applyFilters());
        cbProduct.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbStatus.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbWarehouse.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        btnRefresh.setOnAction(e -> loadFromDatabase());
        btnClearFilters.setOnAction(e -> {
            tfSearch.clear();
            cbProduct.setValue(null);
            cbStatus.setValue(null);
            cbWarehouse.setValue(null);
        });

        cbProduct.getItems().setAll(masterList.stream().map(StencilViewDto::getProductCode).distinct().sorted().toList());
        cbStatus.getItems().setAll(masterList.stream().map(StencilViewDto::getStatus).distinct().sorted().toList());
        cbWarehouse.getItems().setAll(masterList.stream().map(StencilViewDto::getWarehouse).distinct().sorted().toList());
    }

    private void applyFilters() {
        String keyword = tfSearch.getText().trim();
        String selectedModel = cbProduct.getValue();
        String selectedStatus = cbStatus.getValue();
        String selectedWarehouse = cbWarehouse.getValue();

        List<StencilViewDto> filtered = stencilService.searchViews(keyword, selectedModel, selectedStatus, selectedWarehouse);
        tblStencils.setItems(FXCollections.observableArrayList(filtered));
        lblTotal.setText(String.valueOf(filtered.size()));
    }

    private void enableClipboardSupport() {
        tblStencils.getSelectionModel().setCellSelectionEnabled(true);
        tblStencils.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tblStencils.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode().toString().equals("C")) {
                FxClipboardUtils.copySelectionToClipboard(tblStencils);
            }
        });
    }

    private void handleImportExcel() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fc.showOpenDialog(tblStencils.getScene().getWindow());
        if (file != null) {
            importStencilExcel(file);
            loadFromDatabase();
        }
    }

    private void importStencilExcel(File file) {
        try (FileInputStream fis = new FileInputStream(file); Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            int imported = 0, skipped = 0;
            List<String> errorMessages = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String barcode = getCellString(row.getCell(0));
                    String stencilNo = getCellString(row.getCell(1));
                    String productCode = getCellString(row.getCell(2));
                    String modelTypeStr = getCellString(row.getCell(3));
                    String version = getCellString(row.getCell(4));
                    String size = getCellString(row.getCell(5));
                    String arrayStr = getCellString(row.getCell(6));
                    String note = getCellString(row.getCell(9));

                    // Parse array count
                    int array;
                    try {
                        array = Integer.parseInt(arrayStr);
                    } catch (NumberFormatException ex) {
                        skipped++;
                        errorMessages.add("Dòng " + (i + 1) + ": ArrayCount không hợp lệ: '" + arrayStr + "'");
                        continue;
                    }

                    // Parse modelType
                    ModelType modelType;
                    try {
                        modelType = ModelType.valueOf(modelTypeStr.toUpperCase());
                    } catch (IllegalArgumentException ex) {
                        skipped++;
                        errorMessages.add("Dòng " + (i + 1) + ": ModelType không hợp lệ: '" + modelTypeStr + "'");
                        continue;
                    }

                    // Parse received date
                    LocalDate receivedDate;
                    try {
                        receivedDate = row.getCell(8).getLocalDateTimeCellValue().toLocalDate();
                    } catch (Exception ex) {
                        skipped++;
                        errorMessages.add("Dòng " + (i + 1) + ": ReceivedDate không hợp lệ.");
                        continue;
                    }

                    // Check product
                    Product product = productService.getProductByCodeAndType(productCode, modelType);
                    if (product == null) {
                        skipped++;
                        errorMessages.add("Dòng " + (i + 1) + ": Không tìm thấy Product với code '" + productCode + "' và type '" + modelType + "'");
                        continue;
                    }

                    // Check duplicate barcode
                    if (stencilService.existsByBarcode(barcode)) {
                        skipped++;
                        errorMessages.add("Dòng " + (i + 1) + ": Barcode '" + barcode + "' đã tồn tại");
                        continue;
                    }

                    // Create and save stencil
                    Stencil s = new Stencil();
                    s.setBarcode(barcode);
                    s.setStencilNo(stencilNo);
                    s.setVersionLabel(version);
                    s.setSize(size);
                    s.setArrayCount(array);
                    s.setReceivedDate(receivedDate);
                    s.setNote(note);
                    s.setProductId(product.getProductId());
                    s.setCurrentWarehouseId(1025); // phòng mark
                    s.setStatus("NEW");

                    stencilService.addStencil(s);
                    imported++;
                } catch (Exception ex) {
                    skipped++;
                    errorMessages.add("Dòng " + (i + 1) + ": Lỗi không xác định - " + ex.getMessage());
                }
            }

            // Show result
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Kết quả Import");
            alert.setHeaderText("✅ Thành công: " + imported + " | ❌ Bỏ qua: " + skipped);

            if (!errorMessages.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                errorMessages.stream().limit(20).forEach(msg -> sb.append(msg).append("\n"));
                if (errorMessages.size() > 20) {
                    sb.append("...và ").append(errorMessages.size() - 20).append(" dòng khác.");
                }
                alert.setContentText(sb.toString());
            }

            alert.showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Lỗi khi mở file: " + e.getMessage()).showAndWait();
        }
    }

    private String getCellString(Cell cell) {
        return cell == null ? "" : switch (cell.getCellType()) {
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case STRING -> cell.getStringCellValue().trim();
            default -> "";
        };
    }
}
