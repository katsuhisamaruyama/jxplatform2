/*
 *     GameInfo.java
 *
 *     (C) 2010 Katsuhisa Maruyama (maru@cs.ritsumei.ac.jp)
 */

import java.awt.*;

public class GameInfo extends Canvas {
    private int level;
    private int score;
    private int lines;

    private static int rows = Tetris.ROWS;
    private int width;
    private int height;
    private Image offImage;
    private Graphics offg;
    private Font font;

    GameInfo() {
        width = Tile.SIZE * 4 + 30;
        height = Tile.SIZE * rows;
        setSize(width, height);
    }

    public void init() {
        offImage = createImage(width, height);
        offg = offImage.getGraphics();
        font = new Font("TimesRoman", Font.BOLD, 14);
    }

    public void gameStart() {
        level = 0;
        score = 0;
        lines = 0;
    }

    public int getSpeed() {
        int speed;
        if (level < 8) {
            speed = 500 - level * 50;
        } else {
            speed = 200;
        }
        return speed;
    }

    private void addScore(int add) {
        score = score + add;
    }

    private void addLevel(int add) {
        level = level + add;
    }

    public void addLines(int add) {
        lines = lines + add;

        addScore(10 * add * add);

        if (lines >= (10 * (level + 1))) {
            addLevel(1);
        }
    }

    public void paint(Graphics g) {
        if (offImage != null)
            g.drawImage(offImage, 0, 0, this);
    }

    public void update(Graphics g) {
        paint(g);
    }

    public void update(Block nextBlock) {
        offg.setColor(Color.gray);
        offg.fillRect(0, 0, width, height);
        offg.setFont(font);

        offg.setColor(Color.black);
        offg.fillRect(10, 10, Tile.SIZE * 4 + 10, Tile.SIZE * 4 + 10);
        offg.setColor(Color.white);
        offg.drawString("Next", 37, 10 + Tile.SIZE * 4 + 28);

        if (nextBlock != null) {
            nextBlock.paintNext(offg);
        }

        offg.setColor(Color.white);
        offg.drawString(" Level: " + level, 5, height - 50);
        offg.drawString(" Lines: " + lines, 5, height - 30);
        offg.drawString(" Score: " + score, 5, height - 10);

        repaint();
    }
}
