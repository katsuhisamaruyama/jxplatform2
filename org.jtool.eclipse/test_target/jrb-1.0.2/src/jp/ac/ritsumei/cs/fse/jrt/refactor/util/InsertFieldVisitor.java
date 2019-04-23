/*
 *     InsertFieldVisitor.java  Dec 17, 2001
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
import java.util.Iterator;

public class InsertFieldVisitor extends RefactoringVisitor {
    private String insertCode;
    private JavaClass dst;
    private JavaVariable jvar;
    private SummaryJavaField sfield;

    public InsertFieldVisitor(String code, JavaClass jc, JavaVariable jv) {
        super(jc);
        insertCode = "\n" + code;
        dst = jc;
        jvar = jv;

        SummaryJavaClass sc = jv.getJavaClass().getSummaryJavaClass();
        sfield = sc.getJavaField(jv.getName());
    }

    public InsertFieldVisitor(JavaClass jc, JavaVariable jv) {
        this(null, jc, jv);
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

    public Object visit(ASTMethodDeclarator node, Object data) {
        JavaMethod jm = node.getJavaMethod();
        Object obj = node.childrenAccept(this, data);

        Iterator it = jm.getJavaVariables().iterator();
        while (it.hasNext()) {
            JavaVariable jv = (JavaVariable)it.next();
            SummaryJavaField sf = jv.getDeclField();
            if (sf != null && sf.equals(sfield)) {
                eliminatePrefix(jv.getToken());
            }
        }
        return obj;
    }
}

