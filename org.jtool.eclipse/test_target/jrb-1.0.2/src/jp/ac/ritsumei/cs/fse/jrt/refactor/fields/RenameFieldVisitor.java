/*
 *     RenameFieldVisitor.java  Dec 14, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.fields;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.Token;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.Iterator;

public class RenameFieldVisitor extends StatementVisitor {
    private JavaVariable jvar;
    private String newName;
    private boolean attachPrefix = false;
    private SummaryJavaField sfield;

    public RenameFieldVisitor(JavaVariable jv, String name) {
        super(jv);
        jvar = jv;
        newName = name;
        SummaryJavaClass sc = jv.getJavaClass().getSummaryJavaClass();
        sfield = sc.getJavaField(jv.getName());
    }

    public RenameFieldVisitor(JavaVariable jv, String name, JavaFile jf) {
        super(jf.getText());
        jvar = jv;
        newName = name;
        SummaryJavaClass sc = jv.getJavaClass().getSummaryJavaClass();
        sfield = sc.getJavaField(jv.getName());
    }

    protected void perform(JavaStatement jst) {
        renameVariablesInVariableList(jst.getDefVariables());
        renameVariablesInVariableList(jst.getUseVariables());
    }

    private void renameVariablesInVariableList(JavaVariableList jvl) {
        Iterator it = jvl.iterator();
        while (it.hasNext()) {
            JavaVariable jv = (JavaVariable)it.next();
            SummaryJavaField sf = jv.getDeclField();

            if (jvar == jv || (sf != null && sf.equals(sfield))) {
                Token token = jv.getToken();
                if (token != null) {
                    setHighlight(token);

                    if (attachPrefix) {
                        token.image = "this." + newName;
                    } else {
                        token.image = newName;
                    }
                    token.changed = true;
                }
            }
        }
    }

    public Object visit(ASTMethodDeclarator node, Object data) {
        JavaMethod jm = node.getJavaMethod();
        attachPrefix = containsVariableWithSameName(jm);

        return node.childrenAccept(this, data);
    }

    private boolean containsVariableWithSameName(JavaMethod jmethod) {
        Iterator it = jmethod.getJavaVariables().iterator();
        while (it.hasNext()) {
            JavaVariable jv = (JavaVariable)it.next();
            if (newName.compareTo(jv.getName()) == 0) {
                return true;
            }
        }
        return false;
    }
}
