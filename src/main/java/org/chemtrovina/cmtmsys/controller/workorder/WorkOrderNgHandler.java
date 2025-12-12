package org.chemtrovina.cmtmsys.controller.workorder;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import org.chemtrovina.cmtmsys.model.RejectedMaterial;
import org.chemtrovina.cmtmsys.model.WorkOrder;
import org.chemtrovina.cmtmsys.service.base.RejectedMaterialService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WorkOrderNgHandler {

    private final RejectedMaterialService rejectedMaterialService;

    // ID kho NG – bạn có thể cho vào config
    private final int warehouseIdNG = 17;

    public WorkOrderNgHandler(RejectedMaterialService rejectedMaterialService) {
        this.rejectedMaterialService = rejectedMaterialService;
    }

    public void transferNG(WorkOrder workOrder, List<Map<String, Object>> materialRows) {
        if (workOrder == null) {
            showInfo("Vui lòng chọn Work Order.");
            return;
        }

        if (materialRows == null || materialRows.isEmpty()) {
            showInfo("Không có dữ liệu vật tư để chuyển NG.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Lý do chuyển hàng NG");
        dialog.setHeaderText("Nhập lý do chuyển hàng NG:");
        dialog.setContentText("Lý do:");

        Optional<String> res = dialog.showAndWait();
        if (res.isEmpty() || res.get().trim().isEmpty()) {
            showInfo("⚠️ Bạn phải nhập lý do để chuyển hàng NG.");
            return;
        }

        String reason = res.get().trim();
        int workOrderId = workOrder.getWorkOrderId();
        int inserted = 0;

        for (Map<String, Object> row : materialRows) {
            String sap = String.valueOf(row.get("sappn"));
            int missing = (int) row.getOrDefault("MISSING", 0);
            if (missing <= 0) continue;

            RejectedMaterial rm = new RejectedMaterial();
            rm.setWorkOrderId(workOrderId);
            rm.setWarehouseId(warehouseIdNG);
            rm.setSapCode(sap);
            rm.setQuantity(missing);
            rm.setCreatedDate(LocalDateTime.now());
            rm.setNote(reason);

            rejectedMaterialService.addOrUpdateRejectedMaterial(rm);
            inserted++;
        }

        showInfo("✅ Đã chuyển " + inserted + " dòng NG vào kho NG.");
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.showAndWait();
    }
}

