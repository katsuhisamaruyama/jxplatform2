public class Test16 {

    public void m() {
        int a = 0;
        for (int i = 0; i < 10; i++) {
            if (i < 5) {
                a++;
                while (a < 20) {
                    a++;
                }
            } else {
                break;
            }
        }
        System.out.println(a);
    }
}
