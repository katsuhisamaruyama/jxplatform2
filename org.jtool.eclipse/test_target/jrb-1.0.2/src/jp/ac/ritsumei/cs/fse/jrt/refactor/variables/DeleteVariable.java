/*
 *     DeleteVariable.java  Jan 17, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.variables;
import jp.ac.ritsumei.cs.fse.jrt.refactor.VariableRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.List;
import javax.swing.JFrame;

public class DeleteVariable extends VariableRefactoring {
    public DeleteVariable() {
        super();
    }

    public DeleteVariable(JFrame f, JavaVariable jv, String dir) {
        super(f, jv.getJavaClass().getJavaFile(), jv);
        setRootDir(dir);
    }

    protected void preconditions() throws RefactoringException {
        if (impl.isLocallyUsedInMethod(jvar)) {
            throw new RefactoringException("variable " + jvar.getPrettyName()
              + " is used in method " + jvar.getJavaMethod().getName());
        }
    }

    protected void transform() throws RefactoringException {
        RefactoringVisitor transformer = new DeleteVariableVisitor(jvar);
        jfile.accept(transformer);

        PrintVisitor printer = new PrintVisitor();
        String newCode = printer.getCode(jfile);

        DisplayedFile file = new DisplayedFile(jfile.getName(), jfile.getText(), newCode);
        file.setOldHighlight(transformer.getHighlights());
        file.setNewHighlight(printer.getHighlights());
        changedFiles.add(file);
    }

    protected String getLog() {
        return "Delete Field: " + jclass.getName();
    }
}
