package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.User;
import org.chemtrovina.cmtmsys.model.enums.UserRole;
import org.chemtrovina.cmtmsys.repository.RowMapper.UserRowMapper;
import org.chemtrovina.cmtmsys.repository.base.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User findByUsername(String username) {
        String sql = "SELECT * FROM Users WHERE Username = ?";
        List<User> list = jdbcTemplate.query(sql, new UserRowMapper(), username);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void updatePassword(int userId, String newPassword) {
        String sql = "UPDATE Users SET Password = ? WHERE UserId = ?";
        jdbcTemplate.update(sql, newPassword, userId);
    }

    @Override
    public List<User> getAllUsers() {
        return jdbcTemplate.query("SELECT * FROM Users", new UserRowMapper());
    }

    @Override
    public void createUser(String username, String password, UserRole role) {
        String sql = "INSERT INTO Users (Username, Password, Role) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, username, password, role.name());
    }

    @Override
    public void updateUser(User user) {
        String sql = "UPDATE Users SET Username = ?, Role = ? WHERE UserId = ?";
        jdbcTemplate.update(sql, user.getUsername(), user.getRole().name(), user.getUserId());
    }

    @Override
    public void deleteUser(int userId) {
        jdbcTemplate.update("DELETE FROM Users WHERE UserId = ?", userId);
    }

    @Override
    public void resetPassword(int userId, String newPassword) {
        updatePassword(userId, newPassword);
    }


}
