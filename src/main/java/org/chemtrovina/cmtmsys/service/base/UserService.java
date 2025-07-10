package org.chemtrovina.cmtmsys.service.base;


import org.chemtrovina.cmtmsys.model.User;
import org.chemtrovina.cmtmsys.model.enums.UserRole;

import java.util.List;

public interface UserService {
    User login(String username, String password);
    User findByUsername(String username);
    void updatePassword(int userId, String newPassword);
    List<User> getAllUsers();
    void createUser(String username, String password, UserRole role);
    void updateUser(User user);
    void deleteUser(int userId);
    void resetPassword(int userId, String newPassword);
}