/*
 *     PullUpFieldVisitor.java  Dec 18, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.fields;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.Node;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;

public class PullUpFieldVisitor extends RefactoringVisitor {
    private JavaVariable jvar;

    public PullUpFieldVisitor(JavaVariable jv) {
        super(jv);
        jvar = jv;
    }

    public Object visit(ASTVariableDeclarator node, Object data) {
        JavaStatement jst = node.getJavaStatement();
        Object obj = node.childrenAccept(this, data);

        JavaVariable jv = jst.getDeclaration();
        if (jvar == jv) {
            setHighlight(node);
            deleteField(node);
            setTempCode(createField(node));
        }
        return obj;
    }

    private String createField(Node node) {
        StringBuffer buf = new StringBuffer();

        buf.append("\n");
        buf.append("    ");
        if (jvar.isPrivate()) {
            int i = jvar.getModifier().remove("private");
            jvar.getModifier().add(i, "protected");
        }
        if (jvar.getModifier() != null) {
            buf.append(jvar.getModifier().toString());            
            buf.append(" ");
        }
        buf.append(jvar.getPrettyType());
        PrintVisitor printer = new PrintVisitor();
        buf.append(printer.getCode(node));
        buf.append(";");

        return buf.toString();
    }
}
