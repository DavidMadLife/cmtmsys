package org.chemtrovina.cmtmsys.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.chemtrovina.cmtmsys.App;
import org.chemtrovina.cmtmsys.context.UserContext;
import org.chemtrovina.cmtmsys.model.enums.UserRole;
import org.chemtrovina.cmtmsys.utils.SpringFXMLLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class NavbarController {

    // ==== Warehouse Menu ====
    @FXML private MenuButton menuWarehouse;
    @FXML private MenuItem btnInvoice;
    @FXML private MenuItem btnScan;
    @FXML private MenuItem btnHistory;
    @FXML private MenuItem btnMOQ;

    // ==== Employee Menu ====
    @FXML private MenuButton menuEmployee;
    @FXML private MenuItem btnEmployee;
    @FXML private MenuItem btnShiftScheduling;

    // ==== Inventory Menu ====
    @FXML private MenuButton menuInventory;
    @FXML private MenuItem menuInventoryCheck, menuCheckNG;
    @FXML private MenuItem menuInventoryTransfer, menuTransferMaterialReturn;
    @FXML private MenuItem menuTransferLog;
    @FXML private MenuItem menuProduct;
    @FXML private MenuItem menuWorkOrder;

    // ==== Planning Menu ====
    @FXML private MenuButton menuPlan;
    @FXML private MenuItem menuPlanWeekly, menuPlanAll;
    @FXML private MenuItem menuTest;
    @FXML private MenuItem menuFeeder, menuFeederRoll;

    @FXML private MenuButton menuUser;


    @FXML private MenuItem menuChangePassword, menuManageAccount;
    @FXML private MenuItem menuLogout;

    @FXML
    public void initialize() {
        applyRolePermissions();
        setupMenuActions();
    }

    private void applyRolePermissions() {
        UserRole role = UserContext.getUser().getRole();

        if (role != UserRole.ADMIN && role != UserRole.SUBLEEDER && role != UserRole.INVENTORY) {
            menuPlan.setVisible(false);
            menuPlan.setManaged(false);
        }
        if (role != UserRole.ADMIN && role != UserRole.EMPLOYEE) {
            menuEmployee.setVisible(false);
            menuEmployee.setManaged(false);
        }
        if (role != UserRole.ADMIN && role != UserRole.INVENTORY) {
            menuInventory.setVisible(false);
            menuInventory.setManaged(false);
            menuWarehouse.setVisible(false);
            menuWarehouse.setManaged(false);
        }
        if (role != UserRole.ADMIN && role != UserRole.GENERALWAREHOUSE) {
            menuWarehouse.setVisible(false);
            menuWarehouse.setManaged(false);
        }
    }


    private void setupMenuActions() {
        // Warehouse
        btnScan.setOnAction(e -> openTab("Scan", "/org/chemtrovina/cmtmsys/view/scan-feature.fxml"));
        btnHistory.setOnAction(e -> openTab("History", "/org/chemtrovina/cmtmsys/view/historyList-feature.fxml"));
        btnInvoice.setOnAction(e -> openTab("Invoice", "/org/chemtrovina/cmtmsys/view/invoice-feature.fxml"));
        btnMOQ.setOnAction(e -> openTab("MOQ", "/org/chemtrovina/cmtmsys/view/moq-feature.fxml"));

        // Employee
        btnEmployee.setOnAction(e -> openTab("Employee", "/org/chemtrovina/cmtmsys/view/employee-feature.fxml"));
        btnShiftScheduling.setOnAction(e -> openTab("Shift Scheduling", "/org/chemtrovina/cmtmsys/view/shiftScheduling-feature.fxml"));

        // Inventory
        menuInventoryCheck.setOnAction(e -> openTab("Inventory Check", "/org/chemtrovina/cmtmsys/view/inventoryCheck-feature.fxml"));
        menuInventoryTransfer.setOnAction(e -> openTab("Inventory Transfer", "/org/chemtrovina/cmtmsys/view/inventoryTransfer-feature.fxml"));
        menuTransferLog.setOnAction(e -> openTab("Transfer Log", "/org/chemtrovina/cmtmsys/view/transferLog-feature.fxml"));
        menuProduct.setOnAction(e -> openTab("BOM", "/org/chemtrovina/cmtmsys/view/product-feature.fxml"));
        menuWorkOrder.setOnAction(e -> openTab("W/O", "/org/chemtrovina/cmtmsys/view/workOrder-feature.fxml"));
        menuTransferMaterialReturn.setOnAction(e -> openTab("Transfer Material Return", "/org/chemtrovina/cmtmsys/view/material_return.fxml"));
        menuCheckNG.setOnAction(e -> openTab("Check NG", "/org/chemtrovina/cmtmsys/view/rejected_material_history.fxml"));

        // Planning
        menuPlanWeekly.setOnAction(e -> openTab("Planning Weekly", "/org/chemtrovina/cmtmsys/view/weekly_plan.fxml"));
        menuTest.setOnAction(e -> openTab("Test View", "/org/chemtrovina/cmtmsys/view/daily_plan_view.fxml"));
        menuPlanAll.setOnAction(e -> openTab("Plan Weekly/Daily", "/org/chemtrovina/cmtmsys/view/production_plan.fxml"));
        menuFeeder.setOnAction(e -> openTab("Feeder List", "/org/chemtrovina/cmtmsys/view/feederListView-feature.fxml"));
        menuFeederRoll.setOnAction(e -> openTab("Attach Reel", "/org/chemtrovina/cmtmsys/view/feeder-multi-roll.fxml"));


        menuManageAccount.setOnAction(e -> openTab("Manage Account", "/org/chemtrovina/cmtmsys/view/user-management.fxml"));
        menuChangePassword.setOnAction(e -> openChangePasswordDialog());
        menuLogout.setOnAction(e -> handleLogout());
    }

    private void openChangePasswordDialog() {
        try {
            FXMLLoader loader = SpringFXMLLoader.load(
                    App.class.getResource("view/change_password.fxml")
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Đổi mật khẩu");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLogout() {
        // Clear user context
        UserContext.clear();

        // Đóng cửa sổ hiện tại
        Stage stage = (Stage) menuUser.getScene().getWindow();
        stage.close();

        // Mở lại màn hình login
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/login.fxml"));
            Scene scene = new Scene(loader.load());

            Stage loginStage = new Stage();
            loginStage.setScene(scene);
            loginStage.setTitle("Đăng nhập");
            loginStage.setResizable(false);
            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openTab(String title, String fxmlPath) {
        MainController.getInstance().openTab(title, fxmlPath);
    }
}
