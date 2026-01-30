package org.chemtrovina.cmtmsys.controller.productionPlan;


import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.chemtrovina.cmtmsys.dto.SelectedModelDto;
import org.chemtrovina.cmtmsys.dto.WeeklyPlanDto;
import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.service.base.ProductionPlanService;
import org.chemtrovina.cmtmsys.service.base.ProductService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.chemtrovina.cmtmsys.utils.FxAlertUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProductionPlanWeeklySection {

    private final ProductionPlanService productionPlanService;
    private final WarehouseService warehouseService;
    private final ProductService productService;

    public ProductionPlanWeeklySection(
            ProductionPlanService productionPlanService,
            WarehouseService warehouseService,
            ProductService productService
    ) {
        this.productionPlanService = productionPlanService;
        this.warehouseService = warehouseService;
        this.productService = productService;
    }

    // ===== section state =====
    private final ObservableList<SelectedModelDto> selectedProducts = FXCollections.observableArrayList();

    // ===== refs (FXML nodes) =====
    private Refs r;

    public ObservableList<SelectedModelDto> getSelectedProducts() {
        return selectedProducts;
    }

    public void init(Refs refs) {
        this.r = Objects.requireNonNull(refs);

        setupColumns();
        setupSelectedProductsTable();
        loadLinesToCombos();
        setupActions();
        setupContextMenuDelete();
    }

    // ==========================================================
    // Setup
    // ==========================================================
    private void setupColumns() {
        // Weekly plans table
        r.colLine.setCellValueFactory(c -> c.getValue().lineProperty());
        r.colProductCode.setCellValueFactory(c -> c.getValue().productCodeProperty());
        r.colWeekNo.setCellValueFactory(c -> c.getValue().weekNoProperty().asObject());
        r.colFromDate.setCellValueFactory(c -> c.getValue().fromDateProperty());
        r.colToDate.setCellValueFactory(c -> c.getValue().toDateProperty());
        r.colPlannedQty.setCellValueFactory(c -> c.getValue().plannedQtyProperty().asObject());
        r.colActualQty.setCellValueFactory(c -> c.getValue().actualQtyProperty().asObject());
        r.colDiffQty.setCellValueFactory(c -> c.getValue().diffQtyProperty().asObject());

        r.colModelType.setCellValueFactory(c -> c.getValue().modelTypeProperty());
        r.colCompletionRate.setCellValueFactory(c ->
                new SimpleStringProperty(String.format("%.1f%%", c.getValue().getCompletionRate()))
        );

        // ModelType combo
        r.cbModelType.setItems(FXCollections.observableArrayList(ModelType.values()));
        r.cbModelType.getSelectionModel().select(ModelType.NONE);
    }

    private void setupSelectedProductsTable() {
        r.tblSelectedProducts.setItems(selectedProducts);

        r.colSelectedProductCode.setCellValueFactory(c -> c.getValue().modelCodeProperty());
        r.colSelectedModelType.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getModelType() != null ? c.getValue().getModelType().name() : "NONE"
        ));
        r.colSelectedQty.setCellValueFactory(c -> c.getValue().quantityProperty().asObject());

        r.colRemoveAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Xoá");
            {
                btn.setOnAction(e -> {
                    var item = getTableView().getItems().get(getIndex());
                    selectedProducts.remove(item);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void loadLinesToCombos() {
        List<String> warehouseNames = warehouseService.getAllWarehouses()
                .stream()
                .map(Warehouse::getName)
                .collect(Collectors.toList());

        r.cbLine.setItems(FXCollections.observableArrayList(warehouseNames));

        r.cbSearchLine.setItems(FXCollections.observableArrayList(warehouseNames));
        if (!r.cbSearchLine.getItems().contains("Tất cả")) {
            r.cbSearchLine.getItems().add(0, "Tất cả");
        }
        r.cbSearchLine.getSelectionModel().selectFirst();
    }

    private void setupActions() {
        r.btnSearchPlans.setOnAction(e -> handleSearch());
        r.btnResetFilters.setOnAction(e -> resetFilters());

        r.btnAddModel.setOnAction(e -> handleAddModel());
        r.btnCreatePlan.setOnAction(e -> handleCreatePlan());
    }

    private void setupContextMenuDelete() {
        ContextMenu cm = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Xoá kế hoạch này");
        cm.getItems().add(deleteItem);

        deleteItem.setOnAction(e -> {
            WeeklyPlanDto selected = r.tblWeeklyPlans.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận xoá");
            confirm.setHeaderText("Bạn có chắc chắn muốn xoá kế hoạch này?");
            confirm.setContentText("Line: " + selected.getLine() + "\nModel: " + selected.getProductCode());

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) return;

            int planId = productionPlanService.findPlanId(selected);
            if (planId <= 0) {
                FxAlertUtils.warning("Không tìm thấy kế hoạch để xoá.");
                return;
            }

            productionPlanService.deleteWeeklyPlan(planId);
            FxAlertUtils.info("Đã xoá kế hoạch.");
            handleSearch();

            // optional callback nếu bạn muốn Weekly báo Daily reload
            if (r.afterWeeklyChanged != null) r.afterWeeklyChanged.run();
        });

        r.tblWeeklyPlans.setRowFactory(tv -> {
            TableRow<WeeklyPlanDto> row = new TableRow<>();
            row.setContextMenu(cm);
            return row;
        });
    }

    // ==========================================================
    // Handlers
    // ==========================================================
    private void resetFilters() {
        r.cbSearchLine.getSelectionModel().selectFirst();
        r.txtSearchProduct.clear();
        r.dpSearchWeek.setValue(null);
        r.tblWeeklyPlans.setItems(FXCollections.emptyObservableList());
    }

    private void handleAddModel() {
        String modelCode = safeTrim(r.txtModelCode.getText());
        String qtyStr = safeTrim(r.txtPlannedQty.getText());
        ModelType selectedType = r.cbModelType.getValue();

        if (modelCode.isEmpty() || qtyStr.isEmpty() || selectedType == null) {
            FxAlertUtils.warning("Vui lòng nhập đầy đủ model, số lượng và chọn Model Type.");
            return;
        }

        if (!productService.checkProductExists(modelCode, selectedType)) {
            FxAlertUtils.warning("Model không tồn tại với kiểu đã chọn.");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyStr);
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            FxAlertUtils.warning("Số lượng phải > 0.");
            return;
        }

        selectedProducts.add(new SelectedModelDto(modelCode, qty, selectedType));

        r.txtModelCode.clear();
        r.txtPlannedQty.clear();
        r.cbModelType.getSelectionModel().clearSelection();
    }

    private void handleCreatePlan() {
        String line = r.cbLine.getValue();
        LocalDate from = r.dpFromDate.getValue();
        LocalDate to = r.dpToDate.getValue();

        if (line == null || from == null || to == null || selectedProducts.isEmpty()) {
            FxAlertUtils.warning("Vui lòng nhập đầy đủ thông tin và ít nhất một model.");
            return;
        }

        int weekNo = from.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        boolean success = productionPlanService.createWeeklyPlan(
                line,
                selectedProducts,
                from,
                to,
                weekNo,
                from.getYear()
        );

        if (!success) {
            FxAlertUtils.warning("Không thể tạo kế hoạch. Vui lòng kiểm tra lại.");
            return;
        }

        FxAlertUtils.info("Tạo kế hoạch thành công!");
        selectedProducts.clear();
        handleSearch();

        if (r.afterWeeklyChanged != null) r.afterWeeklyChanged.run();
    }

    private void handleSearch() {
        String line = r.cbSearchLine.getValue();
        if ("Tất cả".equalsIgnoreCase(line)) line = "";

        String model = safeTrim(r.txtSearchProduct.getText());

        Integer weekNo = null, year = null;
        if (r.dpSearchWeek.getValue() != null) {
            weekNo = r.dpSearchWeek.getValue().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
            year = r.dpSearchWeek.getValue().getYear();
        }

        var plans = productionPlanService.searchWeeklyPlans(line, model, weekNo, year);
        r.tblWeeklyPlans.setItems(FXCollections.observableArrayList(plans));
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    // ==========================================================
    // Refs holder (gom FXML nodes lại gọn)
    // ==========================================================
    public static class Refs {
        // inputs
        public TextField txtSearchProduct, txtModelCode, txtPlannedQty;
        public ComboBox<String> cbSearchLine;
        public DatePicker dpSearchWeek, dpFromDate, dpToDate;
        public ComboBox<String> cbLine;
        public ComboBox<ModelType> cbModelType;

        // buttons
        public Button btnSearchPlans, btnResetFilters, btnCreatePlan, btnAddModel;

        // tables weekly
        public TableView<WeeklyPlanDto> tblWeeklyPlans;
        public TableColumn<WeeklyPlanDto, String> colLine, colProductCode, colFromDate, colToDate;
        public TableColumn<WeeklyPlanDto, Integer> colWeekNo, colPlannedQty, colActualQty, colDiffQty;
        public TableColumn<WeeklyPlanDto, String> colModelType, colCompletionRate;

        // tables selected models
        public TableView<SelectedModelDto> tblSelectedProducts;
        public TableColumn<SelectedModelDto, String> colSelectedProductCode, colSelectedModelType;
        public TableColumn<SelectedModelDto, Integer> colSelectedQty;
        public TableColumn<SelectedModelDto, Void> colRemoveAction;

        // callback khi weekly đổi (optional)
        public Runnable afterWeeklyChanged;
    }
}
