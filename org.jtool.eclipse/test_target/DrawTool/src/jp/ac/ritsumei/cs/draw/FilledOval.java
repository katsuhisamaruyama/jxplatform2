
package jp.ac.ritsumei.cs.draw;

import java.awt.*;

/**
 * Represents a filled oval.
 */
public class FilledOval extends Oval {
    
    /**
     * The constant value of the name of this figure.
     */
    public static final String name = "FilledOval";
    
    /**
     * Creates a new object.
     */
    public FilledOval() {
        super();
    }
    
    /**
     * Creates a new object.
     * @param c the color of this figure
     * @param w the width of the outline of this figure
     */
    public FilledOval(Color c, float w) {
        super(c, w);
    }
    
    /**
     * Creates a new object.
     * @param c the color of this figure
     */
    public FilledOval(Color c) {
        super(c);
    }
    
    /**
     * Creates a new object.
     * @param w the width of the outline of this figure
     */
    public FilledOval(int w) {
        super(w);
    }
    
    @Override
    public Figure create(Color c, float w) {
        return new FilledOval(c, w);
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Figure createClone() {
        Figure figure = new FilledOval(color, outlineWidth);
        figure.setStart(startX, startY);
        figure.setEnd(endX, endY);
        return figure;
    }
    
    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(color);
        g2.setStroke(new BasicStroke(outlineWidth));
        g.fillOval(getLeft(), getTop(), getWidth(), getHeight());
    }
    
    @Override
    public boolean isGrabbed(int x, int y) {
        return isInOval(x, y, getLeft(), getTop(), getWidth(), getHeight());
    }
    
    @Override
    public void paintIcon(Graphics g, int x, int y, int w, int h) {
        g.fillOval(x + 2, y + h / 2 - 6, w - 4, h - 4);
    }
}
