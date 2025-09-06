package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.chemtrovina.cmtmsys.model.Material;
import org.chemtrovina.cmtmsys.model.MaterialCart;
import org.chemtrovina.cmtmsys.model.MaterialCartTree;
import org.chemtrovina.cmtmsys.service.base.MaterialCartService;
import org.chemtrovina.cmtmsys.service.base.MaterialCartTreeService;
import org.chemtrovina.cmtmsys.service.base.MaterialService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class MaterialCartCreateController {

    private final MaterialCartService cartService;
    private final MaterialCartTreeService treeService;
    private final MaterialService materialService;
    private final WarehouseService warehouseService;

    @FXML private ComboBox<MaterialCart> cbCartSelect;
    @FXML private ComboBox<String> cbLevelSelect;
    @FXML private Button btnCreateCart;
    @FXML private Button btnAddTree;
    @FXML private Button btnAddMaterial;
    @FXML private Button btnSaveAll;
    @FXML private Button btnClearAllTrees;
    @FXML private Button btnDeleteTreesOnCart;


    @FXML private TextField txtScanTree;
    @FXML private TextField txtScanMaterial;
    @FXML private TextField txtScanInput;


    @FXML private TableView<MaterialCartTree> tblTrees;
    @FXML private TableColumn<MaterialCartTree, String> colTreeCode;
    @FXML private TableColumn<MaterialCartTree, String> colTreeCreated;
    @FXML private TableColumn<MaterialCartTree, String> colTreeLevel;

    @FXML private TableView<Material> tblMaterials;
    @FXML private TableColumn<Material, String> colRollCode;
    @FXML private TableColumn<Material, String> colSapCode;
    @FXML private TableColumn<Material, String> colQuantity;
    @FXML private TableColumn<Material, String> colWarehouseName;

    @FXML private TextArea txtStatus;
    private boolean cartsLoaded = false;



    private ObservableList<MaterialCartTree> treeData = FXCollections.observableArrayList();
    private ObservableList<Material> materialData = FXCollections.observableArrayList();

    @Autowired
    public MaterialCartCreateController(MaterialCartService cartService,
                                        MaterialCartTreeService treeService,
                                        MaterialService materialService,
                                        WarehouseService warehouseService) {
        this.cartService = cartService;
        this.treeService = treeService;
        this.materialService = materialService;
        this.warehouseService = warehouseService;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupTableBindings();
        setupComboBox();
        setupButtonActions();
        setupTextFieldActions();
        setupContextMenus();
        setupClipboardSupport();
        loadCartList();
        setupSmartScanInput();
        setupLevelComboBox();
    }

    private void setupLevelComboBox() {
        cbLevelSelect.setItems(FXCollections.observableArrayList("A-1", "A-2", "A-3", "A-4", "A-5", "B-1", "B-2", "B-3", "B-4", "B-5"));
        cbLevelSelect.getSelectionModel().selectFirst();
    }

    private void setupTableColumns() {
        colTreeCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTreeCode()));
        colTreeCreated.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCreatedAt().toString()));
        colTreeLevel.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getLevelNote() != null ? data.getValue().getLevelNote() : ""
        ));


        colRollCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRollCode()));
        colSapCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSapCode()));
        colQuantity.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getQuantity())));
        colWarehouseName.setCellValueFactory(data -> {
            int warehouseId = data.getValue().getWarehouseId();
            String warehouseName = warehouseService.getAllWarehouses().stream()
                    .filter(w -> w.getWarehouseId() == warehouseId)
                    .map(w -> w.getName())
                    .findFirst()
                    .orElse("N/A");
            return new SimpleStringProperty(warehouseName);
        });
    }
    private void setupTableBindings() {
        tblTrees.setItems(treeData);
        tblMaterials.setItems(materialData);

        tblTrees.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadMaterialsByTree(newVal.getTreeId());
            }
        });
    }
    private void setupComboBox() {
        cbCartSelect.setOnAction(e -> {
            MaterialCart selectedCart = cbCartSelect.getValue();
            if (selectedCart != null) {
                loadTreesByCart(selectedCart.getCartId());
            }
        });
    }
    private void setupButtonActions() {
        btnCreateCart.setOnAction(e -> handleCreateCart());
        btnAddTree.setOnAction(e -> handleAddTree());
        btnAddMaterial.setOnAction(e -> handleAddMaterial());
        btnSaveAll.setOnAction(e -> handleSaveAll());
        btnClearAllTrees.setOnAction(e -> handleClearAllTrees());
        btnDeleteTreesOnCart.setOnAction(e -> handleDeleteAllTreesOnSelectedCart());
    }
    private void setupTextFieldActions() {
        txtScanTree.setOnAction(e -> handleAddTree());
        txtScanMaterial.setOnAction(e -> handleAddMaterial());
    }
    private void setupClipboardSupport() {
        tblMaterials.getSelectionModel().setCellSelectionEnabled(true);
        tblMaterials.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tblTrees.getSelectionModel().setCellSelectionEnabled(true);
        tblTrees.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tblMaterials.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode().toString().equals("C")) {
                FxClipboardUtils.copySelectionToClipboard(tblMaterials);
            }
        });
        tblTrees.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode().toString().equals("C")) {
                FxClipboardUtils.copySelectionToClipboard(tblTrees);
            }
        });
    }
    private void setupSmartScanInput() {
        txtScanInput.setOnAction(e -> handleSmartScan());
    }
    private void handleSmartScan() {
        String input = txtScanInput.getText().trim();
        if (input.isEmpty()) return;

        if (input.toUpperCase().matches("SMT-\\d{1,4}")) {
            handleSmartScanTree(input);
        }
        else if (input.toUpperCase().startsWith("M")) {
            handleSmartScanMaterial(input);
        } else if (isValidLevelCode(input.toUpperCase())) {
            cbLevelSelect.setValue(input.toUpperCase());
            showAlertToStatus(Alert.AlertType.INFORMATION, "Đã chọn tầng", "Đã chọn tầng: " + input.toUpperCase());
        } else {
            showAlertToStatus(Alert.AlertType.WARNING, "Không xác định", "Không xác định được loại mã: " + input);
        }

        txtScanInput.clear();
    }

    private void handleSmartScanTree(String treeCode) {
        MaterialCart selectedCart = cbCartSelect.getValue();
        if (selectedCart == null) return;

        String selectedLevel = cbLevelSelect.getValue();

        // ✅ Chỉ tìm cây đúng xe
        MaterialCartTree existingTree = treeService.getTreeByCartIdAndTreeCode(selectedCart.getCartId(), treeCode);
        if (existingTree != null) {
            tblTrees.getSelectionModel().select(existingTree);
            loadMaterialsByTree(existingTree.getTreeId());
            selectTreeInTable(treeCode);
            showAlertToStatus(Alert.AlertType.INFORMATION, "Đã có cây", "Cây đã tồn tại. Đã chọn lại.");
            return;
        }

        // ✅ Nếu chưa có trong xe này → thêm mới
        MaterialCartTree newTree = treeService.addTreeToCart(selectedCart.getCartId(), treeCode);
        newTree.setLevelNote(selectedLevel);
        treeService.updateTree(newTree);

        loadTreesByCart(selectedCart.getCartId());
        tblTrees.getSelectionModel().select(newTree);
        loadMaterialsByTree(newTree.getTreeId());
    }


    private void handleSmartScanMaterial(String rollCode) {
        MaterialCartTree selectedTree = tblTrees.getSelectionModel().getSelectedItem();
        if (attachMaterialToTree(rollCode, selectedTree)) {
            loadMaterialsByTree(selectedTree.getTreeId());
            tblMaterials.getSelectionModel().selectLast();
        }
    }

    private void handleDeleteAllTreesOnSelectedCart() {
        MaterialCart selectedCart = cbCartSelect.getValue();
        if (selectedCart == null) {
            showAlertToStatus(Alert.AlertType.WARNING, "Chưa chọn xe", "Vui lòng chọn một xe trước khi thực hiện xoá.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xoá toàn bộ cây trên xe");
        confirm.setHeaderText("Bạn có chắc muốn xoá toàn bộ cây và cuộn thuộc xe: " + selectedCart.getCartCode() + "?");
        confirm.setContentText("Thao tác này sẽ gỡ toàn bộ cuộn khỏi cây và xoá các cây.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            List<MaterialCartTree> trees = treeService.getTreesByCartId(selectedCart.getCartId());
            for (MaterialCartTree tree : trees) {
                List<Material> materials = materialService.getByTreeId(tree.getTreeId());
                for (Material m : materials) {
                    m.setTreeId(null);
                    materialService.updateMaterial(m);
                }
                treeService.deleteTreeById(tree.getTreeId());
            }

            loadTreesByCart(selectedCart.getCartId());
            materialData.clear();

            showAlertToStatus(Alert.AlertType.INFORMATION, "Thành công", "Đã xoá toàn bộ cây và cuộn trên xe.");
        }
    }
    private void loadCartList() {
        List<MaterialCart> carts = cartService.getAllCarts();
        cbCartSelect.setItems(FXCollections.observableArrayList(carts));

    }

    private void loadTreesByCart(int cartId) {
        List<MaterialCartTree> trees = treeService.getTreesByCartId(cartId);
        treeData.setAll(trees);
        materialData.clear();
    }

    private void loadMaterialsByTree(int treeId) {
        List<Material> materials = materialService.getByTreeId(treeId);
        materialData.setAll(materials);
    }

    private void handleCreateCart() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Tạo xe mới");
        dialog.setHeaderText("Nhập mã xe (hoặc để trống để hệ thống tự sinh):");
        dialog.setContentText("Mã xe:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(cartCode -> {
            MaterialCart newCart = cartService.createCart(cartCode.trim().isEmpty() ? null : cartCode.trim());
            loadCartList();
            cbCartSelect.setValue(newCart);
        });
    }

    private void handleAddTree() {
        String treeCode = txtScanTree.getText().trim();
        if (treeCode.isEmpty()) return;

        MaterialCart selectedCart = cbCartSelect.getValue();
        if (selectedCart == null) return;

        String selectedLevel = cbLevelSelect.getValue();

        // ✅ Chỉ tìm cây theo đúng xe (không dùng getTreeByCode nữa)
        MaterialCartTree existingTree = treeService.getTreeByCartIdAndTreeCode(selectedCart.getCartId(), treeCode);
        if (existingTree != null) {
            tblTrees.getSelectionModel().select(existingTree);
            txtScanTree.clear();
            selectTreeInTable(treeCode);
            txtScanMaterial.requestFocus();
            showAlertToStatus(Alert.AlertType.INFORMATION, "Đã có cây", "Cây đã tồn tại. Đã chọn lại.");
            return;
        }

        // ✅ Nếu chưa có cây này trong xe → thêm mới
        MaterialCartTree newTree = treeService.addTreeToCart(selectedCart.getCartId(), treeCode);
        newTree.setLevelNote(selectedLevel);
        treeService.updateTree(newTree);

        loadTreesByCart(selectedCart.getCartId());
        tblTrees.getSelectionModel().select(newTree);
        txtScanTree.clear();
        txtScanMaterial.requestFocus();
    }


    private void handleAddMaterial() {
        String rollCode = txtScanMaterial.getText().trim();
        if (rollCode.isEmpty()) return;

        MaterialCartTree selectedTree = tblTrees.getSelectionModel().getSelectedItem();
        if (attachMaterialToTree(rollCode, selectedTree)) {
            loadMaterialsByTree(selectedTree.getTreeId());
            tblMaterials.getSelectionModel().selectLast();
            txtScanMaterial.clear();
        } else {
            txtScanMaterial.clear();
        }
    }

    private void handleSaveAll() {
        // If you have something to persist explicitly, do it here
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Lưu");
        alert.setHeaderText(null);
        alert.setContentText("Dữ liệu đã được lưu thành công!");
        alert.showAndWait();
    }

    private void setupContextMenus() {
        // --- Context menu cho cuộn ---
        ContextMenu materialMenu = new ContextMenu();
        MenuItem deleteMaterialItem = new MenuItem("❌ Xoá cuộn khỏi cây");
        deleteMaterialItem.setOnAction(e -> handleRemoveMaterial());
        materialMenu.getItems().add(deleteMaterialItem);
        tblMaterials.setContextMenu(materialMenu);

        // --- Context menu cho cây ---
        ContextMenu treeMenu = new ContextMenu();
        MenuItem deleteTreeItem = new MenuItem("🪓 Xoá cây (và các cuộn)");
        deleteTreeItem.setOnAction(e -> handleDeleteTree());
        treeMenu.getItems().add(deleteTreeItem);
        tblTrees.setContextMenu(treeMenu);
    }

    private void handleRemoveMaterial() {
        Material selected = tblMaterials.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        selected.setTreeId(null); // gỡ khỏi cây
        materialService.updateMaterial(selected); // cập nhật DB

        loadMaterialsByTree(
                tblTrees.getSelectionModel().getSelectedItem().getTreeId()
        );
    }

    private void handleDeleteTree() {
        MaterialCartTree selectedTree = tblTrees.getSelectionModel().getSelectedItem();
        if (selectedTree == null) return;

        // Xác nhận
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xoá");
        confirm.setHeaderText("Bạn có chắc muốn xoá cây và tất cả cuộn trong cây?");
        confirm.setContentText("Cây: " + selectedTree.getTreeCode());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Xoá tất cả cuộn thuộc cây
            List<Material> materials = materialService.getByTreeId(selectedTree.getTreeId());
            for (Material m : materials) {
                m.setTreeId(null); // nếu muốn giữ lại cuộn nhưng xoá khỏi cây
                materialService.updateMaterial(m);
            }

            // Xoá cây
            treeService.deleteTreeById(selectedTree.getTreeId());

            // Cập nhật giao diện
            loadTreesByCart(cbCartSelect.getValue().getCartId());
        }
    }

    private void handleClearAllTrees() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xoá toàn bộ cây");
        confirm.setHeaderText("Bạn có chắc muốn xoá toàn bộ cây và gỡ các cuộn ra khỏi cây?");
        confirm.setContentText("Thao tác này không thể hoàn tác.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 1. Gỡ toàn bộ cuộn khỏi cây
            List<MaterialCartTree> allTrees = treeData; // cây đang hiển thị
            for (MaterialCartTree tree : allTrees) {
                List<Material> materials = materialService.getByTreeId(tree.getTreeId());
                for (Material m : materials) {
                    m.setTreeId(null);
                    materialService.updateMaterial(m);
                }
                treeService.deleteTreeById(tree.getTreeId());
            }

            // 2. Cập nhật lại giao diện
            treeData.clear();
            materialData.clear();
            Alert done = new Alert(Alert.AlertType.INFORMATION, "Đã xoá toàn bộ cây và gỡ cuộn thành công!");
            done.showAndWait();
        }
    }

    private boolean attachMaterialToTree(String rollCode, MaterialCartTree selectedTree) {
        if (selectedTree == null) {
            showAlertToStatus(Alert.AlertType.WARNING, "Chưa chọn cây", "Vui lòng chọn một cây trước khi thêm cuộn.");
            return false;
        }

        Material material = materialService.getMaterialByRollCode(rollCode);
        if (material == null) {
            String treeInfo = selectedTree.getTreeCode() != null
                    ? "Cây đang chọn: " + selectedTree.getTreeCode()
                    : "Tree ID: " + selectedTree.getTreeId();

            showAlertToStatus(
                    Alert.AlertType.ERROR,
                    "Không tìm thấy cuộn",
                    "Không tìm thấy cuộn có mã: " + rollCode + "\n" + treeInfo
            );
            return false;
        }

        // Đã có trong cây hiện tại → bỏ qua
        if (material.getTreeId() != null && material.getTreeId().equals(selectedTree.getTreeId())) {
            showAlertToStatus(Alert.AlertType.INFORMATION,
                    "Đã tồn tại",
                    "Cuộn này đã nằm trong cây hiện tại.");
            return false;
        }

        if (material.getTreeId() != null && material.getTreeId() != 0 &&
                !material.getTreeId().equals(selectedTree.getTreeId())) {

            // ✅ Lấy thông tin cây cũ (treeCode + cartCode)
            MaterialCartTree currentTree = treeService.getById(material.getTreeId());
            String oldTreeCode = currentTree != null ? currentTree.getTreeCode() : "???";
            String oldCartCode = "???";
            if (currentTree != null && currentTree.getCartId() != 0) {
                MaterialCart cart = cartService.getCartById(currentTree.getCartId());
                if (cart != null) {
                    oldCartCode = cart.getCartCode();
                }
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận chuyển cuộn");
            confirm.setHeaderText("Cuộn này đang nằm trong cây `" + oldTreeCode + "` thuộc xe `" + oldCartCode + "`.");
            confirm.setContentText("Bạn có muốn chuyển cuộn sang cây hiện tại không?");
            Optional<ButtonType> result = confirm.showAndWait();

            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return false;
            }
        }


        // Cập nhật lại cây mới
        material.setTreeId(selectedTree.getTreeId());
        materialService.updateMaterial(material);

        showAlertToStatus(Alert.AlertType.INFORMATION,
                "Gán thành công",
                "Đã gán cuộn `" + rollCode + "` vào cây `" +
                        (selectedTree.getTreeCode() != null ? selectedTree.getTreeCode() : selectedTree.getTreeId()) + "`.");

        return true;
    }


    private boolean isValidLevelCode(String code) {
        return cbLevelSelect.getItems().contains(code);
    }


    private void showAlertToStatus(Alert.AlertType type, String title, String message) {
        String prefix = switch (type) {
            case INFORMATION -> "✅ ";
            case WARNING -> "⚠️ ";
            case ERROR -> "❌ ";
            default -> "";
        };

        // Chèn nội dung
        txtStatus.appendText(prefix + message + "\n");

        String backgroundColor = switch (type) {
            case INFORMATION -> "#4CAF50"; // ✅ Xanh lá tươi, Material Green 500
            case WARNING     -> "#FF9800"; // ⚠️ Cam tươi, Material Orange 500
            case ERROR       -> "#F44336"; // ❌ Đỏ tươi, Material Red 500
            default          -> "white";
        };



        txtStatus.setStyle("-fx-control-inner-background: " + backgroundColor + "; -fx-font-size: 20px;");
    }

    private void selectTreeInTable(String treeCode) {
        for (MaterialCartTree tree : treeData) {
            if (tree.getTreeCode().equalsIgnoreCase(treeCode)) {
                tblTrees.getSelectionModel().select(tree);
                tblTrees.scrollTo(tree);
                loadMaterialsByTree(tree.getTreeId());
                break;
            }
        }
    }

}
