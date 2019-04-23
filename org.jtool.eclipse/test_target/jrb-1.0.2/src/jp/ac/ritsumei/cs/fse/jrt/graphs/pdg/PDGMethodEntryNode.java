/*
 *     PDGMethodEntryNode.java  Sep 19, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.pdg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.graphs.cfg.CFGMethodEntryNode;

public class PDGMethodEntryNode extends PDGNode {
    private PDGMethodEntryNode() {
        super();
    }

    public PDGMethodEntryNode(CFGMethodEntryNode node) {
        super(node);
    }

    public String getName() {
        CFGMethodEntryNode node = (CFGMethodEntryNode)cfgNode;
        return node.getName();
    }

    public void print() {
        String mesg = "";
        switch (cfgNode.getSort()) {
            case methodEntry: mesg = "method entry"; break;
            case constructorEntry:  mesg = "constructor entry"; break;
        }
        print(mesg + " " + getName());
    }
}
