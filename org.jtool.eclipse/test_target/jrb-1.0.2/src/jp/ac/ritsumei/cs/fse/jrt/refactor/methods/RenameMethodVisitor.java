/*
 *     RenameMethodVisitor.java  Dec 12, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.methods;
import jp.ac.ritsumei.cs.fse.jrt.refactor.*;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.Node;
import jp.ac.ritsumei.cs.fse.jrt.parser.Token;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;

public class RenameMethodVisitor extends RefactoringVisitor {
    private JavaMethod jmethod;
    private String newName;
    private int renameIndex;
    private String oldMethodDecl;
    private SummaryJavaMethod smethod;

    public RenameMethodVisitor(JavaMethod jm, String name) {
        super(jm);
        jmethod = jm;
        newName = name;
        SummaryJavaClass sc = jm.getJavaClass().getSummaryJavaClass();
        smethod = sc.getJavaMethod(jm.getSignature());
    }

    public RenameMethodVisitor(JavaMethod jm, String name, JavaFile jf) {
        super(jf.getText());
        jmethod = jm;
        newName = name;
        SummaryJavaClass sc = jm.getJavaClass().getSummaryJavaClass();
        smethod = sc.getJavaMethod(jm.getSignature());
    }

    public Object visit(ASTClassBody node, Object data) {
        renameIndex = -1;
        Object obj = node.childrenAccept(this, data);

        if (renameIndex != -1) {
            insertMethod(node, renameIndex, "\n\n" + oldMethodDecl + "\n");
        }
        return obj;
    }

    public Object visit(ASTMethodDeclarator node, Object data) {
        JavaMethod jm = node.getJavaMethod();

        if (jmethod.equals(jm)) {
            Token token = node.getFirstToken();
            setHighlight(token);

            token.image = newName;
            token.changed = true;

            Node n = node.jjtGetParent().jjtGetParent();
            renameIndex = getChildIndex(n) + 1;
            oldMethodDecl = createOldMethod(node, jm);
        }
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTArguments node, Object data) {
        Object obj = node.childrenAccept(this, data);

        SummaryJavaMethod sm = node.getCalledMethod();
        if (sm != null && smethod.equals(sm)) {
            SimpleNode n = getPreviousNode(node.jjtGetParent());

            if (n != null) {
                Token token = n.getLastToken();
                if (token != null) {
                    setHighlight(token);

                    token.image = newName;
                    token.changed = true;
                }
            }
        }
        return obj;
    }

    private String createOldMethod(Node node, JavaMethod jm) {
        StringBuffer buf = new StringBuffer();

        buf.append("    ");
        if (!jm.getModifier().isEmpty()) {
            buf.append(jm.getModifier().toString());
            buf.append(" ");
        }
        buf.append(jm.getPrettyType());
        buf.append(" ");
        buf.append(jm.getName());

        PrintVisitor printer = new PrintVisitor();
        buf.append(printer.getCode(node.jjtGetChild(0)));
        buf.append(" {\n");
        buf.append("        ");
        if (!jm.isVoid()) {
            buf.append("return ");
        }
        buf.append(newName);
        buf.append("(");
        buf.append(jm.getParameterNames());
        buf.append(");\n");
        buf.append("    }");
        
        return buf.toString();
    }
}
