/*
 *     ExtractInterfaceVisitor.java  Dec 18, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.classes;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.Token;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.ArrayList;
import java.util.Iterator;

public class ExtractInterfaceVisitor extends RefactoringVisitor {
    private JavaClass jclass;
    private String newName;
    private String originalSuperClassNameList = null;
    private ArrayList signatures = new ArrayList();  // String

    public ExtractInterfaceVisitor(JavaClass jc, String name) {
        super(jc);
        jclass = jc;
        newName = name;
    }

    public Object visit(ASTCompilationUnit node, Object data) {
        Object obj = node.childrenAccept(this, data);

        int insertIndex = 0;
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if (node.jjtGetChild(i) instanceof ASTTypeDeclaration) {
                insertIndex = i;
                break;
            }
        }

        StringBuffer buf = new StringBuffer();
        buf.append("public interface " + newName);
        if (originalSuperClassNameList != null) {
            buf.append(" extends" + originalSuperClassNameList);
        }
        buf.append(" {\n");
        Iterator it = signatures.iterator();
        while (it.hasNext()) {
            String signature = (String)it.next();
            buf.append(signature);
        }
        buf.append("}\n");
        insertClass(node, insertIndex, "\n\n" + buf.toString());

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
                }
            }
        }
        return obj;
    }

    public Object visit(ASTMethodDeclarator node, Object data) {
        JavaMethod jm = node.getJavaMethod();
        Object obj = node.childrenAccept(this, data);

        if (jm.isPublic()) {
            StringBuffer buf = new StringBuffer();
            buf.append("    ");
            Token token = node.getFirstToken();
            if (!jm.getModifier().isEmpty()) {
                buf.append(jm.getModifier().toString());
                buf.append(" ");
            }
            buf.append(jm.getPrettyType());
            buf.append(" ");
            buf.append(jm.getName());
            PrintVisitor printer = new PrintVisitor();
            buf.append(printer.getCode(node.jjtGetChild(0)));
            buf.append(";\n");
            signatures.add(buf.toString());
        }

        return obj;
    }
}
