package org.chemtrovina.cmtmsys.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.chemtrovina.cmtmsys.App;
import org.chemtrovina.cmtmsys.config.RolePermissionConfig;
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
    @FXML private MenuItem menuMaterialCart;
    @FXML private MenuItem menuMaterialCartCreate;



    // ==== Planning Menu ====
    @FXML private MenuButton menuPlan;
    @FXML private MenuItem menuPlanWeekly, menuPlanAll;
    @FXML private MenuItem menuPlanDaily;

    @FXML private MenuItem menuTest;
    @FXML private MenuItem menuFeeder, menuFeederRoll;

    @FXML private MenuButton menuUser;

    // ==== PCB Performance Menu ====
    @FXML private MenuButton menuPerformance;
    @FXML private MenuItem menuPcbPerformanceLog;
    @FXML private MenuItem menuPcbPerformanceHistory;
    @FXML private MenuItem menuImportCycleTime;
    @FXML private MenuItem menuShiftScheduleSMT;


    @FXML private MenuButton menuStencil;
    @FXML private MenuItem menuStencilManager;
    @FXML private MenuItem menuStencilTransferLog, menuShiftSummary;

    // ==== E-Board Menu ====
    @FXML private MenuButton menuEBoard;
    @FXML private MenuItem menuEBoardLog;
    @FXML private MenuItem menuEBoardHistory;



    // ==== Solder Menu ====
    @FXML private MenuButton menuSolder;
    @FXML private MenuItem menuSolderList;   // có thể thêm item khác sau, ví dụ Import...
    @FXML private MenuItem menuSolderOut;

    // ==== Spare Part Menu ====
    @FXML private MenuButton menuSparePart;
    @FXML private MenuItem menuSparePartManager;
    @FXML private MenuItem menuSparePartManagerOut;

    @FXML private MenuItem menuChangePassword, menuManageAccount;
    @FXML private MenuItem menuLogout;

    @FXML
    public void initialize() {
        applyRolePermissions();
        setupMenuActions();
    }

    private void applyRolePermissions() {
        UserRole role = UserContext.getUser().getRole();

        // Duyệt toàn bộ các menu có thể null
        setPermission(menuWarehouse, "menuWarehouse", role);
        setPermission(btnInvoice, "btnInvoice", role);
        setPermission(btnScan, "btnScan", role);
        setPermission(btnHistory, "btnHistory", role);
        setPermission(btnMOQ, "btnMOQ", role);

        setPermission(menuEmployee, "menuEmployee", role);
        setPermission(btnEmployee, "btnEmployee", role);

        setPermission(menuInventory, "menuInventory", role);
        setPermission(menuProduct, "menuProduct", role);
        setPermission(menuWorkOrder, "menuWorkOrder", role);
        setPermission(menuMaterialCart, "menuMaterialCart", role);
        setPermission(menuMaterialCartCreate, "menuMaterialCart", role);

        setPermission(menuPlan, "menuPlan", role);

        setPermission(menuPerformance, "menuPerformance", role);
        setPermission(menuPcbPerformanceLog, "menuPcbPerformanceLog", role);
        setPermission(menuPcbPerformanceHistory, "menuPcbPerformanceHistory", role);

        setPermission(menuSolder, "menuSolder", role);
        setPermission(menuSolderList, "menuSolderList", role);

        setPermission(menuSparePart, "menuSparePart", role);

        // Stencil
        setPermission(menuStencil, "menuStencil", role);
        setPermission(menuStencilManager, "menuStencilManager", role);
        setPermission(menuStencilTransferLog, "menuStencilTransferLog", role);

        // E-Board
        setPermission(menuEBoard, "menuEBoard", role);
        setPermission(menuEBoardLog, "menuEBoardLog", role);
        setPermission(menuEBoardHistory, "menuEBoardHistory", role);


        System.out.println("✅ Role applied: " + role);
    }

    private void setPermission(Object node, String menuId, UserRole role) {
        boolean canAccess = RolePermissionConfig.canAccess(menuId, role);

        if (node instanceof MenuButton menuButton) {
            menuButton.setVisible(canAccess);
            menuButton.setManaged(canAccess);
        }
        else if (node instanceof MenuItem menuItem) {
            menuItem.setVisible(canAccess);
        }
        else if (node instanceof javafx.scene.control.Control control) {
            control.setVisible(canAccess);
            control.setManaged(canAccess);
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
        menuMaterialCart.setOnAction(e -> openTab("Material Cart", "/org/chemtrovina/cmtmsys/view/material_cart_view.fxml"));
        menuMaterialCartCreate.setOnAction(e -> openTab("Create Cart", "/org/chemtrovina/cmtmsys/view/material_cart_create.fxml"));


        // Planning
       /* menuPlanWeekly.setOnAction(e -> openTab("Planning Weekly", "/org/chemtrovina/cmtmsys/view/weekly_plan.fxml"));
        menuTest.setOnAction(e -> openTab("Test View", "/org/chemtrovina/cmtmsys/view/daily_plan_view.fxml"));*/
        menuPlanAll.setOnAction(e -> openTab("Plan Weekly/Daily", "/org/chemtrovina/cmtmsys/view/production_plan.fxml"));
        menuFeeder.setOnAction(e -> openTab("Feeder List", "/org/chemtrovina/cmtmsys/view/feederListView-feature.fxml"));
        menuFeederRoll.setOnAction(e -> openTab("Attach Reel", "/org/chemtrovina/cmtmsys/view/feeder-multi-roll.fxml"));
        //menuPlanDaily.setOnAction(e -> openTab("Kế hoạch ngày", "/org/chemtrovina/cmtmsys/view/daily_plan_view.fxml"));


        menuManageAccount.setOnAction(e -> openTab("Manage Account", "/org/chemtrovina/cmtmsys/view/user-management.fxml"));

        // PCB Performance
        menuPcbPerformanceLog.setOnAction(e -> openTab("Hiệu suất PCB", "/org/chemtrovina/cmtmsys/view/performance_log_view.fxml"));
        menuPcbPerformanceHistory.setOnAction(e -> openTab("Lịch sử hiệu suất", "/org/chemtrovina/cmtmsys/view/performance_log_history_view.fxml"));
        menuImportCycleTime.setOnAction(e -> openTab("Import Cycle Time", "/org/chemtrovina/cmtmsys/view/import_cycle_time.fxml"));
        menuShiftScheduleSMT.setOnAction(e -> openTab("Create Shift", "/org/chemtrovina/cmtmsys/view/shiftScheduleView.fxml"));
        menuShiftSummary.setOnAction(e -> openTab("Shift summary", "/org/chemtrovina/cmtmsys/view/ShiftSummaryView.fxml"));

        menuEBoardLog.setOnAction(e ->
                openTab("AOI E-Board Log", "/org/chemtrovina/cmtmsys/view/EBoardPerformanceView.fxml"));

        menuEBoardHistory.setOnAction(e ->
                openTab("Lịch sử E-Board", "/org/chemtrovina/cmtmsys/view/EBoardPerformanceHistoryView.fxml"));


        menuStencilManager.setOnAction(e -> openTab("Stencil Manager", "/org/chemtrovina/cmtmsys/view/stencil_manager_view.fxml"));
        menuStencilTransferLog.setOnAction(
                e -> openTab("Stencil Transfer Log", "/org/chemtrovina/cmtmsys/view/stencil_transfer.fxml")
        );


        // Solder
        menuSolderList.setOnAction(e ->
                openTab("Solder", "/org/chemtrovina/cmtmsys/view/solder_manager_view.fxml"));
        menuSolderOut.setOnAction(
                e -> openTab("Solder OUT (Aging)", "/org/chemtrovina/cmtmsys/view/solder-out-view.fxml")
        );

        menuSparePartManager.setOnAction(e ->
                openTab("Spare Part In", "/org/chemtrovina/cmtmsys/view/spare_part_view.fxml"));
        menuSparePartManagerOut.setOnAction(e ->
                openTab("Spare Part Out", "/org/chemtrovina/cmtmsys/view/spare_part_output_view.fxml"));




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
            FXMLLoader loader = SpringFXMLLoader.load(App.class.getResource("view/login.fxml"));
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
