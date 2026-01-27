package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.chemtrovina.cmtmsys.dto.MaterialDto;
import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.model.enums.UserRole;
import org.chemtrovina.cmtmsys.security.RequiresRoles;
import org.chemtrovina.cmtmsys.service.base.MaterialService;
import org.chemtrovina.cmtmsys.service.base.TransferLogService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.chemtrovina.cmtmsys.utils.FxAlertUtils;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiresRoles({
        UserRole.ADMIN,
        UserRole.INVENTORY,
        UserRole.SUBLEEDER
})

@Component
public class InventoryCheckController {

    @FXML private TableView<MaterialDto> tblMaterials;
    @FXML private TableColumn<MaterialDto, Integer> colNo;
    @FXML private TableColumn<MaterialDto, String> colSapCode;
    @FXML private TableColumn<MaterialDto, String> colSpec;
    @FXML private TableColumn<MaterialDto, String> colLot;
    @FXML private TableColumn<MaterialDto, String> colRollCode;
    @FXML private TableColumn<MaterialDto, Integer> colQuantity;
    @FXML private TableColumn<MaterialDto, String> colWarehouse;
    @FXML private TableColumn<MaterialDto, LocalDateTime> colCreatedAt;
    @FXML private TableColumn<MaterialDto, String> colEmployeeId;
    @FXML private TableColumn<MaterialDto, String> colMaker;

    @FXML private Button btnChooseFile;
    @FXML private Button btnImportData;
    @FXML private Text txtFileName;
    @FXML private TextField txtFilterSapCode;
    @FXML private TextField txtFilterBarcode;
    @FXML private DatePicker dpFromDate;
    @FXML private DatePicker dpToDate;
    @FXML private ComboBox<Warehouse> cbWarehouses;
    @FXML private Button btnSearch;
    @FXML private Button btnClear;

    private File selectedFile;

    private final MaterialService materialService;
    private final WarehouseService warehouseService;
    private final TransferLogService transferLogService;

    @Autowired
    public InventoryCheckController(MaterialService materialService,
                                    WarehouseService warehouseService,
                                    TransferLogService transferLogService) {
        this.materialService = materialService;
        this.warehouseService = warehouseService;
        this.transferLogService = transferLogService;
    }

    @FXML
    public void initialize() {
        setupTable();
        setupFileImport();
        setupSearch();

        btnClear.setOnAction(e -> clearFilters());

        FxClipboardUtils.enableCopyShortcut(tblMaterials);
    }

    private void setupTable() {
        colNo.setCellValueFactory(
                cell -> new SimpleIntegerProperty(tblMaterials.getItems().indexOf(cell.getValue()) + 1).asObject()
        );

        colSapCode.setCellValueFactory(new PropertyValueFactory<>("sapCode"));
        colRollCode.setCellValueFactory(new PropertyValueFactory<>("rollCode"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colWarehouse.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        colSpec.setCellValueFactory(new PropertyValueFactory<>("spec"));
        colLot.setCellValueFactory(new PropertyValueFactory<>("lot"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colMaker.setCellValueFactory(new PropertyValueFactory<>("maker"));

        tblMaterials.setRowFactory(tv -> {
            TableRow<MaterialDto> row = new TableRow<>();

            ContextMenu menu = new ContextMenu();

            MenuItem update = new MenuItem("Cập nhật");
            update.setOnAction(e -> {
                MaterialDto dto = row.getItem();
                if (dto != null) showUpdateDialog(dto);
            });

            MenuItem delete = new MenuItem("Xóa");
            delete.setOnAction(e -> {
                MaterialDto dto = row.getItem();
                if (dto != null) deleteMaterial(dto);
            });

            menu.getItems().addAll(update, delete);

            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(menu)
            );

            return row;
        });
    }

    private void setupFileImport() {
        btnChooseFile.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Chọn file Excel");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

            selectedFile = fc.showOpenDialog(btnChooseFile.getScene().getWindow());
            txtFileName.setText(selectedFile != null ? selectedFile.getName() : "No file selected");
        });

        btnImportData.setOnAction(e -> {
            if (selectedFile == null) {
                FxAlertUtils.warning("Vui lòng chọn file Excel trước khi import.");
                return;
            }

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Nhập mã nhân viên");

            Optional<String> input = dialog.showAndWait();

            if (input.isEmpty() || input.get().trim().isEmpty()) {
                FxAlertUtils.warning("Mã nhân viên không được để trống.");
                return;
            }

            importFromExcel(selectedFile, input.get().trim());
            txtFileName.setText("No file selected");
            selectedFile = null;
        });
    }

    private void importFromExcel(File file, String employeeId) {
        try {
            materialService.importMaterialsFromExcel(file, employeeId);
            FxAlertUtils.info("Import dữ liệu thành công.");
            loadData();
        } catch (Exception ex) {
            ex.printStackTrace();
            FxAlertUtils.error("Không thể import dữ liệu: " + ex.getMessage());
        }
    }

    private void loadData() {
        List<MaterialDto> dtos = materialService.getAllMaterialDtos();
        tblMaterials.setItems(FXCollections.observableArrayList(dtos ));
    }

    private void deleteMaterial(MaterialDto dto) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc muốn xóa?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText("Xóa vật liệu");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    materialService.deleteMaterialById(dto.getMaterialId());
                    tblMaterials.getItems().remove(dto);
                    FxAlertUtils.info("Xóa thành công.");
                } catch (Exception ex) {
                    FxAlertUtils.error("Không thể xóa: " + ex.getMessage());
                }
            }
        });
    }

    private void showUpdateDialog(MaterialDto dto) {
        Dialog<MaterialDto> dialog = new Dialog<>();
        dialog.setTitle("Cập nhật vật liệu");
        dialog.setHeaderText("Chỉnh sửa thông tin");

        Label lblQty = new Label("Số lượng:");
        TextField txtQty = new TextField(String.valueOf(dto.getQuantity()));

        Label lblLot = new Label("Mã Lot:");
        TextField txtLot = new TextField(dto.getLot() != null ? dto.getLot() : "");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(lblQty, 0, 0);
        grid.add(txtQty, 1, 0);
        grid.add(lblLot, 0, 1);
        grid.add(txtLot, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    int q = Integer.parseInt(txtQty.getText().trim());
                    dto.setQuantity(q);
                    dto.setLot(txtLot.getText().trim().isEmpty() ? null : txtLot.getText().trim());
                    return dto;
                } catch (Exception ex) {
                    FxAlertUtils.warning("Số lượng phải là số hợp lệ.");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(res -> {
            try {
                materialService.updateMaterialDto(res);
                tblMaterials.refresh();
                FxAlertUtils.info("Đã cập nhật vật liệu thành công.");
            } catch (Exception ex) {
                FxAlertUtils.error("Không thể cập nhật vật liệu: " + ex.getMessage());
            }
        });
    }

    private void setupSearch() {
        cbWarehouses.setItems(FXCollections.observableArrayList(warehouseService.getAllWarehouses()));

        cbWarehouses.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        cbWarehouses.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        txtFilterSapCode.setOnKeyPressed(e -> { if (e.getCode().isLetterKey()) onSearch(); });
        txtFilterBarcode.setOnKeyPressed(e -> { if (e.getCode().isLetterKey()) onSearch(); });

        btnSearch.setOnAction(e -> onSearch());
    }

    private void onSearch() {
        String sap = txtFilterSapCode.getText().trim();
        String barcode = txtFilterBarcode.getText().trim();
        LocalDate from = dpFromDate.getValue();
        LocalDate to = dpToDate.getValue();
        Warehouse wh = cbWarehouses.getValue();

        List<MaterialDto> list = materialService.searchMaterials(
                sap,
                barcode,
                from != null ? from.atStartOfDay() : null,
                to != null ? to.atTime(23, 59, 59) : null,
                wh != null ? wh.getWarehouseId() : null
        );

        tblMaterials.setItems(FXCollections.observableArrayList(list));
    }

    private void clearFilters() {
        txtFilterSapCode.clear();
        txtFilterBarcode.clear();
        cbWarehouses.getSelectionModel().clearSelection();
        dpFromDate.setValue(null);
        dpToDate.setValue(null);
        tblMaterials.setItems(FXCollections.emptyObservableList());
    }
}
