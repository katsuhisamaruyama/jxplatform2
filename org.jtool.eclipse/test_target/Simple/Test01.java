public class Test01 {

    public static int ABC = 0;

    public void main() {
        int a = 0;
        int b = 0;
        if (a == 0) {
            System.out.println(a);
            a++;
        } else { 
            a++;
            a = b;
            System.out.println(a);
        }
        System.out.println(a);
        System.out.println(b);
    }
}
