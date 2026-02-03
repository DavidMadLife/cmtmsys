package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.chemtrovina.cmtmsys.dto.FeederDisplayRow;
import org.chemtrovina.cmtmsys.model.*;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.model.enums.UserRole;
import org.chemtrovina.cmtmsys.security.RequiresRoles;
import org.chemtrovina.cmtmsys.service.base.*;
import org.chemtrovina.cmtmsys.utils.AutoCompleteUtils;
import org.chemtrovina.cmtmsys.utils.FxAlertUtils;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.chemtrovina.cmtmsys.utils.SoundUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.chemtrovina.cmtmsys.utils.TableUtils.centerAlignColumn;

@RequiresRoles({
        UserRole.ADMIN,
        UserRole.INVENTORY,
        UserRole.SUBLEEDER
})

@Component
public class FeederMultiRollController {

    // ============================================================================
    // 🧩 1️⃣ FXML FIELDS & SERVICES
    // ============================================================================
    @FXML private TextField txtModelCode;
    @FXML private TextField txtModelName;

    @FXML private TextField txtSearchFeederCode;
    @FXML private TextField txtRollCode;
    @FXML private TextField txtDetachRollCode;

    @FXML private ComboBox<ModelType> cbModelType;
    @FXML private ComboBox<Warehouse> cbLines;
    @FXML private ComboBox<ModelLineRun> cbRunHistory;

    @FXML private Button btnLoadFeeders;
    @FXML private Button btnCreateRun;
    @FXML private Button btnToggleRun;

    // Main feeder table
    @FXML private TableView<FeederDisplayRow> tblFeederAssignments;
    @FXML private TableColumn<FeederDisplayRow, String> colFeederCode;
    @FXML private TableColumn<FeederDisplayRow, String> colMachine;
    @FXML private TableColumn<FeederDisplayRow, String> colSapCode;
    @FXML private TableColumn<FeederDisplayRow, Integer> colFeederQty;
    @FXML private TableColumn<FeederDisplayRow, String> colRollCode;
    @FXML private TableColumn<FeederDisplayRow, Integer> colMaterialQty;
    @FXML private TableColumn<FeederDisplayRow, String> colStatus;

    // Bottom feeder table (by SAP)
    @FXML private TextField txtScanRollForSap;
    @FXML private TableView<FeederDisplayRow> tblFeederBySap;
    @FXML private TableColumn<FeederDisplayRow, String> colFeederCodeBySap;
    @FXML private TableColumn<FeederDisplayRow, String> colSapCodeBySap;
    @FXML private TableColumn<FeederDisplayRow, String> colRollCodeBySap;
    @FXML private TableColumn<FeederDisplayRow, Integer> colQtyBySap;
    @FXML private TableColumn<FeederDisplayRow, String> colStatusBySap;
    @FXML private TableColumn<FeederDisplayRow, Void> colAttachButton;
    @FXML private TableColumn<FeederDisplayRow, Void> colDeleteButton;

    // Material cart & tree
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

    @FXML private TextArea txtStatusLog;

    private ObservableList<MaterialCartTree> treeData = FXCollections.observableArrayList();
    private ObservableList<Material> rollData = FXCollections.observableArrayList();

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

    // ============================================================================
    // ⚙️ 2️⃣ CONSTRUCTOR & INITIALIZATION
    // ============================================================================
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
        setupAutoCompleteModels();

    }




    // ============================================================================
    // 🧱 3️⃣ SETUP UI COMPONENTS
    // ============================================================================
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
                setText(empty || item == null ? null : item.getRunCode() + " (" + item.getStatus() + ")");
            }
        });
        cbRunHistory.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ModelLineRun item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getRunCode() + " (" + item.getStatus() + ")");
            }
        });
    }

    private void setupTableView() {
        setupTableColumns();
        FxClipboardUtils.enableCopyShortcut(tblFeederBySap);
        FxClipboardUtils.enableCopyShortcut(tblFeederAssignments);
        FxClipboardUtils.enableCopyShortcut(tblRollInTree);
        FxClipboardUtils.enableCopyShortcut(tblTreeList);
    }


    private void setupFeederBySapTable() {
        colFeederCodeBySap.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFeederCode()));
        colSapCodeBySap.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSapCode()));
        colStatusBySap.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        colRollCodeBySap.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRollCode())
        );
        colQtyBySap.setCellValueFactory(
                data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getMaterialQty()).asObject()
        );

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

    private void setupAutoCompleteModels() {
        List<Product> allProducts = productService.getAllProducts();

        // Danh sách gợi ý
        List<String> modelCodes = allProducts.stream()
                .map(Product::getProductCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<String> modelNames = allProducts.stream()
                .map(Product::getName)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .toList();

        // Gắn auto-complete
        AutoCompleteUtils.setupAutoComplete(txtModelCode, modelCodes);
        AutoCompleteUtils.setupAutoComplete(txtModelName, modelNames);

        // Khi nhập hoặc chọn model code → tự fill name & type
        txtModelCode.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) return;
            Product found = allProducts.stream()
                    .filter(p -> p.getProductCode().equalsIgnoreCase(newVal))
                    .findFirst().orElse(null);
            if (found != null) {
                txtModelName.setText(found.getName());
                cbModelType.setValue(found.getModelType());
            }
        });

        // Khi nhập hoặc chọn model name → tự fill code & type
        txtModelName.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) return;
            Product found = allProducts.stream()
                    .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(newVal))
                    .findFirst().orElse(null);
            if (found != null) {
                txtModelCode.setText(found.getProductCode());
                cbModelType.setValue(found.getModelType());
            }
        });
    }


    // ============================================================================
// 📦 7️⃣ MATERIAL CART MANAGEMENT (Refactor theo MaterialCartController)
// ============================================================================
    private void setupMaterialCartSearch() {
        cbTruckCode.setItems(FXCollections.observableArrayList(materialCartService.getAllCarts()));
        tblTreeList.setItems(treeData);
        tblRollInTree.setItems(rollData);

        // Bind cột cây
        colTreeCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTreeCode()));
        colCreatedDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCreatedAt() != null ? d.getValue().getCreatedAt().toString() : ""));
        colFloor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLevelNote()));

        // Bind cột cuộn
        colRollCodeInTree.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRollCode()));
        colSapCodeInTree.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSapCode()));
        colQtyInTree.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantity())));
        colWarehouseInTree.setCellValueFactory(d -> {
            String name = warehouseService.getAllWarehouses().stream()
                    .filter(w -> w.getWarehouseId() == d.getValue().getWarehouseId())
                    .map(Warehouse::getName).findFirst().orElse("N/A");
            return new SimpleStringProperty(name);
        });

        // Bind sự kiện
        btnSearchTree.setOnAction(e -> handleSearchMaterialInCart());
        bindSearchEnter(txtTreeCode);
        bindSearchEnter(txtRollCodeSearch);
        bindSearchEnter(txtSapSearch);

        cbTruckCode.setOnAction(e -> {
            MaterialCart cart = cbTruckCode.getValue();
            if (cart != null) {
                List<MaterialCartTree> trees = materialCartTreeService.getTreesByCartId(cart.getCartId());
                treeData.setAll(trees);
                rollData.clear();
            }
        });

        tblTreeList.setRowFactory(tv -> {
            TableRow<MaterialCartTree> row = new TableRow<>();
            row.setOnMouseClicked(evt -> {
                if (evt.getClickCount() == 2 && !row.isEmpty()) {
                    MaterialCartTree tree = row.getItem();
                    loadRollsByTree(tree.getTreeId());
                }
            });
            return row;
        });

        TableColumn<Material, Void> colRemove = new TableColumn<>("Action");
        colRemove.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Gỡ khỏi cây");
            {
                btn.setOnAction(e -> {
                    Material m = getTableView().getItems().get(getIndex());
                    handleRemoveMaterial(m);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
        tblRollInTree.getColumns().add(colRemove);


        tblRollInTree.setRowFactory(tv -> {
            TableRow<Material> row = new TableRow<>();
            row.setOnMouseClicked(evt -> {
                if (evt.getClickCount() == 2 && !row.isEmpty()) {
                    Material m = row.getItem();
                    txtScanRollForSap.setText(m.getRollCode());
                    handleSearchFeederBySap(); // gọi luôn tìm feeder theo SAP
                }
            });
            return row;
        });

    }

    private void bindSearchEnter(TextField txt) {
        txt.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) handleSearchMaterialInCart();
        });
    }

    private void handleSearchMaterialInCart() {
        String treeCode = txtTreeCode.getText().trim();
        String rollCode = txtRollCodeSearch.getText().trim();
        String sapCode = txtSapSearch.getText().trim();

        treeData.clear();
        rollData.clear();

        if (treeCode.isEmpty() && rollCode.isEmpty() && sapCode.isEmpty()) {
            if (cbTruckCode.getValue() != null) {
                List<MaterialCartTree> trees = materialCartTreeService.getTreesByCartId(cbTruckCode.getValue().getCartId());
                treeData.setAll(trees);
            }
            return;
        }

        if (!treeCode.isEmpty()) {
            MaterialCartTree tree = materialCartTreeService.getTreeByCode(treeCode);
            if (tree != null) {
                treeData.setAll(List.of(tree));
                MaterialCart cart = materialCartService.getCartById(tree.getCartId());
                if (cart != null) cbTruckCode.setValue(cart);
            }
            txtTreeCode.selectAll();
            return;
        }

        if (!rollCode.isEmpty()) {
            Material mat = materialService.getMaterialByRollCode(rollCode);
            if (mat != null && mat.getTreeId() != null) {
                MaterialCartTree tree = materialCartTreeService.getById(mat.getTreeId());
                if (tree != null) {
                    treeData.setAll(List.of(tree));
                    MaterialCart cart = materialCartService.getCartById(tree.getCartId());
                    if (cart != null) cbTruckCode.setValue(cart);
                }
            }
            txtRollCodeSearch.selectAll();
            return;
        }

        if (!sapCode.isEmpty()) {
            List<Material> mats = materialService.findBySapCode(sapCode);
            List<Integer> treeIds = mats.stream()
                    .map(Material::getTreeId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());
            List<MaterialCartTree> trees = materialCartTreeService.getByIds(treeIds);
            treeData.setAll(trees);
            txtSapSearch.selectAll();
        }
    }

    private void handleRemoveMaterial(Material material) {
        if (material == null || material.getTreeId() == null) {
            FxAlertUtils.warning("⚠️ Cuộn này không nằm trong cây nào!");
            return;
        }

        int treeId = material.getTreeId();
        material.setTreeId(null);
        materialService.updateMaterial(material);
        FxAlertUtils.info("✅ Đã gỡ cuộn khỏi cây!");
        loadRollsByTree(treeId);
    }


    // ============================================================================
    // 🔁 4️⃣ EVENT HANDLERS
    // ============================================================================
    private void setupEventHandlers() {
        btnLoadFeeders.setOnAction(event -> loadFeedersAndRuns());
        btnCreateRun.setOnAction(event -> createNewRun());
        btnToggleRun.setOnAction(event -> handleToggleRun());
        txtSearchFeederCode.setOnAction(e -> scrollToFeederCode());
        txtRollCode.setOnAction(e -> handleAttachRollCode());
        txtScanRollForSap.setOnAction(e -> handleSearchFeederBySap());
        cbRunHistory.valueProperty().addListener((obs, o, n) -> {
            if (n != null) { currentRun = n; loadFeederDataByRun(currentRun); }
        });
        tblFeederAssignments.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null && currentRun != null) loadCurrentFeederToBottomTable(n);
        });
    }


// ============================================================================
// 🧩 5️⃣ FEEDER LOGIC
// ----------------------------------------------------------------------------
// 5.1 Load feeder list & assignment
// 5.2 Attach / Detach roll handling
// 5.3 Helper: attachRollToFeeder()
// ============================================================================

    private void loadCurrentFeederToBottomTable(FeederDisplayRow selectedFeeder) {
        try {
            // Lấy assignment của feeder trong run hiện tại
            FeederAssignment assignment = assignmentService.getAssignment(currentRun.getRunId(), selectedFeeder.getFeederId());
            if (assignment == null) {
                tblFeederBySap.setItems(FXCollections.observableArrayList());
                return;
            }

            // Lấy danh sách cuộn đang gắn trong feeder
            List<FeederAssignmentMaterial> mats =
                    materialAssignmentService.getMaterialsByAssignment(assignment.getAssignmentId());

            if (mats.isEmpty()) {
                tblFeederBySap.setItems(FXCollections.observableArrayList());
                return;
            }

            // Map cuộn sang FeederDisplayRow để hiển thị ở bảng dưới
            List<FeederDisplayRow> rows = mats.stream().map(m -> {
                Material mat = materialService.getMaterialById(m.getMaterialId());
                FeederDisplayRow row = new FeederDisplayRow();
                row.setFeederId(selectedFeeder.getFeederId());
                row.setFeederCode(selectedFeeder.getFeederCode());
                row.setSapCode(mat != null ? mat.getSapCode() : "N/A");
                row.setRollCode(mat != null ? mat.getRollCode() : "N/A");
                row.setMaterialQty(mat != null ? mat.getQuantity() : 0);
                row.setStatus("Đã gắn");
                return row;
            }).toList();

            tblFeederBySap.setItems(FXCollections.observableArrayList(rows));

        } catch (Exception e) {
            e.printStackTrace();
            tblFeederBySap.setItems(FXCollections.observableArrayList());
        }
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
        String modelCode = txtModelCode != null && txtModelCode.getText() != null ? txtModelCode.getText().trim() : "";
        String modelName = txtModelName != null && txtModelName.getText() != null ? txtModelName.getText().trim() : "";
        ModelType modelType = cbModelType.getValue();
        Warehouse selectedLine = cbLines.getValue();

        if ((modelCode.isEmpty() && modelName.isEmpty()) || modelType == null || selectedLine == null) {
            FxAlertUtils.warning("⚠️ Vui lòng nhập Mã hoặc Tên Model, chọn Loại và Line trước khi tải.");
            return;
        }

        Product product = null;
        if (!modelCode.isEmpty()) {
            product = productService.getProductByCodeAndType(modelCode, modelType);
        }
        if (product == null && !modelName.isEmpty()) {
            product = productService.getProductByNameAndType(modelName, modelType);
        }

        if (product == null) {
            FxAlertUtils.warning("❌ Không tìm thấy Model trong hệ thống.");
            return;
        }

        // Tìm hoặc tạo ModelLine
        currentModelLine = modelLineService.findOrCreateModelLine(product.getProductId(), selectedLine.getWarehouseId());

        // 🔹 Lấy danh sách feeder
        List<Feeder> feeders = feederService.getFeedersByModelAndLine(product.getProductId(), selectedLine.getWarehouseId());
        if (feeders == null || feeders.isEmpty()) {
            txtStatusLog.appendText("⚠️ Model [" + product.getProductCode() + "] chưa có cấu hình Feeder cho line [" + selectedLine.getName() + "]\n");

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Thiếu cấu hình Feeder");
            alert.setHeaderText("Model này chưa có danh sách Feeder.");
            alert.setContentText("Bạn có muốn mở màn hình cấu hình Feeder cho model này không?");
            alert.showAndWait();

            // 🧩 Nếu bạn muốn tự động mở màn hình feeder manager:
            if (alert.getResult() == ButtonType.OK) {
                MainController.getInstance().openTab("Feeder Config", "/org/chemtrovina/cmtmsys/view/feederListView-feature.fxml");
            }
            return;
        }

        // 🔹 Load danh sách Run
        List<ModelLineRun> runs = runService.getRunsByModelLineId(currentModelLine.getModelLineId());
        cbRunHistory.setItems(FXCollections.observableArrayList(runs));
        cbRunHistory.setDisable(false);

        if (runs.isEmpty()) {
            currentRun = null;
            tblFeederAssignments.setItems(FXCollections.emptyObservableList());
            txtStatusLog.appendText("⚠️ Không có phiên chạy nào. Hãy tạo mới.\n");
            return;
        }

        // 🔹 Ưu tiên run đầu tiên
        currentRun = runs.get(0);
        cbRunHistory.setValue(currentRun);

        // 🔹 Tạo các dòng feeder display
        ObservableList<FeederDisplayRow> rows = FXCollections.observableArrayList();

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
                    row.setStatus("Chưa gắn");
                    row.setRollCode("");
                    row.setMaterialQty(0);
                }
            } catch (Exception e) {
                row.setStatus("Lỗi");
                txtStatusLog.appendText("⚠️ Lỗi khi load cuộn cho Feeder: " + feeder.getFeederCode() + "\n");
            }

            rows.add(row);
        }

        tblFeederAssignments.setItems(rows);
    }



    private void createNewRun() {
        if (currentModelLine == null) {
            FxAlertUtils.warning("Bạn cần tải model trước khi tạo phiên chạy.");
            return;
        }

        currentRun = runService.createRun(currentModelLine.getModelLineId());
        cbRunHistory.getItems().add(0, currentRun);
        cbRunHistory.setValue(currentRun);
        txtStatusLog.appendText("🆕 Tạo phiên chạy mới: " + currentRun.getRunCode() + "\n");
        updateToggleRunButton();
    }
    private void handleAttachRollCode() {

        if (currentRun == null || !"Running".equalsIgnoreCase(currentRun.getStatus())) {
            txtStatusLog.appendText("⛔ Phiên chạy đã kết thúc – không thể gắn cuộn.\n");
            SoundUtils.playSound("Wrong.mp3");
            return;
        }

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

    private boolean attachRollToFeeder(String rollCode, FeederDisplayRow targetFeederRow) {
        if (currentRun == null) {
            txtStatusLog.appendText("⚠️ Vui lòng tạo phiên chạy trước khi gắn cuộn.\n");
            return false;
        }

        if (!"Running".equalsIgnoreCase(currentRun.getStatus())) {
            txtStatusLog.appendText("⛔ Phiên chạy [" + currentRun.getRunCode() + "] đã kết thúc – không thể gắn cuộn.\n");
            SoundUtils.playSound("Wrong.mp3");
            return false;
        }

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
            FxAlertUtils.warning("Vui lòng chọn phiên chạy trước.");
            return;
        }

        FeederAssignment ass = assignmentService.getAssignment(currentRun.getRunId(), row.getFeederId());
        if (ass == null) {
            // ✅ Nếu chưa có assignment cho run này, tạo mới
            ass = assignmentService.assignFeeder(currentRun.getRunId(), row.getFeederId(), "system");
        }

        List<FeederAssignmentMaterial> mats = materialAssignmentService.getMaterialsByAssignment(ass.getAssignmentId());
        if (!mats.isEmpty()) {
            FeederAssignmentMaterial last = mats.get(mats.size() - 1);
            materialAssignmentService.deleteMaterialAssignment(last.getId());

            txtStatusLog.appendText("🗑️ Đã gỡ cuộn gần nhất khỏi Feeder: " + row.getFeederCode() + "\n");
            tblFeederBySap.refresh();
        } else {
            txtStatusLog.appendText("⚠️ Feeder này chưa gắn cuộn nào trong phiên hiện tại.\n");
        }

        handleSearchFeederBySap();
        loadFeederDataByRun(currentRun);
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

    private void reloadRuns() {
        if (currentModelLine == null) return;

        List<ModelLineRun> runs = runService.getRunsByModelLineId(currentModelLine.getModelLineId());
        cbRunHistory.setItems(FXCollections.observableArrayList(runs));

        if (currentRun != null && runs.stream().anyMatch(r -> r.getRunId() == currentRun.getRunId())) {
            cbRunHistory.setValue(currentRun);
        } else if (!runs.isEmpty()) {
            cbRunHistory.setValue(runs.get(0));
            currentRun = runs.get(0);
        }
    }

    private void handleSearchFeederBySap() {

        if (currentModelLine == null || currentRun == null) {
            txtStatusLog.appendText("⚠️ Vui lòng Load model và chọn phiên chạy trước.\n");
            SoundUtils.playSound("Wrong.mp3");
            return;
        }

        String rollCodeScan = txtScanRollForSap.getText().trim();
        if (rollCodeScan.isEmpty()) return;

        Material scanned = materialService.getMaterialByRollCode(rollCodeScan);
        if (scanned == null) {
            txtStatusLog.appendText("❌ Không tìm thấy cuộn [" + rollCodeScan + "]\n");
            SoundUtils.playSound("Wrong.mp3");
            return;
        }

        // (tuỳ chọn) log vị trí cây/xe như bạn đang làm...

        // 1) Lấy feeder đúng SAP cho model+line
        List<Feeder> feeders = feederService.getFeedersByModelAndLine(
                        currentModelLine.getProductId(),
                        currentModelLine.getWarehouseId()
                ).stream().filter(f -> f.getSapCode().equalsIgnoreCase(scanned.getSapCode()))
                .toList();

        if (feeders.isEmpty()) {
            txtStatusLog.appendText("❌ Không tìm thấy Feeder nào cho SAP [" + scanned.getSapCode() + "]\n");
            return;
        }

        ObservableList<FeederDisplayRow> rows = FXCollections.observableArrayList();

        for (Feeder feeder : feeders) {
            FeederDisplayRow row = FeederDisplayRow.fromFeeder(feeder);

            // 2) Lấy assignment theo RUN hiện tại
            FeederAssignment ass = assignmentService.getAssignment(currentRun.getRunId(), feeder.getFeederId());

            if (ass == null) {
                // chưa từng gắn gì trong run này
                row.setRollCode("");          // không có cuộn đang gắn
                row.setMaterialQty(0);
                row.setStatus("Chưa gắn");
            } else {
                List<FeederAssignmentMaterial> mats =
                        materialAssignmentService.getMaterialsByAssignment(ass.getAssignmentId());

                if (mats == null || mats.isEmpty()) {
                    row.setRollCode("");
                    row.setMaterialQty(0);
                    row.setStatus("Chưa gắn");
                } else {
                    FeederAssignmentMaterial last = mats.get(mats.size() - 1);
                    Material attached = materialService.getMaterialById(last.getMaterialId());

                    row.setRollCode(attached != null ? attached.getRollCode() : "N/A");
                    row.setMaterialQty(attached != null ? attached.getQuantity() : 0);

                    // nếu feeder từng bổ sung nhiều cuộn
                    row.setStatus(mats.size() > 1 ? "Bổ sung" : "Đã gắn");
                }
            }

            rows.add(row);
        }

        tblFeederBySap.setItems(rows);

        // 3) Gợi ý UX: tự focus để bấm “Gắn”
        txtStatusLog.appendText("🔎 Tìm feeder theo SAP [" + scanned.getSapCode() + "] cho cuộn [" + rollCodeScan + "]\n");
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

    private void handleToggleRun() {
        if (currentRun == null) {
            FxAlertUtils.warning("⚠️ Không có phiên chạy nào được chọn.");
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



}
