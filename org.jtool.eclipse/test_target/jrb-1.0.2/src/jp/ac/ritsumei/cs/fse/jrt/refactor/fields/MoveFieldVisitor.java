/*
 *     MoveFieldVisitor.java  Dec 17, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.fields;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.Node;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;

public class MoveFieldVisitor extends StatementVisitor {
    private JavaVariable jvar;
    private String rname;
    private String dstName;
    private JavaMethod getter;
    private JavaMethod setter;
    private String gCode;
    private String sCode;
    private String fCode;
    private String gDecl;
    private String sDecl;
    private String fDecl;
    private int insertGetterIndex;
    private int insertSetterIndex;
    private int insertFieldIndex;

    public MoveFieldVisitor(JavaVariable jv, String ref, String dst, JavaMethod gm, JavaMethod sm) {
        super(jv);
        jvar = jv;
        rname = ref;
        dstName = dst;
        getter = gm;
        setter = sm;
    }

    public Object visit(ASTClassBody node, Object data) {
        insertFieldIndex = -1;
        insertGetterIndex = -1;
        insertSetterIndex = -1;
        Object obj = node.childrenAccept(this, data);

        StringBuffer buf = new StringBuffer();
        buf.append("\n");
        buf.append(fCode);
        buf.append(gCode);
        buf.append(sCode);
        setTempCode(buf.toString());

        if (insertFieldIndex != -1 && insertGetterIndex != -1 && insertSetterIndex != -1) {
            insertGetterIndex++;
            insertSetterIndex++;

            insertCode(node, insertFieldIndex, "\n" + fDecl);
            insertCode(node, insertGetterIndex, "\n" + gDecl);
            if (insertGetterIndex < insertSetterIndex) {
                insertSetterIndex++;
            }
            insertCode(node, insertSetterIndex, "\n" + sDecl);
        }
        return obj;
    }
    
    public Object visit(ASTMethodDeclarator node, Object data) {
        JavaMethod jm = node.getJavaMethod();
        Object obj = node.childrenAccept(this, data);

        PrintVisitor printer = new PrintVisitor();

        if (jm.equalsSignature(getter)) {
            gCode = printer.getCode(node.jjtGetParent());
            if (rname != null) {
                gDecl = createGetter(node, jm);
                insertGetterIndex = getChildIndex(node.jjtGetParent().jjtGetParent());
            }
            setHighlight(node.jjtGetParent().jjtGetParent());
            deleteMethod(node);
        }

        if (jm.equalsSignature(setter)) {
            sCode = printer.getCode(node.jjtGetParent());
            if (rname != null) {
                sDecl = createSetter(node, jm);
                insertSetterIndex = getChildIndex(node.jjtGetParent().jjtGetParent());
            }
            setHighlight(node.jjtGetParent().jjtGetParent());
            deleteMethod(node);
        }
        return obj;
    }


    public Object visit(ASTVariableDeclarator node, Object data) {
        JavaStatement jst = node.getJavaStatement();
        Object obj = node.childrenAccept(this, data);

        JavaVariable jv = jst.getDeclaration();
        if (jvar == jv) {
            Node n = node.jjtGetParent().jjtGetParent();

            insertFieldIndex = getChildIndex(node.jjtGetParent().jjtGetParent());
            fDecl = createRefField();

            setHighlight(node);
            deleteField(node);

            fCode = createField(node);
        }
        return obj;
    }

    private String createRefField() {
        StringBuffer buf = new StringBuffer();

        buf.append("    ");
        buf.append("private ");
        buf.append(dstName);
        buf.append(" ");
        buf.append(rname);
        buf.append(";");
        return buf.toString();
    }

    private String createField(Node node) {
        StringBuffer buf = new StringBuffer();

        buf.append("    ");
        buf.append("private ");
        buf.append(jvar.getPrettyType());
        PrintVisitor printer = new PrintVisitor();
        buf.append(printer.getCode(node));
        buf.append(";");

        return buf.toString();
    }

    private String createGetter(Node node, JavaMethod jm) {
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
        buf.append(rname);
        buf.append(".");
        buf.append(jm.getName());
        buf.append("(");
        buf.append(jm.getParameterNames());
        buf.append(");\n");

        buf.append("    }");
        
        return buf.toString();
    }

    private String createSetter(Node node, JavaMethod jm) {
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
        buf.append(rname);
        buf.append(".");
        buf.append(jm.getName());
        buf.append("(");
        buf.append(jm.getParameterNames());
        buf.append(");\n");

        buf.append("    }");
        
        return buf.toString();
    }
}
