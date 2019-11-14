
public class Sample20 {
    public static void main(String[] args) {
        ThreadA thread = new ThreadA();
        thread.start();
        
        for (int i = 0; i < 10; i++) {
            System.out.println("main thread: " + i);
        }
    }
}
  
class ThreadA extends Thread {
    public void run() {
        for (int i = 0; i < 10; i++) {
            System.out.println("my thread: " + i);
        }
    }
}