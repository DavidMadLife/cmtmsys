package org.chemtrovina.cmtmsys.controller;

import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.chemtrovina.cmtmsys.context.UserContext;
import org.chemtrovina.cmtmsys.model.User;
import org.chemtrovina.cmtmsys.repository.Impl.UserRepositoryImpl;
import org.chemtrovina.cmtmsys.security.RequiresRoles;
import org.chemtrovina.cmtmsys.service.Impl.UserServiceImpl;
import org.chemtrovina.cmtmsys.service.base.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.springframework.stereotype.Component;

@RequiresRoles(allowAll = true)

@Component
public class ChangePasswordController {

    @FXML private PasswordField txtOldPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Button btnChangePassword;
    @FXML private Label lblMessage;

    private final UserService userService;

    @Autowired
    public ChangePasswordController(UserService userService) {
        this.userService = userService;
    }

    @FXML
    public void initialize() {
        btnChangePassword.setOnAction(e -> handleChangePassword());
    }

    private void handleChangePassword() {
        String oldPass = txtOldPassword.getText().trim();
        String newPass = txtNewPassword.getText().trim();
        String confirmPass = txtConfirmPassword.getText().trim();
        User user = UserContext.getUser();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        // Disable button để tránh double-click
        btnChangePassword.setDisable(true);
        lblMessage.setStyle("-fx-text-fill: black;");
        lblMessage.setText("Đang xử lý...");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                User loginResult = userService.login(user.getUsername(), oldPass);
                if (loginResult == null || loginResult.getUserId() != user.getUserId()) {
                    throw new IllegalArgumentException("Mật khẩu cũ không đúng.");
                }

                if (!newPass.equals(confirmPass)) {
                    throw new IllegalArgumentException("Mật khẩu mới không khớp.");
                }

                userService.updatePassword(user.getUserId(), newPass);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            lblMessage.setStyle("-fx-text-fill: green;");
            lblMessage.setText("Đổi mật khẩu thành công!");

            // Đóng sau 1s
            PauseTransition delay = new PauseTransition(Duration.seconds(1));
            delay.setOnFinished(event -> {
                Stage stage = (Stage) txtOldPassword.getScene().getWindow();
                stage.close();
            });
            delay.play();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText(ex.getMessage() != null ? ex.getMessage() : "Lỗi khi đổi mật khẩu.");
            btnChangePassword.setDisable(false);
        });

        new Thread(task).start();
    }


}
