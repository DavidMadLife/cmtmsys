package org.chemtrovina.cmtmsys.controller;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import org.chemtrovina.cmtmsys.dto.PcbPerformanceLogHistoryDTO;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.service.base.PcbPerformanceLogService;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Component
public class PerformanceLogHistoryController {

    @FXML private TextField txtModelCode;
    @FXML private ComboBox<ModelType> cbModelType;
    @FXML private DatePicker dpStartDate, dpEndDate;
    @FXML private Button btnSearch;

    @FXML private TableView<PcbPerformanceLogHistoryDTO> tblLogs;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, String> colModelCode;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, String> colCarrierId;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, String> colAoi;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, Integer> colTotal;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, Integer> colNg;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, Double> colPerformance;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, String> colFileName;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, String> colWarehouse;
    @FXML private TableColumn<PcbPerformanceLogHistoryDTO, LocalDateTime> colCreatedAt;

    @FXML private LineChart<String, Number> barChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    private final PcbPerformanceLogService logService;
    private final ObservableList<PcbPerformanceLogHistoryDTO> logList = FXCollections.observableArrayList();

    @Autowired
    public PerformanceLogHistoryController(PcbPerformanceLogService logService) {
        this.logService = logService;
    }

    @FXML
    public void initialize() {
        cbModelType.setItems(FXCollections.observableArrayList(ModelType.values()));

        tblLogs.setItems(logList);

        colModelCode.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getModelCode()));
        colCarrierId.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getCarrierId()));
        colAoi.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getAoi()));
        colTotal.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTotalModules()));
        colNg.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getNgModules()));
        colPerformance.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPerformance()));
        colFileName.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getLogFileName()));
        colWarehouse.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getWarehouseName()));
        colCreatedAt.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCreatedAt()));

        btnSearch.setOnAction(e -> performSearch());

        tblLogs.getSelectionModel().setCellSelectionEnabled(true);
        tblLogs.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tblLogs.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == javafx.scene.input.KeyCode.C) {
                FxClipboardUtils.copySelectionToClipboard(tblLogs); // true = kèm header
            }
        });
    }

    private void performSearch() {
        String modelCode = txtModelCode.getText().trim();
        ModelType modelType = cbModelType.getValue();

        LocalDate start = dpStartDate.getValue();
        LocalDate end = dpEndDate.getValue();

        LocalDateTime from = (start != null) ? start.atStartOfDay() : null;
        LocalDateTime to = (end != null) ? end.atTime(LocalTime.MAX) : null;

        List<PcbPerformanceLogHistoryDTO> results = logService.searchLogs(
                modelCode.isEmpty() ? null : modelCode,
                modelType,
                from,
                to
        );

        logList.setAll(results);
        updateBarChart(results);
    }

    private void updateBarChart(List<PcbPerformanceLogHistoryDTO> results) {
        barChart.getData().clear();

        List<PcbPerformanceLogHistoryDTO> sortedResults = new ArrayList<>(results);
        sortedResults.sort(Comparator.comparing(PcbPerformanceLogHistoryDTO::getCreatedAt));


        XYChart.Series<String, Number> seriesGood = new XYChart.Series<>();
        seriesGood.setName("Good");

        XYChart.Series<String, Number> seriesNg = new XYChart.Series<>();
        seriesNg.setName("NG");

        Map<String, int[]> dataMap = new LinkedHashMap<>();
        for (PcbPerformanceLogHistoryDTO dto : sortedResults) {
            String key = dto.getCreatedAt().toLocalDate().toString();
            int[] counts = dataMap.getOrDefault(key, new int[]{0, 0});
            counts[0] += dto.getTotalModules() - dto.getNgModules(); // Good
            counts[1] += dto.getNgModules();                         // NG
            dataMap.put(key, counts);
        }


        for (Map.Entry<String, int[]> entry : dataMap.entrySet()) {
            XYChart.Data<String, Number> goodData = new XYChart.Data<>(entry.getKey(), entry.getValue()[0]);
            XYChart.Data<String, Number> ngData = new XYChart.Data<>(entry.getKey(), entry.getValue()[1]);

            goodData.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-bar-fill: #4CAF50;");
                }
            });

            ngData.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-bar-fill: #F44336;");
                }
            });

            seriesGood.getData().add(goodData);
            seriesNg.getData().add(ngData);
        }

        barChart.getData().addAll(seriesGood, seriesNg);

        // Gán màu legend sau khi BarChart render xong
        Platform.runLater(() -> {
            // Gán màu đường cho từng series
            for (Node node : barChart.lookupAll(".chart-series-line")) {
                if (node.getStyleClass().toString().contains("series0")) {
                    node.setStyle("-fx-stroke: #4CAF50;"); // xanh lá
                } else if (node.getStyleClass().toString().contains("series1")) {
                    node.setStyle("-fx-stroke: #F44336;"); // đỏ
                }
            }

            // Gán màu chú thích (legend) khớp với màu dòng
            int index = 0;
            for (Node legend : barChart.lookupAll(".chart-legend-item-symbol")) {
                String color = switch (index++) {
                    case 0 -> "#4CAF50"; // Good
                    case 1 -> "#F44336"; // NG
                    default -> "black";
                };
                legend.setStyle("-fx-background-color: " + color + ", white;");
            }
        });


    }

}
