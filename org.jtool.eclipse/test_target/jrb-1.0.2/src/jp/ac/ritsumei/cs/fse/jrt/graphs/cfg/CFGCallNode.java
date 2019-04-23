/*
 *     CFGCallNode.java  Nov 20, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.cfg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.*;
import java.util.ArrayList;

public class CFGCallNode extends CFGStatementNode {
    private String name;
    private SummaryJavaMethod calledMethod = null;
    private ArrayList actualIns = new ArrayList();   // CFGParameterNode
    private ArrayList actualOuts = new ArrayList();  // CFGParameterNode

    private CFGCallNode() {
        super();
    }

    public CFGCallNode(int sort, JavaComponent comp) {
        super(sort, comp);
    }
 
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCalledSummaryMethod(SummaryJavaMethod smethod) {
        calledMethod = smethod;
    }

    public SummaryJavaMethod getCalledSummaryMethod() {
        return calledMethod;
    }

    public void addActualIn(CFGParameterNode node) {
        actualIns.add(node);
    }

    public void addActualOut(CFGParameterNode node) {
        actualOuts.add(node);
    }

    public ArrayList getActualIns() {
        return actualIns;
    }

    public ArrayList getActualOuts() {
        return actualOuts;
    }

    public ArrayList getArguments() {
        return actualIns;
    }

    public int getNumArguments() {
        return actualIns.size();
    }

    public CFGParameterNode getActualIn(int i) {
        return (CFGParameterNode)actualIns.get(i); 
    }

    public CFGParameterNode getActualOut(int i) {
        return (CFGParameterNode)actualOuts.get(i); 
    }

    public boolean hasArguments() {
        if (actualIns.size() != 0) {
            return true;
        }
        return false;
    }

    public boolean callSelf() {
        JavaStatement jst = (JavaStatement)getJavaComponent();
        JavaMethod jm = jst.getJavaMethod();
        SummaryJavaClass sc = jm.getJavaClass().getSummaryJavaClass();

        if (sc.equals(calledMethod.getJavaClass())) {
            if (jm.getSignature().compareTo(calledMethod.getSignature()) == 0) {
                return true;
            }
        }
        return false;
    }

    public void print() {
        String mesg = "";
        switch (sort) {
            case methodCall: mesg = "method call"; break;
            case polymorphicCall: mesg = "polymorphic call"; break;
        }
        print(mesg + " " + getName() + "()");
    }
}
