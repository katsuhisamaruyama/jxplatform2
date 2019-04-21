
package jp.ac.ritsumei.cs.draw;

import java.awt.*;

/**
 * Represents a filled rectangle.
 */
public class FilledRect extends Rect {
    
    /**
     * The constant value of the name of this figure.
     */
    public static final String name = "FilledRect";
    
    /**
     * Creates a new object.
     */
    public FilledRect() {
        super();
    }
    
    /**
     * Creates a new object.
     * @param c the color of this figure
     * @param w the width of the outline of this figure
     */
    public FilledRect(Color c, float w) {
        super(c, w);
    }
    
    /**
     * Creates a new object.
     * @param c the color of this figure
     */
    public FilledRect(Color c) {
        super(c);
    }
    
    /**
     * Creates a new object.
     * @param w the width of the outline of this figure
     */
    public FilledRect(int w) {
        super(w);
    }
    
    @Override
    public Figure create(Color c, float w) {
        return new FilledRect(c, w);
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Figure createClone() {
        Figure figure = new FilledRect(color, outlineWidth);
        figure.setStart(startX, startY);
        figure.setEnd(endX, endY);
        return figure;
    }
    
    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(color);
        g2.setStroke(new BasicStroke(outlineWidth));
        g2.fillRect(getLeft(), getTop(), getWidth(), getHeight());
    }
    
    @Override
    public boolean isGrabbed(int x, int y) {
        return isInRect(x, y, getLeft(), getTop(), getRight(), getBottom());
    }
    
    @Override
    public void paintIcon(Graphics g, int x, int y, int w, int h) {
        g.fillRect(x + 2, y + h / 2 - 6, w - 3, h - 3);
    }
}
