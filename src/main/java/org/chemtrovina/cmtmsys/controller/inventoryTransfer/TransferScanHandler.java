package org.chemtrovina.cmtmsys.controller.inventoryTransfer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import org.chemtrovina.cmtmsys.dto.SAPSummaryDto;
import org.chemtrovina.cmtmsys.dto.TransferredDto;
import org.chemtrovina.cmtmsys.model.*;
import org.chemtrovina.cmtmsys.service.base.*;
import org.apache.poi.ss.usermodel.Row;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TransferScanHandler {

    private final WarehouseTransferService warehouseTransferService;
    private final WarehouseService warehouseService;
    private final MaterialService materialService;
    private final TransferLogService transferLogService;
    private final WorkOrderService workOrderService;

    // State
    private WarehouseTransfer currentTransfer;
    private boolean batchImport = false;

    private final Map<String, SAPSummaryDto> sapSummaryMap = new HashMap<>();
    private final ObservableList<SAPSummaryDto> sapSummaryList = FXCollections.observableArrayList();
    private final Set<String> alreadyScannedRollCodes = new HashSet<>();
    private final ObservableList<TransferredDto> transferredMasterList = FXCollections.observableArrayList();

    public TransferScanHandler(
            WarehouseTransferService warehouseTransferService,
            WarehouseService warehouseService,
            MaterialService materialService,
            TransferLogService transferLogService,
            WorkOrderService workOrderService) {

        this.warehouseTransferService = warehouseTransferService;
        this.warehouseService = warehouseService;
        this.materialService = materialService;
        this.transferLogService = transferLogService;
        this.workOrderService = workOrderService;
    }

    public ObservableList<TransferredDto> getTransferredMasterList() {
        return transferredMasterList;
    }

    public void setBatchImport(boolean batchImport) {
        this.batchImport = batchImport;
    }


    /* ============================================================
     * 1. LOAD REQUIRED SUMMARY FOR WORK ORDER
     * ============================================================ */
    public void loadRequiredSummary(
            String workOrderCode,
            TableView<SAPSummaryDto> tblRequiredSummary,
            TableView<TransferredDto> tblTransferred) {

        sapSummaryMap.clear();
        sapSummaryList.clear();
        alreadyScannedRollCodes.clear();
        transferredMasterList.clear();
        currentTransfer = null;

        int workOrderId = workOrderService.getWorkOrderIdByCode(workOrderCode);

        // 1) YÊU CẦU
        for (var req : workOrderService.getGroupedMaterialRequirements(workOrderCode)) {
            sapSummaryMap.put(req.getSappn(), new SAPSummaryDto(req.getSappn(), req.getRequiredQty()));
        }

        // 2) DETAILS đã chuyển
        List<WarehouseTransferDetail> details = warehouseTransferService.getDetailsByWorkOrderId(workOrderId);
        if (details.isEmpty()) {
            sapSummaryList.addAll(sapSummaryMap.values());
            tblRequiredSummary.setItems(sapSummaryList);
            tblTransferred.getItems().clear();
            return;
        }

        // Cache
        Set<Integer> transferIds = details.stream().map(WarehouseTransferDetail::getTransferId).collect(Collectors.toSet());
        Set<String> rollCodes = details.stream().map(WarehouseTransferDetail::getRollCode).collect(Collectors.toSet());

        Map<Integer, WarehouseTransfer> transferMap = warehouseTransferService.getTransfersByIds(transferIds);
        Map<String, Material> materialMap = materialService.getMaterialsByRollCodes(rollCodes);

        // Update summary
        for (WarehouseTransferDetail d : details) {
            SAPSummaryDto s = sapSummaryMap.get(d.getSapCode());
            if (s != null) {
                s.setScanned(s.getScanned() + d.getQuantity());
                s.setStatus(s.getScanned() >= s.getRequired() ? "Đủ" : "Thiếu");
                alreadyScannedRollCodes.add(d.getRollCode());
            }
        }

        sapSummaryList.addAll(sapSummaryMap.values());
        tblRequiredSummary.setItems(sapSummaryList);

        // Build transferred list
        List<TransferredDto> dtos = details.stream()
                .map(d -> {
                    Material m = materialMap.get(d.getRollCode());
                    WarehouseTransfer t = transferMap.get(d.getTransferId());
                    return new TransferredDto(
                            d.getRollCode(),
                            d.getSapCode(),
                            m != null ? m.getSpec() : "",
                            d.getQuantity(),
                            warehouseService.getWarehouseNameById(t.getFromWarehouseId()),
                            warehouseService.getWarehouseNameById(t.getToWarehouseId())
                    );
                })
                .toList();

        transferredMasterList.setAll(dtos);
        tblTransferred.setItems(transferredMasterList);
    }


    /* ============================================================
     * 2. HANDLE SCAN
     * ============================================================ */
    public void handleBarcodeScanned(
            TextField txtBarcode,
            ComboBox<String> cbSourceWarehouse,
            ComboBox<String> cbTargetWarehouse,
            TextField txtEmployeeID,
            ComboBox<String> cbWorkOrder,
            TableView<TransferredDto> tblTransferred,
            TableView<SAPSummaryDto> tblRequiredSummary) {

        String barcode = txtBarcode.getText().trim();
        if (barcode.isEmpty()) return;

        int toId = getWarehouseId(cbTargetWarehouse);
        int fromId = getWarehouseId(cbSourceWarehouse);
        if (toId == -1 || fromId == -1 || toId == fromId) {
            showAlert("Kho chuyển đến và đi không hợp lệ");
            return;
        }

        String employeeId = txtEmployeeID.getText().trim();
        if (employeeId.isEmpty()) {
            showAlert("Vui lòng nhập mã nhân viên");
            return;
        }

        String woCode = cbWorkOrder.getValue();
        if (woCode == null || woCode.isEmpty()) {
            showAlert("Vui lòng chọn Work Order");
            return;
        }

        Material material = materialService.getMaterialByRollCode(barcode);
        if (material == null) {
            showAlert("Không tìm thấy vật liệu: " + barcode);
            return;
        }

        if (alreadyScannedRollCodes.contains(barcode)) {
            showAlert("Cuộn đã được quét trong W/O này.");
            return;
        }

        String sapCode = material.getSapCode();
        SAPSummaryDto summary = sapSummaryMap.get(sapCode);
        if (summary == null) {
            showAlert("Mã SAP " + sapCode + " không có trong W/O");
            return;
        }

        // Create transfer if needed
        int woId = workOrderService.getWorkOrderIdByCode(woCode);
        initOrLoadCurrentTransfer(fromId, toId, employeeId, woCode, woId);

        // Check existed in transfer
        if (warehouseTransferService.getDetailRepository()
                .existsByTransferIdAndRollCode(currentTransfer.getTransferId(), barcode)) {
            showAlert("Cuộn đã có trong phiếu chuyển.");
            return;
        }

        // Save detail
        WarehouseTransferDetail detail = new WarehouseTransferDetail();
        detail.setTransferId(currentTransfer.getTransferId());
        detail.setRollCode(barcode);
        detail.setSapCode(sapCode);
        detail.setQuantity(material.getQuantity());
        detail.setCreatedAt(LocalDateTime.now());
        warehouseTransferService.getDetailRepository().add(detail);

        updateTransferredTable(tblTransferred, cbSourceWarehouse.getValue(), cbTargetWarehouse.getValue());
        updateSummaryAfterScan(summary, material, tblRequiredSummary);
        updateMaterialWarehouse(material, toId);
        addTransferLog(material, fromId, toId, employeeId, woCode);

        txtBarcode.clear();
    }


    /* ============================================================
     * 3. HANDLE DELETE
     * ============================================================ */
    public void handleDeleteBarcode(
            TextField txtDeleteBarcode,
            ComboBox<String> cbWorkOrder,
            TableView<TransferredDto> tblTransferred,
            TableView<SAPSummaryDto> tblRequiredSummary) {

        String barcode = txtDeleteBarcode.getText().trim();
        if (barcode.isEmpty()) return;

        String woCode = cbWorkOrder.getValue();
        if (woCode == null) {
            showAlert("Vui lòng chọn Work Order.");
            return;
        }

        int woId = workOrderService.getWorkOrderIdByCode(woCode);
        Material material = materialService.getMaterialByRollCode(barcode);
        if (material == null) {
            showAlert("Không tìm thấy vật liệu: " + barcode);
            return;
        }

        Optional<WarehouseTransfer> transferOpt = warehouseTransferService.getAllTransfers()
                .stream()
                .filter(t -> Objects.equals(t.getWorkOrderId(), woId))
                .filter(t -> warehouseTransferService.getDetailRepository()
                        .existsByTransferIdAndRollCode(t.getTransferId(), barcode))
                .findFirst();

        if (transferOpt.isEmpty()) {
            showAlert("Không tìm thấy chi tiết chứa cuộn này.");
            return;
        }

        WarehouseTransfer transfer = transferOpt.get();
        int transferId = transfer.getTransferId();

        // Xóa detail trong DB
        warehouseTransferService.getDetailRepository()
                .deleteByTransferIdAndRollCode(transferId, barcode);

        material.setWarehouseId(16);
        materialService.updateMaterial(material);

        // =======================================================
        // FIX NPE — xử lý update UI đúng theo trạng thái hiện tại
        // =======================================================

        if (currentTransfer != null && Objects.equals(currentTransfer.getTransferId(), transferId)) {
            // Nếu đang ở đúng phiếu đang active
            updateTransferredTable(
                    tblTransferred,
                    warehouseService.getWarehouseNameById(transfer.getFromWarehouseId()),
                    warehouseService.getWarehouseNameById(transfer.getToWarehouseId())
            );
        } else {
            // Ngược lại: phiếu cũ hoặc chưa scan gì → reload theo WO
            loadRequiredSummary(woCode, tblRequiredSummary, tblTransferred);
        }

        // Update summary
        SAPSummaryDto summary = sapSummaryMap.get(material.getSapCode());
        if (summary != null) {
            summary.setScanned(Math.max(0, summary.getScanned() - material.getQuantity()));
            summary.setStatus(summary.getScanned() >= summary.getRequired() ? "Đủ" : "Thiếu");
            tblRequiredSummary.refresh();
        }

        alreadyScannedRollCodes.remove(barcode);
        txtDeleteBarcode.clear();

        showAlert("Đã xóa.");
    }



    /* ============================================================
     * 4. INTERNAL UTILITIES
     * ============================================================ */
    private void initOrLoadCurrentTransfer(
            int fromId, int toId,
            String employeeId, String woCode, int woId) {

        if (currentTransfer != null) return;

        currentTransfer = warehouseTransferService.findExistingTransfer(fromId, toId, woId, employeeId);

        if (currentTransfer == null) {
            WarehouseTransfer t = new WarehouseTransfer();
            t.setFromWarehouseId(fromId);
            t.setToWarehouseId(toId);
            t.setEmployeeId(employeeId);
            t.setWorkOrderId(woId);
            t.setTransferDate(LocalDateTime.now());
            t.setNote("Chuyển từ W/O: " + woCode);

            warehouseTransferService.createTransfer(t, new ArrayList<>());
            currentTransfer = warehouseTransferService.getAllTransfers().getLast();
        }
    }

    private int getWarehouseId(ComboBox<String> cb) {
        String name = cb.getValue();
        if (name == null) return -1;

        return warehouseService.getAllWarehouses()
                .stream().filter(w -> w.getName().equals(name))
                .map(Warehouse::getWarehouseId)
                .findFirst().orElse(-1);
    }

    private void updateTransferredTable(
            TableView<TransferredDto> tblTransferred,
            String fromName,
            String toName) {

        if (currentTransfer == null) return;

        int id = currentTransfer.getTransferId();
        List<TransferredDto> dtos = warehouseTransferService.getDetailsByTransferId(id)
                .stream()
                .map(d -> {
                    Material m = materialService.getMaterialByRollCode(d.getRollCode());
                    return new TransferredDto(
                            d.getRollCode(),
                            d.getSapCode(),
                            m != null ? m.getSpec() : "",
                            d.getQuantity(),
                            fromName,
                            toName
                    );
                })
                .toList();

        transferredMasterList.setAll(dtos);
        tblTransferred.setItems(transferredMasterList);
    }

    private void updateSummaryAfterScan(
            SAPSummaryDto summary,
            Material material,
            TableView<SAPSummaryDto> tblRequiredSummary) {

        summary.setScanned(summary.getScanned() + material.getQuantity());
        summary.setStatus(summary.getScanned() >= summary.getRequired() ? "Đủ" : "Thiếu");
        tblRequiredSummary.refresh();
        alreadyScannedRollCodes.add(material.getRollCode());
    }

    private void updateMaterialWarehouse(Material m, int toId) {
        m.setWarehouseId(toId);
        materialService.updateMaterial(m);
    }

    private void addTransferLog(
            Material material,
            int fromId, int toId,
            String emp,
            String woCode) {

        if (currentTransfer == null) return;

        TransferLog log = new TransferLog();
        log.setTransferId(currentTransfer.getTransferId());
        log.setRollCode(material.getRollCode());
        log.setFromWarehouseId(fromId);
        log.setToWarehouseId(toId);
        log.setTransferDate(LocalDateTime.now());
        log.setEmployeeId(emp);
        log.setNote("Chuyển từ W/O: " + woCode);
        transferLogService.addTransfer(log);
    }

    private void showAlert(String msg) {
        if (batchImport) return;
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
