/*
 *     PDG.java Oct 9, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 *     Shota Ueno (mi@fse.cs.ritsumei.ac.jp)
 *     Katsuhiko Yoshikawa (kappy@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.pdg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.cfg.*;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.JavaVariable;
import java.util.Iterator;

public class PDG extends Graph {
    protected PDGMethodEntryNode entryNode;
    transient private GraphComponentSet blocks = new GraphComponentSet();  // Block

    protected PDG() {
        super();
    }

    public PDG(CFG cfg) {
        super();
        Iterator it = cfg.getNodes().iterator();
        while (it.hasNext()) {
            CFGNode cfgNode = (CFGNode)it.next();
            PDGNode pdgNode = createNode(cfgNode);
            cfgNode.setPDGNode(pdgNode);
            if (pdgNode != null) {
                add(pdgNode);
            }
        }
        CDGFactory.getInstance().create(this, cfg);
        DDGFactory.getInstance().create(this, cfg);
    }

    private PDGNode createNode(CFGNode node) {
        if (node instanceof CFGMethodEntryNode) {
            PDGMethodEntryNode entry = new PDGMethodEntryNode((CFGMethodEntryNode)node);
            setEntryNode(entry);
            return entry;
        } else if (node instanceof CFGStatementNode) {
            return new PDGStatementNode((CFGStatementNode)node);
        } 
        return null;
        // return new PDGNode((CFGNode)node);
    }

    public void setEntryNode(PDGMethodEntryNode node) {
        entryNode = node;
    }

    public PDGMethodEntryNode getEntryNode() {
        return entryNode;
    }

    public String getName() {
        return entryNode.getName();
    }

    public PDGNode getNode(CFGNode node) {
        Iterator it = nodes.iterator();
        while (it.hasNext()) {
            PDGNode pdgNode = (PDGNode)it.next();
            if (node.equals(pdgNode.getCFGNode())) {
                return pdgNode;
            }
        }
        return null;
    }

    /* @deprecated */
    public PDGNode getNode(int id) {
        Iterator it = nodes.iterator();
        while (it.hasNext()) {
            PDGNode pdgNode = (PDGNode)it.next();
            if(id == pdgNode.getID()) {
                return pdgNode;
            }
        }
        return null;
    }

    public void add(PDGNode node) {
        super.add(node);
    }

    public void add(Dependence edge) {
        super.add(edge);
    }

    public boolean isDominated(PDGNode node) {
        return !node.getIncomingEdges().isEmpty();
    }

    public boolean isTrueDominated(PDGNode node) {
        Iterator it = node.getIncomingEdges().iterator();
        while (it.hasNext()) {
            Dependence edge = (Dependence)it.next();
            if (edge.isCD() && ((CD)edge).isTrue()) {
                return true;
            }
        }
        return false;
    }

    public boolean isFalseDominated(PDGNode node) {
        Iterator it = node.getIncomingEdges().iterator();
        while (it.hasNext()) {
            Dependence edge = (Dependence)it.next();
            if (edge.isCD() && ((CD)edge).isFalse()) {
                return true;
            }
        }
        return false;
    }

    public void print() {
        System.out.println("----- PDG (from here) -----");
        printNodes();
        printEdges();
        System.out.println("----- PDG (to here) -----");
    }

    public void printNodes() {
        Iterator it = nodes.iterator();
        while (it.hasNext()) {
            PDGNode node = (PDGNode)it.next();
            node.print();
        }
    }

    public void printEdges() {
        Iterator it = edges.iterator();
        while (it.hasNext()) {
            Dependence edge = (Dependence)it.next();
            edge.print();
        }
    }

    public void printCDG() {
        System.out.println("----- CDG (from here) -----");
        printNodes();
        printCDEdges();
        System.out.println("----- CDG (to here) -----");
    }

    public void printDDG() {
        System.out.println("----- DDG (from here) -----");
        printNodes();
        printDDEdges();
        System.out.println("----- DDG (to here) -----");
    }

    public void printCDEdges() {
        Iterator it = edges.iterator();
        while (it.hasNext()) {
            Dependence edge = (Dependence)it.next();
            if (edge.isCD()) {
                edge.print();
            }
        }
    }

    public void printDDEdges() {
        Iterator it = edges.iterator();
        while (it.hasNext()) {
            Dependence edge = (Dependence)it.next();
            if (edge.isDD()) {
                edge.print();
            }
        }
    }
}
