package org.chemtrovina.cmtmsys.controller.workorder;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.chemtrovina.cmtmsys.App;
import org.chemtrovina.cmtmsys.controller.WorkOrderCreateController;
import org.chemtrovina.cmtmsys.dto.MaterialRequirementDto;
import org.chemtrovina.cmtmsys.model.RejectedMaterial;
import org.chemtrovina.cmtmsys.model.WorkOrder;
import org.chemtrovina.cmtmsys.service.base.RejectedMaterialService;
import org.chemtrovina.cmtmsys.service.base.WarehouseTransferService;
import org.chemtrovina.cmtmsys.service.base.WorkOrderService;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.chemtrovina.cmtmsys.utils.FxFilterUtils;
import org.chemtrovina.cmtmsys.utils.SpringFXMLLoader;
import org.chemtrovina.cmtmsys.utils.TableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class WorkOrderController1 {

    @FXML private TextField txtWorkOrderCode;
    @FXML private DatePicker dpFrom, dpTo;
    @FXML private Button btnLoadWorkOrders, btnClearFilter;
    @FXML private Button btnAddWorkOrder;
    @FXML private Button btnTransferNG;



    @FXML private TableView<WorkOrder> tblWorkOrders;
    @FXML private TableColumn<WorkOrder, String> colWOCode;
    @FXML private TableColumn<WorkOrder, String> colWODesc;
    @FXML private TableColumn<WorkOrder, String> colWODate, colWOUpdatedDate;
    @FXML private TableColumn<Map<String, Object>, Integer> colNo;



    @FXML private TableColumn<Map<String, Object>, String> colSappn;
    @FXML private TableColumn<Map<String, Object>, Integer> colLineTotal;
    @FXML private TableColumn<Map<String,Object>, Integer> colScanned;
    @FXML private TableColumn<Map<String,Object>, Integer> colRemain,colActual, collNote, colMissing;
    @FXML private Button btnChooseImportFile, btnImportWorkOrder;
    @FXML private TextField txtImportFileName;
    private File importFile;


    @FXML private TableView<Map<String, Object>> tblMaterialByProduct;


    private final WorkOrderService workOrderService;
    private final WarehouseTransferService warehouseTransferService;
    private final RejectedMaterialService rejectedMaterialService;

    @Autowired
    public WorkOrderController1(WorkOrderService workOrderService,
                                WarehouseTransferService warehouseTransferService,
                                RejectedMaterialService rejectedMaterialService) {
        this.workOrderService = workOrderService;
        this.warehouseTransferService = warehouseTransferService;
        this.rejectedMaterialService = rejectedMaterialService;
    }



    private ObservableList<Map<String, Object>> masterMaterialData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupWorkOrderTable();
        setupEventHandlers();

        FxClipboardUtils.enableCopyShortcut(tblWorkOrders);
        FxClipboardUtils.enableCopyShortcut(tblMaterialByProduct);
    }

    private void setupWorkOrderTable() {
        TableUtils.centerAlignAllColumns(tblMaterialByProduct);
        TableUtils.centerAlignAllColumns(tblWorkOrders);


        colWOCode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWorkOrderCode()));
        colWODesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));
        colWODate.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCreatedDate().toLocalDate().toString()
        ));
        colWOUpdatedDate.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getUpdatedDate().toLocalDate().toString()
        ));

        tblWorkOrders.setRowFactory(tv -> {
            TableRow<WorkOrder> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    WorkOrder selectedWorkOrder = row.getItem();
                    loadMaterialTable(selectedWorkOrder.getWorkOrderCode());
                }
            });
            return row;
        });

        tblWorkOrders.setRowFactory(tv -> {
            TableRow<WorkOrder> row = new TableRow<>();

            ContextMenu contextMenu = new ContextMenu();

            MenuItem updateItem = new MenuItem("Cập nhật");
            updateItem.setOnAction(e -> {
                WorkOrder selected = row.getItem();
                if (selected != null) {
                    openUpdateWorkOrderDialog(selected);
                }
            });

            MenuItem deleteItem = new MenuItem("Xóa");
            deleteItem.setOnAction(e -> {
                WorkOrder selected = row.getItem();
                if (selected != null) {
                    handleDeleteWorkOrder(selected);
                }
            });

            contextMenu.getItems().addAll(updateItem, deleteItem);

            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );

            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    WorkOrder selectedWorkOrder = row.getItem();
                    loadMaterialTable(selectedWorkOrder.getWorkOrderCode());
                }
            });

            return row;
        });


    }


    private void setupEventHandlers() {
        btnLoadWorkOrders.setOnAction(e -> handleLoadWorkOrders());
        btnClearFilter.setOnAction(e -> handleClearFilters());
        btnAddWorkOrder.setOnAction(e -> openCreateWorkOrderDialog());
        btnTransferNG.setOnAction(e -> handleTransferNG());
        btnChooseImportFile.setOnAction(e -> chooseExcelFile());
        btnImportWorkOrder.setOnAction(e -> importWorkOrderFromExcel());
    }


    private void chooseExcelFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            importFile = file;
            txtImportFileName.setText(file.getName());
        }
    }


    @FXML
    private void importWorkOrderFromExcel() {
        if (importFile == null) {
            showAlert("Vui lòng chọn file Excel.");
            return;
        }
        try {
            workOrderService.importFromExcel(importFile);
            showAlert("✅ Import thành công!");
            handleLoadWorkOrders();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("❌ Lỗi khi import: " + e.getMessage());
        }
    }


    private void openCreateWorkOrderDialog() {
        try {
            FXMLLoader loader = SpringFXMLLoader.load(App.class.getResource("view/work_order_create.fxml"));
            Parent root = loader.load();


            Stage stage = new Stage();
            stage.setTitle("Tạo Work Order mới");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Sau khi tạo xong thì reload list
            handleLoadWorkOrders();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Không thể mở form tạo Work Order.");
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.showAndWait();
    }



    private void handleLoadWorkOrders() {
        String codeLike = txtWorkOrderCode.getText().trim();
        LocalDate from = dpFrom.getValue();
        LocalDate to = dpTo.getValue();

        List<WorkOrder> results = workOrderService.getAllWorkOrders();

        if (!codeLike.isEmpty()) {
            results = results.stream()
                    .filter(wo -> wo.getWorkOrderCode().toLowerCase().contains(codeLike.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (from != null && to != null) {
            results = results.stream()
                    .filter(wo -> {
                        LocalDate created = wo.getCreatedDate().toLocalDate();
                        return (created.isEqual(from) || created.isAfter(from)) &&
                                (created.isEqual(to) || created.isBefore(to));
                    })
                    .collect(Collectors.toList());
        }

        tblWorkOrders.setItems(FXCollections.observableArrayList(results));

        FxFilterUtils.setupFilterMenu(colWOCode, results, WorkOrder::getWorkOrderCode, this::applyGeneralFilter);
        FxFilterUtils.setupFilterMenu(colWODesc, results, WorkOrder::getDescription, this::applyGeneralFilter);

    }

    private void handleTransferNG() {
        WorkOrder selectedWO = tblWorkOrders.getSelectionModel().getSelectedItem();
        if (selectedWO == null) {
            showAlert("Vui lòng chọn Work Order.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Lý do chuyển hàng NG");
        dialog.setHeaderText("Nhập lý do chuyển hàng NG:");
        dialog.setContentText("Lý do:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().trim().isEmpty()) {
            showAlert("⚠️ Bạn phải nhập lý do để chuyển hàng NG.");
            return;
        }

        String reason = result.get().trim();
        int workOrderId = selectedWO.getWorkOrderId();
        int warehouseIdNG = 17; // ⚠️ ID kho NG thực tế

        int inserted = 0;
        for (Map<String, Object> row : tblMaterialByProduct.getItems()) {
            String sapCode = String.valueOf(row.get("sappn"));
            int missingQty = (int) row.getOrDefault("MISSING", 0);
            if (missingQty <= 0) continue;

            RejectedMaterial rm = new RejectedMaterial();
            rm.setWorkOrderId(workOrderId);
            rm.setWarehouseId(warehouseIdNG);
            rm.setSapCode(sapCode);
            rm.setQuantity(missingQty);
            rm.setCreatedDate(java.time.LocalDateTime.now());
            rm.setNote(reason);

            rejectedMaterialService.addOrUpdateRejectedMaterial(rm);
            inserted++;
        }

        showAlert("✅ Đã chuyển " + inserted + " dòng hàng NG vào kho.");
    }


    private void handleClearFilters() {
        txtWorkOrderCode.clear();
        dpFrom.setValue(null);
        dpTo.setValue(null);
        tblWorkOrders.setItems(FXCollections.emptyObservableList());
        tblMaterialByProduct.setItems(FXCollections.emptyObservableList());
        tblMaterialByProduct.getColumns().clear();
    }


    private void loadMaterialTable(String workOrderCode) {
        int woId = workOrderService.getWorkOrderIdByCode(workOrderCode);
        List<MaterialRequirementDto> data = workOrderService.getMaterialRequirements(workOrderCode);
        Map<String, Integer> scannedMap = warehouseTransferService.getScannedQuantitiesByWO(woId);
        Map<String, Integer> actualMap = warehouseTransferService.getActualReturnedByWorkOrderId(woId);

        Map<String, Map<String, Integer>> pivotData = new LinkedHashMap<>();
        Map<String, Integer> productQuantities = new LinkedHashMap<>();

        for (MaterialRequirementDto dto : data) {
            pivotData
                    .computeIfAbsent(dto.getSappn(), k -> new HashMap<>())
                    .merge(dto.getProductCode(), dto.getRequiredQty(), Integer::sum);
            productQuantities.put(dto.getProductCode(), dto.getProductQty());
        }

        Set<String> productCodes = productQuantities.keySet();

        // ❗ Xóa các cột động cũ (không có fx:id)
        tblMaterialByProduct.getColumns().removeIf(col -> col.getId() == null);

        // ✅ Gán lại CellValueFactory cho cột có sẵn
        colSappn.setCellValueFactory(dataMap -> new SimpleStringProperty((String) dataMap.getValue().get("sappn")));
        colLineTotal.setCellValueFactory(dataMap -> new SimpleIntegerProperty((Integer) dataMap.getValue().get("LINE")).asObject());

        // ✅ Tìm vị trí sau colLineTotal để chèn cột động
        int insertIndex = tblMaterialByProduct.getColumns().indexOf(colSappn) + 1;

        for (String productCode : productCodes) {
            int qty = productQuantities.get(productCode);

            Text textCode = new Text(productCode + " ");
            Text textQty = new Text("(" + qty + ")");
            textQty.setStyle("-fx-fill: red;");
            TextFlow headerFlow = new TextFlow(textCode, textQty);

            TableColumn<Map<String, Object>, Integer> col = new TableColumn<>();
            col.setGraphic(headerFlow);
            col.setCellValueFactory(dataMap -> {
                Object value = dataMap.getValue().getOrDefault(productCode, 0);
                return new SimpleIntegerProperty((Integer) value).asObject();
            });
            col.setMinWidth(80);

            tblMaterialByProduct.getColumns().add(insertIndex++, col);
        }

        // ✅ Tạo danh sách dữ liệu
        List<Map<String, Object>> tableRows = new ArrayList<>();
        for (String sappn : pivotData.keySet()) {
            Map<String, Object> row = new HashMap<>();
            row.put("sappn", sappn);

            int total = 0;
            for (String product : productCodes) {
                int val = pivotData.get(sappn).getOrDefault(product, 0);
                row.put(product, val);
                total += val;
            }

            row.put("LINE", total);

            int scannedQty = scannedMap.getOrDefault(sappn, 0);
            int remainQty = Math.max(0, scannedQty - total);

            row.put("SCANNED", scannedQty);
            row.put("REMAIN", remainQty);

            if (actualMap.containsKey(sappn)) {
                int actualQty = actualMap.get(sappn);
                row.put("ACTUAL", actualQty);

                int missingQty = remainQty - actualQty;
                row.put("MISSING", Math.max(0, missingQty));
            }



            tableRows.add(row);
        }

        // ✅ Gán CellValueFactory cho các cột còn lại
        colNo.setCellValueFactory(cell ->
                new SimpleIntegerProperty(tblMaterialByProduct.getItems().indexOf(cell.getValue()) + 1).asObject()
        );
        colScanned.setCellValueFactory(dataMap -> new SimpleIntegerProperty((Integer) dataMap.getValue().get("SCANNED")).asObject());
        colRemain.setCellValueFactory(dataMap -> new SimpleIntegerProperty((Integer) dataMap.getValue().get("REMAIN")).asObject());
        colActual.setCellValueFactory(dataMap ->
                new SimpleIntegerProperty((Integer) dataMap.getValue().getOrDefault("ACTUAL", 0)).asObject());

        colMissing.setCellValueFactory(dataMap ->
                new SimpleIntegerProperty((Integer) dataMap.getValue().getOrDefault("MISSING", 0)).asObject());


        tblMaterialByProduct.setItems(FXCollections.observableArrayList(tableRows));

        // Cập nhật resize policy
        Platform.runLater(this::enableSmartResizePolicy);

        masterMaterialData.setAll(tableRows);
        tblMaterialByProduct.setItems(FXCollections.observableArrayList(masterMaterialData));


        FxFilterUtils.setupFilterMenu(
                colSappn,
                new ArrayList<>(masterMaterialData),
                row -> String.valueOf(row.get("sappn")),
                this::applySappnFilter
        );

    }


    private void openUpdateWorkOrderDialog(WorkOrder workOrder) {
        try {
            FXMLLoader loader = SpringFXMLLoader.load(App.class.getResource("view/work_order_create.fxml"));
            Parent root = loader.load();

            WorkOrderCreateController controller = loader.getController();
            controller.loadWorkOrder(workOrder);  // gọi hàm để load data cần cập nhật

            Stage stage = new Stage();
            stage.setTitle("Cập nhật Work Order");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            handleLoadWorkOrders(); // reload lại list
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Không thể mở form cập nhật Work Order.");
        }
    }
    private void handleDeleteWorkOrder(WorkOrder workOrder) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Xóa Work Order này?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                workOrderService.deleteWorkOrder(workOrder.getWorkOrderId());
                handleLoadWorkOrders();
            }
        });
    }

    private void applyGeneralFilter(List<String> selectedValues) {
        List<WorkOrder> all = workOrderService.getAllWorkOrders();
        List<WorkOrder> filtered = all.stream()
                .filter(wo ->
                        selectedValues.contains(wo.getWorkOrderCode()) ||
                                selectedValues.contains(wo.getDescription()) ||
                                selectedValues.contains(wo.getDescription()) ||
                                selectedValues.contains(wo.getCreatedDate().toLocalDate().toString()) ||
                                selectedValues.contains(wo.getUpdatedDate().toLocalDate().toString())
                )
                .collect(Collectors.toList());

        tblWorkOrders.setItems(FXCollections.observableArrayList(filtered));
    }
    private void enableSmartResizePolicy() {
        double totalWidth = tblMaterialByProduct.getColumns().stream()
                .mapToDouble(TableColumn::getWidth)
                .sum();

        double tableWidth = tblMaterialByProduct.getWidth();

        if (totalWidth > tableWidth) {
            tblMaterialByProduct.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        } else {
            tblMaterialByProduct.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }

        // Theo dõi nếu user kéo thêm:
        for (TableColumn<?, ?> col : tblMaterialByProduct.getColumns()) {
            col.widthProperty().addListener((obs, oldVal, newVal) -> {
                double newTotalWidth = tblMaterialByProduct.getColumns().stream()
                        .mapToDouble(TableColumn::getWidth)
                        .sum();

                if (newTotalWidth > tblMaterialByProduct.getWidth()) {
                    tblMaterialByProduct.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
                }
            });
        }
    }
    private void applySappnFilter(List<String> selectedSappns) {
        List<Map<String, Object>> filtered = masterMaterialData.stream()
                .filter(row -> selectedSappns.contains(String.valueOf(row.get("sappn"))))
                .collect(Collectors.toList());

        tblMaterialByProduct.setItems(FXCollections.observableArrayList(filtered));
    }


}
