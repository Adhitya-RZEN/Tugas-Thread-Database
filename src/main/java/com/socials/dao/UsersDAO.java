package com.socials.dao;

import com.socials.model.User;
import java.sql.*;
import java.util.*;

public class UsersDAO {
    public List<User> getAll() {
        List<User> out = new ArrayList<>();
        String sql = "SELECT username FROM users ORDER BY username";
        try (Connection c = DBUtil.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) out.add(new User(rs.getString("username")));
        } catch (SQLException e){ e.printStackTrace(); }
        return out;
    }

    public boolean exists(String username){
        String sql = "SELECT 1 FROM users WHERE username=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e){ e.printStackTrace(); }
        return false;
    }

    public void insert(String username) throws SQLException {
        String sql = "INSERT IGNORE INTO users(username) VALUES (?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        }
    }
}
