/*
 *     Pit.java
 *
 *     (C) 2010 Katsuhisa Maruyama (maru@cs.ritsumei.ac.jp)
 */

import java.awt.*;
import java.util.*;

public class Pit extends Canvas {

    private int width;
    private int height;
    private Image offImage;
    private Graphics offg;
    private Font font;
    private Random rnd;
    static final private Color backgroundColor = Color.black;

    private GameInfo info;

    private Block currentBlock = null;
    private Block nextBlock = null;

    private Tile[][] box;

    private static int columns = Tetris.COLUMNS;
    private static int rows = Tetris.ROWS;
    private static int initPosX = columns / 2 - 2;
    private static int initPosY = 0;

    Pit(GameInfo i) {
        info = i;
        box = new Tile[rows][columns];

        width = Tile.SIZE * columns;
        height = Tile.SIZE * rows;
        setSize(width, height);

        long seed = Runtime.getRuntime().freeMemory();
        rnd = new Random(seed);
    }

    public void init() {
        offImage = createImage(width, height);
        offg = offImage.getGraphics();
        font = new Font("TimesRoman", Font.BOLD, 24);

        offg.setColor(backgroundColor);
        offg.fillRect(0, 0, width, height);
        printStartPrompt();

        repaint();
    }

    public void gameStart() {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                setBox(x, y, null);
            }
        }
    }

    public void gameOver() {
        offg.setColor(backgroundColor);
        offg.fillRect(25, 100, width - 50, 110);

        offg.setColor(Color.red);
        offg.setFont(font);
        offg.drawString("Game Over", 40, 135);
        
        printStartPrompt();

        repaint();
    }

    private void printStartPrompt() {
    	offg.setColor(Color.white);
        offg.setFont(font);
        offg.drawString(" push 's' key ", 25, 170);
        offg.drawString("   to start !", 25, 195);
    }


    public Block getCurrentBlock() {
        return currentBlock;
    }

    public Block getNextBlock() {
        return nextBlock;
    }

    public boolean placeNextBlock() {
        if (nextBlock != null) {
            currentBlock = nextBlock;
            currentBlock.setPosXY(initPosX, initPosY);
        }

        int kind = rnd.nextInt(Block.NUMBER_OF);
        nextBlock = Block.create(kind, 0, 0);
        nextBlock.setPit(this);

        if (currentBlock != null)
            return currentBlock.canPlace();
 
        return(true);
    }

    public void checkLines() {
        int lines = 0;
        for (int y = 0; y < rows; y++) {
            boolean isLine = true;
            for (int x = 0; x < columns; x++) {
                if (getBox(x, y) == null) {
                    isLine = false;
                }
            }
            if (isLine) {
                eraseLine(y);
                lines++;
            }
        }
       
        if (lines > 0) {
            info.addLines(lines);
            update();
        }    
    }

    private void eraseLine(int y) {
        for (int x = 0; x < columns; x++) {
            Tile tile = getBox(x, y);
            tile = null;  // delete tile
        }

        for (; y > 0; y--) {
            for (int x = 0; x < columns; x++) {
                Tile tile = getBox(x, y - 1);
                if (tile != null)
                    tile.setPosY(tile.getPosY() + 1);
                setBox(x, y, tile);
            }
        }

        for (int x = 0; x < columns; x++) {
            setBox(x, 0, null);
        }
    }

    public Tile getBox(int x, int y) {
        return box[y][x];
    }

    public void setBox(int x, int y, Tile t) {
        box[y][x] = t;
    }

    public void paint(Graphics g) {
        if (offImage != null)
            g.drawImage(offImage, 0, 0, this);
    }

    public void update(Graphics g) {
        paint(g);
    }

    public synchronized void update() {
        offg.setColor(backgroundColor);
        offg.fillRect(0, 0, width, height);

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                Tile tile = getBox(x, y);                
                if (tile != null) {
                    tile.paint(offg);
                }
            }
        }

        if (currentBlock != null) {
            currentBlock.paint(offg);
        }

        repaint();
    }
}
