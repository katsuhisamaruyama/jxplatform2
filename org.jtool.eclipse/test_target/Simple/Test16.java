class Test16 {

    public static void main(String argv[]) {
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
