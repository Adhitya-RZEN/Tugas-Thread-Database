package com.socials.dao;

import com.socials.model.Post;
import java.sql.*;
import java.util.*;

public class PostsDAO {
    public int insert(Post p) throws SQLException {
        String sql = "INSERT INTO posts(author, content, created_at) VALUES (?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getAuthor());
            ps.setString(2, p.getContent());
            ps.setString(3, java.time.LocalDateTime.now().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("No generated key");
    }

    public List<Post> getPostsByAuthors(List<String> authors){
        if (authors == null || authors.isEmpty()) return Collections.emptyList();
        StringBuilder q = new StringBuilder("SELECT id, author, content, created_at FROM posts WHERE author IN (");
        StringJoiner sj = new StringJoiner(",");
        for (int i=0;i<authors.size();i++) sj.add("?");
        q.append(sj.toString()).append(") ORDER BY created_at DESC");
        List<Post> out = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(q.toString())) {
            for (int i=0;i<authors.size();i++) ps.setString(i+1, authors.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(new Post(rs.getInt("id"), rs.getString("author"), rs.getString("content"), rs.getString("created_at")));
            }
        } catch (SQLException e){ e.printStackTrace(); }
        return out;
    }

    public List<Post> getRecent(int limit){
        List<Post> out = new ArrayList<>();
        String sql = "SELECT id, author, content, created_at FROM posts ORDER BY created_at DESC LIMIT ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()){
                while (rs.next()) out.add(new Post(rs.getInt("id"), rs.getString("author"), rs.getString("content"), rs.getString("created_at")));
            }
        } catch (SQLException e){ e.printStackTrace(); }
        return out;
    }
}
