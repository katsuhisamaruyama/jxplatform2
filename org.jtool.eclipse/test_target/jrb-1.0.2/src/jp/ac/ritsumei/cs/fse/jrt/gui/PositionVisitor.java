/*
 *     PositionVisitor.java  Apr 10, 2003
 *
 *     Hisato Imanishi (hisa@fse.cs.ritsumei.ac.jp)
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleVisitor;
import jp.ac.ritsumei.cs.fse.jrt.parser.JavaParserVisitor;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.*;

public class PositionVisitor extends SimpleVisitor {
    private int beginLine;
    private int beginColumn;
    private int endLine;
    private int endColumn;
    private ArrayList javaComps = new ArrayList();
    private boolean unsupported = false;
    
    public PositionVisitor() {
        super();
    }

    public JavaComponent getTokenAt(JavaFile jfile, int line, int column) {
        beginLine = line;
        beginColumn = column;
        endLine = line;
        endColumn = column;
        javaComps.clear();

        jfile.accept(this);
        return getFoundJavaComponent();
    }

    public JavaComponent getTokenAt(JavaFile jfile, int bl, int bc, int el, int ec) {
        beginLine = bl;
        beginColumn = bc;
        endLine = el;
        endColumn = ec;
        javaComps.clear();

        jfile.accept(this);
        return getFoundJavaComponent();
    }

    private boolean foundTokenIn(JavaStatement jst) {
        boolean withIn = isWithin(jst);
        boolean defCheck = checkVariables(jst.getDefVariables());
        boolean useCheck = checkVariables(jst.getUseVariables());

        return withIn || defCheck || useCheck;
    }

    private boolean checkVariables(JavaVariableList jvl) {
        boolean result = false;
        Iterator it = jvl.iterator();
        while (it.hasNext()) {
            JavaVariable jvar = (JavaVariable)it.next();
            if (isWithin(jvar)) {
                result = true;
            }
        }
        return result;
    }

    private boolean isWithin(JavaComponent jc) {
        boolean result = false;

        if (beginLine < jc.getBeginLine() && jc.getEndLine() < endLine) {
            hasFound(jc);
            result = true;
        }
        if (beginLine == jc.getBeginLine() || endLine == jc.getBeginLine()) {
            if (beginColumn <= jc.getBeginColumn() && jc.getEndColumn() <= endColumn) {
                hasFound(jc);
                result = true;
            }
        }

        if (jc.getBeginLine() <= beginLine && endLine <= jc.getBeginLine()
          && jc.getBeginColumn() <= beginColumn && endColumn <= jc.getEndColumn()) {
            hasFound(jc);
            result = true;
        }
        return result;
    }

    private void hasFound(JavaComponent jc) {
        if (!javaComps.contains(jc)) {
            javaComps.add(jc);
        }
    }

    public ArrayList getFoundJavaComponents() {
        return javaComps;
    }

    public JavaComponent getFoundJavaComponent() {
        if (javaComps.size() == 1) {
            return (JavaComponent)javaComps.get(0);
        }
        return null;
    }

    public SimpleNode getFoundASTNode() {
        JavaComponent javaComp = getFoundJavaComponent();
        if (javaComp != null) {
            return javaComp.getASTNode();
        }
        return null;
    }

    public Object visit(ASTUnmodifiedClassDeclaration node, Object data) {
        JavaClass jclass = node.getJavaClass();
        isWithin(jclass);
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTUnmodifiedInterfaceDeclaration node, Object data) {
        JavaClass jclass = node.getJavaClass();
        isWithin(jclass);
        return node.childrenAccept(this, data);
    }


    public Object visit(ASTMethodDeclarator node, Object data) {
        JavaMethod jmethod = node.getJavaMethod();
        isWithin(jmethod);

        Iterator it = jmethod.getParameters().iterator();
        while (it.hasNext()) {
            JavaStatement jst = (JavaStatement)it.next();
            JavaVariable jvar = jst.getDeclaration();
            isWithin(jvar);
        }
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTConstructorDeclaration node, Object data) {
        JavaMethod jmethod = node.getJavaMethod();
        isWithin(jmethod);

        Iterator it = jmethod.getParameters().iterator();
        while (it.hasNext()) {
            JavaStatement jst = (JavaStatement)it.next();
            JavaVariable jvar = jst.getDeclaration();
            isWithin(jvar);
        }
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTVariableDeclarator node, Object data) {
        JavaStatement jst = node.getJavaStatement();

        JavaVariable def = jst.getDeclaration();
        isWithin(def);

        Iterator it = jst.getUseVariables().iterator();
        while (it.hasNext()) {
            JavaVariable use = (JavaVariable)it.next();
            isWithin(use);
        }
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTArguments node, Object data) {
        JavaStatement jst = node.getJavaStatement();

        JavaVariable methodInvocation = jst.getDefVariables().getFirst();
        isWithin(methodInvocation);

        Iterator its = node.getArguments().iterator();
        while (its.hasNext()) {
            JavaStatement js = (JavaStatement)its.next();

            Iterator itv = js.getUseVariables().iterator();
            while (itv.hasNext()) {
                JavaVariable arg = (JavaVariable)itv.next();
                isWithin(arg);
            }
        }
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTStatementExpression node, Object data) {
        foundTokenIn(node.getJavaStatement());
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTSwitchStatement node, Object data) {
        foundTokenIn(node.getJavaStatement());
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTSwitchLabel node, Object data) {
        foundTokenIn(node.getJavaStatement());
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTIfStatement node, Object data) {
        foundTokenIn(node.getJavaStatement());
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTWhileStatement node, Object data) {
        foundTokenIn(node.getJavaStatement());
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTDoStatement node, Object data) {
        foundTokenIn(node.getJavaStatement());
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTForStatement node, Object data) {
        foundTokenIn(node.getJavaStatement());
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTBreakStatement node, Object data) {
        foundTokenIn(node.getJavaStatement());
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTContinueStatement node, Object data) {
        foundTokenIn(node.getJavaStatement());
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTReturnStatement node, Object data) {
        foundTokenIn(node.getJavaStatement());
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTThrowStatement node, Object data) {
        if (foundTokenIn(node.getJavaStatement())) {
            unsupported = true;
        }
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTSynchronizedStatement node, Object data) {
        if (foundTokenIn(node.getJavaStatement())) {
            unsupported = true;
        }
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTTryStatement node, Object data) {
        if (foundTokenIn(node.getJavaStatement())) {
            unsupported = true;
        }
        return node.childrenAccept(this, data);
    }
}
