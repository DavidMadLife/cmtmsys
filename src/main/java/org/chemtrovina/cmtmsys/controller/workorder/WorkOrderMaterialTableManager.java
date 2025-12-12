package org.chemtrovina.cmtmsys.controller.workorder;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.chemtrovina.cmtmsys.dto.MaterialRequirementDto;
import org.chemtrovina.cmtmsys.service.base.WarehouseTransferService;
import org.chemtrovina.cmtmsys.service.base.WorkOrderService;
import org.chemtrovina.cmtmsys.utils.FxFilterUtils;

import java.util.*;
import java.util.stream.Collectors;

public class WorkOrderMaterialTableManager {

    // Lưu full data để filter lại
    private final ObservableList<Map<String, Object>> masterMaterialData = FXCollections.observableArrayList();

    public ObservableList<Map<String, Object>> getMasterMaterialData() {
        return masterMaterialData;
    }

    /**
     * Load bảng pivot material theo WorkOrderCode
     */
    public void loadTable(
            String workOrderCode,
            TableView<Map<String, Object>> tblMaterialByProduct,
            TableColumn<Map<String, Object>, String> colSappn,
            TableColumn<Map<String, Object>, Integer> colLineTotal,
            TableColumn<Map<String, Object>, Integer> colScanned,
            TableColumn<Map<String, Object>, Integer> colRemain,
            TableColumn<Map<String, Object>, Integer> colActual,
            TableColumn<Map<String, Object>, Integer> colMissing,
            TableColumn<Map<String, Object>, Integer> colNo,
            WorkOrderService workOrderService,
            WarehouseTransferService warehouseTransferService
    ) {
        int woId = workOrderService.getWorkOrderIdByCode(workOrderCode);
        List<MaterialRequirementDto> data = workOrderService.getMaterialRequirements(workOrderCode);

        Map<String, Integer> scannedMap = warehouseTransferService.getScannedQuantitiesByWO(woId);
        Map<String, Integer> actualMap = warehouseTransferService.getActualReturnedByWorkOrderId(woId);

        // ===== Pivot theo SAPPN – ProductCode =====
        Map<String, Map<String, Integer>> pivotData = new LinkedHashMap<>();
        Map<String, Integer> productQuantities = new LinkedHashMap<>();

        for (MaterialRequirementDto dto : data) {
            pivotData
                    .computeIfAbsent(dto.getSappn(), k -> new HashMap<>())
                    .merge(dto.getProductCode(), dto.getRequiredQty(), Integer::sum);

            productQuantities.put(dto.getProductCode(), dto.getProductQty());
        }

        Set<String> productCodes = productQuantities.keySet();

        // ===== Cột động: xóa các cột không có fx:id (cột động lần trước) =====
        tblMaterialByProduct.getColumns().removeIf(col -> col.getId() == null);

        // ===== Cột SAPPN & LINE =====
        colSappn.setCellValueFactory(row ->
                new SimpleStringProperty(String.valueOf(row.getValue().get("sappn"))));

        colLineTotal.setCellValueFactory(row ->
                new SimpleIntegerProperty((Integer) row.getValue().get("LINE")).asObject());

        // Vị trí chèn cột động sau cột SAPPN
        int insertIndex = tblMaterialByProduct.getColumns().indexOf(colSappn) + 1;

        // ===== Tạo cột động cho từng productCode =====
        for (String productCode : productCodes) {
            int qty = productQuantities.get(productCode);

            Text codeText = new Text(productCode + " ");
            Text qtyText = new Text("(" + qty + ")");
            qtyText.setStyle("-fx-fill: red;");
            TextFlow header = new TextFlow(codeText, qtyText);

            TableColumn<Map<String, Object>, Integer> colDynamic = new TableColumn<>();
            colDynamic.setGraphic(header);
            colDynamic.setMinWidth(80);

            colDynamic.setCellValueFactory(row -> {
                Object val = row.getValue().getOrDefault(productCode, 0);
                return new SimpleIntegerProperty((Integer) val).asObject();
            });

            tblMaterialByProduct.getColumns().add(insertIndex++, colDynamic);
        }

        // ===== Build row data =====
        List<Map<String, Object>> rows = new ArrayList<>();

        for (String sappn : pivotData.keySet()) {
            Map<String, Object> row = new HashMap<>();
            row.put("sappn", sappn);

            int totalLine = 0;
            for (String product : productCodes) {
                int val = pivotData.get(sappn).getOrDefault(product, 0);
                row.put(product, val);
                totalLine += val;
            }
            row.put("LINE", totalLine);

            int scannedQty = scannedMap.getOrDefault(sappn, 0);
            row.put("SCANNED", scannedQty);

            int remain = Math.max(0, scannedQty - totalLine);
            row.put("REMAIN", remain);

            if (actualMap.containsKey(sappn)) {
                int actualQty = actualMap.get(sappn);
                row.put("ACTUAL", actualQty);

                int missing = Math.max(0, remain - actualQty);
                row.put("MISSING", missing);
            }

            rows.add(row);
        }

        // ===== Cột số thứ tự & cột tổng =====
        colNo.setCellValueFactory(cell ->
                new SimpleIntegerProperty(tblMaterialByProduct.getItems().indexOf(cell.getValue()) + 1)
                        .asObject()
        );

        colScanned.setCellValueFactory(row ->
                new SimpleIntegerProperty((Integer) row.getValue().getOrDefault("SCANNED", 0)).asObject());

        colRemain.setCellValueFactory(row ->
                new SimpleIntegerProperty((Integer) row.getValue().getOrDefault("REMAIN", 0)).asObject());

        colActual.setCellValueFactory(row ->
                new SimpleIntegerProperty((Integer) row.getValue().getOrDefault("ACTUAL", 0)).asObject());

        colMissing.setCellValueFactory(row ->
                new SimpleIntegerProperty((Integer) row.getValue().getOrDefault("MISSING", 0)).asObject());

        // ===== Gán data & lưu master =====
        masterMaterialData.setAll(rows);
        tblMaterialByProduct.setItems(FXCollections.observableArrayList(masterMaterialData));

        // ===== Resize policy (smart) =====
        Platform.runLater(() -> enableSmartResize(tblMaterialByProduct));

        // ===== Filter menu theo SAPPN =====
        FxFilterUtils.setupFilterMenu(
                colSappn,
                new ArrayList<>(masterMaterialData),
                r -> String.valueOf(r.get("sappn")),
                selected -> applySappnFilter(tblMaterialByProduct, selected)
        );
    }

    private void applySappnFilter(TableView<Map<String, Object>> table, List<String> selectedSappn) {
        List<Map<String, Object>> filtered = masterMaterialData.stream()
                .filter(row -> selectedSappn.contains(String.valueOf(row.get("sappn"))))
                .collect(Collectors.toList());
        table.setItems(FXCollections.observableArrayList(filtered));
    }

    private void enableSmartResize(TableView<Map<String, Object>> table) {
        double totalWidth = table.getColumns().stream()
                .mapToDouble(TableColumn::getWidth)
                .sum();
        double tableWidth = table.getWidth();

        if (totalWidth > tableWidth) {
            table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        } else {
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }

        // Theo dõi user resize thêm
        for (TableColumn<?, ?> col : table.getColumns()) {
            col.widthProperty().addListener((obs, o, n) -> {
                double newTotal = table.getColumns().stream()
                        .mapToDouble(TableColumn::getWidth)
                        .sum();
                if (newTotal > table.getWidth()) {
                    table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
                }
            });
        }
    }
}

