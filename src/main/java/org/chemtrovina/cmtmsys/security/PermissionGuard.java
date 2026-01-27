package org.chemtrovina.cmtmsys.security;

import org.chemtrovina.cmtmsys.context.UserContext;
import org.chemtrovina.cmtmsys.model.enums.UserRole;

import java.util.Arrays;

public final class PermissionGuard {

    private PermissionGuard() {}

    public static boolean canAccess(Object controller) {
        if (controller == null) return true; // fallback

        RequiresRoles rr = controller.getClass().getAnnotation(RequiresRoles.class);
        if (rr == null) return true; // không khai thì cho vào (tuỳ bạn muốn default deny hay allow)

        if (rr.allowAll()) return true;

        UserRole role = UserContext.getUser().getRole();
        return Arrays.asList(rr.value()).contains(role);
    }

    public static String deniedMessage(Object controller) {
        RequiresRoles rr = controller.getClass().getAnnotation(RequiresRoles.class);
        if (rr == null) return "You do not have permission.";
        return "Access denied. Required roles: " + Arrays.toString(rr.value());
    }
}
