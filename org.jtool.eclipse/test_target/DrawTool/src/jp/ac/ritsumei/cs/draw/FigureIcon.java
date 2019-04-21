
package jp.ac.ritsumei.cs.draw;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.*;

/**
 * Represents an icon of a figure.
 */
public class FigureIcon implements Icon {
    
    /**
     * The constant value of the width of the icon.
     */
    protected static final int ICON_WIDTH = 32;
    
    /**
     * The constant value of the height of the icon.
     */
    protected static final int ICON_HEIGHT = 16;
    
    /**
     * The figure object corresponding to the icon.
     */
    private Figure figure;
    
    /**
     * Creates an icon denoting a given figure.
     * @param f the given figure
     */
    public FigureIcon(Figure f) {
        figure = f;
    }
    
    /**
     * Returns the figure object corresponding to the icon.
     * @return the corresponding figure
     */
    public Figure getFigure() {
        return figure;
    }
    
    /**
     * Returns the width of the icon
     * @return the width of the icon
     */
    public int getIconWidth() {
        return ICON_WIDTH;
    }
    
    /**
     * Returns the height of the icon
     * @return the height of the icon
     */
    public int getIconHeight() {
        return ICON_HEIGHT;
    }
    
    /**
     * Paints the icon at the specified position.
     * @param comp the component to get properties useful for painting
     * @param g the graphics context
     * @param x the x-coordinate of the icon
     * @param y the y-coordinate of the icon
     */
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(figure.getColor());
        figure.paintIcon(g, x, y, getIconWidth(), getIconHeight());
    }
}
