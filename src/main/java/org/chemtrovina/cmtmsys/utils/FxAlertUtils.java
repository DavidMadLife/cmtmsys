package org.chemtrovina.cmtmsys.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

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

    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Chờ người dùng phản hồi
        Optional<ButtonType> result = alert.showAndWait();

        // Trả về true nếu lựa chọn là OK (hoặc ButtonType mặc định cho xác nhận)
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
