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
        NotificationService notif = new NotificationService(queue, 2);
        PostPublisher publisher = new PostPublisher(queue);

        System.out.println("==== Simple Social CLI ====");
        printHelp();

        while (true) {
            System.out.print("> ");
            String line = in.nextLine().trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(" ", 2);
            String cmd = parts[0].toLowerCase();
            String arg = parts.length>1?parts[1].trim():"";

            try {
                switch (cmd) {
                    case "help": printHelp(); break;
                    case "exit":
                        notif.shutdown();
                        System.out.println("Bye.");
                        return;
                    case "list-users":
                        usersDAO.getAll().forEach(u -> System.out.println("- " + u.getUsername()));
                        break;
                    case "add-user":
                        if (arg.isEmpty()) { System.out.println("Usage: add-user <username>"); break; }
                        usersDAO.insert(arg);
                        System.out.println("User added (or already exists): " + arg);
                        break;
                    case "follow":
                        // usage: follow follower followee
                        {
                            String[] a = arg.split(" ");
                            if (a.length!=2){ System.out.println("Usage: follow <follower> <followee>"); break; }
                            followsDAO.follow(a[0], a[1]);
                            System.out.printf("%s now follows %s%n", a[0], a[1]);
                        }
                        break;
                    case "unfollow":
                        {
                            String[] a = arg.split(" ");
                            if (a.length!=2){ System.out.println("Usage: unfollow <follower> <followee>"); break; }
                            followsDAO.unfollow(a[0], a[1]);
                            System.out.printf("%s unfollowed %s%n", a[0], a[1]);
                        }
                        break;
                    case "post":
                        // usage: post author|content
                        {
                            int sep = arg.indexOf('|');
                            if (sep<0){ System.out.println("Usage: post <author>|<content>"); break; }
                            String author = arg.substring(0, sep).trim();
                            String content = arg.substring(sep+1).trim();
                            Post p = new Post(author, content);
                            publisher.publish(p);
                            System.out.println("Posted.");
                        }
                        break;
                    case "feed":
                        // usage: feed <username>
                        if (arg.isEmpty()){ System.out.println("Usage: feed <username>"); break; }
                        List<String> followees = followsDAO.getFollowees(arg);
                        if (followees.isEmpty()) { System.out.println("No followees, showing recent posts:"); postsDAO.getRecent(10).forEach(System.out::println); break;}
                        postsDAO.getPostsByAuthors(followees).forEach(System.out::println);
                        break;
                    case "recent":
                        postsDAO.getRecent(10).forEach(System.out::println);
                        break;
                    default:
                        System.out.println("Unknown command. Type help.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private static void printHelp(){
        System.out.println("Commands:");
        System.out.println(" help                   - show this");
        System.out.println(" exit                   - quit");
        System.out.println(" list-users             - list all users");
        System.out.println(" add-user <username>    - create user");
        System.out.println(" follow <you> <them>    - you follows them");
        System.out.println(" unfollow <you> <them>  - unfollow");
        System.out.println(" post <author>|<text>   - create post (use | to separate)");
        System.out.println(" feed <username>        - show posts from your followees");
        System.out.println(" recent                 - show recent posts");
    }
}
