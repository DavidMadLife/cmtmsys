package org.chemtrovina.cmtmsys.utils;

import javafx.fxml.FXMLLoader;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.net.URL;

public class SpringFXMLLoader {

    private static ApplicationContext context;

    public static void setApplicationContext(ApplicationContext ctx) {
        context = ctx;
    }

    public static FXMLLoader load(URL fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(fxmlPath);
        loader.setControllerFactory(context::getBean);
        return loader;
    }

}
