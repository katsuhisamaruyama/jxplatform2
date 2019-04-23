/*
 *     SelfEncapsulateFieldVisitor.java  Dec 17, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.fields;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.Node;
import jp.ac.ritsumei.cs.fse.jrt.parser.Token;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.Iterator;

public class SelfEncapsulateFieldVisitor extends StatementVisitor {
    private JavaVariable jvar;
    private JavaMethod getter;
    private JavaMethod setter;
    private SummaryJavaField sfield;

    public SelfEncapsulateFieldVisitor(JavaVariable jv, JavaMethod gm, JavaMethod sm) {
        super(jv);
        getter = gm;
        setter = sm;
        SummaryJavaClass sc = jv.getJavaClass().getSummaryJavaClass();
        sfield = sc.getJavaField(jv.getName());
    }

    public SelfEncapsulateFieldVisitor(JavaVariable jv, JavaMethod gm, JavaMethod sm, JavaFile jf) {
        super(jf.getText());
        getter = gm;
        setter = sm;
        SummaryJavaClass sc = jv.getJavaClass().getSummaryJavaClass();
        sfield = sc.getJavaField(jv.getName());
    }

    protected void perform(JavaStatement jst) {
        JavaMethod jm = jst.getJavaMethod();
        if (jm != null && !jm.equalsSignature(getter)) {
            replaceVariableWithGetter(jst.getUseVariables());
        }
    }

    private void replaceVariableWithGetter(JavaVariableList jvl) {
        Iterator it = jvl.iterator();
        while (it.hasNext()) {
            JavaVariable jv = (JavaVariable)it.next();
            SummaryJavaField sf = jv.getDeclField();

            if (sf != null && sf.equals(sfield)) {
                Token token = jv.getToken();
                if (token != null) {
                    setHighlight(token);

                    token.image = getter.getName() + "()";
                    token.changed = true;
                }
            }
        }
    }

    public Object visit(ASTPreIncrementExpression node, Object data) {
        JavaStatement jst = node.getJavaStatement();
        JavaMethod jm = jst.getJavaMethod();
        Object obj = node.childrenAccept(this, data);

        if (!jm.equalsSignature(getter) && !jm.equalsSignature(setter)) {
            JavaVariable jv = jst.getDeclaration();
            replaceVariableWithSetterGetter(node, jv, "()+1)");
        }
        return obj;
    }

    public Object visit(ASTPreDecrementExpression node, Object data) {
        JavaStatement jst = node.getJavaStatement();
        JavaMethod jm = jst.getJavaMethod();
        Object obj = node.childrenAccept(this, data);

        if (!jm.equalsSignature(getter) && !jm.equalsSignature(setter)) {
            JavaVariable jv = jst.getDeclaration();
            replaceVariableWithSetterGetter(node, jv, "()-1)");
        }
        return obj;
    }

    private void replaceVariableWithSetterGetter(Node node, JavaVariable jv, String suffix) {
        SummaryJavaField sf = jv.getDeclField();
        if (sf != null && sf.equals(sfield)) {
            Token token = ((SimpleNode)node.jjtGetParent()).getFirstToken();
            setHighlight(token);
            setHighlight(token.next);
            
            token.image = setter.getName() + "(" + getter.getName() + suffix;
            token.next.image = "";
            token.changed = true;
        }
    }

    public Object visit(ASTStatementExpression node, Object data) {
        JavaStatement jst = node.getJavaStatement();
        JavaMethod jm = jst.getJavaMethod();
        replaceVariableWithGetter(jst.getUseVariables());
        Object obj = node.childrenAccept(this, data);

        if (!jm.equalsSignature(getter) && !jm.equalsSignature(setter)
          && jst.getDefVariables().size() == 1) {
            JavaVariable jv = jst.getDeclaration();

            SummaryJavaField sf = jv.getDeclField();
            if (sf != null && sf.equals(sfield)) {
                Token token = jv.getToken();
                if (token != null) {
                    if (node.jjtGetChild(0) instanceof ASTPrimaryExpression) {
                        if (node.jjtGetNumChildren() == 3) {
                            replaceVariableWithSetter(node, token);

                        } else {
                            setHighlight(token);
                            setHighlight(token.next);
                            if (token.next.image.equals("++")) {
                                token.image = setter.getName() + "(" + getter.getName() + "()+1)";
                            } else {
                                token.image = setter.getName() + "(" + getter.getName() + "()-1)";
                            }
                            token.next.image = "";
                            token.changed = true;
                        }
                    }
                }
            }
        }
        return obj;
    }

    private void replaceVariableWithSetter(Node node, Token token) {
        PrintVisitor printer = new PrintVisitor();
        String code = printer.getCode(node.jjtGetChild(2));

        deleteNode(node.jjtGetChild(1));
        deleteNode(node.jjtGetChild(2));
        setHighlight(node);

        int i = 0;
        while (code.charAt(i) == ' ') {
            i++;
        }
        token.image = setter.getName() + "(" + code.substring(i) + ")";
        token.changed = true;
    }
}
