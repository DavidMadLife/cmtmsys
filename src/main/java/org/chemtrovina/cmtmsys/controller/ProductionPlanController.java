package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.converter.IntegerStringConverter;
import org.chemtrovina.cmtmsys.dto.*;
import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.service.base.*;
import org.chemtrovina.cmtmsys.utils.TableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

import static org.chemtrovina.cmtmsys.util.TableCellUtils.mergeIdenticalCells;

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

    @Autowired
    public ProductionPlanController(ProductionPlanService productionPlanService, WarehouseService warehouseService,
                                    ProductService productService, ProductionPlanDailyService dailyService) {
        this.productionPlanService = productionPlanService;
        this.warehouseService = warehouseService;
        this.productService = productService;
        this.dailyService = dailyService;
    }

    @FXML
    public void initialize() {
        setupWeeklyPlan();
        setupDailyPlan();

        tblDailyPlans.getSelectionModel().setCellSelectionEnabled(true);
        tblDailyPlans.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tblWeeklyPlans.getSelectionModel().setCellSelectionEnabled(true);
        tblWeeklyPlans.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        cbModelType.setItems(FXCollections.observableArrayList(ModelType.values()));
        cbModelType.getSelectionModel().select(ModelType.NONE); // mặc định
        TableUtils.centerAlignAllColumns(tblWeeklyPlans);
        TableUtils.centerAlignAllColumns(tblDailyPlans);
        TableUtils.centerAlignAllColumns(tblSelectedProducts);

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
        colModel.setCellFactory(mergeIdenticalCells(DailyPlanDisplayRow::getModel));
        colDailyProductCode.setCellFactory(mergeIdenticalCells(DailyPlanDisplayRow::getProductCode));
        colDailyModelType.setCellFactory(mergeIdenticalCells(DailyPlanDisplayRow::getModelType));

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

        if (selectedDate == null) {
            showAlert("Vui lòng chọn ngày trong tuần.");
            return;
        }

        int weekNo = selectedDate.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        int year = selectedDate.getYear();
        LocalDate monday = selectedDate.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
        updateDayColumnHeaders(monday);

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

                var plan = new DailyPlanDisplayRow(dto.getPlanItemId(), line, dto.getModelCode(), dto.getSapCode(), "Plan", dto.getStock(),
                        dto.getDayPlan(1), dto.getDayPlan(2), dto.getDayPlan(3), dto.getDayPlan(4), dto.getDayPlan(5), dto.getDayPlan(6), dto.getDayPlan(7));
                plan.setModelType(modelType);

                var actual = new DailyPlanDisplayRow(dto.getPlanItemId(), line, dto.getModelCode(), dto.getSapCode(), "Actual", dto.getStock(),
                        dto.getDayActual(1), dto.getDayActual(2), dto.getDayActual(3), dto.getDayActual(4), dto.getDayActual(5), dto.getDayActual(6), dto.getDayActual(7));
                actual.setModelType(modelType);

                var diff = new DailyPlanDisplayRow(dto.getPlanItemId(), line, dto.getModelCode(), dto.getSapCode(), "Diff", dto.getStock(),
                        dto.getDayActual(1) - dto.getDayPlan(1), dto.getDayActual(2) - dto.getDayPlan(2), dto.getDayActual(3) - dto.getDayPlan(3),
                        dto.getDayActual(4) - dto.getDayPlan(4), dto.getDayActual(5) - dto.getDayPlan(5), dto.getDayActual(6) - dto.getDayPlan(6), dto.getDayActual(7) - dto.getDayPlan(7));
                diff.setModelType(modelType);

                double completion = (totalPlan == 0) ? 0 : (totalActual * 100.0 / totalPlan);
                var completionRow = new DailyPlanDisplayRow(dto.getPlanItemId(), line, dto.getModelCode(), dto.getSapCode(), "Completion", 0,
                        0, 0, 0, 0, 0, 0, 0);
                completionRow.setModelType(modelType);
                completionRow.setCompletionRate(completion);

                return Arrays.asList(plan, actual, diff, completionRow).stream();
            }).toList();

            allDisplayRows.addAll(displayData);
        }

        tblDailyPlans.setItems(FXCollections.observableArrayList(allDisplayRows));
        tblDailyPlans.setEditable(true);
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