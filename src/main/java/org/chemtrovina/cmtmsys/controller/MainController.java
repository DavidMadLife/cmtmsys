package org.chemtrovina.cmtmsys.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import org.chemtrovina.cmtmsys.model.FxmlPage;

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
            Parent view = loadTask.getValue().getView();
            Tab tab = new Tab(title, view);
            tab.setClosable(true);
            tab.setOnClosed(ev -> FXMLCacheManager.removePage(fxmlPath));
            mainTabPane.getTabs().add(tab);
            mainTabPane.getSelectionModel().select(tab);
        });

        loadTask.setOnFailed(e -> {
            System.err.println("Lỗi khi load tab: " + title);
            loadTask.getException().printStackTrace();
        });

        new Thread(loadTask).start();
    }
}

