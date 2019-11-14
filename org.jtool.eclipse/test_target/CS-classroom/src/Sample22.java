
public class Sample22 {
    public static void main(String[] args) {
        Storage storage = new Storage();
        
        Producer producer = new Producer(storage);
        Consumer consumer = new Consumer(storage);
        producer.start();
        consumer.start();
    }
}

class Storage {
    private String data = null;
    
    synchronized void put(String d) throws InterruptedException {
        while (data != null) {
            wait();
        }
        
        data = d;
        notifyAll();
    }
    
    synchronized String take() throws InterruptedException {
        while (data == null) {
            wait();
        }
        
        String r = new String(data);
        data = null;
        notifyAll();
        return r;
    }
}

class Producer extends Thread {
    private Storage storage;
    
    Producer(Storage storage) {
        this.storage = storage;
    }
    
    public void run() {
        try {
            for (int i = 0; i < 100; i++) {
                String data = String.valueOf(i);
                storage.put(data);
                System.out.println("put: " + data);
                
                Thread.sleep((int)Math.random() * 1000);
            }
            storage.put("END");
        } catch (InterruptedException e) { /* empty */ }
    }
}

class Consumer extends Thread {
    private Storage storage;
    
    Consumer(Storage storage) {
        this.storage = storage;
    }
    
    public void run() {
        try {
            while (true) {
                String data = storage.take();
                if (data.equals("END")) {
                    break;
                }
                System.out.println("take: " + data);
                
                Thread.sleep((int)Math.random() * 1000);
            }
        } catch (InterruptedException e) { /* empty */ }
    }
}

