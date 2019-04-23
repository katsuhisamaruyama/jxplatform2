/*
 *     CCFG.java  Sep 18, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.cfg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import java.util.ArrayList;
import java.util.Iterator;

public class CCFG extends Graph {
    private ArrayList cfgs = new ArrayList();  // CFG
    private CFGClassEntryNode startNode;
    private CFGExitNode endNode;

    public CCFG() {
        super();
    }

    public void add(Graph cfg) {
        cfgs.add(cfg);
    }

    public void setStartNode(CFGClassEntryNode node) {
        startNode = node;
    }

    public CFGClassEntryNode getStartNode() {
        return startNode;
    }

    public void setEndNode(CFGExitNode node) {
        endNode = node;
    }

    public CFGExitNode getEndNode() {
        return endNode;
    }

    public String getName() {
        return startNode.getName();
    }

    public Flow getFlow(CFGNode src, CFGNode dst) {
        Iterator it = edges.iterator();
        while (it.hasNext()) {
            Flow edge = (Flow)it.next();
            if (edge.getSrcNode() == src && edge.getDstNode() == dst) {
                return edge;
            }
        }
        return null;
    }
 
    public void printCFGs() {
        Iterator it = cfgs.iterator();
        while (it.hasNext()) {
            CFG cfg = (CFG)it.next();
            System.out.println("CFG: " + cfg.getStartNode().getName());
        }
    }

    public void print() {
        printNodes();
        printEdges();
    }

    public void printNodes() {
        Iterator it = nodes.iterator();
        while (it.hasNext()) {
            CFGNode node = (CFGNode)it.next();
            node.print();
        }
    }

    public void printEdges() {
        Iterator it = edges.iterator();
        while (it.hasNext()) {
            Flow edge = (Flow)it.next();
            edge.print();
        }
    }
}
