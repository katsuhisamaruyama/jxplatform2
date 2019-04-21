
package jp.ac.ritsumei.cs.draw;

import java.awt.*;

/**
 * Represents an icon of a figure.
 */
public class CursorIcon extends FigureIcon {
    
    /**
     * Creates an icon denoting a given figure.
     */
    public CursorIcon() {
        super(null);
    }
    
    /**
     * Paints the icon at the specified position.
     * @param comp the component to get properties useful for painting
     * @param g the graphics context
     * @param x the x-coordinate of the icon
     * @param y the y-coordinate of the icon
     */
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(Color.black);
        g.drawLine(x + 10, y + 2, x + 15, y + getIconHeight() - 2);
        g.drawLine(x + 11, y + 2, x + 16, y + getIconHeight() - 2);
        int[] px = { x + 10, x + 9,  x + 13, x + 20, x + 11 };
        int[] py = { y + 2,  y + 13, y + 9,  y + 10, y + 2 };
        g.fillPolygon(px, py, px.length);
    }
}
