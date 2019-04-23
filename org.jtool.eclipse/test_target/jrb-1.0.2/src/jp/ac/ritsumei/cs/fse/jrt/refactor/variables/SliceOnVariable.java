/*
 *     SliceOnVariable.java  Jan 8, 2002
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.variables;
import jp.ac.ritsumei.cs.fse.jrt.refactor.VariableRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.refactor.dialog.*;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import javax.swing.JOptionPane;
import javax.swing.JFrame;

public class SliceOnVariable extends VariableRefactoring {

    public SliceOnVariable() {
        super();
    }

    public SliceOnVariable(JFrame f, JavaVariable jv, String dir) {
        super(f, jv.getJavaClass().getJavaFile(), jv);
        setRootDir(dir);
    }

    protected void preconditions() throws RefactoringException {
        int answer = JOptionPane.showConfirmDialog(frame,
          "Slice on variable refactoring is on trial, so it generates code as a comment",
          "Confirm", JOptionPane.OK_CANCEL_OPTION);
        if (answer == JOptionPane.CANCEL_OPTION) {
            throw new RefactoringException("");
        }

        impl.makePDG(jvar);
    }

    protected void transform() throws RefactoringException {
        RefactoringVisitor transformer = new SliceOnVariableVisitor(jvar);
        jfile.accept(transformer);

        PrintVisitor printer = new PrintVisitor();
        String newCode = printer.getCode(jfile);

        DisplayedFile file = new DisplayedFile(jfile.getName(), jfile.getText(), newCode);
        file.setOldHighlight(transformer.getHighlights());
        file.setNewHighlight(printer.getHighlights());
        changedFiles.add(file);
    }

    protected String getLog() {
        return "Slice on variable: " + jvar.getName();
    }
}
