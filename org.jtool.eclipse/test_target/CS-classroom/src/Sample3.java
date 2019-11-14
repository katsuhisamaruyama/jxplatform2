
public class Sample3 {
    public static void main(String[] args) {
        
        Circle circle1 = new Circle(100, 50, 20);
        System.out.println("Radius = " + circle1.getRadius());
        circle1.print();
        
        BoldCircle circle2 = new BoldCircle(100, 50, 20, 2);
        System.out.println("Width = " + circle2.getWidth());
        System.out.println("Radius = " + circle2.getRadius());
        circle2.setRadius(30);
        System.out.println("Radius = " + circle2.getRadius());
        circle2.print();
    }
}

class BoldCircle extends Circle {
    private int width;
    
    BoldCircle(int x, int y, int r, int w) {
        super(x, y, r);
        width = w;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getRadius() {
        return radius + width;
    }
}
