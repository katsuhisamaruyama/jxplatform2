class Test119 {
    private int p;
    
    public void m() {
        p = 10;
        A a = new A();
        a.setX(1);
        int b = a.getX();
        a.setX(2);
        int c = a.getX();
        int d = a.x + p;
        int e = getP() + 2;
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
