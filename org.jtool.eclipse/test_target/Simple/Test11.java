public class Test11 {

    public void m() {
        int a = 2;
        int out = doReturn(a);
        System.out.println(out);
    }

    public int doReturn(int in) {
        in++;
        if (in == 2) {
            return ++in;
        }
        return 0;
    }
}
