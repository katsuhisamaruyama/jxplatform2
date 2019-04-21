
package jp.ac.ritsumei.cs.draw;

import java.awt.*;

/**
 * Represents an oval.
 */
public class Oval extends Figure {
    
    /**
     * The constant value of the name of this figure.
     */
    public static final String name = "Oval";
    
    /**
     * Creates a new object.
     */
    public Oval() {
        super();
    }
    
    /**
     * Creates a new object.
     * @param c the color of this figure
     * @param w the width of the outline of this figure
     */
    public Oval(Color c, float w) {
        super(c, w);
    }
    
    /**
     * Creates a new object.
     * @param c the color of this figure
     */
    public Oval(Color c) {
        super(c);
    }
    
    /**
     * Creates a new object.
     * @param w the width of the outline of this figure
     */
    public Oval(int w) {
        super(w);
    }
    
    @Override
    public Figure create(Color c, float w) {
        return new Oval(c, w);
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Figure createClone() {
        Figure figure = new Oval(color, outlineWidth);
        figure.setStart(startX, startY);
        figure.setEnd(endX, endY);
        return figure;
    }
    
    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(color);
        g2.setStroke(new BasicStroke(outlineWidth));
        g2.drawOval(getLeft(), getTop(), getWidth(), getHeight());
    }
    
    @Override
    public void drawRubber(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(color);
        g2.drawOval(getLeft(), getTop(), getWidth(), getHeight());
    }
    
    @Override
    public void drawOutline(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(color);
        g2.setStroke(dotted);
        g2.drawOval(getLeft(), getTop(), getWidth(), getHeight());
    }
    
    @Override
    public boolean isGrabbed(int x, int y) {
        int ox = getLeft() - ALLOWABLE_GAP_WHEN_GRABBED;
        int oy = getTop() - ALLOWABLE_GAP_WHEN_GRABBED;
        int ow = getWidth() + ALLOWABLE_GAP_WHEN_GRABBED * 2;
        int oh = getHeight() + ALLOWABLE_GAP_WHEN_GRABBED * 2;
        
        if (!isInOval(x, y, ox, oy, ow, oh)) {
            return false;
        }
        
        int ix = getLeft() + ALLOWABLE_GAP_WHEN_GRABBED;
        int iy = getTop() + ALLOWABLE_GAP_WHEN_GRABBED;
        int iw = getWidth() - ALLOWABLE_GAP_WHEN_GRABBED * 2;
        int ih = getHeight() - ALLOWABLE_GAP_WHEN_GRABBED * 2;
        
        return !isInOval(x, y, ix, iy, iw, ih);
    }
    
    protected boolean isInOval(int x, int y, int x1, int y1, int w, int h) {
        int a = w / 2;
        int b = h / 2;
        int cx = x1 + a;
        int cy = y1 + b;
        int rx = x - cx;
        int ry = y - cy;
        
        return b * b * rx * rx + a * a * ry * ry < a * a * b * b;
    }
    
    @Override
    public void drawGrabbedFigure(Graphics g) {
        g.setColor(color);
        drawGrabbedMark(g, getLeft(), getTop() + getHeight() / 2);
        drawGrabbedMark(g, getRight(), getTop() + getHeight() / 2);
        drawGrabbedMark(g, getLeft() + getWidth() / 2, getTop());
        drawGrabbedMark(g, getLeft() + getWidth() / 2, getBottom());
    }
    
    @Override
    public void paintIcon(Graphics g, int x, int y, int w, int h) {
        g.drawOval(x + 2, y + h / 2 - 6, w - 4, h - 4);
    }
}
