class Test126 {
    private int p = 0;

    public void m() {
        AAA a = new AAA();
        int p = 0;
        a.y = 1;
        AAA a2 = a.add(p);
        a2.y = 2;
        int q = a2.getY();
        int r = a.add(p).getY();
    }
}

class AAA {
    int y = 0;

    public AAA() {
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
