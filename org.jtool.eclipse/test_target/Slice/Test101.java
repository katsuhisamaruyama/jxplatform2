class Test101 {
    private int p;
    
    public void m() {
        p = 10;
        A a = new A();
        a.setX(1);
        a.setX(2);
        int b = a.getX();
        int c = a.x + p;
        int d = p + 2;
    }
    
    private int getP() {
        return p;
    }
}

class A {
    public int x, y;
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getX() {
        return x;
    }
}
