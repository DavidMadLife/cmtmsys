package org.chemtrovina.cmtmsys.config;

import org.chemtrovina.cmtmsys.model.enums.UserRole;
import java.util.*;

public class RolePermissionConfig {

    // Map<MenuID, List<Role>>
    private static final Map<String, List<UserRole>> PERMISSIONS = new HashMap<>();

    static {
        // Warehouse module
        PERMISSIONS.put("menuWarehouse", List.of(UserRole.ADMIN, UserRole.GENERALWAREHOUSE));
        PERMISSIONS.put("btnInvoice", List.of(UserRole.ADMIN, UserRole.GENERALWAREHOUSE));
        PERMISSIONS.put("btnScan", List.of(UserRole.ADMIN, UserRole.GENERALWAREHOUSE));
        PERMISSIONS.put("btnHistory", List.of(UserRole.ADMIN, UserRole.GENERALWAREHOUSE));
        PERMISSIONS.put("btnMOQ", List.of(UserRole.ADMIN, UserRole.GENERALWAREHOUSE, UserRole.INVENTORY, UserRole.SUBLEEDER));

        // Employee module
        PERMISSIONS.put("menuEmployee", List.of(UserRole.ADMIN, UserRole.EMPLOYEE));
        PERMISSIONS.put("btnEmployee", List.of(UserRole.ADMIN, UserRole.EMPLOYEE));

        // Inventory
        PERMISSIONS.put("menuInventory", List.of(UserRole.ADMIN, UserRole.INVENTORY, UserRole.SUBLEEDER));
        PERMISSIONS.put("menuProduct", List.of(UserRole.ADMIN, UserRole.INVENTORY));
        PERMISSIONS.put("menuWorkOrder", List.of(UserRole.ADMIN, UserRole.INVENTORY));
        PERMISSIONS.put("menuMaterialCart", List.of(UserRole.ADMIN, UserRole.INVENTORY, UserRole.SUBLEEDER));

        // PCB Performance
        PERMISSIONS.put("menuPerformance", List.of(UserRole.ADMIN, UserRole.SUBLEEDER));
        PERMISSIONS.put("menuPcbPerformanceLog", List.of(UserRole.ADMIN, UserRole.SUBLEEDER));
        PERMISSIONS.put("menuPcbPerformanceHistory", List.of(UserRole.ADMIN, UserRole.SUBLEEDER));

        // Solder
        PERMISSIONS.put("menuSolder", List.of(UserRole.ADMIN, UserRole.INVENTORY, UserRole.SUBLEEDER));
        PERMISSIONS.put("menuSolderList", List.of(UserRole.ADMIN, UserRole.INVENTORY, UserRole.SUBLEEDER));

        // Planning
        PERMISSIONS.put("menuPlan", List.of(UserRole.ADMIN,UserRole.INVENTORY, UserRole.SUBLEEDER));

        // Spare Part
        PERMISSIONS.put("menuSparePart", List.of(UserRole.ADMIN, UserRole.INVENTORY));

        // === Stencil Manager ===
        PERMISSIONS.put("menuStencilManager", List.of(UserRole.ADMIN, UserRole.INVENTORY));
        PERMISSIONS.put("menuStencilTransferLog", List.of(UserRole.ADMIN, UserRole.INVENTORY));

// === E-Board ===
        PERMISSIONS.put("menuEBoard", List.of(UserRole.ADMIN, UserRole.SUBLEEDER));
        PERMISSIONS.put("menuEBoardLog", List.of(UserRole.ADMIN, UserRole.SUBLEEDER));
        PERMISSIONS.put("menuEBoardHistory", List.of(UserRole.ADMIN, UserRole.SUBLEEDER));

    }

    public static boolean canAccess(String menuId, UserRole role) {
        return PERMISSIONS.getOrDefault(menuId, List.of()).contains(role);
    }
}
