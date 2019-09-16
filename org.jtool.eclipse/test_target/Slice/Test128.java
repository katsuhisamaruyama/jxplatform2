class Test128 {
    private int p = 0;

    public void m() {
        A3 a = new A3();
        int p = 0;
        a.setY(2);
        int r = n(0, a.add(p).getY());
    }

    public int n(int x, int y) {
        return y + 4;
    }
}

class A3 {

    static int z;
    int y = 0;

    public A3() {
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getY() {
        return y;
    }        
    
    public AAA add(int x) {
        AAA ret = new AAA();
        ret.setY(y + x);
        return ret;
    }
}
