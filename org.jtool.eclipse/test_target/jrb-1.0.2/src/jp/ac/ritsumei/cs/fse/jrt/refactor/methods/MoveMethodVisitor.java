/*
 *     MoveMethodVisitor.java  Dec 13, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.methods;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.Node;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.Token;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.Iterator;

public class MoveMethodVisitor extends RefactoringVisitor {
    private JavaMethod jmethod;
    private String rname;
    private String sDecl;
    private int moveIndex;
    private SummaryJavaMethod smethod;

    public MoveMethodVisitor(JavaMethod jm, String ref) {
        super(jm);
        jmethod = jm;
        rname = ref;
        SummaryJavaClass sc = jm.getJavaClass().getSummaryJavaClass();
        smethod = sc.getJavaMethod(jm.getSignature());
    }

    public Object visit(ASTClassBody node, Object data) {
        moveIndex = -1;
        Object obj = node.childrenAccept(this, data);

        if (moveIndex != -1) {
            insertMethod(node, moveIndex, "\n" + sDecl);
        }
        return obj;
    }
    
    public Object visit(ASTMethodDeclarator node, Object data) {
        JavaMethod jm = node.getJavaMethod();
        Object obj = node.childrenAccept(this, data);

        if (jmethod.equals(jm)) {
            setHighlight(node.jjtGetParent());
            deleteMethod(node);

            setTempCode(createDstMethod(node, jm));

            if (rname != null) {
                sDecl = createSrcMethod(node, jm);
                moveIndex = getChildIndex(node.jjtGetParent().jjtGetParent());
            }
        }
        return obj;
    }

    public Object visit(ASTArguments node, Object data) {
        Object obj = node.childrenAccept(this, data);

        SummaryJavaMethod sm = node.getCalledMethod();
        if (sm != null && sm.equals(smethod)) {
            SimpleNode n = getPreviousNode(node.jjtGetParent());
            if (n != null) {
                Token token = n.getLastToken();
                token.changed = true;
            }
        }
        return obj;
    }

    private String createSrcMethod(Node node, JavaMethod jm) {
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
        if (rname != null) {
            if (!jm.isVoid()) {
                buf.append("return ");
            }
            if (rname.indexOf(".") != -1) {
                buf.append(rname.substring(rname.lastIndexOf(".") + 1));
            } else {
                buf.append(rname);
            }
            buf.append(".");
        }
        buf.append(jm.getName());
        buf.append("(");
        buf.append(jm.getParameterNames());
        buf.append(");\n");
        buf.append("    }");
        
        return buf.toString();
    }

    private String createDstMethod(Node node, JavaMethod jm) {
        StringBuffer buf = new StringBuffer();

        buf.append("\n\n");
        buf.append("    ");
        jm.getModifier().remove("private");
        jm.getModifier().remove("protected");
        jm.getModifier().add("public");

        buf.append(jm.getModifier().toString());
        buf.append(" ");
        buf.append(jm.getPrettyType());
        buf.append(" ");
        buf.append(jm.getName());
        PrintVisitor printer = new PrintVisitor();
        buf.append(printer.getCode(node.jjtGetChild(0)));

        if (rname != null) {
            replaceVariableNamesWithThisInMethod(rname, jm);
        }

        Node parent = node.jjtGetParent();
        for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
            if (parent.jjtGetChild(i) instanceof ASTBlock) {
                printer = new PrintVisitor();
                buf.append(printer.getCode(parent.jjtGetChild(i)));
            }
        }
        return buf.toString();
    }            

    private void replaceVariableNamesWithThisInMethod(String name, JavaMethod jm) {
        Iterator it = jm.getJavaVariables().iterator();
        while (it.hasNext()) {
            JavaVariable jv = (JavaVariable)it.next();
            if (!jv.isParameter() && name.compareTo(jv.getName()) == 0) {
                Token token = jv.getToken();
                token.image = "this";
                removePreviousThis(jv);
            }
        }
    }

    private void removePreviousThis(JavaVariable jv) {
        SimpleNode prev = getPreviousNode(jv.getASTNode());
        if (prev != null) {
            Token token = prev.getFirstToken();
            if (token.image.equals("this")) {
                token.image = "";
                token.next.image = "";
            }
        }
    }
}
