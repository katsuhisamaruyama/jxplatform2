/*
 *     InsertMethodVisitor.java  Dec 13, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.util;
import jp.ac.ritsumei.cs.fse.jrt.parser.Node;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.Token;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;

public class InsertMethodVisitor extends RefactoringVisitor {
    private String insertCode;
    private JavaClass dst;
    private JavaMethod jmethod;
    private SummaryJavaMethod smethod;

    public InsertMethodVisitor(String code, JavaClass jc, JavaMethod jm) {
        super(jc);
        insertCode = "\n" + code;
        dst = jc;
        jmethod = jm;

        SummaryJavaClass sc = jm.getJavaClass().getSummaryJavaClass();
        smethod = sc.getJavaMethod(jm.getSignature());
    }

    public InsertMethodVisitor(JavaClass jc, JavaMethod jm) {
        this(null, jc, jm);
    }

    public Object visit(ASTUnmodifiedClassDeclaration node, Object data) {
        JavaClass jc = node.getJavaClass();
        Object obj = node.childrenAccept(this, data);

        if (insertCode != null && dst.equals(jc)) {
            ASTClassBody body = (ASTClassBody)node.jjtGetChild(node.jjtGetNumChildren() - 1);

            if (body.jjtGetNumChildren() == 0) {
                Token token = body.getFirstToken();
                token.image = "";
                InsertCodeNode in = insertCode(body, 0, " {");
                in.setChange(false);
            }
            insertCode(body, body.jjtGetNumChildren(), insertCode);
        }
        return obj;
    }

    public Object visit(ASTArguments node, Object data) {
        Object obj = node.childrenAccept(this, data);

        SummaryJavaMethod sm = node.getCalledMethod();
        if (sm != null && sm.equals(smethod)) {
            SimpleNode n = getPreviousNode(node.jjtGetParent());
            if (n != null) {
                eliminatePrefix(n.getFirstToken());
            }
        }
        return obj;

    }
}
