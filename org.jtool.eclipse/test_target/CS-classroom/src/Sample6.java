
import java.util.ArrayList;

public class Sample6 {
    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<String>();
        list.add(new String("Java"));
        list.add(new String("Programming"));
        list.add(new String("Language")); 
        System.out.println("size = " + list.size());
        
        String first = list.get(0);
        System.out.println(first);
        
        System.out.println("size = " + list.size());
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
        
        list.remove(1);
        
        System.out.println("size = " + list.size());
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
    }
}
