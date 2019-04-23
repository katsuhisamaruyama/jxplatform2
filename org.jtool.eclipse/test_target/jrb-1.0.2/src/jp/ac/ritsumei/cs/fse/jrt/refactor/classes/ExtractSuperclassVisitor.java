/*
 *     ExtractSuperclassVisitor.java  Dec 18, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.classes;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.Token;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;

public class ExtractSuperclassVisitor extends RefactoringVisitor {
    private JavaClass jclass;
    private String newName;
    private int insertIndex;
    private String originalSuperClassName = null;
    private String originalSuperClassNameList = null;

    public ExtractSuperclassVisitor(JavaClass jc, String name) {
        super(jc);
        jclass = jc;
        newName = name;
    }

    public Object visit(ASTCompilationUnit node, Object data) {
        insertIndex = -1;
        Object obj = node.childrenAccept(this, data);

        if (insertIndex != -1) {
            StringBuffer buf = new StringBuffer();
            buf.append("class " + newName);
            if (originalSuperClassName != null) {
                buf.append(" extends" + originalSuperClassName);
            }
            if (originalSuperClassNameList != null) {
                buf.append(" implements" + originalSuperClassNameList);
            }
            buf.append(" {\n");
            buf.append("}");

            insertClass(node, insertIndex, "\n\n" + buf.toString());
        }
        return obj;
    }

    public Object visit(ASTUnmodifiedClassDeclaration node, Object data) {
        JavaClass jc = node.getJavaClass();
        Object obj = node.childrenAccept(this, data);

        if (jclass.equals(jc)) {
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                if (node.jjtGetChild(i) instanceof ASTName) {
                    
                    PrintVisitor printer = new PrintVisitor();
                    originalSuperClassName = printer.getCode(node.jjtGetChild(i));

                    ASTName nameNode = (ASTName)node.jjtGetChild(i);
                    Token token = nameNode.getFirstToken();
                    setHighlight(token);
                        
                    token.image = newName;
                    token.changed = true;
                }

                if (node.jjtGetChild(i) instanceof ASTNameList) {
                    PrintVisitor printer = new PrintVisitor();
                    originalSuperClassNameList = printer.getCode(node.jjtGetChild(i));
                    
                    setHighlight(node.jjtGetChild(i));
                    if (originalSuperClassName != null) {
                        deleteNode(node.jjtGetChild(i));
                    } else {
                        Token token = node.getFirstToken().next.next;
                        token.image = "extends " + newName;
                        token.next.image = "";
                        token.changed = true;
                    }
                }
            }
            
            if (originalSuperClassName == null && originalSuperClassNameList == null) {
                Token token = node.getFirstToken().next;
                Token ntoken = new Token();
                ntoken.next = token.next;
                ntoken.image = " extends " + newName;
                token.next = ntoken;
                ntoken.changed = true;
            }

            insertIndex = getChildIndex(node.jjtGetParent().jjtGetParent());
        }            
        return obj;
    }
}
