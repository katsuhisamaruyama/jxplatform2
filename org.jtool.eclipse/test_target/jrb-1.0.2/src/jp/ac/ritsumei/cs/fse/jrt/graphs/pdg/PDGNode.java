/*
 *     PDGNode.java  Oct 6, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 *     Shota Ueno (mi@fse.cs.ritsumei.ac.jp)
 *     Katsuhiko Yoshikawa (kappy@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.pdg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.cfg.*;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.JavaVariable;

public class PDGNode extends GraphNode {
    protected CFGNode cfgNode;

    protected PDGNode() {
        super();
    }

    public PDGNode(CFGNode node) {
        super();
        cfgNode = node;
        id = node.getID();
        sort = node.getSort();
    }

    public CFGNode getCFGNode() {
        return cfgNode;
    }

    public boolean isBranch() {
        return cfgNode.isBranch();
    }

    public boolean isLoop() {
        return cfgNode.isLoop();
    }

    public boolean equals(GraphComponent c) {
        GraphNode node = (GraphNode)c;
        if (this == node) {
            return true;
        }
        return false;
    }

    public boolean containsDefVariable(JavaVariable v) {
        return false;
    }

    public boolean containsUseVariable(JavaVariable v) {
        return false;
    }

    public boolean isStatementNode() {
        return false;
    }

    public void print(String mesg) {
        System.out.print(cfgNode.getID() + ": ");
        System.out.println(mesg);
    }
}
