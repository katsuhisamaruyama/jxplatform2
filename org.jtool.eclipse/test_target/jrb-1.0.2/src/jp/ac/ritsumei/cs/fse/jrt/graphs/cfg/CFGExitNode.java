/*
 *     CFGExitNode.java  Sep 19, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.cfg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;

public class CFGExitNode extends CFGNode {
    private CFGExitNode() {
        super();
    }

    public CFGExitNode(int sort) {
        super(sort);
    }

    public void print() {
        String mesg = "";
        switch (sort) {
            case classExit: mesg = "class exit"; break;
            case interfaceExit: mesg = "interface exit"; break;
            case methodExit: mesg = "method exit"; break;
            case constructorExit: mesg = "constructor exit"; break;
        }
        print(mesg);
    }
}
