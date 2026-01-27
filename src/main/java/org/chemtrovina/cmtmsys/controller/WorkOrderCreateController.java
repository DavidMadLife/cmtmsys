package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.model.WorkOrder;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.model.enums.UserRole;
import org.chemtrovina.cmtmsys.security.RequiresRoles;
import org.chemtrovina.cmtmsys.service.base.ProductService;
import org.chemtrovina.cmtmsys.service.base.WorkOrderService;
import org.chemtrovina.cmtmsys.utils.AutoCompleteUtils;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.chemtrovina.cmtmsys.utils.TableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiresRoles({
        UserRole.ADMIN,
        UserRole.INVENTORY,
        UserRole.SUBLEEDER
})

@Component
public class WorkOrderCreateController {

    @FXML private TextField txtDescription;
    @FXML private TableView<Pair<Product, Integer>> tblItems;
    @FXML private TableColumn<Pair<Product, Integer>, String> colProduct;
    @FXML private TableColumn<Pair<Product, Integer>, String> colDescription;
    @FXML private TableColumn<Pair<Product, Integer>, Integer> colQuantity;
    @FXML private TableColumn<Pair<Product, Integer>, String> colModelType;

    @FXML private Button btnAddItem, btnRemoveItem, btnCreate;


    private final ObservableList<Pair<Product, Integer>> itemList = FXCollections.observableArrayList();
    private Integer editingWorkOrderId = null;

    private final WorkOrderService workOrderService;
    private final ProductService productService;

    @Autowired
    public WorkOrderCreateController(WorkOrderService workOrderService, ProductService productService) {
        this.workOrderService = workOrderService;
        this.productService = productService;
    }

    @FXML
    public void initialize() {
        setupTable();
        setupActions();
        FxClipboardUtils.enableCopyShortcut(tblItems);
        TableUtils.centerAlignAllColumns(tblItems);
    }

    private void setupTable() {
        tblItems.setItems(itemList);
        colProduct.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getKey().getProductCode()));
        colDescription.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getKey().getDescription()));
        colQuantity.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getValue()).asObject());
        colModelType.setCellValueFactory(cell -> {
            var modelType = cell.getValue().getKey().getModelType();
            return new SimpleStringProperty(modelType != null ? modelType.name() : "");
        });

    }

    private void setupActions() {
        btnAddItem.setOnAction(e -> addProductDialog());
        btnRemoveItem.setOnAction(e -> {
            var selected = tblItems.getSelectionModel().getSelectedItem();
            if (selected != null) {
                itemList.remove(selected);
            }
        });
        btnCreate.setOnAction(e -> handleCreateWorkOrder());
    }

    private void addProductDialog() {
        Dialog<Pair<Product, Integer>> dialog = new Dialog<>();
        dialog.setTitle("Thêm sản phẩm vào Work Order");

        // ====== Trường nhập mã & tên ======
        TextField txtCode = new TextField();
        txtCode.setPromptText("Nhập mã sản phẩm (Product Code)");
        txtCode.setPrefWidth(250);

        TextField txtName = new TextField();
        txtName.setPromptText("Nhập tên sản phẩm (Product Name)");
        txtName.setPrefWidth(250);

        TextField txtQty = new TextField();
        txtQty.setPromptText("Số lượng");

        ComboBox<ModelType> cbType = new ComboBox<>();
        cbType.setItems(FXCollections.observableArrayList(ModelType.values()));
        cbType.setPromptText("Chọn Model Type");

        // ====== Gợi ý AutoComplete ======
        List<Product> allProducts = productService.getAllProducts();

        List<String> productCodes = allProducts.stream()
                .map(Product::getProductCode)
                .filter(code -> code != null && !code.isBlank())
                .distinct()
                .toList();

        List<String> productNames = allProducts.stream()
                .map(Product::getName)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .toList();

        AutoCompleteUtils.setupAutoComplete(txtCode, productCodes);
        AutoCompleteUtils.setupAutoComplete(txtName, productNames);

        // Khi người dùng chọn mã → tự động điền tên
        txtCode.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isBlank()) {
                Product found = allProducts.stream()
                        .filter(p -> newVal.equalsIgnoreCase(p.getProductCode()))
                        .findFirst()
                        .orElse(null);
                if (found != null) txtName.setText(found.getName());
            }
        });

        // Khi người dùng chọn tên → tự động điền mã
        txtName.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isBlank()) {
                Product found = allProducts.stream()
                        .filter(p -> newVal.equalsIgnoreCase(p.getName()))
                        .findFirst()
                        .orElse(null);
                if (found != null) txtCode.setText(found.getProductCode());
            }
        });

        VBox content = new VBox(10,
                new Label("Mã sản phẩm:"), txtCode,
                new Label("Tên sản phẩm:"), txtName,
                new Label("Loại sản phẩm (Model Type):"), cbType,
                new Label("Số lượng:"), txtQty
        );
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK &&
                    !txtCode.getText().isBlank() &&
                    !txtQty.getText().isBlank() &&
                    cbType.getValue() != null) {
                try {
                    String code = txtCode.getText().trim();
                    int qty = Integer.parseInt(txtQty.getText().trim());
                    ModelType type = cbType.getValue();

                    Product product = productService.getProductByCodeAndType(code, type);
                    if (product != null) {
                        return new Pair<>(product, qty);
                    } else {
                        showAlert("❌ Không tìm thấy sản phẩm với mã: " + code + " và loại: " + type.name());
                    }
                } catch (NumberFormatException ex) {
                    showAlert("❌ Số lượng không hợp lệ!");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> itemList.add(result));
    }



    private void handleCreateWorkOrder() {
        if (itemList.isEmpty()) {
            showAlert("Chưa có thành phẩm nào được chọn!");
            return;
        }

        Map<Integer, Integer> productMap = new HashMap<>();
        for (var pair : itemList) {
            productMap.put(pair.getKey().getProductId(), pair.getValue());
        }

        String desc = txtDescription.getText();

        try {
            if (editingWorkOrderId != null) {
                workOrderService.updateWorkOrderWithItems(editingWorkOrderId, desc, productMap);
                showAlert("Cập nhật Work Order thành công!");
            } else {
                workOrderService.createWorkOrderWithItems(desc, productMap);
                showAlert("Tạo Work Order thành công!");
            }

            ((Stage) btnCreate.getScene().getWindow()).close();

        } catch (RuntimeException ex) {
            showAlert(ex.getMessage());
        }


        /*txtDescription.clear();
        itemList.clear();
        editingWorkOrderId = null;
        btnCreate.setText("Tạo Work Order");
        //((Stage) btnCreate.getScene().getWindow()).close();*/
    }

    public void loadWorkOrder(WorkOrder workOrder) {
        this.editingWorkOrderId = workOrder.getWorkOrderId();
        txtDescription.setText(workOrder.getDescription());

        Map<Product, Integer> productMap = workOrderService.getWorkOrderItems(workOrder.getWorkOrderId());
        itemList.clear();
        productMap.forEach((product, quantity) -> itemList.add(new Pair<>(product, quantity)));

        btnCreate.setText("Cập nhật Work Order");
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
