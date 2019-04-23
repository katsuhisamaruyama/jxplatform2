/*
 *     StatementVisitor.java  Dec 17, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.util;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.Iterator;

public class StatementVisitor extends RefactoringVisitor {
    protected StatementVisitor() {
    }

    protected StatementVisitor(JavaVariable jv) {
        super(jv);
    }

    protected StatementVisitor(String text) {
        super(text);
    }

    protected void perform(JavaStatement jst) {
    }

    public Object visit(ASTVariableDeclarator node, Object data) {
        Object obj = node.childrenAccept(this, data);
        perform(node.getJavaStatement());
        return obj;
    }

    public Object visit(ASTFormalParameter node, Object data) {
        Object obj = node.childrenAccept(this, data);
        perform(node.getJavaStatement());
        return obj;
    }

    public Object visit(ASTExpression node, Object data) {
        Object obj = node.childrenAccept(this, data);
        perform(node.getJavaStatement());
        return obj;
    }

    public Object visit(ASTPreIncrementExpression node, Object data) {
        Object obj = node.childrenAccept(this, data);
        perform(node.getJavaStatement());
        return obj;
    }

    public Object visit(ASTPreDecrementExpression node, Object data) {
        Object obj = node.childrenAccept(this, data);
        perform(node.getJavaStatement());
        return obj;
    }

    public Object visit(ASTPostfixExpression node, Object data) {
        Object obj = node.childrenAccept(this, data);
        perform(node.getJavaStatement());
        return obj;
    }

    public Object visit(ASTArguments node, Object data) {
        Object obj = node.childrenAccept(this, data);
        Iterator it = node.getArguments().iterator();
        while (it.hasNext()) {
            perform((JavaStatement)it.next());
        }
        return obj;
    }

    public Object visit(ASTStatementExpression node, Object data) {
        Object obj = node.childrenAccept(this, data);
        perform(node.getJavaStatement());
        return obj;
    }

    public Object visit(ASTSwitchStatement node, Object data) {
        Object obj = node.childrenAccept(this, data);
        perform(node.getJavaStatement());
        return obj;
    }

    public Object visit(ASTSwitchLabel node, Object data) {
        Object obj = node.childrenAccept(this, data);
        perform(node.getJavaStatement());
        return obj;
    }

    public Object visit(ASTIfStatement node, Object data) {
        Object obj = node.childrenAccept(this, data);
        perform(node.getJavaStatement());
        return obj;
    }

    public Object visit(ASTWhileStatement node, Object data) {
        Object obj = node.childrenAccept(this, data);
        perform(node.getJavaStatement());
        return obj;
    }

    public Object visit(ASTDoStatement node, Object data) {
        Object obj = node.childrenAccept(this, data);
        perform(node.getJavaStatement());
        return obj;
    }

    public Object visit(ASTForStatement node, Object data) {
        Object obj = node.childrenAccept(this, data);
        perform(node.getJavaStatement());
        return obj;
    }

    public Object visit(ASTBreakStatement node, Object data) {
        Object obj = node.childrenAccept(this, data);
        perform(node.getJavaStatement());
        return obj;
    }

    public Object visit(ASTContinueStatement node, Object data) {
        Object obj = node.childrenAccept(this, data);
        perform(node.getJavaStatement());
        return obj;
    }

    public Object visit(ASTReturnStatement node, Object data) {
        Object obj = node.childrenAccept(this, data);
        perform(node.getJavaStatement());
        return obj;
    }

    public Object visit(ASTThrowStatement node, Object data) {
        Object obj = node.childrenAccept(this, data);
        perform(node.getJavaStatement());
        return obj;
    }

    public Object visit(ASTSynchronizedStatement node, Object data) {
        Object obj = node.childrenAccept(this, data);
        perform(node.getJavaStatement());
        return obj;
    }

    public Object visit(ASTTryStatement node, Object data) {
        Object obj = node.childrenAccept(this, data);
        perform(node.getJavaStatement());
        return obj;
    }
}
