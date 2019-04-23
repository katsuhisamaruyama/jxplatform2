/*
 *     ExtractSubclassVisitor.java  Dec 18, 2001
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

public class ExtractSubclassVisitor extends RefactoringVisitor {
    private JavaClass jclass;
    private String newName;
    private ArrayList constructors = new ArrayList();  // String

    public ExtractSubclassVisitor(JavaClass jc, String name) {
        super(jc);
        jclass = jc;
        newName = name;
    }

    public Object visit(ASTCompilationUnit node, Object data) {
        Object obj = node.childrenAccept(this, data);

        StringBuffer buf = new StringBuffer();
        buf.append("class " + newName);
        buf.append(" extends " + jclass.getName());
        buf.append(" {\n");
        Iterator it = constructors.iterator();
        while (it.hasNext()) {
            String constructorDecl = (String)it.next();
            buf.append(constructorDecl);
        }
        buf.append("}");
        insertClass(node, node.jjtGetNumChildren(), "\n\n" + buf.toString());

        return obj;
    }

    public Object visit(ASTConstructorDeclaration node, Object data) {
        JavaMethod jm = node.getJavaMethod();
        Object obj = node.childrenAccept(this, data);

        if (jclass.equals(jm.getJavaClass())) {

            StringBuffer buf = new StringBuffer();
            buf.append("\n    ");
            Token token = node.getFirstToken();
            if (!jm.getModifier().isEmpty()) {
                buf.append(token.image);
                buf.append(" ");
                token = token.next;
            }
            buf.append(newName);
        
            PrintVisitor printer = new PrintVisitor();
            buf.append(printer.getCode(node.jjtGetChild(0)));
            buf.append(" {\n");
            buf.append("        ");
            buf.append("super(");
            buf.append(jm.getParameterNames());
            buf.append(");\n");
            buf.append("    }\n");

            constructors.add(buf.toString());
        }
        return obj;
    }
}
