class Test112 {

    public void m() {
        int x = 0;
        int y = 0;
        int z = 0;
        switch (x) {
        default:
            x = 10;
        case 1:
            y = 10;
            break;
        case 2:
            z = 20;
            break;
        }
        int p = y;
        int q = z;
        int r = x;
    }
}
