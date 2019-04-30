class Test120 {
    
    public void m() {
        O o = new O();
        int p = m0(o.x, o.y);
        int q = m1(o.x, o.y);
        int r = m2(o.x, o.y);
        int s = m3(o.x, o.y);
    }    

    public int m0(int a, int b) {
        return 1;
    }

    public int m1(int a, int b) {
        return a + 1;
    }

    public int m2(int a, int b) {
        return b + 1;
    }

    public int m3(int a, int b) {
        return a + b;
    }
}

class O {
    int x = 1;
    int y = 2;
}
