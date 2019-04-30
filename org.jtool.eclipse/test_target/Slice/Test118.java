class Test118 {

    public void m() {
        int x = m0(1, 2, 3);
        int y = m1(1, 2, 3);
        int z = m2(1, 2, 3);
        int p = m3(1, 2, 3);
        int q = m4(1, 2, 3);
        int r = m5(1, 2, 3);
        int s = m6(1, 2, 3);
        int t = m7(1, 2, 3);
    }

    public int m0(int a, int b, int c) {
        return 1;
    }

    public int m1(int a, int b, int c) {
        return a + 1;
    }

    public int m2(int a, int b, int c) {
        return b + 1;
    }

    public int m3(int a, int b, int c) {
        return c + 1;
    }

    public int m4(int a, int b, int c) {
        return a + b;
    }

    public int m5(int a, int b, int c) {
        return b + c;
    }

    public int m6(int a, int b, int c) {
        return a + c;
    }

    public int m7(int a, int b, int c) {
        return a + b + c;
    }
}
