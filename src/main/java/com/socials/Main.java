package com.socials;

import com.socials.dao.*;
import com.socials.model.*;
import com.socials.service.*;

import java.util.*;
import java.util.concurrent.*;

public class Main {
    private static final Scanner in = new Scanner(System.in);
    public static void main(String[] args) throws Exception {
        UsersDAO usersDAO = new UsersDAO();
        PostsDAO postsDAO = new PostsDAO();
        FollowsDAO followsDAO = new FollowsDAO();

        BlockingQueue<Post> queue = new LinkedBlockingQueue<>();

        // Ini adalah inti dari thread, yang akan memproses data
        NotificationService notif = new NotificationService(queue, 2);

        // Publisher untuk membuat postingan baru
        PostPublisher publisher = new PostPublisher(queue);

        System.out.println("==== Social Media Sederhana ====");
        printHelp();

        while (true) {
            System.out.print("> ");
            String line = in.nextLine().trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(" ", 2);
            String cmd = parts[0].toLowerCase();
            String arg = parts.length > 1 ? parts[1].trim() : "";

            try {
                switch (cmd) {
                    case "help": printHelp(); break;
                    case "exit":
                        notif.shutdown();
                        System.out.println("Sampai jumpa.");
                        return;
                    case "list-users":
                        // Judul implisit, hanya menampilkan daftar
                        usersDAO.getAll().forEach(u -> System.out.println("- " + u.getUsername()));
                        break;
                    case "add-user":
                        if (arg.isEmpty()) { System.out.println("Usage: add-user <username>"); break; }
                        usersDAO.insert(arg);
                        System.out.println("Pengguna ditambahkan (atau sudah ada): " + arg);
                        break;
                    case "follow":
                        // Usage: follow follower followee
                        {
                            String[] a = arg.split(" ");
                            if (a.length != 2){ System.out.println("Usage: follow <follower> <followee>"); break; }
                            followsDAO.follow(a[0], a[1]);
                            System.out.printf("%s sekarang mengikuti %s%n", a[0], a[1]);
                        }
                        break;
                    case "unfollow":
                        {
                            String[] a = arg.split(" ");
                            if (a.length != 2){ System.out.println("Usage: unfollow <follower> <followee>"); break; }
                            followsDAO.unfollow(a[0], a[1]);
                            System.out.printf("%s berhenti mengikuti %s%n", a[0], a[1]);
                        }
                        break;
                    case "post":
                        // Usage: post author|content
                        {
                            int sep = arg.indexOf('|');
                            if (sep < 0){ System.out.println("Usage: post <author>|<content>"); break; }
                            String author = arg.substring(0, sep).trim();
                            String content = arg.substring(sep + 1).trim();
                            Post p = new Post(author, content);
                            publisher.publish(p);
                            System.out.println("Terkirim.");
                        }
                        break;
                    case "feed":
                        // Usage: feed <username>
                        if (arg.isEmpty()){ System.out.println("Usage: feed <username>"); break; }
                        List<String> followees = followsDAO.getFollowees(arg);
                        if (followees.isEmpty()) { 
                            System.out.println("Tidak ada yang diikuti, menampilkan postingan terbaru:"); 
                            postsDAO.getRecent(10).forEach(System.out::println); 
                            break;
                        }
                        postsDAO.getPostsByAuthors(followees).forEach(System.out::println);
                        break;
                    case "recent":
                        postsDAO.getRecent(10).forEach(System.out::println);
                        break;
                    default:
                        System.out.println("Perintah tidak dikenal. Ketik 'help'.");
                }
            } catch (Exception e) {
                System.err.println("Kesalahan: " + e.getMessage());
            }
        }
    }

    private static void printHelp(){
        System.out.println("Daftar Perintah:");
        System.out.println(" help              - tampilkan bantuan");
        System.out.println(" exit              - keluar");
        System.out.println(" list-users        - tampilkan semua pengguna");
        System.out.println(" add-user <username> - buat pengguna baru");
        System.out.println(" follow <you> <them>   - pengguna (Anda) mengikuti pengguna lainnya (mereka)");
        System.out.println(" unfollow <you> <them> - berhenti mengikuti");
        System.out.println(" post <author>|<text>  - buat postingan (gunakan '|' sebagai pemisah)");
        System.out.println(" feed <username>     - tampilkan postingan dari yang pengguna yang anda ikuti");
        System.out.println(" recent            - tampilkan postingan terbaru");
    }
}