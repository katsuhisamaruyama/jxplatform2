/*
 *     TokenHighlight.java  Dec 22, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.util;
import java.awt.Color;

public class HighlightToken {
    private int begin;
    private int end;
    private Color color;

    public HighlightToken(int begin, int end, Color color) {
        this.begin = begin;
        this.end = end;
        this.color = color;
    }

    public HighlightToken(int begin, int end) {
        this(begin, end, Color.yellow);
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }

    public Color getColor() {
        return color;
    }
}
