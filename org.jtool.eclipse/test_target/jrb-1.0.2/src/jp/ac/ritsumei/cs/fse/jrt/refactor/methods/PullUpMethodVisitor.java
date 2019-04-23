/*
 *     PullUpMethodVisitor.java  Dec 14, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.methods;
import jp.ac.ritsumei.cs.fse.jrt.refactor.*;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.Node;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.Token;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class PullUpMethodVisitor extends RefactoringVisitor {
    private JavaMethod jmethod;
    private List callings;
    private boolean isCalled;
    private JavaMethod cjm;

    public PullUpMethodVisitor(JavaMethod jm, List calls, boolean called) {
        super(jm);
        jmethod = jm;
        callings = calls;
        isCalled = called;
    }

    public Object visit(ASTMethodDeclaration node, Object data) {
        Object obj = node.childrenAccept(this, data);

        if (jmethod.equals(cjm)) {

            Iterator it = cjm.getJavaVariables().iterator();
            while (it.hasNext()) {
                JavaVariable jv = (JavaVariable)it.next();
                eliminatePrefix(jv.getToken());
            }

            it = callings.iterator();
            while (it.hasNext()) {
                JavaMethod m = (JavaMethod)it.next();
                addTempCode(createMethodDecl(m));
            }
        }
        return obj;
    }

    public Object visit(ASTMethodDeclarator node, Object data) {
        JavaMethod jm = node.getJavaMethod();
        cjm = jm;
        Object obj = node.childrenAccept(this, data);

        if (jmethod.equals(jm)) {
            setHighlight(node.jjtGetParent());
            deleteMethod(node);

            setTempCode(createDstMethod(node, jm));
        }
        return obj;
    }

    public Object visit(ASTArguments node, Object data) {
        Object obj = node.childrenAccept(this, data);

        if (jmethod.equals(cjm)) {
            SimpleNode n = getPreviousNode(node.jjtGetParent());
            if (n != null) {
                Token token = n.getFirstToken();
                eliminatePrefix(token);
            }
        }
        return obj;
    }

    private String createDstMethod(Node node, JavaMethod jm) {
        StringBuffer buf = new StringBuffer();

        buf.append("\n");
        buf.append("    ");
        if (isCalled && jm.isPrivate()) {
            jm.getModifier().remove("private");
            jm.getModifier().add("protected");
        }
        if (!jm.getModifier().isEmpty()) {
            buf.append(jm.getModifier().toString());
            buf.append(" ");
        }
        buf.append(jm.getPrettyType());
        buf.append(" ");
        buf.append(jm.getName());
        PrintVisitor printer = new PrintVisitor();
        buf.append(printer.getCode(node.jjtGetChild(0)));

        Node parent = node.jjtGetParent();
        for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
            if (parent.jjtGetChild(i) instanceof ASTBlock) {
                printer = new PrintVisitor();
                buf.append(printer.getCode(parent.jjtGetChild(i)));
            }
        }

        return buf.toString();
    }

    private String createMethodDecl(JavaMethod jm) {
        StringBuffer buf = new StringBuffer();

        buf.append("\n");
        buf.append("    ");
        jm.getModifier().add("abstract");
        buf.append(jm.getModifier().toString());
        buf.append(" ");
        buf.append(jm.getPrettyType());
        PrintVisitor printer = new PrintVisitor();
        buf.append(printer.getCode(jm.getASTNode()));
        buf.append(";");

        return buf.toString();
    }
}
