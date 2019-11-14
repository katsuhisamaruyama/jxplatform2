
public class Sample2 {
    public static void main(String[] args) {
        
        Circle circle1 = new Circle(100, 50, 20);
        System.out.println("Radius = " + circle1.radius);
        System.out.println("Radius = " + circle1.getRadius());
        circle1.print();
        
        Circle circle2 = new Circle(200, 180);
        System.out.println("Radius = " + circle2.getRadius());
        circle2.setRadius(80);
        System.out.println("Radius = " + circle2.getRadius());
        circle2.print();
    }
}

class Circle {
    private int x, y;
    int radius;
    
    Circle(int x, int y, int r) {
        this.x = x;
        this.y = y;
        radius = r;
    }
    
    Circle(int x, int y) {
        this(x, y, 50);
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setRadius(int r) {
        radius = r;
    }
    
    public int getRadius() {
        return radius;
    }
    
    public void print() {
        System.out.println("(x,y) = (" + x + "," + y + ") " + "r = " + radius);
    }
}
