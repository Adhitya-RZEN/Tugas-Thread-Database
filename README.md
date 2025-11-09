## SISTEM SOCIAL MEDIA SEDERHANA ##

**NOTE:** Pada kode yang telah saya buat, disini menggunakan maven sebagai tools untuk menghubungkan database dengan kode agar lebih mudah. 

Pada kode ini terdapat 3 buah folder yang masing masing mempunyai dikategorikan sebagai:
1. **DAO** - Dimana folder ini berisikan file class yang menghandle query-query mysql untuk database yang ada.
2. **Model** - Dimana folder ini berisikan file class yang berfungsi sebagai blueprints untuk objek-objek yang akan dibuat nantinya, yaitu Post dan User.
3. **Service** - Dimana folder ini berisikan logika utama dari thread yang digunakan. Terdapat dua file yaitu `NotificationService` dan `PostPublisher`. Fungsi dari `PostPublisher` pada inti  nya adalah untuk meng-upload post baru, dimana method yang ada pada `PostPublisher` akan mengambil data dari `class Post` dan memasukannya ke dalam database menggunakan `PostDAO.insert(p)`. Setelah berhasil di simpan maka sistem akan men-set/mendapatkan id nya dan post tersebut akan diletakan di antrean (`BlockingQueue`) menggunakan `queue.offer(p)`. 

**Contoh Kode**
```java
public class PostPublisher {
    private final BlockingQueue<Post> queue;
    private final PostsDAO postsDAO;

    public PostPublisher(BlockingQueue<Post> queue){
        this.queue = queue;
        this.postsDAO = new PostsDAO();
    }

    public void publish(Post p) {
        try {
            // masukan data ke DB serta menset id
            int id = postsDAO.insert(p);
            p.setId(id);
            queue.offer(p); // berfungsi untuk memasukan antrian untuk notifikasi
        } catch (Exception e) {
            System.err.println("Gagal dalam membuat post baru: " + e.getMessage());
        }
    }
}
```
Sedangkan fungsi dari `NotificationService` adalah untuk mengawasi antrean yang ada pada `BlockingQueue` dan mengirimkan notifikasi di latar belakang (background). Cara kerja dari `NotificationService` adalah, prosesnya langsung menjalankan satu thread "Notification-Consumer" di latar belakang yang akan menunggu di antrean menggunakan `queue.poll(1, TimeUnit.SECONDS)`, "tidur" jika antrean kosong. Begitu `PostPublisher` memasukkan post baru, thread Consumer ini "bangun", mengambil post tersebut, dan segera melakukan pekerjaannya. Dimana program akan mencari siapa saja yang mengikuti penulis post (`followsDAO.getFollowers`), dan untuk setiap pengikut, akan menyuruh "thread pekerja" dari `ExecutorService` untuk menjalankan `deliverNotification` dengan memanggil workers.submit(...). Sehingga, jika user memiliki 1.000 pengikut, proses pengiriman 1.000 notifikasi itu terjadi di thread pekerja di latar belakang dan tidak memperlambat thread Consumer utama yang harus siap mengambil pekerjaan berikutnya dari antrean.

**Contoh Kode**
```java
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
}
```