class Test127 {
    private int p = 0;

    public void m() {
        A2 a = new A2();
        a.setY(2);
        A2.z = 1;
        int p = a.getY();
        int q = A2.z;
    }
}

class A2 {

    static int z;
    int y = 0;

    public A2() {
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getY() {
        return y;
    }        
}
