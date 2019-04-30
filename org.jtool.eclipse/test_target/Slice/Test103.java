class Test103 {
    private int a;
    
    public void m() {
        a = 1;
        a = 2;
        int p = a;
        setA(1);
        setA(2);
        int q = a;
        int r = getA();
        int s = getA();
        incA();
        incA();
        int t = getA();
    }

    private void setA(int a) {
        this.a = a;
    }

    private int getA() {
        return a;
    }
    
    private void incA() {
        a++;
    }
}
