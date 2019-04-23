/*
 *     PDGClassEntryNode.java  Sep 19, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.pdg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.graphs.cfg.CFGClassEntryNode;

public class PDGClassEntryNode extends PDGNode {
    private PDGClassEntryNode() {
        super();
    }

    public PDGClassEntryNode(CFGClassEntryNode node) {
        super(node);
    }

    public String getName() {
        CFGClassEntryNode node = (CFGClassEntryNode)cfgNode;
        return node.getName();
    }

    public void print() {
        String mesg = "";
        switch (cfgNode.getSort()) {
        case classEntry: mesg = "class entry"; break;
        case interfaceEntry: mesg = "interface entry"; break;
        }
        print(mesg + " " + getName());
    }
}
