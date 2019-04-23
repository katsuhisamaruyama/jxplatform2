/*
 *     CFGAssignmentNode.java  Sep 19, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.cfg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;

public class CFGAssignmentNode extends CFGStatementNode {
    private CFGAssignmentNode() {
        super();
    }

    public CFGAssignmentNode(int sort, JavaComponent comp) {
        super(sort, comp);
    }

    public void print() {
        String mesg = "";
        switch (sort) {
            case assignmentSt: mesg = "assignment"; break;
            case variableDecl: mesg = "variable decralation"; break;
            case returnSt: mesg = "return"; break;
        }
        print(mesg);
    }
}
