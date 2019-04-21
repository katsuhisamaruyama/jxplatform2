
package jp.ac.ritsumei.cs.draw;

import java.awt.*;

/**
 * Represents a line.
 */
public class Line extends Figure {
    
    /**
     * The constant value of the name of this figure.
     */
    public static final String name = "Line";
    
    /**
     * Creates a new object.
     */
    public Line() {
        super();
    }
    
    /**
     * Creates a new object.
     * @param c the color of this figure
     * @param w the width of the outline of this figure
     */
    public Line(Color c, float w) {
        super(c, w);
    }
    
    /**
     * Creates a new object.
     * @param c the color of this figure
     */
    public Line(Color c) {
        super(c);
    }
    
    /**
     * Creates a new object.
     * @param w the width of the outline of this figure
     */
    public Line(int w) {
        super(w);
    }
    
    @Override
    public Figure create(Color c, float w) {
        return new Line(c, w);
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Figure createClone() {
        Figure figure = new Line(color, outlineWidth);
        figure.setStart(startX, startY);
        figure.setEnd(endX, endY);
        return figure;
    }
    
    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(color);
        g2.setStroke(new BasicStroke(outlineWidth));
        g2.drawLine(startX, startY, endX, endY);
    }
    
    @Override
    public void drawRubber(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(color);
        g2.drawLine(startX, startY, endX, endY);
    }
    
    @Override
    public void drawOutline(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(color);
        g2.setStroke(dotted);
        g2.drawLine(startX, startY, endX, endY);
    }
    
    @Override
    public void drawGrabbedFigure(Graphics g) {
        g.setColor(color);
        drawGrabbedMark(g, getLeft(), getTop());
        drawGrabbedMark(g, getRight(), getBottom());
    }
    
    @Override
    public boolean isGrabbed(int x, int y) {
        int rx = startX - x;
        int ry = startY - y;
        int dx = endX - startX;
        int dy = endY - startY;
        double gap = Math.abs(dx * ry - dy * rx) / Math.sqrt(dx * dx + dy * dy);
        
        return gap < ALLOWABLE_GAP_WHEN_GRABBED;
    }
    
    @Override
    public void paintIcon(Graphics g, int x, int y, int w, int h) {
        g.drawLine(x + 2, y + h / 2, x + w - 2, y + h / 2);
        for (int i = 1; i <= outlineWidth / 2; i++) {
            g.drawLine(x + 2, y + h / 2 + i, x + w - 2, y + h / 2 + i);
            g.drawLine(x + 2, y + h / 2 - i, x + w - 2, y + h / 2 - i);
        }
    }
}
