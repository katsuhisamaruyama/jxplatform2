/*
 *     RenameVariableVisitor.java  Dec 25, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.variables;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.Node;
import jp.ac.ritsumei.cs.fse.jrt.parser.Token;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.Iterator;

public class RenameVariableVisitor extends StatementVisitor {
    private JavaVariable jvar;
    private String newName;

    public RenameVariableVisitor(JavaVariable jv, String name) {
        super(jv);
        jvar = jv;
        newName = name;
    }

    protected void perform(JavaStatement jst) {
        renameVariablesInVariableList(jst.getDefVariables());
        renameVariablesInVariableList(jst.getUseVariables());
    }

    private void renameVariablesInVariableList(JavaVariableList jvl) {
        Iterator it = jvl.iterator();
        while (it.hasNext()) {
            JavaVariable jv = (JavaVariable)it.next();
            Token token = jv.getToken();
            if (token != null && jvar.equals(jv)) {
                setHighlight(token);

                token.image = newName;
                token.changed = true;
            }
        }
    }
}
