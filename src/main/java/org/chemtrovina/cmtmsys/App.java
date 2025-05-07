package org.chemtrovina.cmtmsys;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;

public class App extends  javafx.application.Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("view/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 832);
        stage.setTitle("cmtmsys");
        stage.getIcons().add(new javafx.scene.image.Image(
                App.class.getResourceAsStream("asserts/logo.png")
        ));
        stage.setScene(scene);
        stage.show();

    }
}