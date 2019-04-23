/*
 *     Slice.java  Oct 16, 2001
 *
 *     Takashi Adachi (taka@fse.cs.ritsumei.ac.jp)
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.slice;
import jp.ac.ritsumei.cs.fse.jrt.graphs.pdg.*;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.JavaVariable;
import java.util.Iterator;
 
public class Slice extends PDG {
    private PDGNode criteriaNode;
    private JavaVariable criteriaVar;
    transient private GraphComponentSet defs = new GraphComponentSet();

    protected Slice() {
        super();
    }

    public Slice(PDGNode node, JavaVariable var) {
        super();
        criteriaNode = node;
        criteriaVar = var;
        create();
    }

    public Slice(JavaVariable var) {
        super();
        criteriaNode = getNode(var);
        criteriaVar = var;
        if (criteriaNode != null) {
            create();
        }
    }

    public PDGNode getNode(JavaVariable var) {
        PDG pdg = (PDG)var.getJavaMethod().getPDG();
        Iterator it = pdg.getNodes().iterator();
        while (it.hasNext()) {
            PDGNode node = (PDGNode)it.next();

            if (node.containsDefVariable(var)) {
                PDGStatementNode n = (PDGStatementNode)node;
                if (n.getDefVariables().strictlyContains(var)) {
                    return node;
                }
            }
            if (node.containsUseVariable(var)) {
                PDGStatementNode n = (PDGStatementNode)node;
                if (n.getUseVariables().strictlyContains(var)) {
                    return node;
                }
            }
        }
        return null;
    }

    private void create() {
        if (criteriaNode.containsDefVariable(criteriaVar)) {
            traverseBackward(criteriaNode);

        } else if (criteriaNode.containsUseVariable(criteriaVar)) {
            nodes.add(criteriaNode);
            GraphComponentSet defNodes = getDefNode(criteriaNode, criteriaVar);

            Iterator it = defNodes.iterator();
            while (it.hasNext()) {
                PDGNode defNode = (PDGNode)it.next();
                traverseBackward(defNode);
            }
        }
    }

    private GraphComponentSet getDefNode(PDGNode anchor, JavaVariable var) {
        defs.clear();
        Iterator it = anchor.getIncomingEdges().iterator(); 
        while (it.hasNext()) {
            Dependence edge = (Dependence)it.next();

            if (edge.isDU()) {
                DD dd = (DD)edge;
                if (var.equals(dd.getVariable())) {
                    PDGNode node = (PDGNode)edge.getSrcNode();
                    defs.add(node);
                }
            }
        }
        return defs;
    }

    private void traverseBackward(PDGNode anchor) {
        nodes.add(anchor);

        Iterator it = anchor.getIncomingEdges().iterator();
        while (it.hasNext()) {
            Dependence edge = (Dependence)it.next();

            if (edge.isCD() || edge.isDU()) {
                edges.add(edge);
                PDGNode node = (PDGNode)edge.getSrcNode();
                if (!nodes.contains(node)) {
                    traverseBackward(node);
                }
            }
        }
    }

    public void print() {
        System.out.print("Slice(" + criteriaNode.getID() + ", "
                         + criteriaVar.getName() + ") = {");
        printNodes();
        System.out.println(" }");
    }

    public void printNodes() {
        Iterator it = nodes.iterator();
        while (it.hasNext()) {
            PDGNode node = (PDGNode)it.next();
            System.out.print(" " + node.getID());
        }
    }
}
