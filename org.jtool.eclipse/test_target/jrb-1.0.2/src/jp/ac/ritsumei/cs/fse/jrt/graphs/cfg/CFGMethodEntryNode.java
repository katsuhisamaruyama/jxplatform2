/*
 *     CFGMethodEntryNode.java  Sep 19, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.cfg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.ArrayList;

public class CFGMethodEntryNode extends CFGNode {
    private ArrayList formalIns = new ArrayList();   // CFGParameterNode
    private ArrayList formalOuts = new ArrayList();  // CFGParameterNode

    private CFGMethodEntryNode() {
        super();
    }

    public CFGMethodEntryNode(int sort, JavaComponent comp) {
        super(sort, comp);
    }

    public String getName() {
        JavaMethod jmethod = (JavaMethod)javaComp;
        return jmethod.getName();
    }

    public boolean isVoid() {
        JavaMethod jmethod = (JavaMethod)javaComp;
        return jmethod.isVoid();
    }

    public void addFormalIn(CFGParameterNode node) {
        formalIns.add(node);
    }

    public void addFormalOut(CFGParameterNode node) {
        formalOuts.add(node);
    }

    public ArrayList getFormalIns() {
        return formalIns;
    }

    public ArrayList getFormalOuts() {
        return formalOuts;
    }

    public ArrayList getParameters() {
        return formalIns;
    }

    public int getNumParameters() {
        return formalIns.size();
    }

    public CFGParameterNode getFormalIn(int i) {
        return (CFGParameterNode)formalIns.get(i); 
    }

    public CFGParameterNode getFormalOut(int i) {
        return (CFGParameterNode)formalOuts.get(i); 
    }

    public boolean hasParameters() {
        if (formalIns.size() != 0) {
            return true;
        }
        return false;
    }

    public void print() {
        String mesg = "";
        switch (sort) {
            case methodEntry: mesg = "method entry"; break;
            case constructorEntry:  mesg = "constructor entry"; break;
        }
        print(mesg + " " + getName());
    }
}
