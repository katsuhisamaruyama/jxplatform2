class Test116 {

    public void m() {
        int[] a = { 1, 2, 3, 4, 5 };
        int x = 0;
        int y = 1;
        for (int i = 0; i < 5; i++) {
            if (x > 2) {
                x += a[i];
            }
            if (y > 3) {
                y *= a[i];
            }
        }
        int p = x;
        int q = y;
    }
}
