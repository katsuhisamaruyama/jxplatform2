class Test13 {

    public static void main (String argv[]) {
        int a = 2;
        int out = Test13.doReturn(a);
        System.out.println(out);
    }

    public static int doReturn(int in) {
        while (in < 3) {
            in++;
            return in;
        }
        return 0;
    }
}
