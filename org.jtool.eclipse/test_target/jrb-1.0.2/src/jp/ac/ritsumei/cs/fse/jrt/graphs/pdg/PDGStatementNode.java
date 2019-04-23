/*
 *     PDGStatementNode.java  Sep 19, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.pdg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import jp.ac.ritsumei.cs.fse.jrt.graphs.cfg.CFGStatementNode;
import java.util.Iterator;

public class PDGStatementNode extends PDGNode {
    private PDGStatementNode() {
        super();
    }

    public PDGStatementNode(CFGStatementNode node) {
        super(node);
    }

    public boolean isStatementNode() {
        return true;
    }

    public JavaVariableList getDefVariables() {
        CFGStatementNode node = (CFGStatementNode)cfgNode;
        return node.getDefVariables();
    }

    public JavaVariableList getUseVariables() {
        CFGStatementNode node = (CFGStatementNode)cfgNode;
        return node.getUseVariables();
    }

    public boolean containsDefVariable(JavaVariable v) {
        CFGStatementNode node = (CFGStatementNode)cfgNode;
        return node.getDefVariables().contains(v);
    }

    public boolean containsUseVariable(JavaVariable v) {
        CFGStatementNode node = (CFGStatementNode)cfgNode;
        return node.getUseVariables().contains(v);
    }

    /* deprecation */
    public JavaVariable getJavaVariableInDefs(String v) {
        Iterator it = getDefVariables().iterator();
        while (it.hasNext()) {
            JavaVariable var = (JavaVariable)it.next();
            if (v.compareTo(var.getName()) == 0) {
                return var;
            }
        }
        return null;
    }

    /* deprecation */
    public JavaVariable getJavaVariableInUses(String v) {
        Iterator it = getUseVariables().iterator();
        while (it.hasNext()) {
            JavaVariable var = (JavaVariable)it.next();
            if (v.compareTo(var.getName()) == 0) {
                return var;
            }
        }
        return null;
    }

    public void print() {
        CFGStatementNode node = (CFGStatementNode)cfgNode;
        String mesg = "Def = { " + node.toStringDefVariables() + " }  "
                    + "Use = { " + node.toStringUseVariables() + " }";
        super.print(mesg);
    }
}
