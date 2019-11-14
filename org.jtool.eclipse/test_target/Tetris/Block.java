/*
 *     Block.java
 *
 *     (C) 2010 Katsuhisa Maruyama (maru@cs.ritsumei.ac.jp)
 */

import java.awt.*;
import java.util.*;

public abstract class Block {

    private Pit pit;
    private int posX;
    private int posY;

    public static final int NUMBER_OF = 7;

    protected int offsetX = 15;
    protected int offsetY = 15;

    protected Set<Tile> tiles = new HashSet<Tile>();

    protected Block(int x, int y) {
        posX = x;
        posY = y;
    }

    static public Block create(int id, int x, int y) {
        switch (id) {
            case 0: return new YellowBlock(x, y);
            case 1: return new BlueBlock(x, y);
            case 2: return new GreenBlock(x, y);
            case 3: return new CyanBlock(x, y);
            case 4: return new RedBlock(x, y);
            case 5: return new MagentaBlock(x, y);
            case 6: return new OrangeBlock(x, y);
        }
        return null;
    }

    public void setPosXY(int x, int y) {
        posX = x;
        posY = y;

        Iterator<Tile> it = tiles.iterator();
        while (it.hasNext()) {
            Tile tile = it.next();
            tile.setPosXY(tile.getPosX() + x, tile.getPosY() + y);
        }
    }

    public void setPit(Pit p) {
        pit = p;
    }

    protected void addTile(int x, int y, Color c) {
        tiles.add(new Tile(posX + x, posY + y, c));
    }

    public void moveLeft() {
        if (canMove(-1, 0)) {
            posX--;
            changeXPosition(-1);
        }
    }

    public void moveRight() {
        if (canMove(1, 0)) {
            posX++;
            changeXPosition(1);
        }
    }

    public void drop() {
        if (canMove(0, 1)) {
            posY++;
            changeYPosition(1);
        }
    }

    public void rotate() {
        if (canRotate()) {
            changeXYPositionByRotation();
        }
    }

    public boolean fall() {
        if (canMove(0, 1)) {
            posY++;
            changeYPosition(1);
            return true;
        } else {
            place();
            return false;
        }
    }

    public boolean canPlace() {
        return canMove(0, 0);
    }

    private void place() {
        Iterator<Tile> it = tiles.iterator();
        while (it.hasNext()) {
            Tile tile = it.next();
            pit.setBox(tile.getPosX(), tile.getPosY(), tile);
        }
    }

    private boolean canMove(int dx, int dy) {
        Iterator<Tile> it = tiles.iterator();
        while (it.hasNext()) {
            Tile tile = it.next();
            if (!canTake(tile.getPosX() + dx, tile.getPosY() + dy))
                return false;
        }
        return true;
    }

    private void changeXPosition(int dx) {
        Iterator<Tile> it = tiles.iterator();
        while (it.hasNext()) {
            Tile tile = it.next();
            tile.setPosX(tile.getPosX() + dx);
        }
    }

    private void changeYPosition(int dy) {
        Iterator<Tile> it = tiles.iterator();
        while (it.hasNext()) {
            Tile tile = it.next();
            tile.setPosY(tile.getPosY() + dy);
        }
    }

    private boolean canRotate() {
        Iterator<Tile> it = tiles.iterator();
        while (it.hasNext()) {
            Tile tile = it.next();
            if (!canTake(getXByRotation(tile), getYByRotation(tile)))
                return false;
        }
        return true;
    }

    private int getXByRotation(Tile t) {
        int nx = 3 - (t.getPosY() - posY);  // X <- 3 - Y
        int x = nx + posX;
        return x;
    }

    private int getYByRotation(Tile t) {
        int ny = t.getPosX() - posX;      // Y <- X
        int y = ny + posY;
        return y;
    }

    private void changeXYPositionByRotation() {
        Iterator<Tile> it = tiles.iterator();
        while (it.hasNext()) {
            Tile tile = it.next();
            tile.setPosXY(getXByRotation(tile), getYByRotation(tile));
        }
    }

    public boolean canTake(int x, int y) {
        if (x < 0 || x >= Tetris.COLUMNS)
            return false;

        if (y >= Tetris.ROWS)
            return false;

        if (pit.getBox(x, y) != null)
            return false;

        return true;
    }

    public void paint(Graphics g) {
        Iterator<Tile> it = tiles.iterator();
        while (it.hasNext()) {
            Tile tile = it.next();
            tile.paint(g);
        }
    }

    public void paintNext(Graphics g) {
        Iterator<Tile> it = tiles.iterator();
        while (it.hasNext()) {
            Tile tile = it.next();
            tile.paint(g, offsetX, offsetY);
        }
    }
}
