package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
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

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MaterialCartController {

    private final MaterialCartService cartService;
    private final MaterialCartTreeService treeService;
    private final MaterialService materialService;
    private final WarehouseService warehouseService;
    private final MaterialCartService materialCartService;

    @FXML private ComboBox<MaterialCart> cbCartSelect;
    @FXML private TableView<MaterialCartTree> tblTrees;
    @FXML private TableView<Material> tblMaterials;

    @FXML private TableColumn<MaterialCartTree, String> colTreeCode;
    @FXML private TableColumn<MaterialCartTree, String> colTreeCreated;
    @FXML private TableColumn<MaterialCartTree, String> colTreeLevel;


    @FXML private TableColumn<Material, String> colRollCode;
    @FXML private TableColumn<Material, String> colSapCode;
    @FXML private TableColumn<Material, String> colQuantity;
    @FXML private TableColumn<Material, String> colWarehouseName;


    @FXML private TextField txtScanTreeCode;
    @FXML private TextField txtScanRollCode;
    @FXML private TextField txtScanSapCode;
    @FXML private Button btnSearch;

    private String lastScannedRollCode = null;
    private String lastScannedSapCode = null;
    private boolean cartsLoaded = false;

    private ObservableList<MaterialCartTree> treeData = FXCollections.observableArrayList();
    private ObservableList<Material> materialData = FXCollections.observableArrayList();

    public MaterialCartController (MaterialCartService cartService, MaterialCartTreeService treeService, MaterialService materialService, WarehouseService warehouseService, MaterialCartService materialCartService) {
        this.cartService = cartService;
        this.treeService = treeService;
        this.materialService = materialService;
        this.warehouseService = warehouseService;
        this.materialCartService = materialCartService;
    }

    @FXML
    public void initialize() {
        loadCarts();
        setupSearchInputs();
        bindTreeTableColumns();
        bindMaterialTableColumns();
        setupComboBoxEvents();
        setupTableEvents();
        setupClipboardCopy();
    }


    private void setupSearchInputs() {
        bindSearchOnEnter(txtScanTreeCode);
        bindSearchOnEnter(txtScanRollCode);
        bindSearchOnEnter(txtScanSapCode);
        btnSearch.setOnAction(e -> handleSearch());
    }

    private void bindSearchOnEnter(TextField textField) {
        textField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleSearch();
            }
        });
    }

    private void bindTreeTableColumns() {
        colTreeCode.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue() != null ? data.getValue().getTreeCode() : ""));
        colTreeCreated.setCellValueFactory(data -> {
            MaterialCartTree tree = data.getValue();
            return new SimpleStringProperty(
                    tree != null && tree.getCreatedAt() != null
                            ? tree.getCreatedAt().toString()
                            : ""
            );
        });
        colTreeLevel.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue() != null ? data.getValue().getLevelNote() : ""));

        tblTrees.setItems(treeData);
    }


    private void bindMaterialTableColumns() {
        colRollCode.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue() != null ? data.getValue().getRollCode() : ""));
        colSapCode.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue() != null ? data.getValue().getSapCode() : ""));
        colQuantity.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue() != null ? String.valueOf(data.getValue().getQuantity()) : ""));

        colWarehouseName.setCellValueFactory(data -> {
            Material m = data.getValue();
            if (m == null) return new SimpleStringProperty("");
            int warehouseId = m.getWarehouseId();
            String warehouseName = warehouseService.getAllWarehouses().stream()
                    .filter(w -> w.getWarehouseId() == warehouseId)
                    .map(w -> w.getName())
                    .findFirst()
                    .orElse("N/A");
            return new SimpleStringProperty(warehouseName);
        });

        tblMaterials.setItems(materialData);

        TableColumn<Material, Void> colAction = new TableColumn<>("Action");

        colAction.setCellFactory(col -> new TableCell<>() {
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
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                Material m = getTableView().getItems().get(getIndex());
                btn.setStyle(""); // reset

                if (m.getRollCode().equalsIgnoreCase(lastScannedRollCode)) {
                    btn.setStyle("-fx-background-color: green; -fx-text-fill: white;");
                }
                else if (m.getSapCode().equalsIgnoreCase(lastScannedSapCode)) {
                    btn.setStyle("-fx-background-color: green; -fx-text-fill: white;");
                }

                setGraphic(btn);
            }
        });

        tblMaterials.getColumns().add(colAction);

    }

    private void setupComboBoxEvents() {
        cbCartSelect.setOnAction(e -> {
            MaterialCart selectedCart = cbCartSelect.getValue();
            if (selectedCart != null) {
                List<MaterialCartTree> trees = treeService.getTreesByCartId(selectedCart.getCartId());
                treeData.setAll(trees);
                materialData.clear();
            }
        });
    }

    private void setupTableEvents() {
        tblTrees.setRowFactory(tv -> {
            TableRow<MaterialCartTree> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    MaterialCartTree tree = row.getItem();
                    loadMaterialsByTree(tree.getTreeId());
                }
            });
            return row;
        });
    }

    private void setupClipboardCopy() {
        tblMaterials.getSelectionModel().setCellSelectionEnabled(true);
        tblMaterials.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tblTrees.getSelectionModel().setCellSelectionEnabled(true);
        tblTrees.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tblMaterials.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.C) {
                FxClipboardUtils.copySelectionToClipboard(tblMaterials);
            }
        });

        tblTrees.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.C) {
                FxClipboardUtils.copySelectionToClipboard(tblTrees);
            }
        });
    }


    private void loadCarts() {
        List<MaterialCart> carts = cartService.getAllCarts();
        cbCartSelect.setItems(FXCollections.observableArrayList(carts));
        // ✖ Không auto select hoặc load cây ở đây nữa
    }


    private void loadTreesBySelectedCart() {
        MaterialCart selectedCart = cbCartSelect.getValue();
        if (selectedCart == null) return;
        List<MaterialCartTree> trees = treeService.getTreesByCartId(selectedCart.getCartId());
        treeData.setAll(trees);
        materialData.clear();
    }

    private void loadMaterialsByTree(int treeId) {
        List<Material> materials = materialService.getByTreeId(treeId);
        materialData.setAll(materials);
    }

    private void handleSearch() {
        String treeCode = txtScanTreeCode.getText().trim();
        String rollCode = txtScanRollCode.getText().trim();
        String sapCode = txtScanSapCode.getText().trim();

        if (treeCode.isEmpty() && rollCode.isEmpty() && sapCode.isEmpty()) {
            loadTreesBySelectedCart();
            return;
        }

        if (!treeCode.isEmpty()) {
            MaterialCartTree tree = treeService.getTreeByCode(treeCode);
            if (tree != null) {
                MaterialCart cart = cartService.getCartById(tree.getCartId());
                if (cart != null) cbCartSelect.setValue(cart);
                treeData.setAll(List.of(tree));
            } else {
                treeData.clear();
            }
            materialData.clear();
            txtScanTreeCode.selectAll();
            return;
        }

        if (!rollCode.isEmpty()) {
            lastScannedRollCode = rollCode;
            Material material = materialService.getMaterialByRollCode(rollCode);
            if (material != null && material.getTreeId() != null) {
                MaterialCartTree tree = treeService.getById(material.getTreeId());
                if (tree != null) {
                    MaterialCart cart = cartService.getCartById(tree.getCartId());
                    if (cart != null) cbCartSelect.setValue(cart);
                    treeData.setAll(List.of(tree));
                } else {
                    treeData.clear();
                }
            } else {
                treeData.clear();
            }
            materialData.clear();
            txtScanRollCode.selectAll();
            return;
        }

        if (!sapCode.isEmpty()) {
            lastScannedSapCode = sapCode;
            List<Material> materials = materialService.findBySapCode(sapCode);
            List<Integer> treeIds = materials.stream()
                    .map(Material::getTreeId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());

            List<MaterialCartTree> trees = treeService.getByIds(treeIds); // 🔥 new method
            treeData.setAll(trees);
            materialData.clear();
            txtScanSapCode.selectAll();
        }
    }

    private void handleRemoveMaterial(Material material) {
        if (material == null || material.getTreeId() == null) {
            showAlert(Alert.AlertType.WARNING, "Không thể gỡ", "Cuộn không có trong cây.");
            return;
        }

        Integer treeId = material.getTreeId(); // lưu lại trước khi null
        material.setTreeId(null);
        materialService.updateMaterial(material);

        showAlert(Alert.AlertType.INFORMATION, "Đã gỡ", "Đã gỡ cuộn khỏi cây.");
        loadMaterialsByTree(treeId);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
