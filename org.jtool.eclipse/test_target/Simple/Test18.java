public class Test18 {

    public void m() {
        int a = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                a++;
                break;
            }
            if (a < 10) {
                a++;
                break;
            }
        }
    }
}
