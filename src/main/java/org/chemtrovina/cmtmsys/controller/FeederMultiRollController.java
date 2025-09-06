package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.chemtrovina.cmtmsys.dto.FeederActionRow;
import org.chemtrovina.cmtmsys.dto.FeederDisplayRow;
import org.chemtrovina.cmtmsys.model.*;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.service.base.*;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.chemtrovina.cmtmsys.utils.SoundUtils;
import org.chemtrovina.cmtmsys.utils.TableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.chemtrovina.cmtmsys.utils.TableUtils.centerAlignColumn;

@Component
public class FeederMultiRollController {

    // Input fields
    @FXML private TextField txtModelCode;
    @FXML private TextField txtSearchFeederCode;
    @FXML private TextField txtRollCode;
    @FXML private TextField txtDetachRollCode;

    // ComboBoxes
    @FXML private ComboBox<ModelType> cbModelType;
    @FXML private ComboBox<Warehouse> cbLines;
    @FXML private ComboBox<ModelLineRun> cbRunHistory;

    // Buttons
    @FXML private Button btnLoadFeeders;
    @FXML private Button btnCreateRun;
    @FXML private Button btnToggleRun;



    // Table and columns
    @FXML private TableView<FeederDisplayRow> tblFeederAssignments;
    @FXML private TableColumn<FeederDisplayRow, String> colFeederCode;
    @FXML private TableColumn<FeederDisplayRow, String> colMachine;
    @FXML private TableColumn<FeederDisplayRow, String> colSapCode;
    @FXML private TableColumn<FeederDisplayRow, Integer> colFeederQty;
    @FXML private TableColumn<FeederDisplayRow, String> colRollCode;
    @FXML private TableColumn<FeederDisplayRow, Integer> colMaterialQty;
    @FXML private TableColumn<FeederDisplayRow, String> colStatus;


    @FXML private TextField txtScanRollForSap;
    @FXML private TableView<FeederDisplayRow> tblFeederBySap;
    @FXML private TableColumn<FeederDisplayRow, String> colFeederCodeBySap;
    @FXML private TableColumn<FeederDisplayRow, String> colSapCodeBySap;
    @FXML private TableColumn<FeederDisplayRow, String> colStatusBySap;
    @FXML private TableColumn<FeederDisplayRow, Void> colAttachButton;
    @FXML private TableColumn<FeederDisplayRow, Void> colDeleteButton;

    @FXML private ComboBox<MaterialCart> cbTruckCode;
    @FXML private TextField txtTreeCode;
    @FXML private TextField txtRollCodeSearch;
    @FXML private TextField txtSapSearch;
    @FXML private Button btnSearchTree;

    @FXML private TableView<MaterialCartTree> tblTreeList;
    @FXML private TableView<Material> tblRollInTree;

    @FXML private TableColumn<MaterialCartTree, String> colTreeCode;
    @FXML private TableColumn<MaterialCartTree, String> colCreatedDate;
    @FXML private TableColumn<MaterialCartTree, String> colFloor;

    @FXML private TableColumn<Material, String> colRollCodeInTree;
    @FXML private TableColumn<Material, String> colSapCodeInTree;
    @FXML private TableColumn<Material, String> colQtyInTree;
    @FXML private TableColumn<Material, String> colWarehouseInTree;

    private ObservableList<MaterialCartTree> treeData = FXCollections.observableArrayList();
    private ObservableList<Material> rollData = FXCollections.observableArrayList();

    // Logs
    @FXML private TextArea txtStatusLog;


    private final ProductService productService;
    private final MaterialService materialService;

    private final FeederService feederService;
    private final FeederAssignmentService assignmentService;
    private final FeederAssignmentMaterialService materialAssignmentService;

    private final ModelLineService modelLineService;
    private final ModelLineRunService runService;
    private final WarehouseService warehouseService;

    private final MaterialCartService materialCartService;
    private final MaterialCartTreeService materialCartTreeService;


    private ModelLine currentModelLine;
    private ModelLineRun currentRun;

    @Autowired
    public FeederMultiRollController(WarehouseService warehouseService,
                                     ProductService productService,
                                     FeederService feederService,
                                     ModelLineService modelLineService,
                                     ModelLineRunService runService,
                                     MaterialService materialService,
                                     FeederAssignmentService assignmentService,
                                     FeederAssignmentMaterialService materialAssignmentService,
                                     MaterialCartTreeService materialCartTreeService,
                                     MaterialCartService materialCartService) {
        this.warehouseService = warehouseService;
        this.productService = productService;
        this.feederService = feederService;
        this.modelLineService = modelLineService;
        this.runService = runService;
        this.materialService = materialService;
        this.assignmentService = assignmentService;
        this.materialAssignmentService = materialAssignmentService;
        this.materialCartService = materialCartService;
        this.materialCartTreeService = materialCartTreeService;
    }


    @FXML
    public void initialize() {
        setupComboBoxes();
        setupTableView();
        setupEventHandlers();
        setupFeederBySapTable();
        setupMaterialCartSearch();
    }

    private void setupMaterialCartSearch() {
        cbTruckCode.setItems(FXCollections.observableArrayList(materialCartService.getAllCarts()));
        tblTreeList.setItems(treeData);
        tblRollInTree.setItems(rollData);

        colTreeCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTreeCode()));
        colCreatedDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCreatedAt() != null ? data.getValue().getCreatedAt().toString() : ""));
        colFloor.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLevelNote()));

        colRollCodeInTree.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRollCode()));
        colSapCodeInTree.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSapCode()));
        colQtyInTree.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getQuantity())));
        colWarehouseInTree.setCellValueFactory(data -> {
            String name = warehouseService.getAllWarehouses().stream()
                    .filter(w -> w.getWarehouseId() == data.getValue().getWarehouseId())
                    .map(Warehouse::getName).findFirst().orElse("N/A");
            return new SimpleStringProperty(name);
        });

        btnSearchTree.setOnAction(e -> handleSearchMaterialCart());

        tblTreeList.setRowFactory(tv -> {
            TableRow<MaterialCartTree> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    loadRollsByTree(row.getItem().getTreeId());
                }
            });
            return row;
        });
    }

    private void setupComboBoxes() {
        cbModelType.setItems(FXCollections.observableArrayList(ModelType.values()));
        cbLines.setItems(FXCollections.observableArrayList(warehouseService.getAllWarehouses()));
        cbRunHistory.setDisable(true);

        cbLines.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getName());
            }
        });

        cbLines.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getName());
            }
        });

        cbRunHistory.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ModelLineRun item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String label = item.getRunCode() + " (" + item.getStatus() + ")";
                    setText(label);
                }
            }
        });

        cbRunHistory.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ModelLineRun item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String label = item.getRunCode() + " (" + item.getStatus() + ")";
                    setText(label);
                }
            }
        });

    }
    private void setupTableView() {
        setupTableColumns();
        tblFeederAssignments.getSelectionModel().setCellSelectionEnabled(true);
        tblFeederAssignments.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tblFeederAssignments.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tblFeederAssignments.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == javafx.scene.input.KeyCode.C) {
                FxClipboardUtils.copySelectionToClipboard(tblFeederAssignments);
            }
        });
    }
    private void setupEventHandlers() {
        btnLoadFeeders.setOnAction(event -> loadFeedersAndRuns());
        btnCreateRun.setOnAction(event -> createNewRun());
        txtSearchFeederCode.setOnAction(e -> scrollToFeederCode());
        txtRollCode.setOnAction(e -> handleAttachRollCode());
        btnToggleRun.setOnAction(event -> handleToggleRun());


        cbRunHistory.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentRun = newVal;
                loadFeederDataByRun(currentRun);
            }
        });

        txtScanRollForSap.setOnAction(e -> handleSearchFeederBySap());

    }

    private void setupFeederBySapTable() {
        colFeederCodeBySap.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFeederCode()));
        colSapCodeBySap.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSapCode()));
        colStatusBySap.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));

        colAttachButton.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Gắn");

            {
                btn.setOnAction(e -> {
                    FeederDisplayRow row = getTableView().getItems().get(getIndex());
                    handleAttachToFeeder(row);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(btn);
            }
        });

        colDeleteButton.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Xóa");

            {
                btn.setOnAction(e -> {
                    FeederDisplayRow row = getTableView().getItems().get(getIndex());
                    handleDetachFromFeeder(row);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(btn);
            }
        });
    }


    private void setupTableColumns() {
        colFeederCode.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFeederCode()));
        colMachine.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getMachine()));
        colSapCode.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSapCode()));
        colFeederQty.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getFeederQty()).asObject());
        colRollCode.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRollCode()));
        colMaterialQty.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getMaterialQty()).asObject());
        colStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        centerAlignColumn(colFeederCode);
        centerAlignColumn(colMachine);
        centerAlignColumn(colSapCode);
        centerAlignColumn(colFeederQty);
        centerAlignColumn(colRollCode);
        centerAlignColumn(colMaterialQty);
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("-fx-alignment: CENTER;");
                } else {
                    setText(status);
                    switch (status) {
                        case "Chưa gắn" ->
                                setStyle("-fx-alignment: CENTER; -fx-background-color: #ffcccc; -fx-text-fill: red;");
                        case "Đã gắn" ->
                                setStyle("-fx-alignment: CENTER; -fx-background-color: #ccffcc; -fx-text-fill: green;");
                        case "Bổ sung" ->
                                setStyle("-fx-alignment: CENTER; -fx-background-color: #fff0b3; -fx-text-fill: orange;");
                        default ->
                                setStyle("-fx-alignment: CENTER;");
                    }
                }
            }
        });


    }

    private void loadFeederDataByRun(ModelLineRun run) {
        // 1. Lấy danh sách feeder cho model + line
        List<Feeder> feeders = feederService.getFeedersByModelAndLine(
                currentModelLine.getProductId(),
                currentModelLine.getWarehouseId()
        );

        // 2. Lấy tất cả assignment-material theo run và gom nhóm theo feederId
        Map<Integer, List<FeederAssignmentMaterial>> matMap =
                materialAssignmentService.getAllActiveByRunGrouped(run.getRunId());

        // 3. Lấy toàn bộ materialId từ tất cả assignment-material
        Set<Integer> materialIds = matMap.values().stream()
                .flatMap(List::stream)
                .map(FeederAssignmentMaterial::getMaterialId)
                .collect(Collectors.toSet());

        // 4. Truy vấn 1 lần để lấy tất cả Material
        Map<Integer, Material> materialMap = materialService.getMaterialsByIds(materialIds).stream()
                .collect(Collectors.toMap(Material::getMaterialId, m -> m));

        // 5. Tạo rows cho TableView
        ObservableList<FeederDisplayRow> rows = FXCollections.observableArrayList();

        for (Feeder feeder : feeders) {
            FeederDisplayRow row = FeederDisplayRow.fromFeeder(feeder);
            List<FeederAssignmentMaterial> mats = matMap.getOrDefault(feeder.getFeederId(), List.of());

            if (!mats.isEmpty()) {
                FeederAssignmentMaterial latest = mats.get(mats.size() - 1);
                Material mat = materialMap.get(latest.getMaterialId());

                if (mat != null) {
                    row.setRollCode(mat.getRollCode());
                    row.setMaterialQty(mat.getQuantity());
                    row.setStatus(mats.size() > 1 ? "Bổ sung" : "Đã gắn");
                } else {
                    row.setRollCode("N/A");
                    row.setMaterialQty(0);
                    row.setStatus("Dữ liệu lỗi");
                }
            } else {
                row.setRollCode("");
                row.setMaterialQty(0);
                row.setStatus("Chưa gắn");
            }

            rows.add(row);
        }

        tblFeederAssignments.setItems(rows);
    }

    private void loadFeedersAndRuns() {
        String modelCode = txtModelCode.getText().trim();
        ModelType modelType = cbModelType.getValue();
        Warehouse selectedLine = cbLines.getValue();

        if (modelCode.isEmpty() || modelType == null || selectedLine == null) {
            showAlert("Vui lòng nhập đủ Mã Model, Loại và Line.");
            return;
        }

        Product product = productService.getProductByCodeAndType(modelCode, modelType);
        if (product == null) {
            showAlert("Không tìm thấy model trong hệ thống.");
            return;
        }

        currentModelLine = modelLineService.findOrCreateModelLine(product.getProductId(), selectedLine.getWarehouseId());

        // Lấy danh sách phiên chạy
        List<ModelLineRun> runs = runService.getRunsByModelLineId(currentModelLine.getModelLineId());
        cbRunHistory.setItems(FXCollections.observableArrayList(runs));
        cbRunHistory.setDisable(false);

        if (runs.isEmpty()) {
            currentRun = null;
            tblFeederAssignments.setItems(FXCollections.observableArrayList());
            txtStatusLog.appendText("⚠️ Không có phiên chạy nào. Vui lòng tạo phiên chạy mới.\n");
            return;
        }

        // Ưu tiên run đầu tiên
        currentRun = runs.get(0);
        cbRunHistory.setValue(currentRun);

        // Lấy feeders
        List<Feeder> feeders = feederService.getFeedersByModelAndLine(product.getProductId(), selectedLine.getWarehouseId());
        List<FeederDisplayRow> rows = FXCollections.observableArrayList();

        for (Feeder feeder : feeders) {
            FeederDisplayRow row = FeederDisplayRow.fromFeeder(feeder);

            try {
                FeederAssignment assignment = assignmentService.assignFeeder(currentRun.getRunId(), feeder.getFeederId(), "system");
                List<FeederAssignmentMaterial> mats = materialAssignmentService.getMaterialsByAssignment(assignment.getAssignmentId());

                if (!mats.isEmpty()) {
                    FeederAssignmentMaterial latest = mats.get(mats.size() - 1);
                    Material mat = materialService.getMaterialById(latest.getMaterialId());

                    row.setRollCode(mat != null ? mat.getRollCode() : "");
                    row.setMaterialQty(mat != null ? mat.getQuantity() : 0);
                    row.setStatus(mats.size() > 1 ? "Bổ sung" : "Đã gắn");
                } else {
                    row.setRollCode("");
                    row.setMaterialQty(0);
                    row.setStatus("Chưa gắn");
                }
            } catch (Exception e) {
                row.setStatus("Lỗi");
                row.setRollCode("");
                row.setMaterialQty(0);
                txtStatusLog.appendText("⚠️ Lỗi khi load cuộn cho feeder: " + feeder.getFeederCode() + "\n");
            }

            rows.add(row);
        }

        tblFeederAssignments.setItems(FXCollections.observableArrayList(rows));
    }

    private void createNewRun() {
        if (currentModelLine == null) {
            showAlert("Bạn cần tải model trước khi tạo phiên chạy.");
            return;
        }

        currentRun = runService.createRun(currentModelLine.getModelLineId());
        cbRunHistory.getItems().add(0, currentRun);
        cbRunHistory.setValue(currentRun);
        reloadRuns();
    }

    private void handleToggleRun() {
        if (currentRun == null) {
            showAlert("⚠️ Không có phiên chạy nào được chọn.");
            return;
        }

        boolean isRunning = "Running".equalsIgnoreCase(currentRun.getStatus());

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText(null);
        confirm.setContentText(isRunning
                ? "Bạn có chắc chắn muốn kết thúc phiên chạy này?"
                : "Bạn có muốn mở lại phiên chạy này?");
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                if (isRunning) {
                    runService.endRun(currentRun.getRunId());
                    txtStatusLog.appendText("✅ Đã kết thúc phiên: " + currentRun.getRunCode() + "\n");
                } else {
                    runService.reopenRun(currentRun.getRunId()); // 🛠 bạn cần có hàm này trong service
                    txtStatusLog.appendText("🔁 Đã mở lại phiên: " + currentRun.getRunCode() + "\n");
                }

                reloadRuns();
                loadFeederDataByRun(currentRun);
                updateToggleRunButton(); // Cập nhật lại nút
            }
        });
    }


    private void handleSearchFeederBySap() {
        String rollCode = txtScanRollForSap.getText().trim();
        if (rollCode.isEmpty()) return;

        Material material = materialService.getMaterialByRollCode(rollCode);
        if (material == null) {
            txtStatusLog.appendText("❌ Không tìm thấy cuộn [" + rollCode + "]\n");
            SoundUtils.playSound("Wrong.mp3");
            return;
        }

        // 🆕 HIỂN THỊ CÂY VÀ XE CHỨA CUỘN
        if (material.getTreeId() != null) {
            MaterialCartTree tree = materialCartTreeService.getById(material.getTreeId());
            if (tree != null) {
                MaterialCart cart = materialCartService.getCartById(tree.getCartId());
                txtStatusLog.appendText(
                        "📦 Cuộn [" + rollCode + "] đang ở cây [" + tree.getTreeCode() + "] trong xe [" + (cart != null ? cart.getCartCode() : "N/A") + "]\n"
                );
            }
        }

        // Lọc feeder theo sapCode
        List<Feeder> feeders = feederService.getFeedersByModelAndLine(
                        currentModelLine.getProductId(),
                        currentModelLine.getWarehouseId()
                ).stream()
                .filter(f -> f.getSapCode().equalsIgnoreCase(material.getSapCode()))
                .toList();


        if (feeders.isEmpty()) {
            txtStatusLog.appendText("❌ Không tìm thấy Feeder nào cho SAP [" + material.getSapCode() + "]\n");
            return;
        }

        ObservableList<FeederDisplayRow> rows = FXCollections.observableArrayList();
        for (Feeder feeder : feeders) {
            FeederDisplayRow row = FeederDisplayRow.fromFeeder(feeder);
            row.setRollCode(material.getRollCode()); // Roll muốn gắn
            row.setMaterialQty(material.getQuantity());

            // Kiểm tra trạng thái
            List<FeederAssignmentMaterial> mats = materialAssignmentService.getActiveByFeederId(feeder.getFeederId());
            if (mats.isEmpty()) {
                row.setStatus("Chưa gắn");
            } else {
                row.setStatus("Đã gắn");
            }
            rows.add(row);
        }

        tblFeederBySap.setItems(rows);
    }

    private void handleAttachToFeeder(FeederDisplayRow row) {
        if (attachRollToFeeder(row.getRollCode(), row)) {
            tblFeederBySap.refresh();
            tblFeederAssignments.refresh();
            loadFeederDataByRun(currentRun);
            txtScanRollForSap.requestFocus();
            txtScanRollForSap.selectAll();
        }
    }


    private void handleDetachFromFeeder(FeederDisplayRow row) {
        if (currentRun == null) {
            showAlert("Vui lòng chọn phiên chạy trước.");
            return;
        }

        FeederAssignment ass = assignmentService.getAssignment(currentRun.getRunId(), row.getFeederId());
        if (ass == null) {
            txtStatusLog.appendText("⚠️ Không có assignment cho feeder: " + row.getFeederCode() + "\n");
            return;
        }

        List<FeederAssignmentMaterial> mats = materialAssignmentService.getMaterialsByAssignment(ass.getAssignmentId());
        if (!mats.isEmpty()) {
            FeederAssignmentMaterial last = mats.get(mats.size() - 1);
            materialAssignmentService.deleteMaterialAssignment(last.getId());

            txtStatusLog.appendText("🗑️ Đã xóa cuộn gần nhất khỏi Feeder: " + row.getFeederCode() + "\n");
            tblFeederBySap.refresh();
        } else {
            txtStatusLog.appendText("⚠️ Feeder này chưa gắn cuộn nào.\n");
        }

        handleSearchFeederBySap();
        loadFeederDataByRun(currentRun);
    }


    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void scrollToFeederCode() {
        String searchCode = txtSearchFeederCode.getText().trim().toLowerCase();
        if (searchCode.isEmpty()) return;

        var items = tblFeederAssignments.getItems();

        // Ưu tiên match chính xác trước
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getFeederCode().equalsIgnoreCase(searchCode)) {
                tblFeederAssignments.getSelectionModel().clearAndSelect(i);
                tblFeederAssignments.scrollTo(i);
                txtRollCode.requestFocus();
                txtRollCode.clear();
                return;
            }
        }

        // Nếu không có, tìm cái chứa gần giống
        for (int i = 0; i < items.size(); i++) {
            String code = items.get(i).getFeederCode().toLowerCase();
            if (code.contains(searchCode)) {
                tblFeederAssignments.getSelectionModel().clearAndSelect(i);
                tblFeederAssignments.scrollTo(i);
                txtRollCode.requestFocus();
                txtRollCode.clear();
                return;
            }
        }

        txtStatusLog.appendText("❌ Không tìm thấy FeederCode chứa: " + searchCode + "\n");
    }


    private void handleAttachRollCode() {
        String rollCode = txtRollCode.getText().trim();
        if (rollCode.isEmpty()) return;

        FeederDisplayRow selectedRow = tblFeederAssignments.getSelectionModel().getSelectedItem();
        if (selectedRow == null) {
            txtStatusLog.appendText("⚠️ Vui lòng chọn một dòng Feeder để gắn cuộn.\n");
            return;
        }

        if (attachRollToFeeder(rollCode, selectedRow)) {
            tblFeederAssignments.refresh();
            txtSearchFeederCode.requestFocus();
            txtRollCode.selectAll();
            txtSearchFeederCode.clear();
        }
    }



    private void reloadRuns() {
        if (currentModelLine == null) return;

        List<ModelLineRun> runs = runService.getRunsByModelLineId(currentModelLine.getModelLineId());
        cbRunHistory.setItems(FXCollections.observableArrayList(runs));

        if (!runs.isEmpty()) {
            currentRun = runs.get(0);
            cbRunHistory.setValue(currentRun);
        }
    }

    private void handleSearchMaterialCart() {
        String treeCode = txtTreeCode.getText().trim();
        String rollCode = txtRollCodeSearch.getText().trim();
        String sapCode = txtSapSearch.getText().trim();

        treeData.clear();
        rollData.clear();

        if (!treeCode.isEmpty()) {
            MaterialCartTree tree = materialCartTreeService.getTreeByCode(treeCode);
            if (tree != null) {
                treeData.add(tree);
                MaterialCart cart = materialCartService.getCartById(tree.getCartId());
                if (cart != null) cbTruckCode.setValue(cart);
            }
            return;
        }

        if (!rollCode.isEmpty()) {
            Material mat = materialService.getMaterialByRollCode(rollCode);
            if (mat != null && mat.getTreeId() != null) {
                MaterialCartTree tree = materialCartTreeService.getById(mat.getTreeId());
                if (tree != null) {
                    treeData.add(tree);
                    MaterialCart cart = materialCartService.getCartById(tree.getCartId());
                    if (cart != null) cbTruckCode.setValue(cart);
                }
            }
            return;
        }

        if (!sapCode.isEmpty()) {
            List<Material> materials = materialService.findBySapCode(sapCode);
            List<Integer> treeIds = materials.stream().map(Material::getTreeId)
                    .filter(id -> id != null).distinct().collect(Collectors.toList());
            treeData.setAll(materialCartTreeService.getByIds(treeIds));
        }

        if (cbTruckCode.getValue() != null && treeCode.isEmpty() && rollCode.isEmpty() && sapCode.isEmpty()) {
            List<MaterialCartTree> trees = materialCartTreeService.getTreesByCartId(cbTruckCode.getValue().getCartId());
            treeData.setAll(trees);
        }
    }

    private void loadRollsByTree(int treeId) {
        List<Material> rolls = materialService.getByTreeId(treeId);
        rollData.setAll(rolls);
    }
    private void updateToggleRunButton() {
        if (currentRun == null) {
            btnToggleRun.setText("Chọn phiên");
        } else if ("Running".equalsIgnoreCase(currentRun.getStatus())) {
            btnToggleRun.setText("Kết thúc phiên chạy");
        } else {
            btnToggleRun.setText("Mở lại phiên chạy");
        }
    }


    private boolean attachRollToFeeder(String rollCode, FeederDisplayRow targetFeederRow) {
        if (currentRun == null) {
            txtStatusLog.appendText("⚠️ Vui lòng tạo phiên chạy trước khi gắn cuộn.\n");
            return false;
        }

        Material material = materialService.getMaterialByRollCode(rollCode);
        if (material == null) {
            txtStatusLog.appendText("❌ Không tìm thấy cuộn vật liệu: " + rollCode + "\n");
            SoundUtils.playSound("Wrong.mp3");
            return false;
        }

        Feeder feeder = feederService.getFeederById(targetFeederRow.getFeederId());
        if (feeder == null) {
            txtStatusLog.appendText("❌ Không xác định được Feeder từ dòng đã chọn.\n");
            SoundUtils.playSound("Wrong.mp3");
            return false;
        }

        // ✅ KIỂM TRA MÃ SAP
        if (!material.getSapCode().equalsIgnoreCase(feeder.getSapCode())) {
            txtStatusLog.appendText("❌ Mã SAP [" + material.getSapCode() + "] không khớp với Feeder [" + feeder.getSapCode() + "]\n");
            SoundUtils.playSound("Wrong.mp3");
            return false;
        }

        /*// ✅ KIỂM TRA KHO
        if (material.getWarehouseId() != cbLines.getValue().getWarehouseId()) {
            txtStatusLog.appendText("❌ Cuộn không nằm trong đúng kho [" + cbLines.getValue().getName() + "]\n");
            SoundUtils.playSound("Wrong.mp3");
            return false;
        }*/

        // ✅ CẢNH BÁO nếu đã gắn ở feeder khác
        List<FeederAssignmentMaterial> assignedInRun = materialAssignmentService.getActiveByRunId(currentRun.getRunId());
        boolean alreadyAssignedInRun = assignedInRun.stream()
                .anyMatch(m -> m.getMaterialId() == material.getMaterialId());

        if (alreadyAssignedInRun) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận gắn lại");
            confirm.setHeaderText("Cuộn này đã được gắn vào feeder khác trong phiên.");
            confirm.setContentText("Bạn có chắc muốn gắn lại cuộn [" + rollCode + "] vào feeder [" + feeder.getFeederCode() + "]?");
            confirm.showAndWait();

            if (confirm.getResult() != ButtonType.OK) {
                txtStatusLog.appendText("⛔ Hủy gắn cuộn [" + rollCode + "]\n");
                return false;
            }
        }

        // Gắn cuộn nếu hợp lệ
        FeederAssignment assignment = assignmentService.assignFeeder(currentRun.getRunId(), feeder.getFeederId(), "system");
        materialAssignmentService.attachMaterial(assignment.getAssignmentId(), material.getMaterialId(), false, null);

        // Cập nhật lại dòng
        List<FeederAssignmentMaterial> mats = materialAssignmentService.getMaterialsByAssignment(assignment.getAssignmentId());
        if (!mats.isEmpty()) {
            FeederAssignmentMaterial lastMat = mats.get(mats.size() - 1);
            Material mat = materialService.getMaterialById(lastMat.getMaterialId());

            targetFeederRow.setRollCode(mat.getRollCode());
            targetFeederRow.setMaterialQty(mat.getQuantity());
            targetFeederRow.setStatus(mats.size() > 1 ? "Bổ sung" : "Đã gắn");
        }

        txtStatusLog.appendText("✅ Đã gắn cuộn [" + rollCode + "] vào Feeder [" + feeder.getFeederCode() + "]\n");
        SoundUtils.playSound("done.mp3");
        return true;
    }



}
