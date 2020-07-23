public class Test23 {

    public void m() {
        int a = 10;
        int x = inc(dec(a));
    }
    
    public int inc(int num) {
        return num + 1;
    }
    
    public int dec(int num) {
        return num - 1;
    }
}
