package org.chemtrovina.cmtmsys.controller;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chemtrovina.cmtmsys.dto.StencilViewDto;
import org.chemtrovina.cmtmsys.dto.WarehouseOption;
import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.model.Stencil;
import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.service.base.ProductService;
import org.chemtrovina.cmtmsys.service.base.StencilService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.chemtrovina.cmtmsys.utils.VersionUtils;
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

    // =======================
    // Services
    // =======================
    private final StencilService stencilService;
    private final ProductService productService;
    private final WarehouseService warehouseService;

    // =======================
    // FXML: Table
    // =======================
    @FXML private TableView<StencilViewDto> tblStencils;
    @FXML private TableColumn<StencilViewDto, Number> colSTT, colArray;
    @FXML private TableColumn<StencilViewDto, String> colBarcode, colStencilNo, colProductCode, colProductName,
            colModelType, colVersion, colSize, colStatus, colWarehouse, colNote;
    @FXML private TableColumn<StencilViewDto, LocalDate> colReceived;

    // =======================
    // FXML: Filter/Search toolbar
    // =======================
    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbProduct, cbStatus, cbWarehouse;
    @FXML private Button btnRefresh, btnClearFilters, btnImportExcel;

    // =======================
    // FXML: Scan & Transfer toolbar
    // =======================
    @FXML private TextField tfScanBarcode;
    @FXML private ComboBox<WarehouseOption> cbFromWarehouse, cbToWarehouse;
    @FXML private Button btnScanTransfer;
    @FXML private CheckBox chkEnforceLatest;

    // =======================
    // FXML: Upload/New panel
    // =======================
    @FXML private TextField tfNewProductCode, tfNewBarcode, tfNewStencilNo, tfNewVersion, tfNewSize;
    @FXML private ComboBox<ModelType> cbNewModelType;
    @FXML private ComboBox<String> cbNewStatus, cbNewWarehouse;
    @FXML private Spinner<Integer> spNewArray;
    @FXML private DatePicker dpNewReceived;
    @FXML private TextArea taNewNote;
    @FXML private Button btnAddNew;

    // =======================
    // FXML: Footer
    // =======================
    @FXML private Label lblTotal;

    // =======================
    // State
    // =======================
    private List<StencilViewDto> masterList = new ArrayList<>();

    // =======================
    // Ctor
    // =======================
    @Autowired
    public StencilManagerController(StencilService stencilService,
                                    ProductService productService,
                                    WarehouseService warehouseService) {
        this.stencilService = stencilService;
        this.productService = productService;
        this.warehouseService = warehouseService;
    }

    // =======================
    // Lifecycle
    // =======================
    @FXML
    public void initialize() {
        setupColumns();
        loadFromDatabase();

        setupSearchFilters();
        setupClipboardSupport();
        setupRowContextMenu();

        setupNewForm();
        setupScanTransferUI();

        btnAddNew.setOnAction(e -> handleAddNew());
        btnImportExcel.setOnAction(e -> handleImportExcel());
    }

    // =======================
    // UI: Column setup
    // =======================
    private void setupColumns() {
        // Nếu muốn tránh O(n^2), có thể đổi sang cellFactory theo TableRow.getIndex() (để nguyên theo bản hiện tại)
        colSTT.setCellValueFactory(cd ->
                javafx.beans.binding.Bindings.createIntegerBinding(
                        () -> tblStencils.getItems().indexOf(cd.getValue()) + 1));

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

    // =======================
    // Data loading + filters
    // =======================
    private void loadFromDatabase() {
        masterList = stencilService.getAllStencilViews();
        rebuildFilterChoices();
        applyFilters();
    }

    private void rebuildFilterChoices() {
        cbProduct.getItems().setAll(masterList.stream().map(StencilViewDto::getProductCode).distinct().sorted().toList());
        cbStatus.getItems().setAll(masterList.stream().map(StencilViewDto::getStatus).distinct().sorted().toList());
        cbWarehouse.getItems().setAll(masterList.stream().map(StencilViewDto::getWarehouse).distinct().sorted().toList());
    }

    private void setupSearchFilters() {
        tfSearch.setOnAction(e -> applyFilters());
        cbProduct.valueProperty().addListener((obs, o, n) -> applyFilters());
        cbStatus.valueProperty().addListener((obs, o, n) -> applyFilters());
        cbWarehouse.valueProperty().addListener((obs, o, n) -> applyFilters());

        btnRefresh.setOnAction(e -> loadFromDatabase());
        btnClearFilters.setOnAction(e -> {
            tfSearch.clear();
            cbProduct.setValue(null);
            cbStatus.setValue(null);
            cbWarehouse.setValue(null);
            applyFilters();
        });
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

    // =======================
    // UI: Clipboard & Context Menu
    // =======================
    private void setupClipboardSupport() {
        tblStencils.getSelectionModel().setCellSelectionEnabled(true);
        tblStencils.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tblStencils.setOnKeyPressed(e -> {
            if (e.isControlDown() && "C".equals(e.getCode().toString())) {
                FxClipboardUtils.copySelectionToClipboard(tblStencils);
            }
        });
    }

    private void setupRowContextMenu() {
        tblStencils.setRowFactory(tv -> {
            TableRow<StencilViewDto> row = new TableRow<>();

            ContextMenu menu = new ContextMenu();
            MenuItem miUpdate = new MenuItem("Cập nhật…");
            MenuItem miDelete = new MenuItem("Xoá…");
            menu.getItems().addAll(miUpdate, miDelete);

            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(menu));

            row.setOnMousePressed(ev -> {
                if (ev.getButton() == MouseButton.SECONDARY && !row.isEmpty()) {
                    tblStencils.getSelectionModel().select(row.getIndex());
                }
            });

            miUpdate.setOnAction(e -> {
                StencilViewDto v = row.getItem();
                if (v != null) openEditDialog(v);
            });
            miDelete.setOnAction(e -> {
                StencilViewDto v = row.getItem();
                if (v != null) deleteOne(v);
            });

            return row;
        });
    }

    // =======================
    // UI: New/Upload panel
    // =======================
    private void setupNewForm() {
        spNewArray.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 1));
        cbNewModelType.getItems().setAll(ModelType.values());
        cbNewStatus.getItems().setAll("NEW", "LOCKED", "IN_USE");
        cbNewStatus.setValue("NEW");
        dpNewReceived.setValue(LocalDate.now());
    }

    private void handleAddNew() {
        String barcode   = tfNewBarcode.getText().trim();
        String stencilNo = tfNewStencilNo.getText().trim();
        String prodCode  = tfNewProductCode.getText().trim();
        ModelType mtype  = cbNewModelType.getValue();
        String version   = tfNewVersion.getText().trim();
        String size      = tfNewSize.getText().trim();
        Integer array    = spNewArray.getValue();
        LocalDate received = dpNewReceived.getValue();
        String status    = cbNewStatus.getValue();
        String note      = taNewNote.getText();

        if (barcode.isEmpty() || stencilNo.isEmpty() || prodCode.isEmpty() || mtype == null || received == null) {
            alert(Alert.AlertType.WARNING, "Vui lòng nhập đủ: Barcode, Stencil No, Model Code, Model Type, Received date.");
            return;
        }
        if (stencilService.existsByBarcode(barcode)) {
            alert(Alert.AlertType.ERROR, "Barcode đã tồn tại: " + barcode);
            return;
        }

        Product p = productService.getProductByCodeAndType(prodCode, mtype);
        if (p == null) {
            alert(Alert.AlertType.ERROR, "Không tìm thấy Product với code '" + prodCode + "' và type '" + mtype + "'");
            return;
        }

        Stencil s = new Stencil();
        s.setBarcode(barcode);
        s.setStencilNo(stencilNo);
        s.setVersionLabel(version);
        s.setSize(size);
        s.setArrayCount(array != null ? array : 1);
        s.setReceivedDate(received);
        s.setNote(note);
        s.setProductId(p.getProductId());
        s.setCurrentWarehouseId(1025);
        s.setStatus(status == null || status.isBlank() ? "NEW" : status);

        try {
            stencilService.addStencil(s);              // auto-lock các bản cũ diễn ra trong service
            alert(Alert.AlertType.INFORMATION, "Đã thêm stencil mới cho " + prodCode + " (" + mtype + ")");
            clearNewForm();
            loadFromDatabase();
        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, "Thêm stencil lỗi: " + ex.getMessage());
        }
    }

    private void clearNewForm() {
        tfNewBarcode.clear();
        tfNewStencilNo.clear();
        tfNewProductCode.clear();
        cbNewModelType.setValue(null);
        cbNewWarehouse.setValue(null);
        tfNewVersion.clear();
        tfNewSize.clear();
        spNewArray.getValueFactory().setValue(1);
        dpNewReceived.setValue(LocalDate.now());
        cbNewStatus.setValue("NEW");
        taNewNote.clear();
    }

    // =======================
    // UI: Scan & Transfer
    // =======================
    private void setupScanTransferUI() {
        List<Warehouse> all = warehouseService.getAllWarehouses();
        ObservableList<WarehouseOption> opts = FXCollections.observableArrayList();
        for (Warehouse w : all) opts.add(new WarehouseOption(w.getWarehouseId(), w.getName()));

        ObservableList<WarehouseOption> fromOpts = FXCollections.observableArrayList();
        fromOpts.add(new WarehouseOption(null, "Auto (kho hiện tại)"));
        fromOpts.addAll(opts);

        cbFromWarehouse.setItems(fromOpts);
        cbToWarehouse.setItems(opts);

        cbFromWarehouse.setConverter(warehouseConverter());
        cbToWarehouse.setConverter(warehouseConverter());

        cbFromWarehouse.getSelectionModel().selectFirst(); // From = Auto

        tfScanBarcode.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) handleScanTransfer(); });
        btnScanTransfer.setOnAction(e -> handleScanTransfer());
    }

    private void handleScanTransfer() {
        String barcode = tfScanBarcode.getText() == null ? "" : tfScanBarcode.getText().trim();
        if (barcode.isEmpty()) { alert(Alert.AlertType.WARNING, "Nhập/Quét barcode trước."); return; }

        Stencil stencil = stencilService.getStencilByBarcode(barcode);
        if (stencil == null) { alert(Alert.AlertType.ERROR, "Không tìm thấy stencil: " + barcode); return; }

        WarehouseOption fromSel = cbFromWarehouse.getValue();
        WarehouseOption toSel   = cbToWarehouse.getValue();
        if (toSel == null) { alert(Alert.AlertType.WARNING, "Chọn kho đích (To)."); return; }

        Integer toWarehouseId = toSel.id;
        Integer currentWarehouseId = stencil.getCurrentWarehouseId();
        Integer fromWarehouseId = (fromSel == null || fromSel.id == null) ? currentWarehouseId : fromSel.id;

        if (toWarehouseId != null && fromWarehouseId != null && toWarehouseId.equals(fromWarehouseId)) {
            alert(Alert.AlertType.INFORMATION, "Kho đích trùng kho hiện tại. Không cần chuyển.");
            tfScanBarcode.clear();
            return;
        }

        if (chkEnforceLatest.isSelected() && !isLatestVersionOfProduct(stencil)) {
            alert(Alert.AlertType.ERROR,
                    "Stencil này KHÔNG phải version mới nhất của model.\n" +
                            "Bỏ tick 'Chỉ cho chuyển nếu bản mới nhất' nếu vẫn muốn chuyển.");
            return;
        }

        try {
            stencilService.transferWarehouse(stencil.getStencilId(), toWarehouseId);
            tfScanBarcode.clear();
            loadFromDatabase();
            alert(Alert.AlertType.INFORMATION,
                    "Đã chuyển " + barcode + " từ " + displayWarehouse(fromWarehouseId) + " → " + displayWarehouse(toWarehouseId));
        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, "Chuyển kho thất bại: " + ex.getMessage());
        }
    }

    private StringConverter<WarehouseOption> warehouseConverter() {
        return new StringConverter<>() {
            @Override public String toString(WarehouseOption o) { return o == null ? "" : o.name; }
            @Override public WarehouseOption fromString(String s) { return null; }
        };
    }

    // =======================
    // CRUD: Edit/Delete (context menu)
    // =======================
    private void openEditDialog(StencilViewDto v) {
        Stencil s = stencilService.getStencilByBarcode(v.getBarcode());
        if (s == null) { alert(Alert.AlertType.ERROR, "Không tìm thấy stencil: " + v.getBarcode()); return; }

        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Cập nhật Stencil");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane gp = new GridPane();
        gp.setHgap(10); gp.setVgap(8); gp.setPadding(new Insets(10));

        Label lbBarcode   = new Label(s.getBarcode());
        Label lbStencilNo = new Label(s.getStencilNo());
        TextField tfVersion = new TextField(s.getVersionLabel() == null ? "" : s.getVersionLabel());
        TextField tfSize    = new TextField(s.getSize() == null ? "" : s.getSize());
        Spinner<Integer> spArray = new Spinner<>(1, 1000, s.getArrayCount() > 0 ? s.getArrayCount() : 1);
        ComboBox<String> cbStatus = new ComboBox<>(FXCollections.observableArrayList("NEW","IN_USE","AVAILABLE","LOCKED","DAMAGED"));
        cbStatus.setEditable(true);
        cbStatus.setValue(s.getStatus() == null || s.getStatus().isBlank() ? "NEW" : s.getStatus());
        TextField tfWarehouseId = new TextField(s.getCurrentWarehouseId() == null ? "" : String.valueOf(s.getCurrentWarehouseId()));
        tfWarehouseId.setPromptText("WarehouseId (tuỳ chọn)");
        TextArea taNote = new TextArea(s.getNote() == null ? "" : s.getNote());
        taNote.setPrefRowCount(3);

        int r = 0;
        gp.addRow(r++, new Label("Barcode:"), lbBarcode);
        gp.addRow(r++, new Label("Stencil No:"), lbStencilNo);
        gp.addRow(r++, new Label("Version:"), tfVersion);
        gp.addRow(r++, new Label("Size:"), tfSize);
        gp.addRow(r++, new Label("Array:"), spArray);
        gp.addRow(r++, new Label("Status:"), cbStatus);
        gp.addRow(r++, new Label("WarehouseId:"), tfWarehouseId);
        gp.addRow(r++, new Label("Note:"), taNote);

        dlg.getDialogPane().setContent(gp);

        dlg.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    s.setVersionLabel(tfVersion.getText().trim());
                    s.setSize(tfSize.getText().trim());
                    s.setArrayCount(spArray.getValue());
                    s.setStatus(cbStatus.getValue());
                    s.setNote(taNote.getText());

                    String wh = tfWarehouseId.getText().trim();
                    if (!wh.isEmpty()) {
                        try {
                            Integer toId = Integer.parseInt(wh);
                            if (!toId.equals(s.getCurrentWarehouseId())) {
                                stencilService.transferWarehouse(s.getStencilId(), toId);
                                s.setCurrentWarehouseId(toId);
                            }
                        } catch (NumberFormatException ex) {
                            alert(Alert.AlertType.WARNING, "WarehouseId không hợp lệ. Bỏ qua chuyển kho.");
                        }
                    }

                    stencilService.updateStencil(s);
                    loadFromDatabase();
                } catch (Exception ex) {
                    alert(Alert.AlertType.ERROR, "Cập nhật lỗi: " + ex.getMessage());
                }
            }
        });
    }

    private void deleteOne(StencilViewDto v) {
        Alert cf = new Alert(Alert.AlertType.CONFIRMATION);
        cf.setTitle("Xoá Stencil");
        cf.setHeaderText("Xoá stencil với barcode: " + v.getBarcode());
        cf.setContentText("Thao tác này không thể hoàn tác. Bạn chắc chứ?");
        cf.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        cf.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                Stencil s = stencilService.getStencilByBarcode(v.getBarcode());
                if (s == null) { alert(Alert.AlertType.ERROR, "Không tìm thấy stencil: " + v.getBarcode()); return; }
                stencilService.deleteStencilById(s.getStencilId());
                loadFromDatabase();
            }
        });
    }

    // =======================
    // Helpers
    // =======================
    private String displayWarehouse(Integer id) {
        if (id == null) return "(không xác định)";
        try {
            Warehouse w = warehouseService.getWarehouseById(id);
            return w != null ? w.getName() : ("#" + id);
        } catch (Exception e) {
            return "#" + id;
        }
    }

    private boolean isLatestVersionOfProduct(Stencil s) {
        List<Stencil> sameProduct = stencilService.getStencilsByProductId(s.getProductId());
        if (sameProduct == null || sameProduct.isEmpty()) return true;
        String maxVer = sameProduct.stream()
                .map(Stencil::getVersionLabel)
                .filter(v -> v != null && !v.isBlank())
                .max(VersionUtils::compare)
                .orElse(s.getVersionLabel());
        return VersionUtils.compare(s.getVersionLabel(), maxVer) >= 0;
    }

    private void alert(Alert.AlertType type, String msg) {
        new Alert(type, msg).showAndWait();
    }

    // =======================
    // Import Excel (giữ nguyên logic hiện có)
    // =======================
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
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

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

                    int array;
                    try { array = Integer.parseInt(arrayStr); }
                    catch (NumberFormatException ex) {
                        skipped++; errorMessages.add("Dòng " + (i + 1) + ": ArrayCount không hợp lệ: '" + arrayStr + "'"); continue;
                    }

                    ModelType modelType;
                    try { modelType = ModelType.valueOf(modelTypeStr.toUpperCase()); }
                    catch (IllegalArgumentException ex) {
                        skipped++; errorMessages.add("Dòng " + (i + 1) + ": ModelType không hợp lệ: '" + modelTypeStr + "'"); continue;
                    }

                    LocalDate receivedDate;
                    try { receivedDate = row.getCell(8).getLocalDateTimeCellValue().toLocalDate(); }
                    catch (Exception ex) {
                        skipped++; errorMessages.add("Dòng " + (i + 1) + ": ReceivedDate không hợp lệ."); continue;
                    }

                    Product product = productService.getProductByCodeAndType(productCode, modelType);
                    if (product == null) {
                        skipped++; errorMessages.add("Dòng " + (i + 1) + ": Không tìm thấy Product '" + productCode + "' (" + modelType + ")"); continue;
                    }

                    if (stencilService.existsByBarcode(barcode)) {
                        skipped++; errorMessages.add("Dòng " + (i + 1) + ": Barcode '" + barcode + "' đã tồn tại"); continue;
                    }


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
                    skipped++; errorMessages.add("Dòng " + (i + 1) + ": Lỗi không xác định - " + ex.getMessage());
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Kết quả Import");
            alert.setHeaderText("✅ Thành công: " + imported + " | ❌ Bỏ qua: " + skipped);

            if (!errorMessages.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                errorMessages.stream().limit(20).forEach(msg -> sb.append(msg).append("\n"));
                if (errorMessages.size() > 20) sb.append("...và ").append(errorMessages.size() - 20).append(" dòng khác.");
                alert.setContentText(sb.toString());
            }

            alert.showAndWait();
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, "Lỗi khi mở file: " + e.getMessage());
        }
    }

    private String getCellString(Cell cell) {
        return cell == null ? "" : switch (cell.getCellType()) {
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case STRING  -> cell.getStringCellValue().trim();
            default      -> "";
        };
    }
}
