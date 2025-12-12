package org.chemtrovina.cmtmsys.controller.workorder;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.chemtrovina.cmtmsys.service.base.WorkOrderService;

import java.io.File;

public class WorkOrderExcelImporter {

    private final WorkOrderService workOrderService;

    public WorkOrderExcelImporter(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    /**
     * Import file Excel, gọi callback khi xong.
     */
    public void importFile(File file, Runnable onSuccessMessage, Runnable reloadAction) {
        if (file == null) {
            showError("Vui lòng chọn file Excel.");
            return;
        }

        try {
            workOrderService.importFromExcel(file);

            if (onSuccessMessage != null) onSuccessMessage.run();
            if (reloadAction != null) reloadAction.run();

        } catch (Exception e) {
            e.printStackTrace();
            showError("❌ Lỗi khi import: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.showAndWait();
    }
}

