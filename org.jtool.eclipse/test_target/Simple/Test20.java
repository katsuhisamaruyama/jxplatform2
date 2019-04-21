public class Test20 {
    private int priVar;
    private int priVar2 = 4;

    public Test20(int i){
        priVar = i;
    }

    public int get() {
        return priVar2;
    }

    public int calc(int a) {
        return a;
    }

    public static void main(String[] args) {
        Test20 t20 = new Test20(50);
        int a = t20.get();
        if (a > 5) {
            int b = t20.calc(a);
        }
        System.out.println(t20);
    }
}
