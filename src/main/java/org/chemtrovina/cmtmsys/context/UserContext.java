package org.chemtrovina.cmtmsys.context;

import org.chemtrovina.cmtmsys.model.User;

public class UserContext {
    private static User currentUser;

    public static void setUser(User user) {
        currentUser = user;
    }

    public static User getUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }
}
