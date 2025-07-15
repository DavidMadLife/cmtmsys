package org.chemtrovina.cmtmsys.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.dto.SelectedModelDto;
import org.chemtrovina.cmtmsys.dto.WeeklyPlanDto;
import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.repository.Impl.ProductRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.Impl.ProductionPlanRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.Impl.WarehouseRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.base.ProductRepository;
import org.chemtrovina.cmtmsys.repository.base.ProductionPlanRepository;
import org.chemtrovina.cmtmsys.repository.base.WarehouseRepository;
import org.chemtrovina.cmtmsys.service.Impl.ProductServiceImpl;
import org.chemtrovina.cmtmsys.service.Impl.ProductionPlanServiceImpl;
import org.chemtrovina.cmtmsys.service.Impl.WarehouseServiceImpl;
import org.chemtrovina.cmtmsys.service.base.ProductService;
import org.chemtrovina.cmtmsys.service.base.ProductionPlanService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.chemtrovina.cmtmsys.utils.TableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class WeeklyPlanController {

    @FXML private TextField txtSearchLine;
    @FXML private TextField txtSearchProduct;
    @FXML private DatePicker dpSearchWeek;
    @FXML private Button btnSearchPlans;
    @FXML
    private Button btnResetFilters;
    @FXML private TableView<WeeklyPlanDto> tblWeeklyPlans;

    @FXML private TableColumn<WeeklyPlanDto, String> colLine;
    @FXML private TableColumn<WeeklyPlanDto, String> colProductCode;
    @FXML private TableColumn<WeeklyPlanDto, Integer> colWeekNo;
    @FXML private TableColumn<WeeklyPlanDto, String> colFromDate;
    @FXML private TableColumn<WeeklyPlanDto, String> colToDate;
    @FXML private TableColumn<WeeklyPlanDto, Integer> colPlannedQty;
    @FXML private TableColumn<WeeklyPlanDto, Integer> colActualQty;
    @FXML private TableColumn<WeeklyPlanDto, Integer> colDiffQty;

    @FXML private ComboBox<String> cbLine;

    @FXML private DatePicker dpFromDate;
    @FXML private DatePicker dpToDate;
    @FXML private Button btnCreatePlan;

    @FXML private TextField txtModelCode;
    @FXML private TextField txtPlannedQty;
    @FXML private Button btnAddModel;
    @FXML private TableView<SelectedModelDto> tblSelectedProducts;
    @FXML private TableColumn<SelectedModelDto, String> colSelectedProductCode;
    @FXML private TableColumn<SelectedModelDto, Integer> colSelectedQty;
    @FXML private TableColumn<SelectedModelDto, Void> colRemoveAction;

    @FXML private ComboBox<ModelType> cbModelType;

    private ObservableList<SelectedModelDto> selectedProducts = FXCollections.observableArrayList();




    private final ProductionPlanService productionPlanService;
    private final WarehouseService warehouseService;
    private final ProductService productService;

    @Autowired
    public WeeklyPlanController(ProductionPlanService productionPlanService, WarehouseService warehouseService, ProductService productService) {

        this.productionPlanService = productionPlanService;
        this.warehouseService = warehouseService;
        this.productService = productService;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupEvents();
        loadComboBoxes();
        tblSelectedProducts.setItems(selectedProducts);
        setupSelectedModelTable();
        cbModelType.setItems(FXCollections.observableArrayList(ModelType.values()));
        TableUtils.centerAlignAllColumns(tblWeeklyPlans);
        TableUtils.centerAlignAllColumns(tblSelectedProducts);

    }


    private void setupTableColumns() {
        colLine.setCellValueFactory(c -> c.getValue().lineProperty());
        colProductCode.setCellValueFactory(c -> c.getValue().productCodeProperty());
        colWeekNo.setCellValueFactory(c -> c.getValue().weekNoProperty().asObject());
        colFromDate.setCellValueFactory(c -> c.getValue().fromDateProperty());
        colToDate.setCellValueFactory(c -> c.getValue().toDateProperty());
        colPlannedQty.setCellValueFactory(c -> c.getValue().plannedQtyProperty().asObject());
        colActualQty.setCellValueFactory(c -> c.getValue().actualQtyProperty().asObject());
        colDiffQty.setCellValueFactory(c -> c.getValue().diffQtyProperty().asObject());
    }

    private void setupEvents() {
        btnSearchPlans.setOnAction(e -> handleSearch());
        btnResetFilters.setOnAction(e -> {
            txtSearchLine.clear();
            txtSearchProduct.clear();
            dpSearchWeek.setValue(null);
            tblWeeklyPlans.setItems(FXCollections.emptyObservableList());
        });

        btnAddModel.setOnAction(e -> handleAddModel());
        btnCreatePlan.setOnAction(e -> handleCreatePlan());
    }

    private void loadComboBoxes() {
        List<String> warehouseNames = warehouseService.getAllWarehouses()
                .stream()
                .map(Warehouse::getName)
                .collect(Collectors.toList());
        cbLine.setItems(FXCollections.observableArrayList(warehouseNames));
    }

    private void setupSelectedModelTable() {
        colSelectedProductCode.setCellValueFactory(c -> c.getValue().modelCodeProperty());
        colSelectedQty.setCellValueFactory(c -> c.getValue().quantityProperty().asObject());

        colRemoveAction.setCellFactory(col -> {
            TableCell<SelectedModelDto, Void> cell = new TableCell<>() {
                private final Button btn = new Button("Xóa");

                {
                    btn.setOnAction(e -> {
                        SelectedModelDto item = getTableView().getItems().get(getIndex());
                        selectedProducts.remove(item);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(btn);
                    }
                }
            };
            return cell;
        });
    }


    private void handleAddModel() {
        String modelCode = txtModelCode.getText().trim();
        String qtyStr = txtPlannedQty.getText().trim();
        ModelType modelType = cbModelType.getValue();

        if (modelCode.isEmpty() || qtyStr.isEmpty() || modelType == null) {
            showAlert("Vui lòng nhập đầy đủ model, số lượng và chọn Model Type.");
            return;
        }

        if (!productService.checkProductExists(modelCode, modelType)) {
            showAlert("Mã model không tồn tại hoặc sai loại Model Type.");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyStr);
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Số lượng phải là số nguyên dương.");
            return;
        }

        selectedProducts.add(new SelectedModelDto(modelCode, qty, modelType));
        txtModelCode.clear();
        txtPlannedQty.clear();
        cbModelType.getSelectionModel().clearSelection();
    }




    private void handleSearch() {
        String line = txtSearchLine.getText().trim();
        String model = txtSearchProduct.getText().trim();

        Integer weekNo = null;
        Integer year = null;

        if (dpSearchWeek.getValue() != null) {
            LocalDate selectedDate = dpSearchWeek.getValue();
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            weekNo = selectedDate.get(weekFields.weekOfWeekBasedYear());
            year = selectedDate.getYear();
        }

        List<WeeklyPlanDto> plans = productionPlanService.searchWeeklyPlans(line, model, weekNo, year);
        tblWeeklyPlans.setItems(FXCollections.observableArrayList(plans));
    }

    private void handleCreatePlan() {
        String lineName = cbLine.getValue();

        if (lineName == null || dpFromDate.getValue() == null || dpToDate.getValue() == null || selectedProducts.isEmpty()) {
            showAlert("Vui lòng nhập đầy đủ thông tin và ít nhất một model.");
            return;
        }

        LocalDate fromDate = dpFromDate.getValue();
        LocalDate toDate = dpToDate.getValue();

        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNo = fromDate.get(weekFields.weekOfWeekBasedYear());
        int year = fromDate.getYear();

        boolean success = productionPlanService.createWeeklyPlan(lineName, selectedProducts, fromDate, toDate, weekNo, year);

        if (success) {
            showAlert("Tạo kế hoạch thành công!");
            selectedProducts.clear();
            handleSearch();
        } else {
            showAlert("Không thể tạo kế hoạch. Vui lòng kiểm tra lại.");
        }
    }




    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



}
