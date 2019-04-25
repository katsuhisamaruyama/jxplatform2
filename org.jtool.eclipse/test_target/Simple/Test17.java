public class Test17 {

    public void m() {
        int a = 0;
        int b = 0;

        while (b < 10) {
            if (a < 10) {
                switch (a) {
                case 0: a++;
                    break;
                case 1: a++;
                    break;
                case 2: a++;
                    break;
                }
            } else {
                switch (b) {
                case 0: b++;
                    break;
                case 1: b++;
                    break;
                case 2: b++;
                    break;
                }
            }
        }
    }
}
