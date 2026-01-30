package org.chemtrovina.cmtmsys.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import org.chemtrovina.cmtmsys.helper.TabDisposable;
import org.chemtrovina.cmtmsys.model.FxmlPage;
import org.chemtrovina.cmtmsys.security.PermissionGuard;
import org.chemtrovina.cmtmsys.utils.FxAlertUtils;

public class MainController {

    @FXML private TabPane mainTabPane;
    private static MainController instance;

    @FXML
    public void initialize() {
        instance = this;
    }

    public static MainController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MainController chưa được khởi tạo!");
        }
        return instance;
    }

    public void openTab(String title, String fxmlPath) {
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals(title)) {
                mainTabPane.getSelectionModel().select(tab);
                return;
            }
        }

        Task<FxmlPage> loadTask = new Task<>() {
            @Override
            protected FxmlPage call() throws Exception {
                return FXMLCacheManager.getPage(fxmlPath);
            }
        };

        loadTask.setOnSucceeded(e -> {
            FxmlPage page = loadTask.getValue();

            // ✅ CHECK PERMISSION HERE
            Object controller = page.getController(); // bạn đảm bảo FxmlPage có getter này
            if (!PermissionGuard.canAccess(controller)) {
                FxAlertUtils.warning(PermissionGuard.deniedMessage(controller));
                return;
            }

            Tab tab = new Tab(title);
            tab.setContent(page.getRoot());

            // ✅ Khi đóng tab thì cleanup controller
            tab.setOnClosed(ev -> {
                if (controller instanceof TabDisposable disposable) {
                    disposable.onTabClose();
                }
            });

            // (tuỳ chọn) Nếu bạn có nút X trên tab và muốn chắc chắn nó đóng:
            tab.setClosable(true);

            mainTabPane.getTabs().add(tab);
            mainTabPane.getSelectionModel().select(tab);

            tab.selectedProperty().addListener((obs, was, isSelected) -> {
                if (controller instanceof ProductionPlanController c) {
                    if (isSelected) c.resumeAutoRefresh();
                    else c.pauseAutoRefresh();
                }
            });

        });

        loadTask.setOnFailed(e -> loadTask.getException().printStackTrace());
        new Thread(loadTask).start();


    }

}

