package org.chemtrovina.cmtmsys.controller;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;

public class NavbarController {

    @FXML private MenuItem btnInvoice;
    @FXML private MenuItem btnScan;
    @FXML private MenuItem btnHistory;
    @FXML private MenuItem btnMOQ;

    @FXML private MenuItem btnEmployee;
    @FXML private MenuItem btnShiftScheduling;

    @FXML private MenuItem menuInventoryCheck;
    @FXML private MenuItem menuInventoryTransfer;
    @FXML private MenuItem menuTransferLog;
    @FXML private MenuItem menuProduct;

    @FXML
    public void initialize() {
        btnScan.setOnAction(e -> openTab("Scan", "/org/chemtrovina/cmtmsys/view/scan-feature.fxml"));
        btnHistory.setOnAction(e -> openTab("History", "/org/chemtrovina/cmtmsys/view/historyList-feature.fxml"));
        btnInvoice.setOnAction(e -> openTab("Invoice", "/org/chemtrovina/cmtmsys/view/invoice-feature.fxml"));
        btnMOQ.setOnAction(e -> openTab("MOQ", "/org/chemtrovina/cmtmsys/view/moq-feature.fxml"));

        btnEmployee.setOnAction(e -> openTab("Employee", "/org/chemtrovina/cmtmsys/view/employee-feature.fxml"));
        btnShiftScheduling.setOnAction(e -> openTab("Shift Scheduling", "/org/chemtrovina/cmtmsys/view/shiftScheduling-feature.fxml"));

        menuInventoryCheck.setOnAction(e -> openTab("Inventory Check", "/org/chemtrovina/cmtmsys/view/inventoryCheck-feature.fxml"));
        menuInventoryTransfer.setOnAction(e -> openTab("Inventory Transfer", "/org/chemtrovina/cmtmsys/view/inventoryTransfer-feature.fxml"));
        menuTransferLog.setOnAction(e -> openTab("Transfer Log", "/org/chemtrovina/cmtmsys/view/transferLog-feature.fxml"));
        menuProduct.setOnAction(e -> openTab("Product", "/org/chemtrovina/cmtmsys/view/product-feature.fxml"));
    }

    private void openTab(String title, String fxmlPath) {
        MainController.getInstance().openTab(title, fxmlPath);
    }
}
