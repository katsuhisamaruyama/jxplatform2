import java.io.*;

public class Test08 {

    public void test() {
        int a = 5;
        int b = 3;
        int c = 0;

        int d = 3;

        while (c < 10) {
            int i = 3;
            if (a < b) {
                c = c + 1;
                b = a;
                a = a + 1;
                i += a;
            } else if (b < a){
                c = c + 2;
                a = c;
                a = b;
                b = b+1;
            }
            System.out.println(i);
        }

        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
    }
}
