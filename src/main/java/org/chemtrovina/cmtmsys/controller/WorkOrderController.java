package org.chemtrovina.cmtmsys.controller;

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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.dto.MaterialRequirementDto;
import org.chemtrovina.cmtmsys.model.WorkOrder;
import org.chemtrovina.cmtmsys.repository.Impl.WarehouseTransferDetailRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.Impl.WarehouseTransferRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.Impl.WorkOrderItemRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.Impl.WorkOrderRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.base.WarehouseTransferDetailRepository;
import org.chemtrovina.cmtmsys.repository.base.WarehouseTransferRepository;
import org.chemtrovina.cmtmsys.repository.base.WorkOrderItemRepository;
import org.chemtrovina.cmtmsys.service.Impl.WarehouseServiceImpl;
import org.chemtrovina.cmtmsys.service.Impl.WarehouseTransferServiceImpl;
import org.chemtrovina.cmtmsys.service.Impl.WorkOrderServiceImpl;
import org.chemtrovina.cmtmsys.service.base.WarehouseTransferService;
import org.chemtrovina.cmtmsys.service.base.WorkOrderService;
import org.chemtrovina.cmtmsys.utils.FxFilterUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class WorkOrderController {

    @FXML private TextField txtWorkOrderCode;
    @FXML private DatePicker dpFrom, dpTo;
    @FXML private Button btnLoadWorkOrders, btnClearFilter;
    @FXML private Button btnAddWorkOrder;


    @FXML private TableView<WorkOrder> tblWorkOrders;
    @FXML private TableColumn<WorkOrder, String> colWOCode;
    @FXML private TableColumn<WorkOrder, String> colWODesc;
    @FXML private TableColumn<WorkOrder, String> colWODate, colWOUpdatedDate;
    @FXML private TableColumn<Map<String, Object>, Integer> colNo;



    @FXML private TableColumn<Map<String, Object>, String> colSappn;
    @FXML private TableColumn<Map<String, Object>, Integer> colLineTotal;
    @FXML private TableColumn<Map<String,Object>, Integer> colScanned;
    @FXML private TableColumn<Map<String,Object>, Integer> colRemain,colActual, colFall, colMissing;
    @FXML private Button btnChooseImportFile, btnImportWorkOrder;
    @FXML private TextField txtImportFileName;
    private File importFile;


    @FXML private TableView<Map<String, Object>> tblMaterialByProduct;

    private WorkOrderService workOrderService;
    private WarehouseTransferService warehouseTransferService;


    private ObservableList<Map<String, Object>> masterMaterialData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupServices();
        setupWorkOrderTable();
        setupEventHandlers();
        tblMaterialByProduct.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblWorkOrders.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tblWorkOrders.getSelectionModel().setCellSelectionEnabled(true);
        tblWorkOrders.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tblMaterialByProduct.getSelectionModel().setCellSelectionEnabled(true);
        tblMaterialByProduct.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        btnChooseImportFile.setOnAction(e -> chooseExcelFile());
        btnImportWorkOrder.setOnAction(e -> importWorkOrderFromExcel());


    }

    private void setupServices() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getDataSource());

        WorkOrderItemRepository workOrderItemRepository = new WorkOrderItemRepositoryImpl(jdbcTemplate);


        WarehouseTransferRepository warehouseTransferRepository = new WarehouseTransferRepositoryImpl(jdbcTemplate);
        WarehouseTransferDetailRepository warehouseTransferDetailRepository = new WarehouseTransferDetailRepositoryImpl(jdbcTemplate);
        this.warehouseTransferService = new WarehouseTransferServiceImpl(warehouseTransferRepository, warehouseTransferDetailRepository);
        this.workOrderService = new WorkOrderServiceImpl(new WorkOrderRepositoryImpl(jdbcTemplate), jdbcTemplate, workOrderItemRepository, warehouseTransferRepository, warehouseTransferDetailRepository);
    }

    private void setupWorkOrderTable() {
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

        tblWorkOrders.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode().toString().equals("C")) {
                copySelectionToClipboard(tblWorkOrders);
            }
        });

        tblMaterialByProduct.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode().toString().equals("C")) {
                copySelectionToClipboard(tblMaterialByProduct);
            }
        });

        btnAddWorkOrder.setOnAction(e -> openCreateWorkOrderDialog());

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


    private void importWorkOrderFromExcel() {
        if (importFile == null) {
            showAlert("Vui lòng chọn file Excel.");
            return;
        }

        try (FileInputStream fis = new FileInputStream(importFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getDataSource());

            // 1. Tạo WorkOrder mới
            String woCode = workOrderService.generateNewWorkOrderCode(LocalDate.now());
            // bạn có thể tự định dạng
            jdbcTemplate.update(
                    "INSERT INTO WorkOrders (workOrderCode, description, createdDate, updatedDate) VALUES (?, ?, GETDATE(), GETDATE())",
                    woCode, "Tạo từ import Excel"
            );

            // 2. Lấy workOrderId vừa tạo
            int workOrderId = jdbcTemplate.queryForObject(
                    "SELECT workOrderId FROM WorkOrders WHERE workOrderCode = ?",
                    new Object[]{woCode}, Integer.class
            );

            int success = 0;
            int failed = 0;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Bỏ header

                String productCode = getCellString(row.getCell(0)).trim();
                double quantity = row.getCell(1).getNumericCellValue();

                List<Integer> productIds = jdbcTemplate.query(
                        "SELECT productId FROM Products WHERE productCode = ?",
                        new Object[]{productCode},
                        (rs, rowNum) -> rs.getInt("productId")
                );

                if (productIds.isEmpty()) {
                    System.out.println("⚠ Không tìm thấy Product: " + productCode);
                    failed++;
                    continue;
                }

                jdbcTemplate.update(
                        "INSERT INTO WorkOrderItems (workOrderId, productId, quantity, createdDate, updatedDate) VALUES (?, ?, ?, GETDATE(), GETDATE())",
                        workOrderId, productIds.get(0), (int) quantity
                );

                success++;
            }

            showAlert("✅ Đã tạo Work Order: " + woCode + "\nSản phẩm thành công: " + success + "\nBỏ qua: " + failed);
            handleLoadWorkOrders(); // reload list

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("❌ Lỗi khi import: " + e.getMessage());
        }
    }


    private void openCreateWorkOrderDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chemtrovina/cmtmsys/view/work_order_create.fxml"));
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

            // optional: nếu có ACTUAL, FALL, MISSING bạn xử lý thêm
            row.putIfAbsent("ACTUAL", 0);
            row.putIfAbsent("FALL", 0);
            row.putIfAbsent("MISSING", 0);

            tableRows.add(row);
        }

        // ✅ Gán CellValueFactory cho các cột còn lại
        colNo.setCellValueFactory(cell ->
                new SimpleIntegerProperty(tblMaterialByProduct.getItems().indexOf(cell.getValue()) + 1).asObject()
        );
        colScanned.setCellValueFactory(dataMap -> new SimpleIntegerProperty((Integer) dataMap.getValue().get("SCANNED")).asObject());
        colRemain.setCellValueFactory(dataMap -> new SimpleIntegerProperty((Integer) dataMap.getValue().get("REMAIN")).asObject());
        colActual.setCellValueFactory(dataMap -> new SimpleIntegerProperty((Integer) dataMap.getValue().get("ACTUAL")).asObject());
        colFall.setCellValueFactory(dataMap -> new SimpleIntegerProperty((Integer) dataMap.getValue().get("FALL")).asObject());
        colMissing.setCellValueFactory(dataMap -> new SimpleIntegerProperty((Integer) dataMap.getValue().get("MISSING")).asObject());

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

    private void copySelectionToClipboard(TableView<?> table) {
        StringBuilder clipboardString = new StringBuilder();
        ObservableList<TablePosition> positionList = table.getSelectionModel().getSelectedCells();

        int prevRow = -1;
        for (TablePosition position : positionList) {
            int row = position.getRow();
            int col = position.getColumn();

            Object cell = table.getColumns().get(col).getCellData(row);
            if (cell == null) cell = "";

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

    private void openUpdateWorkOrderDialog(WorkOrder workOrder) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chemtrovina/cmtmsys/view/work_order_create.fxml"));
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

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            default -> "";
        };
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
