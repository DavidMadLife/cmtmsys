package org.chemtrovina.cmtmsys.controller;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.chemtrovina.cmtmsys.controller.inventoryTransfer.TransferExcelImporter;
import org.chemtrovina.cmtmsys.controller.inventoryTransfer.TransferScanHandler;
import org.chemtrovina.cmtmsys.controller.inventoryTransfer.TransferTableManager;
import org.chemtrovina.cmtmsys.dto.SAPSummaryDto;
import org.chemtrovina.cmtmsys.dto.TransferredDto;
import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.model.WorkOrder;
import org.chemtrovina.cmtmsys.service.base.*;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class InventoryTransferController {

    /* ============================================================
     * 1. FXML FIELDS
     * ============================================================ */
    @FXML private ComboBox<String> cbSourceWarehouse;
    @FXML private ComboBox<String> cbTargetWarehouse;
    @FXML private ComboBox<String> cbWorkOrder;

    @FXML private TextField txtEmployeeID;
    @FXML private TextField txtBarcode;
    @FXML private TextField txtDeleteBarcode;

    @FXML private Button btnImportFromExcel;
    @FXML private Button btnDeleteFromWO;

    @FXML private TableView<TransferredDto> tblTransferred;
    @FXML private TableColumn<TransferredDto, String> colBarcode;
    @FXML private TableColumn<TransferredDto, String> colSapCode;
    @FXML private TableColumn<TransferredDto, String> colSpec;
    @FXML private TableColumn<TransferredDto, Integer> colQuantity;
    @FXML private TableColumn<TransferredDto, String> colFromWarehouse;
    @FXML private TableColumn<TransferredDto, String> colToWarehouse;
    @FXML private TableColumn<Object, Integer> colNoTransferred;
    @FXML private TextField txtSearchTransferred;

    @FXML private TableView<SAPSummaryDto> tblRequiredSummary;
    @FXML private TableColumn<SAPSummaryDto, String> colSapCodeRequired;
    @FXML private TableColumn<SAPSummaryDto, Integer> colRequired;
    @FXML private TableColumn<SAPSummaryDto, Integer> colScanned;
    @FXML private TableColumn<SAPSummaryDto, String> colStatus;
    @FXML private TableColumn<Object, Integer> colNoRequired;


    /* ============================================================
     * 2. SERVICES & HELPERS
     * ============================================================ */
    private final WarehouseTransferService warehouseTransferService;
    private final WarehouseService warehouseService;
    private final MaterialService materialService;
    private final TransferLogService transferLogService;
    private final WorkOrderService workOrderService;

    private final TransferScanHandler scanHandler;
    private final TransferExcelImporter excelImporter;
    private final TransferTableManager tableManager;


    /* ============================================================
     * 3. CONSTRUCTOR
     * ============================================================ */
    @Autowired
    public InventoryTransferController(
            WarehouseTransferService warehouseTransferService,
            WarehouseService warehouseService,
            MaterialService materialService,
            TransferLogService transferLogService,
            WorkOrderService workOrderService) {

        this.warehouseTransferService = warehouseTransferService;
        this.warehouseService = warehouseService;
        this.materialService = materialService;
        this.transferLogService = transferLogService;
        this.workOrderService = workOrderService;

        this.scanHandler = new TransferScanHandler(
                warehouseTransferService,
                warehouseService,
                materialService,
                transferLogService,
                workOrderService
        );

        this.excelImporter = new TransferExcelImporter(
                scanHandler,
                workOrderService,
                warehouseTransferService
        );

        this.tableManager = new TransferTableManager(scanHandler);
    }


    /* ============================================================
     * 4. INITIALIZE
     * ============================================================ */
    @FXML
    public void initialize() {

        loadWarehouses();
        loadWorkOrders();

        tableManager.setupRequiredSummaryTable(
                tblRequiredSummary,
                colSapCodeRequired,
                colRequired,
                colScanned,
                colStatus,
                colNoRequired
        );

        tableManager.setupTransferredTable(
                tblTransferred,
                colNoTransferred,
                colBarcode,
                colSapCode,
                colSpec,
                colQuantity,
                colFromWarehouse,
                colToWarehouse
        );

        tableManager.setupTransferredSearch(tblTransferred, txtSearchTransferred);

        setupEvents();

        FxClipboardUtils.enableCopyShortcut(tblTransferred);
        FxClipboardUtils.enableCopyShortcut(tblRequiredSummary);
    }


    /* ============================================================
     * 5. UI EVENTS
     * ============================================================ */
    private void setupEvents() {

        // Reload danh sách WO khi click
        cbWorkOrder.setOnMouseClicked(e -> loadWorkOrders());

        // Khi chọn W/O => load summary + transferred list
        ChangeListener<String> woListener = (obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                scanHandler.loadRequiredSummary(
                        newVal,
                        tblRequiredSummary,
                        tblTransferred
                );
            }
        };
        cbWorkOrder.valueProperty().addListener(woListener);

        // EmployeeID nhập xong mới cho chọn kho
        txtEmployeeID.textProperty().addListener((obs, oldText, newText) -> {
            boolean enable = !newText.trim().isEmpty();
            cbSourceWarehouse.setDisable(!enable);
            cbTargetWarehouse.setDisable(!enable);
        });

        // Scan barcode
        txtBarcode.setOnAction(e ->
                scanHandler.handleBarcodeScanned(
                        txtBarcode,
                        cbSourceWarehouse,
                        cbTargetWarehouse,
                        txtEmployeeID,
                        cbWorkOrder,
                        tblTransferred,
                        tblRequiredSummary
                )
        );

        // Delete barcode
        btnDeleteFromWO.setOnAction(e ->
                scanHandler.handleDeleteBarcode(
                        txtDeleteBarcode,
                        cbWorkOrder,
                        tblTransferred,
                        tblRequiredSummary
                )
        );
        txtDeleteBarcode.setOnAction(e ->
                scanHandler.handleDeleteBarcode(
                        txtDeleteBarcode,
                        cbWorkOrder,
                        tblTransferred,
                        tblRequiredSummary
                )
        );

        // Import Excel
        btnImportFromExcel.setOnAction(e ->
                excelImporter.importFromExcel(
                        cbWorkOrder,
                        txtBarcode,
                        txtEmployeeID,
                        cbSourceWarehouse,
                        cbTargetWarehouse,
                        tblTransferred,
                        tblRequiredSummary
                )
        );
    }


    /* ============================================================
     * 6. LOAD DATA (Warehouses, WorkOrders)
     * ============================================================ */
    private void loadWarehouses() {
        List<String> names = warehouseService.getAllWarehouses()
                .stream().map(Warehouse::getName).toList();

        cbSourceWarehouse.setItems(FXCollections.observableArrayList(names));
        cbTargetWarehouse.setItems(FXCollections.observableArrayList(names));
    }

    private void loadWorkOrders() {
        List<String> codes = workOrderService.getAllWorkOrders()
                .stream().map(WorkOrder::getWorkOrderCode).toList();

        cbWorkOrder.setItems(FXCollections.observableArrayList(codes));
    }
}
