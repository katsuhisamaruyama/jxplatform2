/*
 *     MoveClassVisitor.java  Dec 6, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.classes;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.Token;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.Iterator;

public class MoveClassVisitor extends RefactoringVisitor {
    private JavaClass src;
    private boolean isPublic;

    public MoveClassVisitor(JavaClass jc, boolean ip) {
        super(jc);
        src = jc;
        isPublic = ip;
    }

    public MoveClassVisitor(JavaClass jc, JavaFile jf) {
        super(jf.getText());
        src = jc;
    }

    public Object visit(ASTUnmodifiedClassDeclaration node, Object data) {
        JavaClass jc = node.getJavaClass();
        Object obj = node.childrenAccept(this, data);

        if (src.equals(jc)) {
            StringBuffer buf = new StringBuffer();
            PrintVisitor printer = new PrintVisitor();
            setHighlight(node.jjtGetParent());

            buf.append("\n");
            if (isPublic) {
                buf.append("public ");
            }
            buf.append(printer.getCode(node).trim());
            buf.append("\n");
            setTempCode(buf.toString());
            deleteClass(node);
        }

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if (node.jjtGetChild(i) instanceof ASTName) {
                if (jc.getSuperClassName().compareTo(src.getQualifiedName()) == 0) {
                    ASTName nameNode = (ASTName)node.jjtGetChild(i);
                    Token token = nameNode.getFirstToken();
                    token.changed = true;
                }
            }
        }
        return obj;
    }

    public Object visit(ASTConstructorDeclaration node, Object data) {
        JavaMethod jm = node.getJavaMethod();
        Object obj = node.childrenAccept(this, data);

        Token token = node.getFirstToken();
        if (!jm.getModifier().isEmpty()) {
            token = token.next;
        }

        if (src.getName().compareTo(token.image) == 0) {
            token.changed = true;
        }
        return obj;
    }

    public Object visit(ASTType node, Object data) {
        Object obj = node.childrenAccept(this, data);

        if (node.jjtGetChild(0) instanceof ASTName) {
            ASTName nameNode = (ASTName)node.jjtGetChild(0);
            Token token = nameNode.getFirstToken();

            if (src.getName().compareTo(token.image) == 0) {
                token.changed = true;
            }
        }
        return obj;
    }

    public Object visit(ASTAllocationExpression node, Object data) {
        Object obj = node.childrenAccept(this, data);

        if (node.jjtGetChild(0) instanceof ASTResponsiveName) {
            ASTResponsiveName nameNode = (ASTResponsiveName)node.jjtGetChild(0);
            Token token = nameNode.getFirstToken();

            if (src.getName().compareTo(token.image) == 0) {
                token.changed = true;
            }
        }
        return obj;
    }
}
