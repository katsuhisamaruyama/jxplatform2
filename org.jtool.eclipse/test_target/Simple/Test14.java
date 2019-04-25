public class Test14 {
    private static boolean flag = true;

    public void m() {
        int a = 5;
        int b = 3;
        int c = 1;
        String str = "abc";
        
        while (flag) {
            a = b + c + 1;
            if (a % 2 == 0)
                break;
        }

        flag = true;
        c = 3;

        while (flag) {
            b = a + c;
            a = a + 1;
            if (a % 2 == 0)
                break;
        }

        System.out.println(a + " " + b + " " + c);
        System.out.println(str);
    }
}
