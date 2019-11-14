
public class Sample7 {
    public static void main(String[] args) {
        int numbers[] = new int[10];
        numbers[0] = 3;
        
        try {
            numbers[20] = 0;
            System.out.println(numbers[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(e.toString());
        }
        
        System.out.println("Finish!");
    }
}
