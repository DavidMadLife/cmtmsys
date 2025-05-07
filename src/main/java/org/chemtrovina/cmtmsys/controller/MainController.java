package org.chemtrovina.cmtmsys.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class MainController {

    @FXML private AnchorPane mainContentPane;
    private static AnchorPane staticContentPane;

    @FXML
    public void initialize() {
        staticContentPane = mainContentPane;
    }

    public static AnchorPane getMainContentPane() {
        return staticContentPane;
    }
}