/*
 *     RenameClassVisitor.java  Dec 1, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.classes;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.Token;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;

public class RenameClassVisitor extends RefactoringVisitor {
    private JavaClass jclass;
    private String oldName;
    private String newName;

    public RenameClassVisitor(JavaClass jc, String name) {
        super(jc);
        jclass = jc;
        oldName = jclass.getName();
        newName = name;
    }

    public RenameClassVisitor(JavaClass jc, String name, JavaFile jf) {
        super(jf.getText());
        jclass = jc;
        oldName = jclass.getName();
        newName = name;
    }

    public Object visit(ASTUnmodifiedClassDeclaration node, Object data) {
        JavaClass jc = node.getJavaClass();
        Object obj = node.childrenAccept(this, data);

        if (jclass.equals(jc)) {
            Token token = node.getFirstToken().next;
            setHighlight(token);

            token.image = newName;
            token.changed = true;
        }

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if (node.jjtGetChild(i) instanceof ASTName) {
                if (jc.getSuperClassName().compareTo(jclass.getQualifiedName()) == 0) {
                    ASTName nameNode = (ASTName)node.jjtGetChild(i);
                    Token token = nameNode.getFirstToken();
                    setHighlight(token);

                    token.image = newName;
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

        if (oldName.compareTo(token.image) == 0) {
            setHighlight(token);
            token.image = newName;
            token.changed = true;
        }
        return obj;
    }

    public Object visit(ASTType node, Object data) {
        Object obj = node.childrenAccept(this, data);

        if (node.jjtGetChild(0) instanceof ASTName) {
            ASTName nameNode = (ASTName)node.jjtGetChild(0);
            Token token = nameNode.getFirstToken();

            if (oldName.compareTo(token.image) == 0) {
                setHighlight(token);
                token.image = newName;
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

            if (oldName.compareTo(token.image) == 0) {
                setHighlight(token);
                token.image = newName;
                token.changed = true;
            }
        }
        return obj;
    }
}
