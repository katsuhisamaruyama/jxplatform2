
public class Sample4 {
    public static void main(String[] args) {
        
        Circle circle = new Circle(100, 50, 20);
        ColoredCircle ccircle = new ColoredCircle(circle, "red");
        System.out.println("Color = " + ccircle.getColor());
        System.out.println("Radius = " + ccircle.getRadius());
        circle.print();
    }
}

class ColoredCircle {
    private Circle circle;
    private String color;
    
    ColoredCircle(Circle circle, String c) {
        this.circle = circle;
        color = c;
    }
    
    public String getColor() {
        return color;
    }
    
    public int getRadius() {
        return circle.getRadius();
    }
}
