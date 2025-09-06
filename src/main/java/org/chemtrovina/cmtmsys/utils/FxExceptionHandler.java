package org.chemtrovina.cmtmsys.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.function.Consumer;

public class FxExceptionHandler {
    private final Consumer<String> uiLogger;

    public FxExceptionHandler(Consumer<String> uiLogger) {
        this.uiLogger = uiLogger;
    }

    public void handle(FxAction action) {
        try {
            action.run();
        } catch (Exception e) {
            e.printStackTrace(); // Log file/dev nếu cần
            String message = "❌ Lỗi: " + e.getMessage();
            uiLogger.accept(message);
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
                alert.setHeaderText("Lỗi trong quá trình xử lý");
                alert.showAndWait();
            });
        }
    }
}
