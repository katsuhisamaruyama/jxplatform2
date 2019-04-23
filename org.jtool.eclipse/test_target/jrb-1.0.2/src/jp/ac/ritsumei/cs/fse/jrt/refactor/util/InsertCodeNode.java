/*
 *     InsertCodeNode.java  Dec 3, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.util;
import jp.ac.ritsumei.cs.fse.jrt.parser.*;

public class InsertCodeNode extends SimpleNode {
    private StringBuffer code;
    private boolean changed = true;

    public InsertCodeNode() {
        super();
        code = new StringBuffer();
    }

    public InsertCodeNode(String text) {
        super();
        code = new StringBuffer(text);
    }

    public void setCode(String text) {
        code = new StringBuffer(text);
    }

    public void addCode(String text) {
        code.append(text);
    }

    public String getCode() {
        return code.toString();
    }

    public void setChange(boolean bool) {
        changed = bool;
    }

    public boolean hasChanged() {
        return changed;
    }

    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
