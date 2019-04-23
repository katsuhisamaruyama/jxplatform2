/*
 *     EncapsulateFieldVisitor.java  Dec 17, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.fields;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.Node;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;

public class EncapsulateFieldVisitor extends RefactoringVisitor {
    private JavaVariable jvar;
    private JavaMethod getter;
    private JavaMethod setter;
    private int insertIndex;
    private String insertCode;

    public EncapsulateFieldVisitor(JavaVariable jv, JavaMethod gm, JavaMethod sm) {
        super(jv);
        jvar = jv;
        getter = gm;
        setter = sm;
    }

    public Object visit(ASTClassBody node, Object data) {
        insertIndex = -1;
        Object obj = node.childrenAccept(this, data);

        if (insertIndex != -1) {
            insertCode(node, insertIndex, "\n" + insertCode);
        }
        return obj;
    }

    public Object visit(ASTVariableDeclarator node, Object data) {
        JavaStatement jst = node.getJavaStatement();
        Object obj = node.childrenAccept(this, data);

        JavaVariable jv = jst.getDeclaration();
        if (jvar == jv) {
            Node n = node.jjtGetParent().jjtGetParent();
            insertIndex = getChildIndex(n) + 1;
            setHighlight(node);
            deleteField(node);

            String name = jvar.getPrettyName();
            StringBuffer buf = new StringBuffer();
            buf.append("\n");
            buf.append(createField(node));
            if (getter != null) {
                buf.append(createGetter(name));
            }
            if (setter != null) {
                buf.append(createSetter(name));
            }
            insertCode = buf.toString();
        }
        return obj;
    }

    private String createField(Node node) {
        StringBuffer buf = new StringBuffer();

        buf.append("    ");
        buf.append("private ");
        buf.append(jvar.getPrettyType());
        PrintVisitor printer = new PrintVisitor();
        buf.append(printer.getCode(node));
        buf.append(";\n");

        return buf.toString();
    }

    private String createGetter(String name) {
        StringBuffer buf = new StringBuffer();

        buf.append("    ");
        buf.append("public ");
        buf.append(jvar.getPrettyType());
        buf.append(" ");
        buf.append(getter.getName());
        buf.append("()");
        buf.append(" {\n");
        buf.append("        ");
        buf.append("return ");
        buf.append(name);
        buf.append(";\n");

        buf.append("    }\n");

        return buf.toString();
    }

    private String createSetter(String name) {
        StringBuffer buf = new StringBuffer();

        String arg;
        if (name.startsWith("_")) {
            arg = "arg" + name;
        } else {
            arg = "arg_" + name;
        }
        buf.append("    ");
        buf.append("public ");
        buf.append("void ");
        buf.append(setter.getName());
        buf.append("(");
        buf.append(jvar.getPrettyType());
        buf.append(" ");
        buf.append(arg);
        buf.append(") {\n");

        buf.append("        ");
        buf.append(name);
        buf.append(" = ");
        buf.append(arg);
        buf.append(";\n");

        buf.append("    }\n");

        return buf.toString();
    }
}
