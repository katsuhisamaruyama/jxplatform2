/*
 *     Token.java  Dec 22, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

/* Generated By:JavaCC: Do not edit this line. Token.java Version 0.7pre3 */

package jp.ac.ritsumei.cs.fse.jrt.parser;

public class Token {
    public int kind;
    public int beginLine, beginColumn, endLine, endColumn;
    public String image;
    public Token next;
    public Token specialToken;
    public boolean changed = false;
    public boolean toBeChanged = false;

    public final String toString() {
        return image;
    }

    public static final Token newToken(int ofKind) {
        switch(ofKind) {
            default : return new Token();
        }
    }
}
