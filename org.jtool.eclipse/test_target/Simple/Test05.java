import java.io.*;

public class Test05 {

    public void test() {
        int a = 0;
        int b = 0;
        int c = 0;
        int d = 10;

        if (c < (d = a + b + c)) {
            while (b < 10) {
                b = b + 1;
            }
        } else {
            while ((a++) < 10) {
                System.out.println(a);
            }
        }

        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        System.out.println(d);
    }
}
