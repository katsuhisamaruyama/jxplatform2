/*
 *     GreenBlock.java
 *
 *     (C) 2010 Katsuhisa Maruyama (maru@cs.ritsumei.ac.jp)
 */

import java.awt.*;

public class GreenBlock extends Block {
    private static final Color COLOR = Color.green;

    GreenBlock(int x, int y) {
        super(x, y);
        
        addTile(2, 0, COLOR);
        addTile(2, 1, COLOR);
        addTile(1, 1, COLOR);
        addTile(1, 2, COLOR);

        offsetY = offsetY + (Tile.SIZE / 2);
    }
}
