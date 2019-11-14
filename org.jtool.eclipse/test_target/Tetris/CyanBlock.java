/*
 *     CyanBlock.java
 *
 *     (C) 2010 Katsuhisa Maruyama (maru@cs.ritsumei.ac.jp)
 */

import java.awt.*;

public class CyanBlock extends Block {
    private static final Color COLOR = Color.cyan;

    CyanBlock(int x, int y) {
        super(x, y);

        addTile(1, 1, COLOR);
        addTile(1, 2, COLOR);
        addTile(2, 1, COLOR);
        addTile(2, 2, COLOR);
    }
}
