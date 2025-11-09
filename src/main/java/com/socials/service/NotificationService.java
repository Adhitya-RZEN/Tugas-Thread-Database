package com.socials.service;

import com.socials.model.Post;
import com.socials.dao.FollowsDAO;

import java.util.concurrent.*;
import java.util.*;

public class NotificationService {
    private final BlockingQueue<Post> queue;
    private final ExecutorService workers; // disini adalah thread pool 
    private final FollowsDAO followsDAO;
    private volatile boolean running = true;

    public NotificationService(BlockingQueue<Post> queue, int workerThreads){
        this.queue = queue;
        // membuat thread pool dengan jumlah workerThreads
        this.workers = Executors.newFixedThreadPool(workerThreads);
        this.followsDAO = new FollowsDAO();
        startConsumer(); // memulai consumer thread
    }

    private void startConsumer(){
        // consumer thread untuk mengambil data dari antrian
        Thread consumer = new Thread(() -> {
            while (running || !queue.isEmpty()){
                try {
                    // ambil data dari antrian dengan timeout 1 detik
                    Post p = queue.poll(1, TimeUnit.SECONDS);
                    if (p == null) continue; // Jika 1 detik tidak ada data, ulangi loop
                    // Jika terdapat data, teruskan ke worker pool
                    List<String> followers = followsDAO.getFollowers(p.getAuthor());
                    for (String follower : followers) {
                        // kirim notifikasi menggunakan worker thread
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
        System.out.printf("[NOTIF] %s ada post baru dari %s: \"%s\"%n", username, p.getAuthor(), p.getContent());
    }

    public void shutdown(){
        running = false;
        workers.shutdown();
        try { if (!workers.awaitTermination(5, TimeUnit.SECONDS)) workers.shutdownNow(); } catch (InterruptedException ignored){ workers.shutdownNow(); }
    }
}
