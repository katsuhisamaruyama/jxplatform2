/*
 *     MergeClassVisitor.java  Dec 19, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.classes;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.Token;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.Node;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.Iterator;

public class MergeClassVisitor extends RefactoringVisitor {
    private JavaClass src;
    private JavaClass dst;

    public MergeClassVisitor(JavaClass jc, JavaClass dst) {
        super(jc);
        src = jc;
        this.dst = dst;
    }

    public Object visit(ASTUnmodifiedClassDeclaration node, Object data) {
        JavaClass jc = node.getJavaClass();
        Object obj = node.childrenAccept(this, data);

        if (src.equals(jc)) {
            Node body = node.jjtGetChild(node.jjtGetNumChildren() - 1);

            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < body.jjtGetNumChildren(); i++) {
                PrintVisitor printer = new PrintVisitor();
                buf.append(printer.getCode(body.jjtGetChild(i)));
                deleteNode(body.jjtGetChild(i));
            }
            setTempCode(buf.toString());
            setHighlight(node.jjtGetParent());

            deleteClass(node);
        }
        return obj;
    }

    public Object visit(ASTMethodDeclarator node, Object data) {
        JavaMethod jm = node.getJavaMethod();
        Object obj = node.childrenAccept(this, data);

        if (src.isChildOf(dst)) {
            Iterator it = jm.getJavaVariables().iterator();
            while (it.hasNext()) {
                JavaVariable jv = (JavaVariable)it.next();
                eliminatePrefix(jv.getToken());
            }
        }
        return obj;
    }

    public Object visit(ASTArguments node, Object data) {
        Object obj = node.childrenAccept(this, data);

        if (src.isChildOf(dst)) {
            SimpleNode n = getPreviousNode(node.jjtGetParent());
            if (n != null) {
                Token token = n.getFirstToken();
                eliminatePrefix(token);
            }
        }
        return obj;
    }
}
