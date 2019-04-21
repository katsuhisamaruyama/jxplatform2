
import java.util.function.*;

public class Test {

    public Test() {
    }

    public static String print(String str) {
        return "{" + str + "}";
    }

    public static String print(String str, String mark) {
        return mark + str + mark;
    }

    public String run1() {
        UnaryOperator<String> func = str -> Test.print(str);
        String res = func.apply("abc");
        return res;
    }

    public String run2() {
        UnaryOperator<String> func = Test::print;
        String res = func.apply("abc");
        return res;
    }

    public void run21() {
        apply(Test::print);
    }

    private void apply(UnaryOperator<String> func) {
        func.apply("abc");
    }

    public String run3() {
        BinaryOperator<String> func = (str, mark) -> Test.print(str, mark);
        String res = func.apply("abc", "*");
        return res;
    }

    public String run4() {
        BinaryOperator<String> func = Test::print;
        String res = func.apply("abc", "*");
        return res;
    }

    public String run5() {
        Function<String, String> func = str -> str.toUpperCase();
        String res = func.apply("abc");
        return res;
    }

    public String run6() {
        Function<String, String> func = String::toUpperCase;
        String res = func.apply("abc");
        return res;
    }

    public static void main(String argv[]) {
        Test test = new Test();
        System.out.println(test.run1());
        System.out.println(test.run2());
        System.out.println(test.run3());
        System.out.println(test.run4());
        System.out.println(test.run5());
        System.out.println(test.run6());
    }
}
