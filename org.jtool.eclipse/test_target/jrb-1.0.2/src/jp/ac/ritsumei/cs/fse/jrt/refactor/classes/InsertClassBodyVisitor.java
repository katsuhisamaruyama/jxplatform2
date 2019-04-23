/*
 *     InsertClassBodyVisitor.java  Dec 13, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.classes;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.refactor.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.Token;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.Iterator;

public class InsertClassBodyVisitor extends RefactoringVisitor {
    private String insertCode;
    private JavaClass dst;
    private JavaClass src;
    private RefactoringVisitor subVisitor;

    public InsertClassBodyVisitor(String code, JavaClass jc, JavaClass src) {
        super();
        insertCode = "\n" + code;
        dst = jc;
        this.src = src;
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

            eliminatePrefixInMethods();
            eliminatePrefixInFields();
        }
        return obj;
    }

    private void eliminatePrefixInMethods() {
        Iterator it = src.getJavaMethods().iterator();
        while (it.hasNext()) {
            JavaMethod jm = (JavaMethod)it.next();
            subVisitor = new InsertMethodVisitor(dst, jm);
        }
    }

    private void eliminatePrefixInFields() {
        Iterator it = src.getJavaFields().iterator();
        while (it.hasNext()) {
            JavaStatement jst = (JavaStatement)it.next();
            JavaVariable jv = jst.getDeclaration();
            subVisitor = new InsertFieldVisitor(dst, jv);
        }
    }
}
