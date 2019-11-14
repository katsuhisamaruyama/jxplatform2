
public class Sample8 {
    public static void main(String[] args) {
        try {
            Circle2 circle = new Circle2(0);
        } catch (ZeroRadiusException e) {
            System.out.println("Zero radius");
        }
    }
}

class Circle2 {
    int radius;
    
    Circle2(int r) throws ZeroRadiusException {
        if (r == 0) {
            throw new ZeroRadiusException();
        }
        radius = r;
    }
}

class ZeroRadiusException extends Exception {
}
