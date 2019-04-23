/*
 *     JavaComponent.java  Sep 11, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.model;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.JavaParserVisitor;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.GraphNode;

public class JavaComponent implements java.io.Serializable {
    transient protected SimpleNode astNode = null;
    protected int beginLine, beginColumn, endLine, endColumn;
    protected GraphNode cfgNode;

    public JavaComponent() {
    }

    public JavaComponent(SimpleNode node) {
        astNode = node;
    }

    public void setASTNode(SimpleNode node) {
        astNode = node;
    }

    public SimpleNode getASTNode() {
        return astNode;
    }

    public void setCFGNode(GraphNode node) {
        cfgNode = node;
    }

    public GraphNode getCFGNode() {
        return cfgNode;
    }

    public Object accept(JavaParserVisitor visitor, Object data) {
        return astNode.jjtAccept(visitor, data);
    }

    public void setResponsive(int beginL, int beginC, int endL, int endC) {
        beginLine = beginL;
        beginColumn = beginC;
        endLine = endL;
        endColumn = endC;
    }

    protected void setResponsive(JavaComponent comp) {
        beginLine = comp.getBeginLine();
        beginColumn = comp.getBeginColumn();
        endLine = comp.getEndLine();
        endColumn = comp.getEndColumn();
    }

    public int getBeginLine() {
        return beginLine;
    }

    public int getBeginColumn() {
        return beginColumn;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public boolean isSpecified(int beginL, int beginC, int endL, int endC) {
        return true;
    }

    public String toStringPosition() {
        return " [" + beginLine + "," + beginColumn + "]"
             + "-[" + endLine + "," + endColumn + "]";
    }

    public boolean isJavaFile() {
        return false;
    }

    public boolean isJavaClass() {
        return false;
    }

    public boolean isJavaMethod() {
        return false;
    }

    public boolean isJavaStatement() {
        return false;
    }

    public boolean isJavaVariable() {
        return false;
    }
}
