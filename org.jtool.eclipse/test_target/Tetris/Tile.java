/*
 *     Tile.java
 *
 *     (C) 2010 Katsuhisa Maruyama (maru@cs.ritsumei.ac.jp)
 */

import java.awt.*;

public class Tile {

    private int posX;
    private int posY;

    private Color color;
    public static final int SIZE = 20;
    private static final int BORDER = 3;

    Tile(int x, int y, Color c) {
        posX = x;
        posY = y;
        color = c;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosX(int x) {
        posX = x;
    }

    public void setPosY(int y) {
        posY = y;
    }

    public void setPosXY(int x, int y) {
        posX = x;
        posY = y;
    }

    public void paint(Graphics g) {
        paint(g, 0, 0);
    }

    public void paint(Graphics g, int offsetX, int offsetY) {
        int left = posX * SIZE + offsetX;
        int right = (posX + 1) * SIZE - 1 + offsetX;
        int top = posY * SIZE + offsetY;
        int bottom = (posY + 1) * SIZE - 1 + offsetY;
        
        g.setColor(color);
        g.fillRect(left + BORDER, top + BORDER, SIZE - 2 * BORDER, SIZE - 2 * BORDER);

        g.setColor(Color.white);
        for (int i = 0; i < BORDER; i++) {
            g.drawLine(left + i, top + i, right - i, top + i);
            g.drawLine(left + i, top + i, left + i, bottom - i);
        }

        g.setColor(color);
        g.drawLine(left, top, left + BORDER, top + BORDER);

        g.setColor(color.darker());
        for (int i = 0; i < BORDER; i++) {
            g.drawLine(left + i, bottom - i, right - i, bottom - i);
            g.drawLine(right - i, top + i, right - i, bottom - i);
        }

        g.setColor(Color.black);
        g.drawRect(left , top, SIZE - 1, SIZE - 1);
    }
}
