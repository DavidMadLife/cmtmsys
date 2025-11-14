package org.chemtrovina.cmtmsys;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import org.chemtrovina.cmtmsys.config.SpringContextConfig;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.chemtrovina.cmtmsys.utils.SpringFXMLLoader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

import java.io.IOException;

public class App extends Application {

    private ConfigurableApplicationContext springContext;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        // Khởi tạo Spring context
        springContext = new AnnotationConfigApplicationContext(SpringContextConfig.class);
        SpringFXMLLoader.setApplicationContext(springContext);
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Dùng SpringFXMLLoader để load FXML có inject controller
        var fxmlUrl = App.class.getResource("view/login.fxml");
        var loader = SpringFXMLLoader.load(fxmlUrl);
        var scene = new Scene(loader.load());

        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.C) {
                Node focusNode = scene.getFocusOwner();
                if (focusNode instanceof TableView<?> tableView) {
                    FxClipboardUtils.copySelectionToClipboard(tableView);
                    event.consume();
                }
            }
            // (Tùy chọn) Ctrl+A để chọn toàn bộ TableView
            if (event.isControlDown() && event.getCode() == KeyCode.A) {
                Node focusNode = scene.getFocusOwner();
                if (focusNode instanceof TableView<?> tableView) {
                    tableView.getSelectionModel().selectAll();
                    event.consume();
                }
            }
        });

        stage.setTitle("Đăng nhập hệ thống");
        stage.getIcons().add(new Image(App.class.getResourceAsStream("asserts/logo.png")));
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> System.exit(0));
        stage.show();
    }

    @Override
    public void stop() {
        // Dọn tài nguyên Spring
        springContext.close();
    }
}
