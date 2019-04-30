class Test102 {

    public void m() {
        int x = inc(10);
        int y = 0;
        int z = inc(y);
        int p = x;
        int q = y;
        int r = z;
        System.out.println(z);
    }

    public int inc(int n) {
        return n + 1;
    }
}
