package org.chemtrovina.cmtmsys.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.chemtrovina.cmtmsys.model.User;
import org.chemtrovina.cmtmsys.model.enums.UserRole;
import org.chemtrovina.cmtmsys.service.base.UserService;
import org.chemtrovina.cmtmsys.utils.FxAlertUtils;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;



@Component
public class UserController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<UserRole> cbRole;
    @FXML private Button btnAddUser;

    @FXML private TableView<User> tblUsers;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, UserRole> colRole;

    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnResetPassword;

    private final UserService userService;
    private final ObservableList<User> userList = FXCollections.observableArrayList();

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        cbRole.setItems(FXCollections.observableArrayList(UserRole.values()));

        btnAddUser.setOnAction(e -> onAddUser());
        btnDelete.setOnAction(e -> onDeleteUser());
        btnEdit.setOnAction(e -> onEditUser());
        btnResetPassword.setOnAction(e -> onResetPassword());

        FxClipboardUtils.enableCopyShortcut(tblUsers);
        refreshUserList();
    }

    private void refreshUserList() {
        userList.setAll(userService.getAllUsers());
        tblUsers.setItems(userList);
    }

    private void onAddUser() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        UserRole role = cbRole.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            FxAlertUtils.warning("Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        try {
            userService.createUser(username, password, role);
            FxAlertUtils.info("✅ Đã thêm người dùng mới.");
            txtUsername.clear();
            txtPassword.clear();
            cbRole.getSelectionModel().clearSelection();
            refreshUserList();
        } catch (Exception e) {
            FxAlertUtils.error("❌ Lỗi khi thêm người dùng: " + e.getMessage());
        }
    }

    private void onDeleteUser() {
        User selected = tblUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxAlertUtils.warning("Vui lòng chọn người dùng để xóa.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Bạn có chắc chắn muốn xóa người dùng này?");
        confirm.setContentText("Tên đăng nhập: " + selected.getUsername());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            userService.deleteUser(selected.getUserId());
            FxAlertUtils.info("✅ Đã xóa người dùng.");
            refreshUserList();
        }
    }

    private void onEditUser() {
        User selected = tblUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxAlertUtils.warning("Vui lòng chọn người dùng để sửa.");
            return;
        }

        TextField txtNewUsername = new TextField(selected.getUsername());
        ComboBox<UserRole> cbNewRole = new ComboBox<>(FXCollections.observableArrayList(UserRole.values()));
        cbNewRole.setValue(selected.getRole());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Tên đăng nhập:"), 0, 0);
        grid.add(txtNewUsername, 1, 0);
        grid.add(new Label("Vai trò:"), 0, 1);
        grid.add(cbNewRole, 1, 1);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Cập nhật người dùng");
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                selected.setUsername(txtNewUsername.getText().trim());
                selected.setRole(cbNewRole.getValue());
                userService.updateUser(selected);
                FxAlertUtils.info("✅ Đã cập nhật thông tin người dùng.");
                refreshUserList();
            }
        });
    }

    private void onResetPassword() {
        User selected = tblUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxAlertUtils.warning("Vui lòng chọn người dùng để đặt lại mật khẩu.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Đặt lại mật khẩu");
        dialog.setHeaderText("Nhập mật khẩu mới cho người dùng:");
        dialog.setContentText("Mật khẩu mới:");

        dialog.showAndWait().ifPresent(newPass -> {
            userService.updatePassword(selected.getUserId(), newPass);
            FxAlertUtils.info("✅ Đã cập nhật mật khẩu.");
        });
    }

}
