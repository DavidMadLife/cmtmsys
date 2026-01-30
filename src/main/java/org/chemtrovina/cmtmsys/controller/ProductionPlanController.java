package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;
import org.chemtrovina.cmtmsys.controller.productionPlan.ProductionPlanDailySection;
import org.chemtrovina.cmtmsys.controller.productionPlan.ProductionPlanHourlySection;
import org.chemtrovina.cmtmsys.controller.productionPlan.ProductionPlanWeeklySection;
import org.chemtrovina.cmtmsys.dto.*;
import org.chemtrovina.cmtmsys.helper.TabDisposable;
import org.chemtrovina.cmtmsys.model.ProductionPlanDaily;
import org.chemtrovina.cmtmsys.model.ProductionPlanHourly;
import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.model.enums.UserRole;
import org.chemtrovina.cmtmsys.security.RequiresRoles;
import org.chemtrovina.cmtmsys.service.base.*;
import org.chemtrovina.cmtmsys.utils.FxAlertUtils;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.chemtrovina.cmtmsys.utils.FxFilterUtils;
import org.chemtrovina.cmtmsys.utils.TableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.util.Duration;

import static org.chemtrovina.cmtmsys.utils.TableCellUtils.mergeIdenticalCells;


@Component
@RequiresRoles({ UserRole.ADMIN, UserRole.INVENTORY, UserRole.SUBLEEDER })
public class ProductionPlanController implements TabDisposable {

    // ==========================================================
    // 1) FXML - WEEKLY PLAN
    // ==========================================================
    @FXML private TextField txtSearchProduct, txtModelCode, txtPlannedQty;
    @FXML private ComboBox<String> cbSearchLine;
    @FXML private DatePicker dpSearchWeek, dpFromDate, dpToDate;
    @FXML private Button btnSearchPlans, btnResetFilters, btnCreatePlan, btnAddModel;

    @FXML private ComboBox<String> cbLine;
    @FXML private ComboBox<ModelType> cbModelType;

    @FXML private TableView<WeeklyPlanDto> tblWeeklyPlans;
    @FXML private TableColumn<WeeklyPlanDto, String> colLine, colProductCode, colFromDate, colToDate;
    @FXML private TableColumn<WeeklyPlanDto, Integer> colWeekNo, colPlannedQty, colActualQty, colDiffQty;
    @FXML private TableColumn<WeeklyPlanDto, String> colModelType, colCompletionRate;

    @FXML private TableView<SelectedModelDto> tblSelectedProducts;
    @FXML private TableColumn<SelectedModelDto, String> colSelectedProductCode, colSelectedModelType;
    @FXML private TableColumn<SelectedModelDto, Integer> colSelectedQty;
    @FXML private TableColumn<SelectedModelDto, Void> colRemoveAction;


    // ==========================================================
    // 2) FXML - DAILY PLAN
    // ==========================================================
    @FXML private ComboBox<String> cbLineFilter;
    @FXML private DatePicker dpWeekDate;
    @FXML private Button btnLoadDailyPlans, btnSaveActuals, btnRollbackMaterial;

    @FXML private TableView<DailyPlanDisplayRow> tblDailyPlans;
    @FXML private TableColumn<DailyPlanDisplayRow, String> colDailyLine, colModel, colDailyProductCode, colType, colDailyModelType;
    @FXML private TableColumn<DailyPlanDisplayRow, Integer> colD1, colD2, colD3, colD4, colD5, colD6, colD7, colTotal;


    // ==========================================================
    // 3) FXML - HOURLY PANE
    // ==========================================================
    @FXML private ComboBox<String> cbLineHourly;
    @FXML private DatePicker dpHourlyDate;
    @FXML private Button btnLoadHourly;

    @FXML private TableView<HourlyActualRow> tblHourly;
    @FXML private TableColumn<HourlyActualRow, String> colHLine, colHModel, colHModelType, colHProduct, colHStage;
    @FXML private TableColumn<HourlyActualRow, Integer> colS1,colS2,colS3,colS4,colS5,colS6,colS7,colS8,colS9,colS10,colS11,colS12,colHTotal;


    // ==========================================================
    // 4) DEPENDENCIES (SERVICES)
    // ==========================================================
    private final ProductionPlanService productionPlanService;
    private final WarehouseService warehouseService;
    private final ProductService productService;
    private final ProductionPlanDailyService dailyService;
    private final PcbPerformanceLogService pcbPerformanceLogService;


    // ==========================================================
    // 5) STATE / DATA
    // ==========================================================
    private final ObservableList<SelectedModelDto> selectedProducts = FXCollections.observableArrayList();
    private List<DailyPlanDisplayRow> masterRows = new ArrayList<>();


    // ==========================================================
    // 6) AUTO REFRESH STATE
    // ==========================================================
    private Timeline autoRefresh;
    private volatile long lastUserEventAt = System.currentTimeMillis();
    private static final long IDLE_MS = 10_000;

    private javafx.scene.Scene trackedScene;
    private EventHandler<Event> anyEventHandler;

    // ==========================================================
    // 7) CONSTRUCTOR
    // ==========================================================
    @Autowired
    public ProductionPlanController(
            ProductionPlanService productionPlanService,
            WarehouseService warehouseService,
            ProductService productService,
            ProductionPlanDailyService dailyService,
            PcbPerformanceLogService pcbPerformanceLogService
    ) {
        this.productionPlanService = productionPlanService;
        this.warehouseService = warehouseService;
        this.productService = productService;
        this.dailyService = dailyService;
        this.pcbPerformanceLogService = pcbPerformanceLogService;
    }

    @Autowired
    private ProductionPlanWeeklySection weeklySection;
    @Autowired
    private ProductionPlanDailySection dailySection;
    @Autowired private ProductionPlanHourlySection hourlySection;



    // ==========================================================
    // 8) LIFECYCLE
    // ==========================================================
    @FXML
    public void initialize() {
        weeklySection.init(buildWeeklyRefs());
        dailySection.init(buildDailyRefs());
        hourlySection.init(buildHourlyRefs());

        setupCommonUi();
    }

    @Override
    public void onTabClose() {
        hourlySection.dispose();
    }

    // ==========================================================
    // 9) COMMON UI
    // ==========================================================
    private void setupCommonUi() {
        cbModelType.setItems(FXCollections.observableArrayList(ModelType.values()));
        cbModelType.getSelectionModel().select(ModelType.NONE);

        FxClipboardUtils.enableCopyShortcut(tblHourly);
        FxClipboardUtils.enableCopyShortcut(tblWeeklyPlans);
        FxClipboardUtils.enableCopyShortcut(tblDailyPlans);
        FxClipboardUtils.enableCopyShortcut(tblSelectedProducts);

        tblWeeklyPlans.setStyle("-fx-font-size: 14px;");
        tblDailyPlans.setStyle("-fx-font-size: 14px;");
        tblHourly.setStyle("-fx-font-size: 14px;");
        tblSelectedProducts.setStyle("-fx-font-size: 14px;");
    }

    // ==========================================================
    // 10) AUTO REFRESH (IDLE TRACKING)
    // ==========================================================
 /*   private void setupIdleTrackingAndAutoRefresh() {
        autoRefresh = new Timeline(new KeyFrame(Duration.seconds(5), e -> maybeAutoLoadHourly()));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();

        Platform.runLater(() -> attachIdleEventTracking());
    }
    private void attachIdleEventTracking() {
        var scene = tblHourly.getScene();
        if (scene == null) return;

        trackedScene = scene;

        anyEventHandler = e -> lastUserEventAt = System.currentTimeMillis();
        trackedScene.addEventFilter(Event.ANY, anyEventHandler);
    }

    private void detachIdleEventTracking() {
        if (trackedScene != null && anyEventHandler != null) {
            trackedScene.removeEventFilter(Event.ANY, anyEventHandler);
        }
        trackedScene = null;
        anyEventHandler = null;
    }

    private void stopAutoRefresh() {
        if (autoRefresh != null) {
            autoRefresh.stop();
            autoRefresh = null;
        }
    }

    private void maybeAutoLoadHourly() {
        long idle = System.currentTimeMillis() - lastUserEventAt;
        if (idle < IDLE_MS) return;
        if (tblHourly.getEditingCell() != null) return;
        if (dpHourlyDate.getValue() == null) return;

        loadHourlyActuals();
    }

*/
    public void pauseAutoRefresh() { if (autoRefresh != null) autoRefresh.pause(); }
    public void resumeAutoRefresh() { if (autoRefresh != null) autoRefresh.play(); }

    // ==========================================================
    // 11) WEEKLY PLAN
    // ==========================================================

    /*private void setupWeeklyPlan() {
        colLine.setCellValueFactory(c -> c.getValue().lineProperty());
        colProductCode.setCellValueFactory(c -> c.getValue().productCodeProperty());
        colWeekNo.setCellValueFactory(c -> c.getValue().weekNoProperty().asObject());
        colFromDate.setCellValueFactory(c -> c.getValue().fromDateProperty());
        colToDate.setCellValueFactory(c -> c.getValue().toDateProperty());
        colPlannedQty.setCellValueFactory(c -> c.getValue().plannedQtyProperty().asObject());
        colActualQty.setCellValueFactory(c -> c.getValue().actualQtyProperty().asObject());
        colDiffQty.setCellValueFactory(c -> c.getValue().diffQtyProperty().asObject());

        tblSelectedProducts.setItems(selectedProducts);
        colSelectedProductCode.setCellValueFactory(c -> c.getValue().modelCodeProperty());
        colSelectedModelType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getModelType().name()));

        colSelectedQty.setCellValueFactory(c -> c.getValue().quantityProperty().asObject());
        colRemoveAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Xoá");
            {
                btn.setOnAction(e -> selectedProducts.remove(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        List<String> warehouseNames = warehouseService.getAllWarehouses()
                .stream().map(Warehouse::getName).collect(Collectors.toList());

        cbLine.setItems(FXCollections.observableArrayList(warehouseNames));

        // Gán danh sách line cho ComboBox tìm kiếm
        cbSearchLine.setItems(FXCollections.observableArrayList(warehouseNames));
        cbSearchLine.getItems().add(0, "Tất cả");

        btnSearchPlans.setOnAction(e -> handleSearch());
        btnResetFilters.setOnAction(e -> {
            cbSearchLine.getSelectionModel().selectFirst();
            txtSearchProduct.clear();
            dpSearchWeek.setValue(null);
            tblWeeklyPlans.setItems(FXCollections.emptyObservableList());
        });
        btnAddModel.setOnAction(e -> handleAddModel());
        btnCreatePlan.setOnAction(e -> handleCreatePlan());

        colModelType.setCellValueFactory(c -> c.getValue().modelTypeProperty());
        colCompletionRate.setCellValueFactory(c -> {
            double rate = c.getValue().getCompletionRate();
            return new javafx.beans.property.SimpleStringProperty(String.format("%.1f%%", rate));
        });

        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Xoá kế hoạch này");

        deleteItem.setOnAction(e -> {
            WeeklyPlanDto selected = tblWeeklyPlans.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            // Xác nhận trước khi xoá
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận xoá");
            confirm.setHeaderText("Bạn có chắc chắn muốn xoá kế hoạch này?");
            confirm.setContentText("Line: " + selected.getLine() + "\nModel: " + selected.getProductCode());

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Gọi xóa
                int planId = productionPlanService.findPlanId(selected); // <- cần viết hàm này
                if (planId > 0) {
                    productionPlanService.deleteWeeklyPlan(planId);
                    handleSearch(); // Tải lại danh sách
                    loadDailyPlans();
                    FxAlertUtils.warning("Đã xoá kế hoạch.");
                } else {
                    FxAlertUtils.warning("Không tìm thấy kế hoạch để xoá.");
                }
            }
        });

        contextMenu.getItems().add(deleteItem);

// Gán context menu cho bảng
        tblWeeklyPlans.setRowFactory(tv -> {
            TableRow<WeeklyPlanDto> row = new TableRow<>();
            row.setContextMenu(contextMenu);
            return row;
        });

    }

    private void handleAddModel() {
        String modelCode = txtModelCode.getText().trim();
        String qtyStr = txtPlannedQty.getText().trim();
        ModelType selectedType = cbModelType.getValue();

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
        txtModelCode.clear();
        txtPlannedQty.clear();
        cbModelType.getSelectionModel().clearSelection();
    }

    private void handleCreatePlan() {
        String line = cbLine.getValue();
        if (line == null || dpFromDate.getValue() == null || dpToDate.getValue() == null || selectedProducts.isEmpty()) {
            FxAlertUtils.warning("Vui lòng nhập đầy đủ thông tin và ít nhất một model."); return;
        }
        int weekNo = dpFromDate.getValue().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        boolean success = productionPlanService.createWeeklyPlan(line, selectedProducts, dpFromDate.getValue(), dpToDate.getValue(), weekNo, dpFromDate.getValue().getYear());
        if (success) {
            FxAlertUtils.info("Tạo kế hoạch thành công!"); selectedProducts.clear(); handleSearch();
        } else FxAlertUtils.warning("Không thể tạo kế hoạch. Vui lòng kiểm tra lại.");
    }

    private void handleSearch() {
        String line = cbSearchLine.getValue();
        if ("Tất cả".equalsIgnoreCase(line)) line = "";

        String model = txtSearchProduct.getText().trim();
        Integer weekNo = null, year = null;
        if (dpSearchWeek.getValue() != null) {
            weekNo = dpSearchWeek.getValue().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
            year = dpSearchWeek.getValue().getYear();
        }
        var plans = productionPlanService.searchWeeklyPlans(line, model, weekNo, year);
        tblWeeklyPlans.setItems(FXCollections.observableArrayList(plans));
    }*/

    private ProductionPlanWeeklySection.Refs buildWeeklyRefs() {
        var refs = new ProductionPlanWeeklySection.Refs();

        refs.txtSearchProduct = txtSearchProduct;
        refs.txtModelCode = txtModelCode;
        refs.txtPlannedQty = txtPlannedQty;

        refs.cbSearchLine = cbSearchLine;
        refs.dpSearchWeek = dpSearchWeek;
        refs.dpFromDate = dpFromDate;
        refs.dpToDate = dpToDate;

        refs.btnSearchPlans = btnSearchPlans;
        refs.btnResetFilters = btnResetFilters;
        refs.btnCreatePlan = btnCreatePlan;
        refs.btnAddModel = btnAddModel;

        refs.tblWeeklyPlans = tblWeeklyPlans;
        refs.colLine = colLine;
        refs.colProductCode = colProductCode;
        refs.colFromDate = colFromDate;
        refs.colToDate = colToDate;
        refs.colWeekNo = colWeekNo;
        refs.colPlannedQty = colPlannedQty;
        refs.colActualQty = colActualQty;
        refs.colDiffQty = colDiffQty;
        refs.colModelType = colModelType;
        refs.colCompletionRate = colCompletionRate;

        refs.cbLine = cbLine;
        refs.cbModelType = cbModelType;

        refs.tblSelectedProducts = tblSelectedProducts;
        refs.colSelectedProductCode = colSelectedProductCode;
        refs.colSelectedModelType = colSelectedModelType;
        refs.colSelectedQty = colSelectedQty;
        refs.colRemoveAction = colRemoveAction;

        // weekly thay đổi -> bạn muốn daily reload thì gắn callback:
        refs.afterWeeklyChanged = () -> dailySection.loadDailyPlans();

        return refs;
    }

    // ==========================================================
    // 12) DAILY PLAN
    // ==========================================================

    /*private void setupDailyPlan() {
        List<String> lineNames = warehouseService.getAllWarehouses()
                .stream().map(Warehouse::getName).toList();

        List<String> allOptions = new ArrayList<>();
        allOptions.add("Tất cả");
        allOptions.addAll(lineNames);

        cbLineFilter.setItems(FXCollections.observableArrayList(allOptions));
        cbLineFilter.getSelectionModel().selectFirst(); // Mặc định chọn "Tất cả"


        btnLoadDailyPlans.setOnAction(e -> loadDailyPlans());
        btnSaveActuals.setOnAction(e -> {
            saveDailyPlans();      // Lưu dữ liệu actual
            loadDailyPlans();      // Nạp lại dữ liệu và tính lại % hoàn thành
        });
        btnRollbackMaterial.setOnAction(e -> rollbackSelectedMaterial());

        colType.setCellValueFactory(c -> c.getValue().typeProperty());
        colDailyLine.setCellValueFactory(c -> c.getValue().lineProperty());
        colModel.setCellValueFactory(c -> c.getValue().modelProperty());
        colDailyProductCode.setCellValueFactory(c -> c.getValue().productCodeProperty());

        setupEditableColumn(colD1, 0); setupEditableColumn(colD2, 1);
        setupEditableColumn(colD3, 2); setupEditableColumn(colD4, 3);
        setupEditableColumn(colD5, 4); setupEditableColumn(colD6, 5);
        setupEditableColumn(colD7, 6);
        colTotal.setCellValueFactory(c -> c.getValue().totalProperty().asObject());

        colDailyLine.setCellFactory(mergeIdenticalCells(DailyPlanDisplayRow::getLine));
        colModel.setCellFactory(mergeIdenticalCells(DailyPlanDisplayRow::getModel, DailyPlanDisplayRow::getLine));
        colDailyProductCode.setCellFactory(mergeIdenticalCells(DailyPlanDisplayRow::getProductCode, DailyPlanDisplayRow::getLine));
        colDailyModelType.setCellFactory(mergeIdenticalCells(DailyPlanDisplayRow::getModelType, DailyPlanDisplayRow::getLine));

        colDailyModelType.setCellValueFactory(c -> c.getValue().modelTypeProperty());

        colType.setCellFactory(column -> new TableCell<DailyPlanDisplayRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Actual" -> setStyle("-fx-text-fill: #0077cc; -fx-font-weight: bold;"); // Xanh dương
                        case "Diff" -> setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        default -> setStyle(""); // Plan hoặc khác thì để mặc định
                    }
                }
            }
        });

    }

    private void setupEditableColumn(TableColumn<DailyPlanDisplayRow, Integer> col, int dayIndex) {
        col.setCellValueFactory(c -> c.getValue().dayProperty(dayIndex).asObject());
        col.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        col.setEditable(true);
        col.setOnEditCommit(event -> {
            var row = event.getRowValue();
            int value = Optional.ofNullable(event.getNewValue()).orElse(0);
            row.dayProperty(dayIndex).set(value);
            row.totalProperty().set(row.totalProperty().get());
            LocalDate runDate = dpWeekDate.getValue()
                    .with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1)
                    .plusDays(dayIndex);
            if ("Diff".equals(row.getType())) return;
            if ("Plan".equals(row.getType())) dailyService.updateDailyPlan(row.getPlanItemId(), runDate, value);
            else if ("Actual".equals(row.getType())) dailyService.updateActual(row.getPlanItemId(), runDate, value);
            loadDailyPlans();
            handleSearch();

        });
    }

    private void loadDailyPlans() {
        String selectedLine = cbLineFilter.getValue();
        LocalDate selectedDate = dpWeekDate.getValue();
        if (selectedDate == null) { FxAlertUtils.warning("Vui lòng chọn ngày trong tuần."); return; }

        // Monday của tuần
        LocalDate monday = selectedDate.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
        updateDayColumnHeaders(monday);

        // (A) GỌI BACKFILL từ AOI (Good Modules) → cập nhật ProductionPlanDaily.actualQuantity
        dailyService.backfillActualFromPerformanceByGoodModules(
                (selectedLine != null && !"Tất cả".equalsIgnoreCase(selectedLine)) ? selectedLine : null,
                monday,
                true   // cho phép insert Daily nếu thiếu
        );

        // (B) Sau đó mới đọc dữ liệu và vẽ bảng như hiện tại
        int weekNo = selectedDate.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        int year   = selectedDate.getYear();

        List<String> linesToLoad = (selectedLine == null || selectedLine.equalsIgnoreCase("Tất cả"))
                ? productionPlanService.getLinesWithPlan(weekNo, year)
                : List.of(selectedLine);

        List<DailyPlanDisplayRow> allDisplayRows = new ArrayList<>();
        for (String line : linesToLoad) {
            var rawData = dailyService.getDailyPlanView(line, weekNo, year);
            var displayData = rawData.stream().flatMap(dto -> {
                int totalPlan = dto.getTotalPlan();
                int totalActual = dto.getTotalActual();
                String modelType = dto.getModelType();

                var plan = new DailyPlanDisplayRow(dto.getPlanItemId(), line, dto.getModelName(), dto.getSapCode(),
                        "Plan", dto.getStock(),
                        dto.getDayPlan(1), dto.getDayPlan(2), dto.getDayPlan(3), dto.getDayPlan(4),
                        dto.getDayPlan(5), dto.getDayPlan(6), dto.getDayPlan(7));
                plan.setModelType(modelType);

                var actual = new DailyPlanDisplayRow(dto.getPlanItemId(), line, dto.getModelName(), dto.getSapCode(),
                        "Actual", dto.getStock(),
                        dto.getDayActual(1), dto.getDayActual(2), dto.getDayActual(3), dto.getDayActual(4),
                        dto.getDayActual(5), dto.getDayActual(6), dto.getDayActual(7));
                actual.setModelType(modelType);

                var diff = new DailyPlanDisplayRow(dto.getPlanItemId(), line, dto.getModelName(), dto.getSapCode(),
                        "Diff", dto.getStock(),
                        dto.getDayActual(1) - dto.getDayPlan(1),
                        dto.getDayActual(2) - dto.getDayPlan(2),
                        dto.getDayActual(3) - dto.getDayPlan(3),
                        dto.getDayActual(4) - dto.getDayPlan(4),
                        dto.getDayActual(5) - dto.getDayPlan(5),
                        dto.getDayActual(6) - dto.getDayPlan(6),
                        dto.getDayActual(7) - dto.getDayPlan(7));
                diff.setModelType(modelType);

                double completion = (totalPlan == 0) ? 0 : (totalActual * 100.0 / totalPlan);
                var completionRow = new DailyPlanDisplayRow(dto.getPlanItemId(), line, dto.getModelName(), dto.getSapCode(),
                        "Completion", 0, 0,0,0,0,0,0,0);
                completionRow.setModelType(modelType);
                completionRow.setCompletionRate(completion);

                return Arrays.asList(plan, actual, diff, completionRow).stream();
            }).toList();

            allDisplayRows.addAll(displayData);
        }

        masterRows = allDisplayRows; // để filter menu hoạt động
        tblDailyPlans.setItems(FXCollections.observableArrayList(allDisplayRows));
        tblDailyPlans.setEditable(true);
        setupColumnFilters(); // như bạn đã viết
    }

    private void setupColumnFilters() {
        // hủy menu cũ (nếu util của bạn gán đè thì có thể bỏ bước này)
        // colDailyLine.setContextMenu(null); ...

        var src = masterRows;  // luôn lấy từ nguồn gốc

        FxFilterUtils.setupFilterMenu(colDailyLine, src, DailyPlanDisplayRow::getLine,
                selected -> applyGeneralFilter(selected, DailyPlanDisplayRow::getLine));

        FxFilterUtils.setupFilterMenu(colModel, src, DailyPlanDisplayRow::getModel,
                selected -> applyGeneralFilter(selected, DailyPlanDisplayRow::getModel));

        FxFilterUtils.setupFilterMenu(colDailyProductCode, src, DailyPlanDisplayRow::getProductCode,
                selected -> applyGeneralFilter(selected, DailyPlanDisplayRow::getProductCode));

        FxFilterUtils.setupFilterMenu(colDailyModelType, src, DailyPlanDisplayRow::getModelType,
                selected -> applyGeneralFilter(selected, DailyPlanDisplayRow::getModelType));

        FxFilterUtils.setupFilterMenu(colType, src, DailyPlanDisplayRow::getType,
                selected -> applyGeneralFilter(selected, DailyPlanDisplayRow::getType));
    }

    private void applyGeneralFilter(List<String> selected, java.util.function.Function<DailyPlanDisplayRow, String> extractor) {
        List<DailyPlanDisplayRow> filtered = masterRows.stream()
                .filter(row -> selected.contains(extractor.apply(row)))
                .toList();
        tblDailyPlans.setItems(FXCollections.observableArrayList(filtered));
    }

    private void saveDailyPlans() {
        var displayRows = tblDailyPlans.getItems();
        LocalDate selectedDate = dpWeekDate.getValue();
        if (selectedDate == null || displayRows.isEmpty()) {
            FxAlertUtils.warning("Không có dữ liệu để lưu."); return;
        }
        LocalDate monday = selectedDate.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
        var planRows = displayRows.stream().filter(r -> "Plan".equals(r.getType())).toList();
        var actualRows = displayRows.stream().filter(r -> "Actual".equals(r.getType())).toList();

        for (var planRow : planRows) {
            int weekly = dailyService.getPlannedWeeklyQuantityByPlanItemId(planRow.getPlanItemId());
            int total = planRow.totalProperty().get();
            if (total > weekly) { FxAlertUtils.warning("Kế hoạch ngày vượt quá kế hoạch tuần của model " + planRow.getModel()); return; }
            for (int i = 0; i < 7; i++) dailyService.updateDailyPlan(planRow.getPlanItemId(), monday.plusDays(i), planRow.getDay(i));
        }

        for (var actualRow : actualRows) {
            int weekly = dailyService.getPlannedWeeklyQuantityByPlanItemId(actualRow.getPlanItemId());
            int total = actualRow.totalProperty().get();
            if (total > weekly) { FxAlertUtils.warning("Số lượng thực tế vượt kế hoạch tuần của model " + actualRow.getModel()); return; }
            for (int i = 0; i < 7; i++) {
                var date = monday.plusDays(i);
                int actual = actualRow.getDay(i);
                dailyService.updateActual(actualRow.getPlanItemId(), date, actual);
                try { dailyService.consumeMaterialByActual(actualRow.getPlanItemId(), date, actual); }
                catch (Exception e) { FxAlertUtils.warning("Lỗi trừ liệu: " + e.getMessage()); return; }
            }
        }

        FxAlertUtils.info("Đã lưu kế hoạch ngày và số lượng thực tế thành công!");
    }

    private void rollbackSelectedMaterial() {
        var selected = tblDailyPlans.getSelectionModel().getSelectedItem();
        if (selected == null || !"Actual".equals(selected.getType())) {
            FxAlertUtils.warning("Vui lòng chọn một dòng loại 'Actual' để hoàn tác."); return;
        }
        LocalDate selectedDate = dpWeekDate.getValue();
        for (int i = 0; i < 7; i++) {
            var runDate = selectedDate.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1).plusDays(i);
            if (selected.getDay(i) > 0) {
                try { dailyService.rollbackConsumeMaterial(selected.getPlanItemId(), runDate); }
                catch (Exception e) { FxAlertUtils.warning("Lỗi hoàn tác: " + e.getMessage()); return; }
            }
        }
        FxAlertUtils.info("Đã hoàn tác trừ liệu cho model: " + selected.getModel());
    }

    private void updateDayColumnHeaders(LocalDate start) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        colD1.setText(start.plusDays(0).format(fmt)); colD2.setText(start.plusDays(1).format(fmt));
        colD3.setText(start.plusDays(2).format(fmt)); colD4.setText(start.plusDays(3).format(fmt));
        colD5.setText(start.plusDays(4).format(fmt)); colD6.setText(start.plusDays(5).format(fmt));
        colD7.setText(start.plusDays(6).format(fmt));
        int weekNo = start.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        colTotal.setText("Total W" + weekNo);
    }*/

    private ProductionPlanDailySection.Refs buildDailyRefs() {
        var refs = new ProductionPlanDailySection.Refs();

        refs.cbLineFilter = cbLineFilter;
        refs.dpWeekDate = dpWeekDate;

        refs.btnLoadDailyPlans = btnLoadDailyPlans;
        refs.btnSaveActuals = btnSaveActuals;
        refs.btnRollbackMaterial = btnRollbackMaterial;

        refs.tblDailyPlans = tblDailyPlans;

        refs.colDailyLine = colDailyLine;
        refs.colModel = colModel;
        refs.colDailyProductCode = colDailyProductCode;
        refs.colType = colType;
        refs.colDailyModelType = colDailyModelType;

        refs.colD1 = colD1;
        refs.colD2 = colD2;
        refs.colD3 = colD3;
        refs.colD4 = colD4;
        refs.colD5 = colD5;
        refs.colD6 = colD6;
        refs.colD7 = colD7;
        refs.colTotal = colTotal;

        return refs;
    }

    // ==========================================================
    // 13) HOURLY PANE
    // ==========================================================

   /* private void setupHourlyPane() {
        List<String> lineNames = warehouseService.getAllWarehouses().stream()
                .map(Warehouse::getName).toList();

        cbLineHourly.setItems(FXCollections.observableArrayList("Tất cả"));
        cbLineHourly.getItems().addAll(lineNames);
        cbLineHourly.getSelectionModel().selectFirst();

        colHLine.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLine()));
        colHModel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getModel()));
        colHProduct.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductCode()));
        colHModelType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getModelType()));
        colHStage.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStage()));

        TableColumn<HourlyActualRow, Integer>[] sCols = new TableColumn[]{
                colS1, colS2, colS3, colS4, colS5, colS6,
                colS7, colS8, colS9, colS10, colS11, colS12
        };

        String[] hourLabels = {
                "🕗 08–10", "🕙 10–12", "🕛 12–14", "🕓 14–16",
                "🕕 16–18", "🕗 18–20", "🕘 20–22", "🌙 22–00",
                "🌑 00–02", "🕑 02–04", "🕓 04–06", "🌅 06–08"
        };
        for (int i = 0; i < sCols.length; i++) {
            Label lbl = new Label(hourLabels[i]);
            lbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-alignment: center;");
            sCols[i].setGraphic(lbl);
            sCols[i].setText(null);
        }

        for (int i = 0; i < sCols.length; i++) {
            final int idx = i;
            sCols[i].setCellValueFactory(c -> c.getValue().slotProperty(idx).asObject());

            // Cho phép chỉnh sửa PLAN
            sCols[i].setCellFactory(col -> new TextFieldTableCell<>(new IntegerStringConverter()));
            sCols[i].setOnEditCommit(evt -> {
                HourlyActualRow row = evt.getRowValue();
                if (!"Plan".equals(row.getStage())) return;

                int newQty = Optional.ofNullable(evt.getNewValue()).orElse(0);
                int oldQty = row.slotProperty(idx).get();

                // Cập nhật tại chỗ
                row.slotProperty(idx).set(newQty);
                row.totalProperty().set(row.totalProperty().get() - oldQty + newQty);

                // Gọi service để lưu (xử lý validate phía dưới)
                boolean success = dailyService.updateHourlyPlanWithValidation(
                        row.getPlanItemId(),
                        idx,
                        newQty,
                        row.getRunDate()
                );

                if (!success) {
                    FxAlertUtils.warning("Tổng kế hoạch giờ vượt quá kế hoạch ngày!");
                    // rollback UI
                    row.slotProperty(idx).set(oldQty);
                    row.totalProperty().set(row.totalProperty().get());
                }
            });
        }

        tblHourly.setEditable(true);

        // Gộp theo Line
        colHLine.setCellFactory(mergeIdenticalCells(HourlyActualRow::getLine));

        // Các cột còn lại CHỈ gộp khi cùng Line
        colHModel.setCellFactory(mergeIdenticalCells(
                HourlyActualRow::getModel,
                HourlyActualRow::getLine

        ));
        colHProduct.setCellFactory(mergeIdenticalCells(
                HourlyActualRow::getProductCode,
                HourlyActualRow::getLine
        ));
        colHModelType.setCellFactory(mergeIdenticalCells(
                HourlyActualRow::getModelType,
                HourlyActualRow::getLine
        ));

        // Stage: vừa merge theo Stage + Line, vừa tô màu
        colHStage.setCellFactory(mergeStageWithStyling());

        colHTotal.setCellValueFactory(c -> c.getValue().totalProperty().asObject());

        btnLoadHourly.setOnAction(e -> loadHourlyActuals());
    }

    private void loadHourlyActuals() {
        LocalDate day = dpHourlyDate.getValue();
        String lineFilter = cbLineHourly.getValue();

        if (day == null) { FxAlertUtils.warning("Vui lòng chọn ngày."); return; }

        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end   = start.plusDays(1);

        // Lines cần load
        List<String> lines = (lineFilter == null || "Tất cả".equalsIgnoreCase(lineFilter))
                ? warehouseService.getAllWarehouses().stream().map(Warehouse::getName).toList()
                : List.of(lineFilter);

        // 1) Logs từ AOI
        List<PcbPerformanceLogHistoryDTO> logs = new ArrayList<>();
        for (String ln : lines) {
            logs.addAll(pcbPerformanceLogService.fetchPerformanceGoodModules(ln, start, end));
        }

        if (logs.isEmpty()) {
            tblHourly.setItems(FXCollections.observableArrayList());
            return;
        }

        // 2) Map (ProductCode|ModelType) -> ProductName ✅ CHẮC ĂN
        //    - key lấy từ log: modelCode + modelType
        Set<String> productKeys = logs.stream()
                .filter(l -> l.getModelCode() != null && l.getModelType() != null)
                .map(l -> l.getModelCode() + "|" + l.getModelType().name())
                .collect(Collectors.toSet());

        // ✅ Hàm mới (đúng yêu cầu productCode + modelType)
        Map<String, String> productNameMap = productService.findProductNamesByCodeAndModelType(productKeys);

        // 3) Daily map (model|line|date -> ProductionPlanDaily)
        Set<String> keysToFetch = logs.stream()
                .map(l -> l.getModelCode() + "|" + l.getWarehouseName() + "|" + l.getCreatedAt().toLocalDate())
                .collect(Collectors.toSet());

        Map<String, ProductionPlanDaily> dailyMap = dailyService.findByModelLineAndDates(keysToFetch);

        Map<String, HourlyActualRow> resultMap = new LinkedHashMap<>();

        // ===================== PLAN =====================
        for (var entry : dailyMap.entrySet()) {
            ProductionPlanDaily d = entry.getValue();
            if (d == null) continue;

            String[] parts = entry.getKey().split("\\|");
            if (parts.length < 3) continue;

            String modelCode = parts[0];
            String lineName  = parts[1];
            LocalDate runDate = LocalDate.parse(parts[2]);

            String baseKey = modelCode + "|" + lineName;

            // ✅ modelType cho PLAN: lấy từ daily (đã có plan record)
            String planModelType = (d.getModelType() != null && !d.getModelType().isBlank())
                    ? d.getModelType()
                    : "NONE";

            String productKey = modelCode + "|" + planModelType;

            // ✅ tên ưu tiên từ daily, fallback sang Products map theo (code|type)
            String modelName = (d.getProductName() != null && !d.getProductName().isBlank())
                    ? d.getProductName()
                    : productNameMap.getOrDefault(productKey, modelCode);

            HourlyActualRow planRow = new HourlyActualRow(
                    d.getPlanItemID(),
                    lineName,
                    modelName,
                    modelCode,
                    planModelType,
                    new int[12],
                    "Plan",
                    "AOI",
                    runDate
            );

            List<ProductionPlanHourly> slots = dailyService.getHourlyPlansByDailyId(d.getDailyID());
            for (ProductionPlanHourly slot : slots) {
                int idx = Math.max(0, Math.min(11, slot.getSlotIndex()));
                int q = Math.max(0, slot.getPlanQuantity());
                planRow.slotProperty(idx).set(q);
                planRow.totalProperty().set(planRow.totalProperty().get() + q);
            }

            resultMap.put(baseKey + "|Plan", planRow);
        }

        // ===================== ACTUAL =====================
        for (var log : logs) {
            int good = Math.max(0, log.getTotalModules() - log.getNgModules());
            int idx  = slotIndexTwoHours(log.getCreatedAt().toLocalTime());

            String modelCode = log.getModelCode();
            String lineName  = log.getWarehouseName();
            LocalDate runDate = log.getCreatedAt().toLocalDate();

            String baseKey = modelCode + "|" + lineName;
            String fullKey = baseKey + "|Actual";

            ProductionPlanDaily d = dailyMap.get(modelCode + "|" + lineName + "|" + runDate);

            int planItemId = (d != null) ? d.getPlanItemID() : 0;

            // ✅ modelType ưu tiên daily nếu có, không có thì lấy từ log
            String modelType = (d != null && d.getModelType() != null && !d.getModelType().isBlank())
                    ? d.getModelType()
                    : (log.getModelType() != null ? log.getModelType().name() : "NONE");

            // ✅ TÊN: lookup theo (code|type)
            String productKey = modelCode + "|" + modelType;
            String modelName = productNameMap.getOrDefault(productKey, modelCode);

            HourlyActualRow row = resultMap.computeIfAbsent(fullKey, k -> new HourlyActualRow(
                    planItemId,
                    lineName,
                    modelName,
                    modelCode,
                    modelType,
                    new int[12],
                    "Actual",
                    log.getAoi(),
                    runDate
            ));

            row.slotProperty(idx).set(row.slotProperty(idx).get() + good);
            row.totalProperty().set(row.totalProperty().get() + good);
        }

        // ===================== DIFF & COMPLETION =====================
        Map<String, HourlyActualRow> finalMap = new LinkedHashMap<>(resultMap);

        for (var entry : resultMap.entrySet()) {
            String key = entry.getKey();
            if (!key.endsWith("|Plan")) continue;

            String baseKey = key.substring(0, key.length() - "|Plan".length());
            HourlyActualRow planRow   = resultMap.get(key);
            HourlyActualRow actualRow = resultMap.get(baseKey + "|Actual");

            if (planRow != null && actualRow != null) {
                int[] diffSlots = new int[12];
                for (int i = 0; i < 12; i++) {
                    diffSlots[i] = planRow.slotProperty(i).get() - actualRow.slotProperty(i).get();
                }

                HourlyActualRow diffRow = new HourlyActualRow(
                        planRow.getPlanItemId(),
                        planRow.getLine(),
                        planRow.getModel(),
                        planRow.getProductCode(),
                        planRow.getModelType(),
                        diffSlots,
                        "Diff",
                        planRow.getStage(),
                        planRow.getRunDate()
                );
                finalMap.put(baseKey + "|Diff", diffRow);

                HourlyActualRow compRow = new HourlyActualRow(
                        planRow.getPlanItemId(),
                        planRow.getLine(),
                        planRow.getModel(),
                        planRow.getProductCode(),
                        planRow.getModelType(),
                        new int[12],
                        "Completion",
                        planRow.getStage(),
                        planRow.getRunDate()
                );
                finalMap.put(baseKey + "|Completion", compRow);
            }
        }

        // Sort
        List<HourlyActualRow> rows = new ArrayList<>(finalMap.values());
        rows.sort(
                Comparator
                        .comparing(HourlyActualRow::getLine, Comparator.nullsLast(String::compareTo))
                        .thenComparing(HourlyActualRow::getModel, Comparator.nullsLast(String::compareTo))
                        .thenComparingInt(r -> switch (r.getStage()) {
                            case "Plan" -> 0;
                            case "Actual" -> 1;
                            case "Diff" -> 2;
                            case "Completion" -> 3;
                            default -> 9;
                        })
        );

        tblHourly.setItems(FXCollections.observableArrayList(rows));
        tblHourly.setEditable(true);
    }*/

    private ProductionPlanHourlySection.Refs buildHourlyRefs() {
        var refs = new ProductionPlanHourlySection.Refs();

        refs.cbLineHourly = cbLineHourly;
        refs.dpHourlyDate = dpHourlyDate;
        refs.btnLoadHourly = btnLoadHourly;
        refs.tblHourly = tblHourly;

        refs.colHLine = colHLine;
        refs.colHModel = colHModel;
        refs.colHProduct = colHProduct;
        refs.colHModelType = colHModelType;
        refs.colHStage = colHStage;

        refs.colS1 = colS1; refs.colS2 = colS2; refs.colS3 = colS3; refs.colS4 = colS4;
        refs.colS5 = colS5; refs.colS6 = colS6; refs.colS7 = colS7; refs.colS8 = colS8;
        refs.colS9 = colS9; refs.colS10 = colS10; refs.colS11 = colS11; refs.colS12 = colS12;

        refs.colHTotal = colHTotal;

        // optional: nếu bạn add UI cho idle seconds
        // refs.idleSecondsSpinner = spIdleSeconds;
        // refs.idleSecondsSlider  = slIdleSeconds;
        // refs.idleSecondsText    = txtIdleSeconds;

        return refs;
    }

    // ==========================================================
    // 14) HELPERS
    // ==========================================================

  /*  // Gắn style cho Stage mà KHÔNG ghi đè merge
    private static void applyStageStyle(TableCell<HourlyActualRow, String> cell, String item) {
        if (item == null || cell.isEmpty()) {
            cell.setStyle("");
            return;
        }
        String style = "-fx-font-weight: bold; -fx-font-size: 16px;";
        switch (item) {
            case "Actual" -> style += " -fx-text-fill: #0077cc;";
            case "Diff" -> style += " -fx-text-fill: red;";
            case "Plan" -> style += " -fx-text-fill: black;";
            case "Completion" -> style += " -fx-text-fill: green;";
            default -> {  giữ nguyên  }
        }
        cell.setStyle(style);
    }
    // 08:00..09:59 -> 0, 10:00..11:59 -> 1, ..., 06:00..07:59 -> 11
    private int slotIndexTwoHours(LocalTime time) {
        int hour = time.getHour();
        int minute = time.getMinute();
        int totalMinutes = hour * 60 + minute;

        // Chuyển mốc 08:00 → 0, 10:00 → 1, ..., 06:00 hôm sau → 11
        int baseMinutes = 8 * 60; // 08:00
        int index = (totalMinutes - baseMinutes + 24 * 60) % (24 * 60) / 120;
        return Math.min(index, 11);
    }
    private static Callback<TableColumn<HourlyActualRow, String>, TableCell<HourlyActualRow, String>>
    mergeStageWithStyling() {
        Callback<TableColumn<HourlyActualRow, String>, TableCell<HourlyActualRow, String>> base =
                mergeIdenticalCells(HourlyActualRow::getStage, HourlyActualRow::getLine);

        return column -> {
            TableCell<HourlyActualRow, String> cell = base.call(column);

            cell.itemProperty().addListener((obs, oldV, newV) -> applyStageStyle(cell, newV));
            cell.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
                if (isEmpty) cell.setStyle("");
                else applyStageStyle(cell, cell.getItem());
            });

            Platform.runLater(() -> applyStageStyle(cell, cell.getItem()));
            return cell;
        };
    }


*/
}