
package jp.ac.ritsumei.cs.draw;

import java.awt.*;

/**
 * Represents a rectangle.
 */
public class Rect extends Figure {
    
    /**
     * The constant value of the name of this figure.
     */
    public static final String name = "Rect";
    
    /**
     * Creates a new object.
     */
    public Rect() {
        super();
    }
    
    /**
     * Creates a new object.
     * @param c the color of this figure
     * @param w the width of the outline of this figure
     */
    public Rect(Color c, float w) {
        super(c, w);
    }
    
    /**
     * Creates a new object.
     * @param c the color of this figure
     */
    public Rect(Color c) {
        super(c);
    }
    
    /**
     * Creates a new object.
     * @param w the width of the outline of this figure
     */
    public Rect(int w) {
        super(w);
    }
    
    @Override
    public Figure create(Color c, float w) {
        return new Rect(c, w);
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Figure createClone() {
        Figure figure = new Rect(color, outlineWidth);
        figure.setStart(startX, startY);
        figure.setEnd(endX, endY);
        return figure;
    }
    
    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(color);
        g2.setStroke(new BasicStroke(outlineWidth));
        g.drawRect(getLeft(), getTop(), getWidth(), getHeight());
    }
    
    @Override
    public void drawRubber(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(color);
        g2.drawRect(getLeft(), getTop(), getWidth(), getHeight());
    }
    
    @Override
    public void drawOutline(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(color);
        g2.setStroke(dotted);
        g2.drawRect(getLeft(), getTop(), getWidth(), getHeight());
    }
    
    @Override
    public boolean isGrabbed(int x, int y) {
        int ox1 = getLeft() - ALLOWABLE_GAP_WHEN_GRABBED;
        int oy1 = getTop() - ALLOWABLE_GAP_WHEN_GRABBED;
        int ox2 = getRight() + ALLOWABLE_GAP_WHEN_GRABBED;
        int oy2 = getBottom() + ALLOWABLE_GAP_WHEN_GRABBED;
        if (!isInRect(x, y, ox1, oy1, ox2, oy2)) {
            return false;
        }
        
        int ix1 = getLeft() + ALLOWABLE_GAP_WHEN_GRABBED;
        int iy1 = getTop() + ALLOWABLE_GAP_WHEN_GRABBED;
        int ix2 = getRight() - ALLOWABLE_GAP_WHEN_GRABBED;
        int iy2 = getBottom() - ALLOWABLE_GAP_WHEN_GRABBED;
        
        return !isInRect(x, y, ix1, iy1, ix2, iy2);
    }
    
    protected boolean isInRect(int x, int y, int x1, int y1, int x2, int y2) {
        return x1 <= x && x <= x2 && y1 <= y && y <= y2;
    }
    
    @Override
    public void drawGrabbedFigure(Graphics g) {
        g.setColor(color);
        drawGrabbedMark(g, getLeft(), getTop());
        drawGrabbedMark(g, getLeft(), getBottom());
        drawGrabbedMark(g, getRight(), getTop());
        drawGrabbedMark(g, getRight(), getBottom());
    }
    
    @Override
    public void paintIcon(Graphics g, int x, int y, int w, int h) {
        g.drawRect(x + 2, y + h / 2 - 6, w - 4, h - 4);
    }
}
