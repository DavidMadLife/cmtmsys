package org.chemtrovina.cmtmsys.controller.workorder;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.chemtrovina.cmtmsys.App;
import org.chemtrovina.cmtmsys.controller.WorkOrderCreateController;
import org.chemtrovina.cmtmsys.model.WorkOrder;
import org.chemtrovina.cmtmsys.service.base.WorkOrderService;
import org.chemtrovina.cmtmsys.utils.SpringFXMLLoader;

import java.io.IOException;
import java.util.Optional;

public class WorkOrderDialogManager {

    public void openCreateDialog(Runnable afterClose) {
        try {
            FXMLLoader loader = SpringFXMLLoader.load(App.class.getResource("view/work_order_create.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Tạo Work Order mới");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (afterClose != null) afterClose.run();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Không thể mở form tạo Work Order.");
        }
    }

    public void openUpdateDialog(WorkOrder workOrder, Runnable afterClose) {
        if (workOrder == null) return;

        try {
            FXMLLoader loader = SpringFXMLLoader.load(App.class.getResource("view/work_order_create.fxml"));
            Parent root = loader.load();

            WorkOrderCreateController controller = loader.getController();
            controller.loadWorkOrder(workOrder);

            Stage stage = new Stage();
            stage.setTitle("Cập nhật Work Order");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (afterClose != null) afterClose.run();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Không thể mở form cập nhật Work Order.");
        }
    }

    public void handleDelete(WorkOrder workOrder, WorkOrderService service, Runnable afterDelete) {
        if (workOrder == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Xóa Work Order này?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> res = confirm.showAndWait();

        if (res.isPresent() && res.get() == ButtonType.YES) {
            service.deleteWorkOrder(workOrder.getWorkOrderId());
            if (afterDelete != null) afterDelete.run();
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.showAndWait();
    }
}
