/*
 *     CFGParameterNode.java  Sep 25, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.cfg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;

public class CFGParameterNode extends CFGStatementNode {
    private int ordinal;

    private CFGParameterNode() {
        super();
    }

    public CFGParameterNode(int sort, JavaComponent comp) {
        super(sort, comp);
    }

    public CFGParameterNode(int sort, JavaComponent comp, int n) {
        super(sort, comp);
        ordinal = n;
    }

    public void setOrdinal(int n) {
        ordinal = n;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public JavaVariable getDefVariable() {
        return getDefVariables().getFirst();
    }

    public JavaVariable getUseVariable() {
        return getUseVariables().getFirst();
    }

    public String toStringDefVariable() {
        return getDefVariable().getName();
    }

    public String toStringUseVariable() {
        return getUseVariable().getName();
    }

    public void print() {
        String mesg = "";
        switch (sort) {
            case formalIn: mesg = "formal_in"; break;
            case formalOut: mesg = "formal_out"; break;
            case actualIn: mesg = "actual_in"; break;
            case actualOut: mesg = "actual_out"; break;
        }
        print(mesg);
    }
}
