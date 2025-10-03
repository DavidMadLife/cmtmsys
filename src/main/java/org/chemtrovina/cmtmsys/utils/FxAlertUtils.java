package org.chemtrovina.cmtmsys.utils;

import javafx.scene.control.Alert;

public class FxAlertUtils {

    public static void info(String message) {
        show(Alert.AlertType.INFORMATION, "Thông báo", message);
    }

    public static void warning(String message) {
        show(Alert.AlertType.WARNING, "Cảnh báo", message);
    }

    public static void error(String message) {
        show(Alert.AlertType.ERROR, "Lỗi", message);
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
