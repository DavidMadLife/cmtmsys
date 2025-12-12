package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.chemtrovina.cmtmsys.controller.workorder.WorkOrderDialogManager;
import org.chemtrovina.cmtmsys.controller.workorder.WorkOrderExcelImporter;
import org.chemtrovina.cmtmsys.controller.workorder.WorkOrderMaterialTableManager;
import org.chemtrovina.cmtmsys.controller.workorder.WorkOrderNgHandler;
import org.chemtrovina.cmtmsys.model.WorkOrder;
import org.chemtrovina.cmtmsys.service.base.RejectedMaterialService;
import org.chemtrovina.cmtmsys.service.base.WarehouseTransferService;
import org.chemtrovina.cmtmsys.service.base.WorkOrderService;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class WorkOrderController {

    // ========= UI =========
    @FXML private TextField txtWorkOrderCode;
    @FXML private DatePicker dpFrom, dpTo;
    @FXML private Button btnLoadWorkOrders, btnClearFilter, btnAddWorkOrder, btnTransferNG;
    @FXML private Button btnChooseImportFile, btnImportWorkOrder;

    @FXML private TextField txtImportFileName;
    @FXML private TableView<WorkOrder> tblWorkOrders;
    @FXML private TableColumn<WorkOrder, String> colWOCode;
    @FXML private TableColumn<WorkOrder, String> colWODesc;
    @FXML private TableColumn<WorkOrder, String> colWODate, colWOUpdatedDate;

    // Pivot Table
    @FXML
    private TableView<Map<String, Object>> tblMaterialByProduct;
    @FXML private TableColumn<Map<String, Object>, String> colSappn;
    @FXML private TableColumn<Map<String, Object>, Integer> colLineTotal;
    @FXML private TableColumn<Map<String, Object>, Integer> colScanned, colRemain, colActual, colMissing;
    @FXML private TableColumn<Map<String, Object>, Integer> colNo;

    // ========= Services =========
    private final WorkOrderService workOrderService;
    private final WarehouseTransferService warehouseTransferService;
    private final RejectedMaterialService rejectedMaterialService;

    // ========= Helpers (tách lớp riêng) =========
    private final WorkOrderMaterialTableManager materialTableManager;
    private final WorkOrderDialogManager dialogManager;
    private final WorkOrderExcelImporter excelImporter;
    private final WorkOrderNgHandler ngHandler;

    private File importFile;


    @Autowired
    public WorkOrderController(
            WorkOrderService workOrderService,
            WarehouseTransferService warehouseTransferService,
            RejectedMaterialService rejectedMaterialService
    ) {
        this.workOrderService = workOrderService;
        this.warehouseTransferService = warehouseTransferService;
        this.rejectedMaterialService = rejectedMaterialService;

        // Inject helpers
        this.materialTableManager = new WorkOrderMaterialTableManager();
        this.dialogManager = new WorkOrderDialogManager();
        this.excelImporter = new WorkOrderExcelImporter(workOrderService);
        this.ngHandler = new WorkOrderNgHandler(rejectedMaterialService);
    }


    @FXML
    public void initialize() {
        setupWorkOrderTable();
        setupEventHandlers();
        FxClipboardUtils.enableCopyShortcut(tblWorkOrders);
        FxClipboardUtils.enableCopyShortcut(tblMaterialByProduct);
    }

    // ========= TABLE SETUP =========
    private void setupWorkOrderTable() {

        colWOCode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWorkOrderCode()));
        colWODesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));
        colWODate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCreatedDate().toLocalDate().toString()));
        colWOUpdatedDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUpdatedDate().toLocalDate().toString()));

        // Double-click mở material table
        tblWorkOrders.setRowFactory(tv -> {
            TableRow<WorkOrder> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    WorkOrder wo = row.getItem();
                    loadMaterialTable(wo.getWorkOrderCode());
                }
            });

            // Context menu
            ContextMenu menu = new ContextMenu();

            MenuItem update = new MenuItem("Cập nhật");
            update.setOnAction(e -> dialogManager.openUpdateDialog(row.getItem(), this::reloadList));

            MenuItem delete = new MenuItem("Xóa");
            delete.setOnAction(e -> dialogManager.handleDelete(row.getItem(), workOrderService, this::reloadList));

            menu.getItems().addAll(update, delete);

            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(menu)
            );
            return row;
        });
    }

    // ========= EVENT HANDLERS =========
    private void setupEventHandlers() {

        btnLoadWorkOrders.setOnAction(e -> reloadList());
        btnClearFilter.setOnAction(e -> clearFilters());
        btnAddWorkOrder.setOnAction(e -> dialogManager.openCreateDialog(this::reloadList));

        btnTransferNG.setOnAction(e -> {
            WorkOrder wo = tblWorkOrders.getSelectionModel().getSelectedItem();
            if (wo == null) {
                showAlert("⚠️ Vui lòng chọn Work Order.");
                return;
            }
            ngHandler.transferNG(wo, tblMaterialByProduct.getItems());
        });

        btnChooseImportFile.setOnAction(e -> chooseExcelFile());

        btnImportWorkOrder.setOnAction(e -> {
            if (importFile == null) {
                showAlert("⚠️ Vui lòng chọn file Excel.");
                return;
            }
            excelImporter.importFile(importFile, () -> showAlert("Import OK!"), this::reloadList);
        });
    }


    // ========= LOAD WORK ORDER LIST =========
    private void reloadList() {
        String codeFilter = txtWorkOrderCode.getText().trim();
        LocalDate from = dpFrom.getValue();
        LocalDate to = dpTo.getValue();

        List<WorkOrder> list = workOrderService.getAllWorkOrders();

        if (!codeFilter.isEmpty()) {
            list = list.stream()
                    .filter(wo -> wo.getWorkOrderCode().toLowerCase().contains(codeFilter.toLowerCase()))
                    .toList();
        }

        if (from != null && to != null) {
            list = list.stream()
                    .filter(wo -> {
                        LocalDate d = wo.getCreatedDate().toLocalDate();
                        return !d.isBefore(from) && !d.isAfter(to);
                    })
                    .toList();
        }

        tblWorkOrders.setItems(FXCollections.observableArrayList(list));
    }


    // ========= MATERIAL PIVOT TABLE =========
    private void loadMaterialTable(String workOrderCode) {

        materialTableManager.loadTable(
                workOrderCode,
                tblMaterialByProduct,
                colSappn, colLineTotal, colScanned, colRemain, colActual, colMissing, colNo,
                workOrderService,
                warehouseTransferService
        );
    }

    private void chooseExcelFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Chọn file Excel");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));

        File file = fc.showOpenDialog(null);
        if (file != null) {
            importFile = file;
            txtImportFileName.setText(file.getName());
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.showAndWait();
    }


    private void clearFilters() {
        txtWorkOrderCode.clear();
        dpFrom.setValue(null);
        dpTo.setValue(null);

        tblWorkOrders.setItems(FXCollections.emptyObservableList());
        tblMaterialByProduct.setItems(FXCollections.emptyObservableList());
        tblMaterialByProduct.getColumns().clear();
    }
}

