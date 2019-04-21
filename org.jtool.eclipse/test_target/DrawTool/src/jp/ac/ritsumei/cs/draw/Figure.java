
package jp.ac.ritsumei.cs.draw;

import java.awt.*;

/**
 * Represents a figure
 */
public abstract class Figure {
    
    /**
     * The x-coordinate of the start point of this figure.
     */
    protected int startX;
    
    /**
     * The y-coordinate of the start point of this figure.
     */
    protected int startY;
    
    /**
     * The x-coordinate of the end point of this figure.
     */
    protected int endX;
    
    /**
     * The y-coordinate of the end point of this figure.
     */
    protected int endY;
    
    /**
     * The color of this figure.
     */
    protected Color color;
    
    /**
     * The line width of the outline of this figure.
     */
    protected float outlineWidth;
    
    /**
     * The constant value indicating the allowable gap between the figure and its grabbed point.
     */
    protected static final int ALLOWABLE_GAP_WHEN_GRABBED = 4;
    
    /**
     * The constant value indicating a dotted line.
     */
    protected static final Stroke dotted = new BasicStroke(1.0f,
            BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, new float[] { 4.0f, 4.0f }, 0.0f);
    
    /**
     * Creates a new object.
     */
    public Figure() {
        this(Color.black, 1.0f);
    }
    
    /**
     * Creates a new object.
     * @param c the color of this figure
     * @param w the width of the outline of this figure
     */
    protected Figure(Color c, float w) {
        color = c;
        outlineWidth = w;
    }
    
    /**
     * Creates a new object.
     * @param c the color of this figure
     */
    protected Figure(Color c) {
        this(c, 1);
    }
    
    /**
     * Creates a new object.
     * @param w the width of the outline of this figure
     */
    protected Figure(float w) {
        this(Color.black, w);
    }
    /**
     * Sets the start point of this figure.
     * @param x the x-coordinate of the start point
     * @param y the y-coordinate of the start point
     */
    public void setStart(int x, int y) {
        startX = x;
        startY = y;
    }
    
    /**
     * Sets the end point of this figure.
     * @param x the x-coordinate of the end point
     * @param y the y-coordinate of the end point
     */
    public void setEnd(int x, int y) {
        endX = x;
        endY = y;
    }
    
    /**
     * Returns the left-most x-coordinate of this figure
     * @return the left-most x-coordinate
     */
    protected int getLeft() {
        return Math.min(startX, endX);
    }
    
    /**
     * Returns the right-most x-coordinate of this figure
     * @return the right-most x-coordinate
     */
    protected int getRight() {
        return Math.max(startX, endX);
    }
    
    /**
     * Returns the top-most y-coordinate of this figure
     * @return the top-most y-coordinate
     */
    protected int getTop() {
        return Math.min(startY, endY);
    }
    
    /**
     * Returns the bottom-most y-coordinate of this figure
     * @return the bottom-most y-coordinate
     */
    protected int getBottom() {
        return Math.max(startY, endY);
    }
    
    /**
     * Returns the width of the least rectangle enclosing this figure.
     * @return the width
     */
    protected int getWidth() {
        return getRight() - getLeft();
    }
    
    /**
     * Returns the height of the least rectangle enclosing this figure.
     * @return the height
     */
    protected int getHeight() {
        return getBottom() - getTop();
    }
    
    /**
     * Returns the color of thus figure.
     * @return the color
     */
    protected Color getColor() {
        return color;
    }
    
    /**
     * Returns the width of the outline of this figure.
     * @return the outline width
     */
    protected float getOutlineWidth() {
        return outlineWidth;
    }
    
    /**
     * Obtains information about the z- and y-coordinates.
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return the information string
     */
    private String getPositionInfo(int x, int y) {
        return "(" + x + "," + y +")"; 
    }
    
    /**
     * Obtains information about this figure.
     * @return the information string
     */
    public String getInfo() {
        return getName() + ": " + getPositionInfo(getLeft(), getTop()) + "-" + getPositionInfo(getRight(), getBottom());
    }
    
    /**
     * Tests of this figure equals to a given one.
     * @param figure the figure object to be compared
     * @return <code>true</code> if this figure equals to the given one, otherwise <code>false</code>
     */
    private boolean equals(Figure figure) {
        return getName().equals(figure.getName()) && isOverlapped(figure);
    }
    
    /**
     * Tests of this figure equals to a given one.
     * @param figure the figure object to be compared
     * @return <code>true</code> if this figure equals to the given one, otherwise <code>false</code>
     */
    public boolean equals(Object obj) {
        if (obj instanceof Figure) {
            Figure figure = (Figure)obj;
            return equals(figure);
        }
        return false;
    }
    
    /**
     * Returns the hash code for this figure.
     * @return the hash code
     */
    public int hashCode() {
        return getName().hashCode();
    }
    
    /**
     * Checks if this figure overlaps a given one.
     * @param figure the figure object to be checked
     * @return <code>true</code> if this figure overlaps the given one, otherwise <code>false</code>
     */
    public boolean isOverlapped(Figure figure) {
        return getLeft() == figure.getLeft() && getTop() == figure.getTop() &&
               getRight() == figure.getRight() && getBottom() == figure.getBottom();
    }
    
    /**
     * Moves this figure.
     * @param dx the distance to move this figure along the x-axis
     * @param dy the distance to move this figure along the y-axis
     */
    public void move(int dx, int dy) {
        startX = startX + dx;
        startY = startY + dy;
        endX = endX + dx;
        endY = endY + dy;
    }
    
    /**
     * Replaces this figure with a given figure.
     * @param figure the replacing figure
     */
    public void replace(Figure figure) {
        startX = figure.startX;
        startY = figure.startY;
        endX = figure.endX;
        endY = figure.endY;
    }
    
    /**
     * Creates a new object and returns it.
     * @param c the color of this figure
     * @param w the width of the outline of this figure
     * @return the created object
     */
    public abstract Figure create(Color c, float w);
    
    /**
     * Returns the name of this figure.
     * @return the name
     */
    public abstract String getName();
    
    /**
     * Creates a clone of this figure and returns it.
     * @return the clone object
     */
    public abstract Figure createClone();
    
    /**
     * Draws this figure.
     * @param g the graphics context
     */
    public abstract void draw(Graphics g);
    
    /**
     * Draws a rubber representing this figure while drawing it.
     * @param g the graphics context
     */
    public abstract void drawRubber(Graphics g);
    
    /**
     * Draws the outline of this figure while moving it.
     * @param g the graphics context
     */
    public abstract void drawOutline(Graphics g);
    
    /**
     * Draws this figure while grabbing it.
     * @param g the graphics context
     */
    public abstract void drawGrabbedFigure(Graphics g);
    
    /**
     * Tests if this figure is grabbed.
     * @param x the x-coordinate of the grabbed point
     * @param y the y-coordinate of the grabbed point
     * @return <code>true</code> if this figure is grabbed, otherwise <code>false</code>
     */
    public abstract boolean isGrabbed(int x, int y);
    
    /**
     * Draws a mark denoting that this figure is grabbed.
     * @param g the graphics context
     * @param x the x-coordinate of the center of the mark
     * @param y the x-coordinate of the center of the mark
     */
    protected void drawGrabbedMark(Graphics g, int x, int y) {
        g.fillRect(x - 3, y - 3, 8, 8);
    }
    
    /**
     * Paints the icon of this figure.
     * @param g the graphics context
     * @param x the x-coordinate of the top-left position of the icon.
     * @param y the y-coordinate of the top-left position of the icon.
     * @param w the width of the icon
     * @param h the height of the icon
     */
    public abstract void paintIcon(Graphics g, int x, int y, int w, int h);
}
