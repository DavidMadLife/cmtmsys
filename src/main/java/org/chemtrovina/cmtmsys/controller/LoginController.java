package org.chemtrovina.cmtmsys.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.chemtrovina.cmtmsys.App;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.context.UserContext;
import org.chemtrovina.cmtmsys.model.User;
import org.chemtrovina.cmtmsys.repository.Impl.UserRepositoryImpl;
import org.chemtrovina.cmtmsys.service.Impl.UserServiceImpl;
import org.chemtrovina.cmtmsys.service.base.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblError;

    private final UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @FXML
    public void initialize() {
        //setupServices();
        setupEventHandlers();
    }


    private void setupEventHandlers() {
        btnLogin.setOnAction(e -> handleLogin());

        txtPassword.setOnAction(e -> handleLogin()); // Enter to login
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập tài khoản và mật khẩu.");
            return;
        }

        User user = userService.login(username, password);
        if (user == null) {
            showError("Sai tài khoản hoặc mật khẩu.");
        } else {
            lblError.setVisible(false);
            UserContext.setUser(user);
            openMainWindow();
            closeWindow();
        }
    }

    private void openMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/main.fxml"));
            Scene scene = new Scene(loader.load());

            Stage mainStage = new Stage();
            mainStage.setTitle("cmtmsys");
            mainStage.setScene(scene);
            mainStage.getIcons().add(new javafx.scene.image.Image(
                    App.class.getResourceAsStream("asserts/logo.png")
            ));

            mainStage.setMaximized(true);

            mainStage.setOnCloseRequest(e -> System.exit(0));
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Lỗi khi mở giao diện chính.");
        }
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
    }

    private void closeWindow() {
        Stage stage = (Stage) txtUsername.getScene().getWindow();
        stage.close();
    }
}
