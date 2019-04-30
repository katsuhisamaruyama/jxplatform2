class Test115 {

    public void m() {
        int[] a = { 1, 2, 3, 4, 5 };
        int x = 0;
        int y = 1;
        for (int i = 0; i < 5; i++) {
            x = x + a[i];
            y = y * a[i];
        }
        int p = x;
        int q = y;
    }
}
