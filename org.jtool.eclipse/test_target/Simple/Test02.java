public class Test02 {

    public void m() {
        int a = 0;
        int b = 0;
        int c = 0;
        int d = 0;

        while (a < 5 + d) {
            c = a;
            a++;
            b += c;
            System.out.println(a);
            System.out.println(c);
        }

        System.out.println(b);
        System.out.println(d);
    }
}
