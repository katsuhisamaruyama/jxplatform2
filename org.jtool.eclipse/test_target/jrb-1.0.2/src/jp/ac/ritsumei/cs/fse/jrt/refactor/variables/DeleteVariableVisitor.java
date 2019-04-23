/*
 *     DeleteVariableVisitor.java  Jan 17, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.variables;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.Node;
import jp.ac.ritsumei.cs.fse.jrt.parser.Token;

public class DeleteVariableVisitor extends RefactoringVisitor {
    private JavaVariable jvar;

    private String fieldCode;
    private int deleteIndex;
    
    public DeleteVariableVisitor(JavaVariable jv) {
        super(jv);
        jvar = jv;
    }

    public Object visit(ASTVariableDeclarator node, Object data) {
        JavaStatement jst = node.getJavaStatement();
        JavaVariable jv = jst.getDeclaration();
        Object obj = node.childrenAccept(this, data);

        if (jvar.equals(jv)) {
            deleteLocalVariable(node);
        }
        return obj;
    }

    private void deleteLocalVariable(SimpleNode node) {
        Node parent = node.jjtGetParent();
        int num = 0;
        for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
            if (parent.jjtGetChild(i) instanceof ASTVariableDeclarator) {
                num++;
            }
        }

        if (num == 1) {
            deleteNode(parent);
        } else {
            int index = getChildIndex(node);
            if (index > 0 && parent.jjtGetChild(index - 1) instanceof ASTType) {
                Token token = node.getLastToken();
                token.next.image = "";
            }
            deleteNode(node);
        }
        setHighlight(node);
    }

    public Object visit(ASTFormalParameter node, Object data) {
        JavaStatement jst = node.getJavaStatement();
        JavaVariable jv = jst.getDeclaration();
        Object obj = node.childrenAccept(this, data);

        if (jvar.equals(jv)) {
            deleteFormalParameter(node);
        }
        return obj;
    }

    private void deleteFormalParameter(SimpleNode node) {
        int index = getChildIndex(node);
        if (index == 0) {
            if (node.jjtGetParent().jjtGetNumChildren() != 1) {
                Token token = node.getLastToken();
                token.next.image = "";
            }

            InsertCodeNode n = insertCode(node.jjtGetParent(), 0, "(");
            n.setChange(false);
        }

        deleteNode(node);
        setHighlight(node);
    }
}
