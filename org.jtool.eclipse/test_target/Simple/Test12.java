class Test12 {

    public static void main (String argv[]) {
        int a = 0;
        int b = 2;

        while (a < 3) {
            a++;
            b++;
            System.out.println(a + b);
            break;
        }
        a = a + b;
        System.out.println(a);
        System.out.println(b);
    }
}
