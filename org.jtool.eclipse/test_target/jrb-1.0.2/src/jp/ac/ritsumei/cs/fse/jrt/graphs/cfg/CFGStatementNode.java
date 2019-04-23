/*
 *     CFGStatementNode.java  Sep 19, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.cfg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;

public class CFGStatementNode extends CFGNode {
    protected CFGStatementNode() {
        super();
    }

    public CFGStatementNode(int sort, JavaComponent comp) {
        super(sort, comp);
    }

    public JavaVariableList getDefVariables() {
        JavaStatement jst = (JavaStatement)javaComp;
        return jst.getDefVariables();
    }

    public JavaVariableList getUseVariables() {
        JavaStatement jst = (JavaStatement)javaComp;
        return jst.getUseVariables();
    }

    public boolean containsDefVariable(JavaVariable v) {
        return getDefVariables().contains(v);
    }

    public boolean containsUseVariable(JavaVariable v) {
        return getUseVariables().contains(v);
    }

    public String toStringDefVariables() {
        return getDefVariables().toString();
    }

    public String toStringUseVariables() {
        return getUseVariables().toString();
    }

    public void print() {
        String mesg = "";
        switch (sort) {
            case switchSt: mesg = "switch"; break;
        }
        print(mesg);
    }

    public void print(String m) {
        String mesg = m + "\n"
            + "  Def = { " + toStringDefVariables() + " }\n"
            + "  Use = { " + toStringUseVariables() + " }";
        super.print(mesg);
    }
}
