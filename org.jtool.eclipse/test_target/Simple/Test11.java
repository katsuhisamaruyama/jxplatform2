class Test11 {

    public static void main(String argv[]) {

        int a = 2;
        int out = Test11.doReturn(a);
        System.out.println(out);
    }

    public static int doReturn(int in) {
        in++;
        if (in == 2) {
            return ++in;
        }
        return 0;
    }
}
