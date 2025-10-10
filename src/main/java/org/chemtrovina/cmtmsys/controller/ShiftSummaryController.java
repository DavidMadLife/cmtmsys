package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.chemtrovina.cmtmsys.dto.ShiftSummaryDTO;
import org.chemtrovina.cmtmsys.service.base.ShiftSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
public class ShiftSummaryController {

    @FXML private DatePicker dpDate;
    @FXML private ComboBox<String> cbShiftType;
    @FXML private Button btnSearch;
    @FXML private VBox vbLineCheckboxes;
    @FXML private Button btnSelectAllLines;
    @FXML private Button btnDeselectAllLines;
    @FXML private TableView<ShiftSummaryDTO> tblSummary;
    @FXML private TableColumn<ShiftSummaryDTO, String> colWarehouse;
    @FXML private TableColumn<ShiftSummaryDTO, String> colStart;
    @FXML private TableColumn<ShiftSummaryDTO, String> colEnd;

    @FXML private TableColumn<ShiftSummaryDTO, Number> colPorTime;
    @FXML private TableColumn<ShiftSummaryDTO, Number> colPorQty;
    @FXML private TableColumn<ShiftSummaryDTO, Number> colPorPercent;

    @FXML private TableColumn<ShiftSummaryDTO, Number> colTorTime;
    @FXML private TableColumn<ShiftSummaryDTO, Number> colTorQty;
    @FXML private TableColumn<ShiftSummaryDTO, Number> colTorPercent;

    @FXML private TableColumn<ShiftSummaryDTO, Number> colIdleTime;
    @FXML private TableColumn<ShiftSummaryDTO, Number> colIdleQty;
    @FXML private TableColumn<ShiftSummaryDTO, Number> colIdlePercent;

    @FXML private TableColumn<ShiftSummaryDTO, Number> colMcTime;
    @FXML private TableColumn<ShiftSummaryDTO, Number> colMcQty;
    @FXML private TableColumn<ShiftSummaryDTO, Number> colMcPercent;

    private final ShiftSummaryService summaryService;
    private Map<String, CheckBox> lineCheckboxes = new HashMap<>();

    // Danh sách các line có sẵn
    private final List<String> availableLines = Arrays.asList(
            "Line 1", "Line 2", "Line 3", "Line 4", "Line 5",
            "Line 6", "Line 7", "Line 8", "Line 9", "Line 10",
            "Line 11", "Line 12", "Line 13", "Line 14", "Line 15",
            "Line Assy"
    );

    @Autowired
    public ShiftSummaryController(ShiftSummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @FXML
    public void initialize() {
        // Khởi tạo combobox ca làm việc
        cbShiftType.setItems(FXCollections.observableArrayList("DAY", "NIGHT"));
        dpDate.setValue(LocalDate.now());

        // Khởi tạo các checkbox cho line
        initializeLineCheckboxes();

        // Thiết lập các cột cho table
        setupTableColumns();

        // Thiết lập event handlers
        setupEventHandlers();
    }

    private void initializeLineCheckboxes() {
        vbLineCheckboxes.getChildren().clear();
        lineCheckboxes.clear();

        for (String line : availableLines) {
            CheckBox checkBox = new CheckBox(line);
            checkBox.setSelected(true); // Mặc định chọn tất cả
            lineCheckboxes.put(line, checkBox);
            vbLineCheckboxes.getChildren().add(checkBox);
        }
    }

    private void setupTableColumns() {
        colWarehouse.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getWarehouseName()));
        colStart.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStartTime()));
        colEnd.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEndTime()));

        colPorTime.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getPorTimeSec()));
        colPorQty.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getPorQty()));
        colPorPercent.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPorPercent()));

        colTorTime.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getTorTimeSec()));
        colTorQty.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getTorQty()));
        colTorPercent.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getTorPercent()));

        colIdleTime.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getIdleTimeSec()));
        colIdleQty.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getIdleQty()));
        colIdlePercent.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getIdlePercent()));

        colMcTime.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getMcTimeSec()));
        colMcQty.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getMcQty()));
        colMcPercent.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getMcPercent()));
    }

    private void setupEventHandlers() {
        // Xử lý nút chọn tất cả
        btnSelectAllLines.setOnAction(e -> {
            for (CheckBox checkBox : lineCheckboxes.values()) {
                checkBox.setSelected(true);
            }
        });

        // Xử lý nút bỏ chọn tất cả
        btnDeselectAllLines.setOnAction(e -> {
            for (CheckBox checkBox : lineCheckboxes.values()) {
                checkBox.setSelected(false);
            }
        });

        // Xử lý nút search
        btnSearch.setOnAction(e -> loadSummary());
    }

    private void loadSummary() {
        LocalDate date = dpDate.getValue();
        String shiftType = cbShiftType.getValue();
        List<String> selectedLines = getSelectedLines();

        if (date == null || shiftType == null) {
            showAlert("⚠️ Vui lòng chọn ngày và ca");
            return;
        }


        // ✅ Gọi service truyền nhiều line
        List<ShiftSummaryDTO> data = summaryService.getShiftSummary(date, shiftType, selectedLines);
        tblSummary.setItems(FXCollections.observableArrayList(data));
    }

    private List<String> getSelectedLines() {
        List<String> selected = new ArrayList<>();
        for (Map.Entry<String, CheckBox> entry : lineCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                selected.add(entry.getKey());
            }
        }
        return selected;
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}