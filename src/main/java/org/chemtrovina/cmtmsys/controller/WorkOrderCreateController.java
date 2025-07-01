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
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.model.WorkOrder;
import org.chemtrovina.cmtmsys.model.WorkOrderItem;
import org.chemtrovina.cmtmsys.repository.Impl.*;
import org.chemtrovina.cmtmsys.service.Impl.ProductServiceImpl;
import org.chemtrovina.cmtmsys.service.Impl.WorkOrderServiceImpl;
import org.chemtrovina.cmtmsys.service.base.ProductService;
import org.chemtrovina.cmtmsys.service.base.WorkOrderService;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;

public class WorkOrderCreateController {

    @FXML private TextField txtDescription;
    @FXML private TableView<Pair<Product, Integer>> tblItems;
    @FXML private TableColumn<Pair<Product, Integer>, String> colProduct;
    @FXML private TableColumn<Pair<Product, Integer>, Integer> colQuantity;
    @FXML private Button btnAddItem, btnRemoveItem, btnCreate;
    @FXML private TableColumn<Pair<Product, Integer>, String> colDescription;
    private final ObservableList<Pair<Product, Integer>> itemList = FXCollections.observableArrayList();
    private WorkOrderService workOrderService;
    private ProductService productService;
    private Integer editingWorkOrderId = null;


    @FXML
    public void initialize() {
        initServices();
        setupTable();
        setupActions();
        tblItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }


    private void addProductDialog() {
        Dialog<Pair<Product, Integer>> dialog = new Dialog<>();
        dialog.setTitle("Nhập sản phẩm");

        TextField txtCode = new TextField();
        txtCode.setPromptText("Nhập mã sản phẩm (ProductCode)");
        txtCode.setPrefWidth(250);

        TextField txtQty = new TextField();
        txtQty.setPromptText("Số lượng");

        VBox content = new VBox(10, new Label("Mã sản phẩm:"), txtCode, new Label("Số lượng:"), txtQty);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK && !txtCode.getText().isBlank() && !txtQty.getText().isBlank()) {
                try {
                    String code = txtCode.getText().trim();
                    int qty = Integer.parseInt(txtQty.getText().trim());

                    Product product = productService.getProductByCode(code); // <-- dùng service lấy product

                    if (product != null) {
                        return new Pair<>(product, qty);
                    } else {
                        showAlert("❌ Không tìm thấy sản phẩm với mã: " + code);
                    }

                } catch (NumberFormatException ex) {
                    showAlert("❌ Số lượng không hợp lệ!");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> itemList.add(result));
    }


    private void initServices() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getDataSource());
        this.productService = new ProductServiceImpl(new ProductRepositoryImpl(jdbcTemplate));
        this.workOrderService = new WorkOrderServiceImpl(
                new WorkOrderRepositoryImpl(jdbcTemplate),
                jdbcTemplate,
                new WorkOrderItemRepositoryImpl(jdbcTemplate),
                new WarehouseTransferRepositoryImpl(jdbcTemplate),
                new WarehouseTransferDetailRepositoryImpl(jdbcTemplate)
        );
    }
    private void setupTable() {
        tblItems.setItems(itemList);
        colProduct.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getKey().getProductCode()));
        colDescription.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getKey().getDescription()));
        colQuantity.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getValue()).asObject());
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

        if (editingWorkOrderId != null) {
            workOrderService.updateWorkOrderWithItems(editingWorkOrderId, desc, productMap);
            showAlert("Cập nhật Work Order thành công!");
        } else {
            workOrderService.createWorkOrderWithItems(desc, productMap);
            showAlert("Tạo Work Order thành công!");
        }

        txtDescription.clear();
        itemList.clear();
        editingWorkOrderId = null;
        btnCreate.setText("Tạo Work Order");
        ((Stage) btnCreate.getScene().getWindow()).close();
    }

    public void loadWorkOrder(WorkOrder workOrder) {
        this.editingWorkOrderId = workOrder.getWorkOrderId();
        txtDescription.setText(workOrder.getDescription());

        Map<Product, Integer> productMap = workOrderService.getWorkOrderItems(workOrder.getWorkOrderId());
        itemList.clear();
        productMap.forEach((product, quantity) -> itemList.add(new javafx.util.Pair<>(product, quantity)));

        btnCreate.setText("Cập nhật Work Order");
    }


    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
