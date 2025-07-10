package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.User;
import org.chemtrovina.cmtmsys.model.enums.UserRole;
import org.chemtrovina.cmtmsys.repository.base.UserRepository;
import org.chemtrovina.cmtmsys.service.base.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void updatePassword(int userId, String newPassword) {
        userRepository.updatePassword(userId, newPassword);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @Override
    public void createUser(String username, String password, UserRole role) {
        userRepository.createUser(username, password, role);
    }

    @Override
    public void updateUser(User user) {
        userRepository.updateUser(user);
    }

    @Override
    public void deleteUser(int userId) {
        userRepository.deleteUser(userId);
    }

    @Override
    public void resetPassword(int userId, String newPassword) {
        userRepository.resetPassword(userId, newPassword);
    }


}
