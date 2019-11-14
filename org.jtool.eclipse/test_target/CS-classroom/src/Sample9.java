
import java.io.*;

public class Sample9 {
    public static void main(String[] args) {
        String filename = "test9.txt";
        
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)))) {
            pw.println("Enjoy");
            pw.println("Java programming");
            
        } catch (IOException e) {
            System.out.println("Cannot write: " + filename);
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found: " + filename);
        } catch (IOException e) {
            System.out.println("Cannot read: " + filename);
        }
    }
}
