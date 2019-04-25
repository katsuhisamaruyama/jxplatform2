
import static org.junit.Assert.*;
import org.junit.Test;

public class CustomerTest {

    @Test
    public void testStatement1() {
        Customer customer = new Customer("C1");
        customer.discount = 0.1;
        Order order = new Order();
        Rental movie = new Rental(200, 2);
        order.addRental(movie);
        
        String message = customer.statement(order);
        assertEquals(message, "C1's amount: 360");
    }

    @Test
    public void testStatement2() {
        Customer customer = new Customer("C2");
        customer.discount = 0.1;
        Order order = new Order();
        Rental movie = new Rental(200, 3);
        Rental music = new Rental(100, 3);
        order.addRental(movie);
        order.addRental(music);

        String message = customer.statement(order);
        assertEquals(message, "C2's amount: 720");
    }

    @Test
    public void testStatement3() {
        Customer customer = new Customer("C3");
        customer.discount = 0.2;
        Order order = new Order();

        String message = customer.statement(order);
        assertEquals(message, "C3's amount: 0");
    }

    @Test
    public void testStatement4() {
        Customer customer = new Customer("C4");
        customer.discount = 0.2;
        Order order = null;
        
        String message = customer.statement(order);
        assertEquals(message, "No order");
    }
}
