package org.chemtrovina.cmtmsys.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import org.chemtrovina.cmtmsys.model.FxmlPage;

public class NavbarController {

    @FXML private Button btnHistory;
    @FXML private Button btnInvoice;
    @FXML private Button btnScan;
    @FXML private Button btnMOQ;

    @FXML
    public void initialize() {
        btnScan.setOnAction(this::handleScanButton);
        btnHistory.setOnAction(this::handleHistoryButton);
        btnInvoice.setOnAction(this::handleInvoiceButton);
        btnMOQ.setOnAction(this::handleMOQButton);
    }

    private void handleScanButton(ActionEvent event) {
        navigateTo("/org/chemtrovina/cmtmsys/view/scan-feature.fxml");
    }

    private void handleHistoryButton(ActionEvent event) {
        navigateTo("/org/chemtrovina/cmtmsys/view/history-feature.fxml");
    }

    private void handleInvoiceButton(ActionEvent event) {
        navigateTo("/org/chemtrovina/cmtmsys/view/invoice-feature.fxml");
    }
    private void handleMOQButton(ActionEvent event) {
        navigateTo("/org/chemtrovina/cmtmsys/view/moq-feature.fxml");
    }

    private void navigateTo(String fxmlPath) {
        try {
            FxmlPage page = FXMLCacheManager.getPage(fxmlPath);
            Parent view = page.getView();

            // Gán view mới vào mainContentPane trong MainController
            AnchorPane contentPane = MainController.getMainContentPane();
            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Lỗi khi load view: " + fxmlPath);
        }
    }

}