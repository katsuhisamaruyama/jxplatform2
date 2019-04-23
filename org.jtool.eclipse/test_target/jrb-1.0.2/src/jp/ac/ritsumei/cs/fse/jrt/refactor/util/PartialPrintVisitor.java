/*
 *     PartialPrintVisitor.java  Dec 7, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.util;
import jp.ac.ritsumei.cs.fse.jrt.parser.Node;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class PartialPrintVisitor extends PrintVisitor {
    protected List nodes;  // Node
    protected List allNodes;  // Node

    public PartialPrintVisitor(List an, List pn) {
        super();
        allNodes = an;
        nodes = pn;
    }

    public PartialPrintVisitor(List pn) {
        super();
        allNodes = pn;
        nodes = pn;
        adjustStatementExpression();
        adjustLocalVariableDeclaration();
    }

    protected void appendCode(Node node, String text) {
        Node n = getAncestorWithJavaStatement(node);
        if (n != null && nodes.contains(n)) {
            code.append(text);
        }
    }

    protected Node getAncestorWithJavaStatement(Node node) {
        while (node != null) {
            if (nodes.contains(node) || allNodes.contains(node)) {
                return node;
            }
            node = node.jjtGetParent();
        }
        return null;
    }

    private void adjustStatementExpression() {
        List nlist = new ArrayList(nodes);

        Iterator it = nlist.iterator();
        while (it.hasNext()) {
            Node n = (Node)it.next();
            if (n instanceof ASTStatementExpression
              && n.jjtGetParent() instanceof ASTStatement) {
                nodes.add(n.jjtGetParent());
            }
        }
    }

    private void adjustLocalVariableDeclaration() {
        List nlist = new ArrayList(nodes);

        Iterator it = nlist.iterator();
        while (it.hasNext()) {
            Node n = (Node)it.next();
            if (n instanceof ASTLocalVariableDeclaration
                && n.jjtGetParent() instanceof ASTBlockStatement) {
                nodes.add(n.jjtGetParent());
            }
        }
    }
}
