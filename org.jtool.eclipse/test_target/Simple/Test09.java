
import java.io.*;

public class Test09 {

    public void test(){
        int c = 1;
        int d = 1;

        int a = 5;
        int b = 0;

        int inA = 2;
        int inB = 3;
        int out = 3;

        while (b < 10) {
            inB = inA;
            d=1;
            while (a < 10){
                a = a + 1;
                c = a + d;
            }
            b = b + 1;
            inA = inB + a + c;
            c = inA + d;
        }

        System.out.println(out);
    }
}
