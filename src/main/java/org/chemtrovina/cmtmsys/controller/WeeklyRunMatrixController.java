package org.chemtrovina.cmtmsys.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.chemtrovina.cmtmsys.dto.WeeklyRunMatrixRow;
import org.chemtrovina.cmtmsys.model.enums.UserRole;
import org.chemtrovina.cmtmsys.security.RequiresRoles;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiresRoles({
        UserRole.ADMIN,
        UserRole.INVENTORY,
        UserRole.SUBLEEDER
})

@Component
public class WeeklyRunMatrixController {

    @FXML private ComboBox<String> cbLineFilter;
    @FXML private DatePicker dpStartOfWeek;
    @FXML private Button btnLoadMatrix;
    @FXML private TableView<WeeklyRunMatrixRow> tblRunMatrix;

    @FXML private TableColumn<WeeklyRunMatrixRow, String> colLine;
    @FXML private TableColumn<WeeklyRunMatrixRow, String> colModel;
    @FXML private TableColumn<WeeklyRunMatrixRow, String> colSapCode;
    @FXML private TableColumn<WeeklyRunMatrixRow, String> colStage;
    @FXML private TableColumn<WeeklyRunMatrixRow, Integer> colStock;
    @FXML private TableColumn<WeeklyRunMatrixRow, Integer> colTotal;

    private final ObservableList<WeeklyRunMatrixRow> matrixRows = FXCollections.observableArrayList();
    private final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("MM/dd");

    @FXML
    public void initialize() {
        setupColumns();
        btnLoadMatrix.setOnAction(e -> loadMatrix());
    }

    private void setupColumns() {
        colLine.setCellValueFactory(c -> c.getValue().lineProperty());
        colModel.setCellValueFactory(c -> c.getValue().modelProperty());
        colSapCode.setCellValueFactory(c -> c.getValue().sapCodeProperty());
        colStage.setCellValueFactory(c -> c.getValue().stageProperty());
        colStock.setCellValueFactory(c -> c.getValue().stockProperty().asObject());
        colTotal.setCellValueFactory(c -> c.getValue().totalProperty().asObject());
    }

    private void loadMatrix() {
        LocalDate startDate = dpStartOfWeek.getValue();
        if (startDate == null) {
            showAlert("Vui lòng chọn ngày bắt đầu tuần!");
            return;
        }

        matrixRows.clear();
        tblRunMatrix.getColumns().removeIf(col -> col.getText().matches("\\d{2}/\\d{2}"));

        List<LocalDate> weekDates = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weekDates.add(startDate.plusDays(i));
        }

        for (LocalDate date : weekDates) {
            String colName = dayFormatter.format(date);
            TableColumn<WeeklyRunMatrixRow, Integer> dayCol = new TableColumn<>(colName);

            dayCol.setCellValueFactory(cellData -> {
                WeeklyRunMatrixRow row = cellData.getValue();
                return row.getDayProperty(colName).asObject();
            });

            tblRunMatrix.getColumns().add(tblRunMatrix.getColumns().size() - 1, dayCol);
        }

        // Dummy data
        WeeklyRunMatrixRow row1 = new WeeklyRunMatrixRow("PBA EBOARD", "CTSO 0850 PD(1)", "H01030588C", "Plan", 0);
        WeeklyRunMatrixRow row2 = new WeeklyRunMatrixRow("PBA EBOARD", "CTSO 0850 PD(1)", "H01030588C", "Actual", 0);
        WeeklyRunMatrixRow row3 = new WeeklyRunMatrixRow("PBA EBOARD", "CTSO 0850 PD(1)", "H01030588C", "Diff", 0);

        row1.getDayProperty("06/30").set(560);
        row1.totalProperty().set(560);
        row2.getDayProperty("06/30").set(560);
        row2.totalProperty().set(560);
        row3.getDayProperty("06/30").set(0);
        row3.totalProperty().set(0);

        matrixRows.addAll(row1, row2, row3);

        // ✅ Gộp ảo các ô trùng (ẩn text nếu trùng)
        String lastKey = "";
        for (WeeklyRunMatrixRow row : matrixRows) {
            String key = row.getLine() + "|" + row.getModel() + "|" + row.getSapCode();
            if (key.equals(lastKey)) {
                row.lineProperty().set("");
                row.modelProperty().set("");
                row.sapCodeProperty().set("");
            } else {
                lastKey = key;
            }
        }

        tblRunMatrix.setItems(matrixRows);
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
