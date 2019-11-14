
import static org.junit.Assert.*;
import org.junit.Test;

public class Sample2Test {

   @Test
    public void test1() {
        Circle circle = new Circle(100, 50, 20);
        assertEquals(circle.getX(), 100);
    }

    @Test
    public void test2() {
        Circle circle = new Circle(120, 40, 10);
        assertEquals(circle.getY(), 40);
    }
}
