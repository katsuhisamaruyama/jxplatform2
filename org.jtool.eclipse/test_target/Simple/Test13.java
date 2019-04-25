public class Test13 {

    public void m() {
        int a = 2;
        int out = doReturn(a);
        System.out.println(out);
    }

    public int doReturn(int in) {
        while (in < 3) {
            in++;
            return in;
        }
        return 0;
    }
}
