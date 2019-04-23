/*
 *     CFGBranchNode.java  Sep 19, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.cfg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;

public class CFGBranchNode extends CFGStatementNode {
    private CFGBranchNode() {
        super();
    }

    public CFGBranchNode(int sort, JavaComponent comp) {
        super(sort, comp);
    }

    public void print() {
        String mesg = "";
        switch (sort) {
            case switchLabel: mesg = "case"; break;
            case ifSt: mesg = "if"; break;
            case whileSt: mesg = "while"; break;
            case doSt: mesg = "do"; break;
            case forSt: mesg = "for"; break;
            case breakSt: mesg = "break"; break;
            case continueSt: mesg = "continue"; break;
        }
        print(mesg);
    }
}
