
public class Sample5 {
    public static void main(String[] args) {
        Shape shape;
        
        Line line = new Line(100, 50, 200, 80);
        line.draw();
        
        Rectangle rectangle = new Rectangle(100, 50, 200, 80);
        rectangle.draw();
        
        shape = line;
        shape.draw();
        
        shape = rectangle;
        shape.draw();
    }
}

abstract class Shape {
    protected int x1, y1;
    protected int x2, y2;
    
    Shape(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }
    
    abstract public void draw();
}

class Line extends Shape {
    
    Line(int x1, int y1, int x2, int y2) {
        super(x1, y1, x2, y2);
    }
    
    public void draw() {
        System.out.println("Line");
    }
}

class Rectangle extends Shape {

    Rectangle(int x1, int y1, int x2, int y2) {
        super(x1, y1, x2, y2);
    }
    
    public void draw() {
        System.out.println("Rectangle");
    }
}
