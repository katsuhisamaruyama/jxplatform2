public class Test19{

    public static void main(String[] args) {
        int i = 0;
        int a = 0;
        int d = 3;

        while (i < 10) {
            while (i < 0)
                a = a + i + d;

            if (i > 0)
                a = a + i + d;

            while (i < 3)
                a = a - i + d;

            a = a + i;
            i++;
        }

        System.out.println(a);
        System.out.println(d);
    }
}
