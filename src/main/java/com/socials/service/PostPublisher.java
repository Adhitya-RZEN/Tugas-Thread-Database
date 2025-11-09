package com.socials.service;

import com.socials.model.Post;
import com.socials.dao.PostsDAO;

import java.util.concurrent.BlockingQueue;

public class PostPublisher {
    private final BlockingQueue<Post> queue;
    private final PostsDAO postsDAO;

    public PostPublisher(BlockingQueue<Post> queue){
        this.queue = queue;
        this.postsDAO = new PostsDAO();
    }

    public void publish(Post p) {
        try {
            // insert to DB and retrieve id
            int id = postsDAO.insert(p);
            p.setId(id);
            queue.offer(p); // notify background service
        } catch (Exception e) {
            System.err.println("Failed to publish post: " + e.getMessage());
        }
    }
}
