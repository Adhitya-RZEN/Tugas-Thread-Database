package com.socials.dao;

import java.sql.*;
import java.util.*;

public class FollowsDAO {
    public void follow(String follower, String followee) throws SQLException {
        String sql = "INSERT IGNORE INTO follows(follower, followee) VALUES (?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, follower);
            ps.setString(2, followee);
            ps.executeUpdate();
        }
    }

    public void unfollow(String follower, String followee) throws SQLException {
        String sql = "DELETE FROM follows WHERE follower=? AND followee=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, follower);
            ps.setString(2, followee);
            ps.executeUpdate();
        }
    }

    public List<String> getFollowees(String follower){
        List<String> out = new ArrayList<>();
        String sql = "SELECT followee FROM follows WHERE follower=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, follower);
            try (ResultSet rs = ps.executeQuery()){
                while (rs.next()) out.add(rs.getString("followee"));
            }
        } catch (SQLException e){ e.printStackTrace(); }
        return out;
    }

    public List<String> getFollowers(String followee){
        List<String> out = new ArrayList<>();
        String sql = "SELECT follower FROM follows WHERE followee=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, followee);
            try (ResultSet rs = ps.executeQuery()){
                while (rs.next()) out.add(rs.getString("follower"));
            }
        } catch (SQLException e){ e.printStackTrace(); }
        return out;
    }
}
