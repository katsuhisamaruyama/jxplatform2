/*
 *     JavaStatement.java  Sep 19, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.model;
import jp.ac.ritsumei.cs.fse.jrt.parser.Node;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.ASTStatementExpression;
import java.util.ArrayList;
import java.util.Iterator;

public class JavaStatement extends JavaComponent {
    private JavaMethod jmethod;
    private JavaVariableList defs = new JavaVariableList();
    private JavaVariableList uses = new JavaVariableList();
    private boolean declaration = false;

    public JavaStatement() {
        super();
    }

    public JavaStatement(SimpleNode node) {
        super(node);
    }

    public JavaStatement(JavaStatement jst) {
        astNode = jst.getASTNode();
        addDefVariables(jst.getDefVariables());
        addUseVariables(jst.getUseVariables());
        setResponsive(jst);
    }

    public String toString() {
        return "JavaStatement";
    }

    public boolean isJavaStatement() {
        return true;
    }

    public void setJavaMethod(JavaMethod jmethod) {
        this.jmethod = jmethod;
    }

    public JavaMethod getJavaMethod() {
        return jmethod;
    }

    public JavaClass getJavaClass() {
        if (jmethod == null) {
            return null;
        }
        return jmethod.getJavaClass();
    }

    public void addDefVariable(JavaVariable jvar) {
        defs.add(jvar);
    }

    public void addUseVariable(JavaVariable jvar) {
        uses.add(jvar);
    }

    public void addDefVariables(ArrayList jvars) {
        addDefVariables(jvars.iterator());
    }

    public void addDefVariables(JavaVariableList jvars) {
        addDefVariables(jvars.iterator());
    }

    private void addDefVariables(Iterator it) {
        while (it.hasNext()) {
            JavaVariable jvar = (JavaVariable)it.next();
            addDefVariable(jvar);
        }
    }

    public void addUseVariables(ArrayList jvars) {
        addUseVariables(jvars.iterator());
    }

    public void addUseVariables(JavaVariableList jvars) {
        addUseVariables(jvars.iterator());
    }

    private void addUseVariables(Iterator it) {
        while (it.hasNext()) {
            JavaVariable jvar = (JavaVariable)it.next();
            addUseVariable(jvar);
        }
    }

    public void setDefVariables(JavaVariableList list) {
        defs = list;
    }

    public void setUseVariables(JavaVariableList list) {
        uses = list;
    }

    public void clearDefVariables() {
        defs.clear();
    }

    public void clearUseVariables() {
        uses.clear();
    }

    public JavaVariableList getDefVariables() {
        return defs;
    }

    public JavaVariableList getUseVariables() {
        return uses;
    }

    public boolean containsDefVariable(JavaVariable v) {
        return defs.contains(v);
    }

    public boolean containsUseVariable(JavaVariable v) {
        return uses.contains(v);
    }

    public String toStringDefVariables() {
        return defs.toString();
    }

    public String toStringUseVariables() {
        return uses.toString();
    }

    public void setDeclaration(boolean bool) {
        declaration = bool;
    }

    public boolean isDeclaration() {
        return declaration;
    }

    public JavaVariable getDeclaration() {
        return defs.getFirst();
    }

    public boolean isStatementExpression() {
        Node node = getASTNode();
        if (node instanceof ASTStatementExpression) {
            return true;
        }
        if (node.jjtGetParent() instanceof ASTStatementExpression) {
            return true;
        }
        return false;
    }
}
