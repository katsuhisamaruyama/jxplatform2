class Test105 {
    private int a;
    
    public void m() {
        int x = setA(1);
        int y = x;
        int z = a;
    }

    private int setA(int a) {
        this.a = a;
        return a;
    }
}
