package org.chemtrovina.cmtmsys.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.chemtrovina.cmtmsys.dto.ShiftSummaryDTO;
import org.chemtrovina.cmtmsys.service.base.ShiftSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ShiftSummaryController {

    @FXML private DatePicker dpDate;
    @FXML private ComboBox<String> cbShiftType;
    @FXML private Button btnSearch;
    @FXML private ListView<String> lvLines;
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

    @FXML private TableColumn<ShiftSummaryDTO, String> colIdleStart;
    @FXML private TableColumn<ShiftSummaryDTO, Number> colIdleQty;
    @FXML private TableColumn<ShiftSummaryDTO, Number> colIdleTime;

    @FXML private TableColumn<ShiftSummaryDTO, String> colMcStart;
    @FXML private TableColumn<ShiftSummaryDTO, Number> colMcQty;

    private final ShiftSummaryService summaryService;

    @Autowired
    public ShiftSummaryController(ShiftSummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @FXML
    public void initialize() {
        cbShiftType.setItems(FXCollections.observableArrayList("DAY", "NIGHT"));
        dpDate.setValue(LocalDate.now());

        lvLines.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lvLines.setItems(FXCollections.observableArrayList("Line 1", "Line 2", "Line 3", "Line 4", "Line 5", "Line 6", "Line 7", "Line 8", "Line 9", "Line 10", "Line 11", "Line 12", "Line 13", "Line 14", "Line 15", "Line Assy"));

        colWarehouse.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getWarehouseName()));
        colStart.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStartTime()));
        colEnd.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEndTime()));

        colPorTime.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getPorTimeSec()));
        colPorQty.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getPorQty()));
        colPorPercent.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getPorPercent()));

        colTorTime.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getTorTimeSec()));
        colTorQty.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getTorQty()));
        colTorPercent.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getTorPercent()));

        colIdleStart.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getIdleStart()));
        colIdleQty.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getIdleQty()));
        colIdleTime.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getIdleTimeSec()));

        colMcStart.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMcStart()));
        colMcQty.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getMcQty()));

        btnSearch.setOnAction(e -> loadSummary());
    }

    private void loadSummary() {
        LocalDate date = dpDate.getValue();
        String shiftType = cbShiftType.getValue();
        List<String> selectedLines = lvLines.getSelectionModel().getSelectedItems();

        if (date == null || shiftType == null) {
            showAlert("⚠️ Vui lòng chọn ngày và ca");
            return;
        }

        if (date == null || shiftType == null) {
            showAlert("⚠️ Vui lòng chọn ngày và ca");
            return;
        }
        if (selectedLines.isEmpty()) {
            showAlert("⚠️ Vui lòng chọn ít nhất 1 line");
            return;
        }

        // ✅ Gọi service truyền nhiều line
        List<ShiftSummaryDTO> data = summaryService.getShiftSummary(date, shiftType, selectedLines);
        tblSummary.setItems(FXCollections.observableArrayList(data));
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
