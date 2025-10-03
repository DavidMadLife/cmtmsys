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
import org.chemtrovina.cmtmsys.dto.*;
import org.chemtrovina.cmtmsys.model.ProductionPlanDaily;
import org.chemtrovina.cmtmsys.model.ProductionPlanHourly;
import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.service.base.*;
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
public class ProductionPlanController {
    // --- Weekly Plan ---
    @FXML private TextField txtSearchProduct, txtModelCode, txtPlannedQty;
    @FXML private ComboBox<String> cbSearchLine;

    @FXML private DatePicker dpSearchWeek, dpFromDate, dpToDate;
    @FXML private Button btnSearchPlans, btnResetFilters, btnCreatePlan, btnAddModel;
    @FXML private TableView<WeeklyPlanDto> tblWeeklyPlans;
    @FXML private TableColumn<WeeklyPlanDto, String> colLine, colProductCode, colFromDate, colToDate;
    @FXML private TableColumn<WeeklyPlanDto, Integer> colWeekNo, colPlannedQty, colActualQty, colDiffQty;
    @FXML private ComboBox<String> cbLine;
    @FXML private TableView<SelectedModelDto> tblSelectedProducts;
    @FXML private TableColumn<SelectedModelDto, String> colSelectedProductCode;
    @FXML private TableColumn<SelectedModelDto, Integer> colSelectedQty;
    @FXML private TableColumn<SelectedModelDto, Void> colRemoveAction;
    @FXML
    private ComboBox<ModelType> cbModelType;



    @FXML private TableColumn<WeeklyPlanDto, String> colModelType;
    @FXML private TableColumn<WeeklyPlanDto, String> colCompletionRate;
    @FXML private TableColumn<HourlyActualRow,String> colHStage;

    @FXML private TableColumn<DailyPlanDisplayRow, String> colDailyModelType;



    private ObservableList<SelectedModelDto> selectedProducts = FXCollections.observableArrayList();
    @FXML private TableColumn<SelectedModelDto, String> colSelectedModelType;


    // --- Daily Plan ---
    @FXML private ComboBox<String> cbLineFilter;
    @FXML private DatePicker dpWeekDate;
    @FXML private Button btnLoadDailyPlans, btnSaveActuals, btnRollbackMaterial;
    @FXML private TableView<DailyPlanDisplayRow> tblDailyPlans;
    @FXML private TableColumn<DailyPlanDisplayRow, String> colDailyLine, colModel, colDailyProductCode, colType;
    @FXML private TableColumn<DailyPlanDisplayRow, Integer> colD1, colD2, colD3, colD4, colD5, colD6, colD7, colTotal;

    private final ProductionPlanService productionPlanService;
    private final WarehouseService warehouseService;
    private final ProductService productService;
    private final ProductionPlanDailyService dailyService;
    private final PcbPerformanceLogService pcbPerformanceLogService;

    private List<DailyPlanDisplayRow> masterRows = new ArrayList<>();

    private Timeline autoRefresh;
    private volatile long lastUserEventAt = System.currentTimeMillis();
    private static final long IDLE_MS = 5000;

    // Hourly pane
    @FXML private ComboBox<String> cbLineHourly;
    @FXML private DatePicker dpHourlyDate;
    @FXML private Button btnLoadHourly;
    @FXML private TableView<HourlyActualRow> tblHourly;

    @FXML private TableColumn<HourlyActualRow,String> colHLine, colHModel, colHModelType, colHProduct;
    @FXML private TableColumn<HourlyActualRow,Integer> colS1,colS2,colS3,colS4,colS5,colS6,colS7,colS8,colS9,colS10,colS11,colS12,colHTotal;


    @Autowired
    public ProductionPlanController(ProductionPlanService productionPlanService, WarehouseService warehouseService,
                                    ProductService productService, ProductionPlanDailyService dailyService, PcbPerformanceLogService pcbPerformanceLogService) {
        this.productionPlanService = productionPlanService;
        this.warehouseService = warehouseService;
        this.productService = productService;
        this.dailyService = dailyService;
        this.pcbPerformanceLogService = pcbPerformanceLogService;
    }

    @FXML
    public void initialize() {
        setupWeeklyPlan();
        setupDailyPlan();

        tblDailyPlans.getSelectionModel().setCellSelectionEnabled(true);
        tblDailyPlans.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tblWeeklyPlans.getSelectionModel().setCellSelectionEnabled(true);
        tblWeeklyPlans.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tblHourly.getSelectionModel().setCellSelectionEnabled(true);
        tblHourly.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        cbModelType.setItems(FXCollections.observableArrayList(ModelType.values()));
        cbModelType.getSelectionModel().select(ModelType.NONE); // mặc định
        setupHourlyPane();
        tblSelectedProducts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblHourly.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblWeeklyPlans.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblDailyPlans.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblWeeklyPlans.setStyle("-fx-font-size: 14px;");
        tblDailyPlans.setStyle("-fx-font-size: 14px;");
        tblHourly.setStyle("-fx-font-size: 14px;");
        tblSelectedProducts.setStyle("-fx-font-size: 14px;");

        setupIdleTrackingAndAutoRefresh();

    }

    private void setupIdleTrackingAndAutoRefresh() {
        // tick mỗi 5s
        autoRefresh = new Timeline(new KeyFrame(Duration.seconds(5), e -> maybeAutoLoadHourly()));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();

        // gắn event filter sau khi scene sẵn sàng
        javafx.application.Platform.runLater(() -> {
            var scene = tblHourly.getScene();
            if (scene == null) return;

            // Cách 1: bắt tất cả event
            EventHandler<Event> any = e -> lastUserEventAt = System.currentTimeMillis();
            scene.addEventFilter(Event.ANY, any);

            // (hoặc Cách 2: tách từng loại)
            // scene.addEventFilter(MouseEvent.ANY, e -> lastUserEventAt = System.currentTimeMillis());
            // scene.addEventFilter(KeyEvent.ANY,   e -> lastUserEventAt = System.currentTimeMillis());
            // scene.addEventFilter(ScrollEvent.ANY,e -> lastUserEventAt = System.currentTimeMillis());

            var window = scene.getWindow();
            if (window != null) window.setOnHidden(ev -> { if (autoRefresh != null) autoRefresh.stop(); });
        });
    }

    private void maybeAutoLoadHourly() {
        long idle = System.currentTimeMillis() - lastUserEventAt;
        if (idle < IDLE_MS) return;                 // còn đang thao tác
        if (tblHourly.getEditingCell() != null) return; // đang edit ô
        if (dpHourlyDate.getValue() == null) return;

        loadHourlyActuals();
    }


    private void setupWeeklyPlan() {
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
        tblWeeklyPlans.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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
                    showAlert("Đã xoá kế hoạch.");
                } else {
                    showAlert("Không tìm thấy kế hoạch để xoá.");
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
            showAlert("Vui lòng nhập đầy đủ model, số lượng và chọn Model Type.");
            return;
        }

        if (!productService.checkProductExists(modelCode, selectedType)) {
            showAlert("Model không tồn tại với kiểu đã chọn.");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyStr);
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Số lượng phải > 0.");
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
            showAlert("Vui lòng nhập đầy đủ thông tin và ít nhất một model."); return;
        }
        int weekNo = dpFromDate.getValue().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        boolean success = productionPlanService.createWeeklyPlan(line, selectedProducts, dpFromDate.getValue(), dpToDate.getValue(), weekNo, dpFromDate.getValue().getYear());
        if (success) {
            showAlert("Tạo kế hoạch thành công!"); selectedProducts.clear(); handleSearch();
        } else showAlert("Không thể tạo kế hoạch. Vui lòng kiểm tra lại.");
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
    }

    private void setupDailyPlan() {
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

        tblDailyPlans.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode().toString().equals("C")) {
                copySelectionToClipboard(tblDailyPlans);
            }
        });

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
        if (selectedDate == null) { showAlert("Vui lòng chọn ngày trong tuần."); return; }

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

                var plan = new DailyPlanDisplayRow(dto.getPlanItemId(), line, dto.getModelCode(), dto.getSapCode(),
                        "Plan", dto.getStock(),
                        dto.getDayPlan(1), dto.getDayPlan(2), dto.getDayPlan(3), dto.getDayPlan(4),
                        dto.getDayPlan(5), dto.getDayPlan(6), dto.getDayPlan(7));
                plan.setModelType(modelType);

                var actual = new DailyPlanDisplayRow(dto.getPlanItemId(), line, dto.getModelCode(), dto.getSapCode(),
                        "Actual", dto.getStock(),
                        dto.getDayActual(1), dto.getDayActual(2), dto.getDayActual(3), dto.getDayActual(4),
                        dto.getDayActual(5), dto.getDayActual(6), dto.getDayActual(7));
                actual.setModelType(modelType);

                var diff = new DailyPlanDisplayRow(dto.getPlanItemId(), line, dto.getModelCode(), dto.getSapCode(),
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
                var completionRow = new DailyPlanDisplayRow(dto.getPlanItemId(), line, dto.getModelCode(), dto.getSapCode(),
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
            showAlert("Không có dữ liệu để lưu."); return;
        }
        LocalDate monday = selectedDate.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
        var planRows = displayRows.stream().filter(r -> "Plan".equals(r.getType())).toList();
        var actualRows = displayRows.stream().filter(r -> "Actual".equals(r.getType())).toList();

        for (var planRow : planRows) {
            int weekly = dailyService.getPlannedWeeklyQuantityByPlanItemId(planRow.getPlanItemId());
            int total = planRow.totalProperty().get();
            if (total > weekly) { showAlert("Kế hoạch ngày vượt quá kế hoạch tuần của model " + planRow.getModel()); return; }
            for (int i = 0; i < 7; i++) dailyService.updateDailyPlan(planRow.getPlanItemId(), monday.plusDays(i), planRow.getDay(i));
        }

        for (var actualRow : actualRows) {
            int weekly = dailyService.getPlannedWeeklyQuantityByPlanItemId(actualRow.getPlanItemId());
            int total = actualRow.totalProperty().get();
            if (total > weekly) { showAlert("Số lượng thực tế vượt kế hoạch tuần của model " + actualRow.getModel()); return; }
            for (int i = 0; i < 7; i++) {
                var date = monday.plusDays(i);
                int actual = actualRow.getDay(i);
                dailyService.updateActual(actualRow.getPlanItemId(), date, actual);
                try { dailyService.consumeMaterialByActual(actualRow.getPlanItemId(), date, actual); }
                catch (Exception e) { showAlert("Lỗi trừ liệu: " + e.getMessage()); return; }
            }
        }

        showAlert("Đã lưu kế hoạch ngày và số lượng thực tế thành công!");
    }

    private void rollbackSelectedMaterial() {
        var selected = tblDailyPlans.getSelectionModel().getSelectedItem();
        if (selected == null || !"Actual".equals(selected.getType())) {
            showAlert("Vui lòng chọn một dòng loại 'Actual' để hoàn tác."); return;
        }
        LocalDate selectedDate = dpWeekDate.getValue();
        for (int i = 0; i < 7; i++) {
            var runDate = selectedDate.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1).plusDays(i);
            if (selected.getDay(i) > 0) {
                try { dailyService.rollbackConsumeMaterial(selected.getPlanItemId(), runDate); }
                catch (Exception e) { showAlert("Lỗi hoàn tác: " + e.getMessage()); return; }
            }
        }
        showAlert("Đã hoàn tác trừ liệu cho model: " + selected.getModel());
    }

    private void updateDayColumnHeaders(LocalDate start) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        colD1.setText(start.plusDays(0).format(fmt)); colD2.setText(start.plusDays(1).format(fmt));
        colD3.setText(start.plusDays(2).format(fmt)); colD4.setText(start.plusDays(3).format(fmt));
        colD5.setText(start.plusDays(4).format(fmt)); colD6.setText(start.plusDays(5).format(fmt));
        colD7.setText(start.plusDays(6).format(fmt));
        int weekNo = start.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        colTotal.setText("Total W" + weekNo);
    }

    private void setupHourlyPane() {
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
                    showAlert("Tổng kế hoạch giờ vượt quá kế hoạch ngày!");
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

    // Gắn style cho Stage mà KHÔNG ghi đè merge
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
            default -> { /* giữ nguyên */ }
        }
        cell.setStyle(style);
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

    private void loadHourlyActuals() {
        LocalDate day = dpHourlyDate.getValue();
        String line = cbLineHourly.getValue();

        if (day == null) {
            showAlert("Vui lòng chọn ngày.");
            return;
        }

        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        // Lines cần load
        List<String> lines;
        if (line == null || line.equalsIgnoreCase("Tất cả")) {
            lines = warehouseService.getAllWarehouses().stream().map(Warehouse::getName).toList();
        } else {
            lines = List.of(line);
        }

        // Actual logs từ AOI
        List<PcbPerformanceLogHistoryDTO> logs = new ArrayList<>();
        for (String ln : lines) {
            logs.addAll(pcbPerformanceLogService.fetchPerformanceGoodModules(ln, start, end));
        }

        // Daily map (model|line|date -> ProductionPlanDaily)
        Set<String> keysToFetch = logs.stream()
                .map(log -> log.getModelCode() + "|" + log.getWarehouseName() + "|" + log.getCreatedAt().toLocalDate())
                .collect(Collectors.toSet());
        Map<String, ProductionPlanDaily> dailyMap = dailyService.findByModelLineAndDates(keysToFetch);

        Map<String, HourlyActualRow> resultMap = new LinkedHashMap<>();

        // --- PLAN luôn hiển thị ---
        for (var entry : dailyMap.entrySet()) {
            ProductionPlanDaily daily = entry.getValue();
            if (daily == null) continue;

            String[] parts = entry.getKey().split("\\|");
            String modelCode = parts[0];
            String warehouseNm = parts[1];
            LocalDate runDate = LocalDate.parse(parts[2]);

            String baseKey = modelCode + "|" + warehouseNm;
            String fullKey = baseKey + "|Plan";

            HourlyActualRow planRow = new HourlyActualRow(
                    daily.getPlanItemID(),
                    warehouseNm,
                    modelCode,
                    modelCode,
                    daily.getModelType(),   // <-- dùng luôn từ Daily
                    new int[12],
                    "Plan",
                    "AOI",
                    runDate
            );

            // Load kế hoạch giờ nếu đã có trong DB
            List<ProductionPlanHourly> slots = dailyService.getHourlyPlansByDailyId(daily.getDailyID());
            for (ProductionPlanHourly slot : slots) {
                int idx = Math.max(0, Math.min(11, slot.getSlotIndex()));
                int q = Math.max(0, slot.getPlanQuantity());
                planRow.slotProperty(idx).set(q);
                planRow.totalProperty().set(planRow.totalProperty().get() + q);
            }

            resultMap.put(fullKey, planRow);
        }

        // --- ACTUAL từ log ---
        for (var log : logs) {
            int actual = log.getTotalModules() - log.getNgModules();
            int idx = slotIndexTwoHours(log.getCreatedAt().toLocalTime());

            String baseKey = log.getModelCode() + "|" + log.getWarehouseName();
            String fullKey = baseKey + "|Actual";

            ProductionPlanDaily daily = dailyMap.get(
                    log.getModelCode() + "|" + log.getWarehouseName() + "|" + log.getCreatedAt().toLocalDate()
            );
            int planItemId = (daily != null) ? daily.getPlanItemID() : 0;

            HourlyActualRow row = resultMap.computeIfAbsent(fullKey, k -> new HourlyActualRow(
                    planItemId,
                    log.getWarehouseName(),
                    log.getModelCode(),
                    log.getModelCode(),
                    log.getModelType().name(),
                    new int[12],
                    "Actual",
                    log.getAoi(),
                    log.getCreatedAt().toLocalDate()
            ));

            row.slotProperty(idx).set(row.slotProperty(idx).get() + Math.max(0, actual));
            row.totalProperty().set(row.totalProperty().get() + Math.max(0, actual));
        }

        // --- DIFF & COMPLETION ---
        Map<String, HourlyActualRow> finalMap = new LinkedHashMap<>(resultMap);

        for (var entry : resultMap.entrySet()) {
            String key = entry.getKey();
            if (!key.endsWith("|Plan")) continue;

            String baseKey = key.substring(0, key.length() - "|Plan".length());
            HourlyActualRow planRow = resultMap.get(key);
            HourlyActualRow actualRow = resultMap.get(baseKey + "|Actual");

            if (planRow != null && actualRow != null) {
                // Diff
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

                // Completion
                int totalPlan = planRow.totalProperty().get();
                int totalActual = actualRow.totalProperty().get();
                double rate = (totalPlan == 0) ? 0 : (totalActual * 100.0 / totalPlan);

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
                //compRow.setCompletionRate(rate);
                finalMap.put(baseKey + "|Completion", compRow);
            }
        }

        // Sort để Plan → Actual → Diff → Completion
        List<HourlyActualRow> rows = new ArrayList<>(finalMap.values());
        rows.sort(
                Comparator
                        .comparing(HourlyActualRow::getLine, Comparator.nullsLast(String::compareTo))
                        .thenComparing(HourlyActualRow::getModel, Comparator.nullsLast(String::compareTo))
                        .thenComparingInt(row -> switch (row.getStage()) {
                            case "Plan" -> 0;
                            case "Actual" -> 1;
                            case "Diff" -> 2;
                            case "Completion" -> 3;
                            default -> 9;
                        })
        );

        tblHourly.setItems(FXCollections.observableArrayList(rows));
        tblHourly.setEditable(true);
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

    private void copySelectionToClipboard(TableView<?> table) {
        StringBuilder sb = new StringBuilder();
        var positions = table.getSelectionModel().getSelectedCells();
        int prevRow = -1;
        for (var pos : positions) {
            int row = pos.getRow(), col = pos.getColumn();
            Object cell = table.getColumns().get(col).getCellData(row);
            if (cell == null) cell = "";
            if (prevRow == row) sb.append('\t');
            else if (prevRow != -1) sb.append('\n');
            sb.append(cell); prevRow = row;
        }
        final ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString());
        Clipboard.getSystemClipboard().setContent(content);
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo"); alert.setHeaderText(null);
        alert.setContentText(msg); alert.showAndWait();
    }
}