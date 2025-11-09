package com.socials.service;

import com.socials.model.Post;
import com.socials.dao.FollowsDAO;

import java.util.concurrent.*;
import java.util.*;

public class NotificationService {
    private final BlockingQueue<Post> queue;
    private final ExecutorService workers;
    private final FollowsDAO followsDAO;
    private volatile boolean running = true;

    public NotificationService(BlockingQueue<Post> queue, int workerThreads){
        this.queue = queue;
        this.workers = Executors.newFixedThreadPool(workerThreads);
        this.followsDAO = new FollowsDAO();
        startConsumer();
    }

    private void startConsumer(){
        // single producer-consumer loop that dispatches notification tasks to threadpool
        Thread consumer = new Thread(() -> {
            while (running || !queue.isEmpty()){
                try {
                    Post p = queue.poll(1, TimeUnit.SECONDS);
                    if (p == null) continue;
                    List<String> followers = followsDAO.getFollowers(p.getAuthor());
                    for (String follower : followers) {
                        workers.submit(() -> deliverNotification(follower, p));
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Notification-Consumer");
        consumer.setDaemon(true);
        consumer.start();
    }

    private void deliverNotification(String username, Post p){
        // sederhana: print ke console
        System.out.printf("[NOTIF] %s got new post from %s: \"%s\"%n", username, p.getAuthor(), p.getContent());
    }

    public void shutdown(){
        running = false;
        workers.shutdown();
        try { if (!workers.awaitTermination(5, TimeUnit.SECONDS)) workers.shutdownNow(); } catch (InterruptedException ignored){ workers.shutdownNow(); }
    }
}
