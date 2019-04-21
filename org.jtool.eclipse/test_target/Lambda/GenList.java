
import java.util.Random;
import java.util.StringTokenizer;
import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class GenList {
    
    private static int LIST_SIZE = 1000000;
    private static int TITLE_LEN = 10;
    private static int RATIO = 50;
    private static String FILENAME = "bookdata" + String.valueOf(RATIO) + ".txt";
    
    public static void main(String[] args) {
        Random random = new Random();
        List<GeneratedBookData> data = new ArrayList<GeneratedBookData>(LIST_SIZE);
        
        int num;
        if (RATIO == 0) {
            num = 0;
        } else {
            num  = LIST_SIZE * RATIO / 100;
        }
        for (int i = 0; i < num; i++) {
            String title = getTitle(random);
            long seq = Math.abs(random.nextLong());
            data.add(new GeneratedBookData(title, 0, seq));
        }
        
        num = LIST_SIZE - num;
        for (int i = 0; i < num; i++) {
            String title = getTitle(random);
            int size = random.nextInt(60000) + 100;
            long seq = Math.abs(random.nextLong());
            data.add(new GeneratedBookData(title, size, seq));
        }
        
        GeneratedBookData.sort(data);
        
        store(FILENAME, data);
    }
    
    private static String getTitle(Random random) {
        StringBuilder title = new StringBuilder(TITLE_LEN);
        for (int i = 0; i < TITLE_LEN; i++) {
            char ch = (char)('a' + random.nextInt(26));
            title.append(ch);
        }
        return title.toString();
    }
    
    static void store(String filename, List<GeneratedBookData> data) {
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)))) {
            for (GeneratedBookData d : data) {
                pw.println(d.getTitle() + " " + d.getSize());
            }
        } catch (IOException e) {
            System.err.println("Cannot write: " + filename);
        }
    }
    
    static List<BookData> load(String filename) {
        List<BookData> bookdata = new ArrayList<BookData>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line);
                String title = st.nextToken();
                int size = Integer.parseInt(st.nextToken());
                
                if (size == 0) {
                    bookdata.add(new BookData(title));
                } else {
                    bookdata.add(new BookData(title, size));
                }
            }
            return bookdata;
            
        } catch (FileNotFoundException e) {
            System.err.println("File Not Found: " + filename);   
        } catch (IOException e) {
            System.err.println("Cannot read: " + filename);
        }
        bookdata.clear();
        return bookdata;
    }
}
