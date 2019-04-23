/*
 *     SplitVariableVisitor.java  Apr 10, 2003
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.variables;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.Node;
import jp.ac.ritsumei.cs.fse.jrt.parser.Token;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.graphs.pdg.*;
import jp.ac.ritsumei.cs.fse.jrt.graphs.cfg.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class SplitVariableVisitor extends StatementVisitor {
    private JavaVariable jvar;
    private String newName;
    private List nodes = new ArrayList();
    private CFGNode declNode;

    public SplitVariableVisitor(JavaVariable jv, String name) {
        super(jv);
        jvar = jv;
        newName = name;

        collectChangedNodes(jvar);
        declNode = getDeclataionStatement();
    }

    private void collectChangedNodes(JavaVariable var) {
        PDGNode pdgNode = getPDGNode(var);
        if (pdgNode != null) {
            traverseBackward(pdgNode);

            List inodes = new ArrayList(nodes);
            Iterator it = inodes.iterator();
            while (it.hasNext()) {
                PDGNode node = (PDGNode)it.next();
                traverseForward(node);
            }

            inodes = new ArrayList(nodes);
            it = inodes.iterator();
            while (it.hasNext()) {
                PDGNode node = (PDGNode)it.next();
                traverseBackward(node);
            }
        }
    }

    private PDGNode getPDGNode(JavaVariable var) {
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

    private void traverseBackward(PDGNode anchor) {
        if (!nodes.contains(anchor)) {
            nodes.add(anchor);
        }

        Iterator it = anchor.getIncomingEdges().iterator();
        while (it.hasNext()) {
            Dependence edge = (Dependence)it.next();
            if (edge.isDU()) {
                DD dd = (DD)edge;

                if (jvar.equals(dd.getVariable())) {
                    PDGNode node = (PDGNode)edge.getSrcNode();
                    if (!nodes.contains(node)) {
                        traverseBackward(node);
                    }
                }
            }
        }
    }

    private void traverseForward(PDGNode anchor) {
        if (!nodes.contains(anchor)) {
            nodes.add(anchor);
        }

        Iterator it = anchor.getOutgoingEdges().iterator();
        while (it.hasNext()) {
            Dependence edge = (Dependence)it.next();
            if (edge.isDU()) {
                DD dd = (DD)edge;

                if (jvar.equals(dd.getVariable())) {
                    PDGNode node = (PDGNode)edge.getDstNode();
                    if (!nodes.contains(node)) {
                        traverseForward(node);
                    }
                }
            }
        }
    }

    private CFGNode getDeclataionStatement() {
        List candidates = getCandidatesForDeclaration();
        CFGNode cfgNode;
        if (candidates.size() == 1) {
            PDGNode pdgNode = (PDGNode)candidates.get(0);
            cfgNode = pdgNode.getCFGNode();
        } else {
            CFG cfg = (CFG)jvar.getJavaMethod().getCFG();
            cfgNode = traverseBackward(candidates, cfg);
        }

        cfgNode.print();
        return cfgNode;
    }

    private List getCandidatesForDeclaration() {
        List candidates = new ArrayList();

        Iterator it = nodes.iterator();
        while (it.hasNext()) {
            PDGNode node = (PDGNode)it.next();
            if (node.containsDefVariable(jvar)) {
                candidates.add(node);
            }
        }

        return candidates;
    }

    private CFGNode traverseBackward(List candidates, CFG cfg) {
        GraphComponentSet path = cfg.getNodes();
        
        Iterator it = candidates.iterator();
        while (it.hasNext()) {
            PDGNode node = (PDGNode)it.next();
            CFGNode anchor = node.getCFGNode();

            GraphComponentSet p = cfg.getBackwardReachableNodesWithoutLoopback(anchor, cfg.getStartNode());
            path = path.intersection(p);
        }

        return (CFGNode)path.getFirst();
    }

    private boolean isDefinedAt(JavaVariable var, CFGNode node) {
        if (node.hasDefVariable()) {
            CFGStatementNode n = (CFGStatementNode)node;
            if (n.containsDefVariable(var)) {
                return true;
            }
        }
        return false;
    }

    private Node insertNode;
    private int insertIndex;
    private String insertCode = null;

    protected void perform(JavaStatement jst) {
        CFGNode cfgNode =(CFGNode)jst.getCFGNode();
        if (cfgNode != null) {
            PDGNode pdgNode = cfgNode.getPDGNode();

            boolean needsType = false;
            if (cfgNode.equals(declNode)) {
                if (nodes.contains(pdgNode)) {

                    if (isDefinedAt(jvar, declNode)) {

                        if (declNode.getSort() != GraphNodeSort.variableDecl) {
                            needsType = true;
                        }

                    } else {
                        System.out.println("ADD STATEMENT");
                        insertDeclaration(declNode);
                    }                        

                } else {
                    System.out.println("ADD STATEMENT2");
                    insertDeclaration(declNode);
                }
            }

            if (nodes.contains(pdgNode)) {
                if (needsType) {
                    String type = jvar.getPrettyType();
                    renameVariablesInVariableList(jst.getDefVariables(), type + " " + newName);
                } else {
                    renameVariablesInVariableList(jst.getDefVariables(), newName);
                }
                renameVariablesInVariableList(jst.getUseVariables(), newName);
            }
        }
    }

    private void insertDeclaration(CFGNode node) {
        String decl = "    " + jvar.getPrettyType() + " " + newName + ";";

        insertNode = node.getJavaComponent().getASTNode();
        if (node.getSort() == GraphNodeSort.ifSt ||
            node.getSort() == GraphNodeSort.whileSt ||
            node.getSort() == GraphNodeSort.doSt ||
            node.getSort() == GraphNodeSort.forSt ||
            node.getSort() == GraphNodeSort.switchSt) {
            insertIndex = 0;
            insertCode = "\n" + decl;
        } else {
            insertIndex = insertNode.jjtGetNumChildren();
            insertCode = decl + "\n";
        }
    }

    public Object visit(ASTUnmodifiedClassDeclaration node, Object data) {
        JavaClass jc = node.getJavaClass();
        Object obj = node.childrenAccept(this, data);
        
        if (jvar.getJavaMethod().getJavaClass().equals(jc) && insertCode != null) {
            insertCode(insertNode, insertIndex, insertCode);
            System.out.println(insertCode);
        }

        return obj;
    }

    private void renameVariablesInVariableList(JavaVariableList jvl, String name) {
        Iterator it = jvl.iterator();
        while (it.hasNext()) {
            JavaVariable jv = (JavaVariable)it.next();
            Token token = jv.getToken();
            if (token != null && jvar.equals(jv)) {
                setHighlight(token);

                token.image = name;
                token.changed = true;
            }
        }
    }
}
