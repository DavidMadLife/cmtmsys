package org.chemtrovina.cmtmsys.controller;

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
        return instance;
    }

    public void openTab(String title, String fxmlPath) {
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals(title)) {
                mainTabPane.getSelectionModel().select(tab);
                return;
            }
        }

        try {
            FxmlPage page = FXMLCacheManager.getPage(fxmlPath);
            Parent view = page.getView();

            Tab tab = new Tab(title, view);
            tab.setClosable(true);

            tab.setOnClosed(e -> FXMLCacheManager.removePage(fxmlPath));
            tab.setOnClosed(e -> FXMLCacheManager.clearCache());
            mainTabPane.getTabs().add(tab);
            mainTabPane.getSelectionModel().select(tab);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Lỗi khi load tab: " + title);
        }
    }
}
