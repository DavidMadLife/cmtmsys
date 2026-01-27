package org.chemtrovina.cmtmsys.security;

import javafx.scene.control.Alert;
import org.chemtrovina.cmtmsys.context.UserContext;
import org.chemtrovina.cmtmsys.model.enums.UserRole;

import java.util.Set;

public final class ActionGuard {
    private ActionGuard() {}

    public static boolean allowRoles(String actionName, UserRole... roles) {
        UserRole current = UserContext.getUser().getRole();

        // nếu không truyền roles thì cho qua
        if (roles == null || roles.length == 0) return true;

        for (UserRole r : roles) {
            if (r == current) return true;
        }

        showDenied(actionName, roles);
        return false;
    }

    public static boolean adminOnly(String actionName) {
        return allowRoles(actionName, UserRole.ADMIN);
    }

    private static void showDenied(String actionName, UserRole... roles) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Access denied");
        a.setHeaderText(null);
        a.setContentText(buildMessage(actionName, roles));
        a.showAndWait();
    }

    private static String buildMessage(String actionName, UserRole... roles) {
        StringBuilder sb = new StringBuilder();
        sb.append("You do not have permission to ").append(actionName).append(".\n");
        sb.append("Required role(s): ");
        for (int i = 0; i < roles.length; i++) {
            sb.append(roles[i]);
            if (i < roles.length - 1) sb.append(", ");
        }
        return sb.toString();
    }
}
