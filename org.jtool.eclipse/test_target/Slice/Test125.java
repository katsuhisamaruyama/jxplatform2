class Test125 {
    private int p = 0;

    public void m() {
        int p = 0;
        int q = inc1(p);
        int r = inc2(inc1(p));
    }

    public int inc1(int x) {
        return x + 1;
    }

    public int inc2(int x) {
        return x + 2;
    }
}
