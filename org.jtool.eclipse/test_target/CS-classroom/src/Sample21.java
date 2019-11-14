
public class Sample21 {
    public static void main(String[] args) {
        ThreadB th = new ThreadB();
        Thread thread = new Thread(th);
        thread.start();
        
        for (int i = 0; i < 10; i++) {
            System.out.println("main thread: " + i);
        }
    }
}

class ThreadB implements Runnable {
    public void run() {
        for (int i = 0; i < 10; i++) {
            System.out.println("my thread: " + i);
        }
    }
}
