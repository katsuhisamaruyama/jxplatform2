/*
 *     ExtractSuperInterfaceVisitor.java  Dec 18, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.classes;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.Token;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;

public class ExtractSuperInterfaceVisitor extends RefactoringVisitor {
    private JavaClass jclass;
    private String newName;
    private int insertIndex;
    private String originalSuperClassNameList = null;

    public ExtractSuperInterfaceVisitor(JavaClass jc, String name) {
        super(jc);
        jclass = jc;
        newName = name;
    }

    public Object visit(ASTCompilationUnit node, Object data) {
        insertIndex = -1;
        Object obj = node.childrenAccept(this, data);

        if (insertIndex != -1) {
            StringBuffer buf = new StringBuffer();
            buf.append("public interface " + newName);
            if (originalSuperClassNameList != null) {
                buf.append(" extends" + originalSuperClassNameList);
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
                if (node.jjtGetChild(i) instanceof ASTNameList) {
                    PrintVisitor printer = new PrintVisitor();
                    originalSuperClassNameList = printer.getCode(node.jjtGetChild(i));

                    setHighlight(node.jjtGetChild(i));
                    deleteNode(node.jjtGetChild(i));
                    if (i == 0) {
                        insertCode(node, i, " " + jc.getName() + " implements " + newName);
                    } else {
                        insertCode(node, i, " implements " + newName);
                    }
                }
            }

            insertIndex = getChildIndex(node.jjtGetParent().jjtGetParent());
        }
        return obj;
    }
}
