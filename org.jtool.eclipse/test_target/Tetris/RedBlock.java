/*
 *     RedBlock.java
 *
 *     (C) 2010 Katsuhisa Maruyama (maru@cs.ritsumei.ac.jp)
 */

import java.awt.*;

public class RedBlock extends Block {
    private static final Color color = Color.red;

    RedBlock(int x, int y) {
        super(x, y);
        
        addTile(1, 0, color);
        addTile(1, 1, color);
        addTile(1, 2, color);
        addTile(1, 3, color);

        offsetX = offsetX + (Tile.SIZE / 2);
    }
}
