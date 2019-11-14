/*
 *     BlueBlock.java
 *
 *     (C) 2010 Katsuhisa Maruyama (maru@cs.ritsumei.ac.jp)
 */

import java.awt.*;

public class BlueBlock extends Block {
    private static final Color COLOR = Color.blue;

    BlueBlock(int x, int y) {
        super(x, y);

        addTile(1, 0, COLOR);
        addTile(1, 1, COLOR);
        addTile(2, 1, COLOR);
        addTile(2, 2, COLOR);

        offsetY = offsetY + (Tile.SIZE / 2);
    }
}
