class Test124 {
    private int p;
    
    public void m() {
        int p = 10;
        int q = 20;
        AA a = new AA(p);
        int b = a.getX();
        int c = a.inc(q);
    }
}

class AA {
    public int x;

    public AA(int x) {
        this.x = x;
    }
    
    public int getX() {
        return x;
    }

    public int inc(int y) {
        return x + y;
    }
}
