/*
 *     PushDownMethodVisitor.java  Dec 14, 2001
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
import java.util.ArrayList;
import java.util.Iterator;

public class PushDownMethodVisitor extends RefactoringVisitor {
    private JavaMethod jmethod;

    public PushDownMethodVisitor(JavaMethod jm) {
        super(jm);
        jmethod = jm;
    }

    public Object visit(ASTMethodDeclarator node, Object data) {
        JavaMethod jm = node.getJavaMethod();
        Object obj = node.childrenAccept(this, data);

        if (jmethod.equals(jm)) {
            setHighlight(node.jjtGetParent());
            deleteMethod(node);
            
            setTempCode(createDstMethod(node, jm));
        }
        return obj;
    }

    private String createDstMethod(Node node, JavaMethod jm) {
        StringBuffer buf = new StringBuffer();

        buf.append("\n");
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

        Node parent = node.jjtGetParent();
        for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
            if (parent.jjtGetChild(i) instanceof ASTBlock) {
                printer = new PrintVisitor();
                buf.append(printer.getCode(parent.jjtGetChild(i)));
            }
        }

        return buf.toString();
    }
}
