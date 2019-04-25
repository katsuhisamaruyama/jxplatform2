
import java.util.List;
import java.util.ArrayList;

class Customer {
    private String name = "";
    public double discount = 0;

    public Customer(String name) {
        this.name = name;
    }

    public String statement(Order order) {
        if (order == null) {
            return "No order";
        }

        if (order.getSize() > 1 && discount < 0.2) {
            discount = discount * 2;
        }

        int amount = getAmount(order);

        return name + "'s amount: " + amount;
    }

    public int getAmount(Order order) {
        int amount = 0;
        for (Rental rental : order.rentals) {
            amount += rental.getCharge(discount);
        }
        return amount;
    }
}

class Order {
    List<Rental>rentals = new ArrayList<Rental>();

    public void addRental(Rental rental) {
        rentals.add(rental);
    }

    public int getSize() {
        return rentals.size();
    }
}

class Rental {
    private int price;
    private int days;

    public Rental(int price, int days) {
        this.price = price;
        this.days = days;
    }

    public int getCharge(double discount) {
        double charge = Math.floor((price * days) * (1 - discount));
        return (int)charge;
    }
}