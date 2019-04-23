/*
 *     DeleteCodeNode.java  Dec 3, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.util;
import jp.ac.ritsumei.cs.fse.jrt.parser.*;

public class DeleteCodeNode extends SimpleNode {
    private DeleteCodeNode() {
    }

    public DeleteCodeNode(Node node) {
        super();
        last = ((SimpleNode)node).getLastToken();
    }

    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
