public class Test15 {
    
    public void m() {
        int a = 0;
        while (a < 30) {
            a++;
            for (int i = 0; i < 5; i++) {
                a++;
            }
        }
        System.out.println(a);
    }
}
