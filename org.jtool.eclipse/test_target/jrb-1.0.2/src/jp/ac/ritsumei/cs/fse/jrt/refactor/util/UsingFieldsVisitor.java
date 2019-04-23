/*
 *     UsingFieldsVisitor.java  Jan 8, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.util;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.Node;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.Token;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.Iterator;

public class UsingFieldsVisitor extends RefactoringVisitor {
    private SummaryJavaField sfield;
    
    public UsingFieldsVisitor(JavaVariable jv, JavaFile jf) {
        super(jf.getText());
        SummaryJavaClass sc = jv.getJavaClass().getSummaryJavaClass();
        sfield = sc.getJavaField(jv.getName());
    }

    protected void perform(JavaStatement jst) {
        checkVariablesInVariableList(jst.getDefVariables());
        checkVariablesInVariableList(jst.getUseVariables());
    }

    private void checkVariablesInVariableList(JavaVariableList jvl) {
        Iterator it = jvl.iterator();
        while (it.hasNext()) {
            JavaVariable jv = (JavaVariable)it.next();

            SummaryJavaField sf = jv.getDeclField();
            if (sf != null && sf.equals(sfield)) {

                Token token = jv.getToken();
                if (token != null) {
                    token.toBeChanged = true;
                }
            }
        }
    }
}
