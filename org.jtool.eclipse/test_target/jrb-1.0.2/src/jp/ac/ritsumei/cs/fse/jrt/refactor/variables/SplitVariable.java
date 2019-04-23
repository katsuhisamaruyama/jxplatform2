/*
 *     SplitVariable.java  Apr 10, 2003
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.variables;
import jp.ac.ritsumei.cs.fse.jrt.refactor.VariableRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.refactor.dialog.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.List;
import javax.swing.JFrame;

public class SplitVariable extends VariableRefactoring {
    private String newName;

    public SplitVariable() {
        super();
    }

    public SplitVariable(JFrame f, JavaVariable jv, String dir, String name) {
        super(f, jv.getJavaClass().getJavaFile(), jv);
        setRootDir(dir);
        newName = name;
    }

    protected void preconditions() throws RefactoringException {
        if (newName == null) {
            newName = RenameDialog.show(frame, "Split Variable", jvar.getName());
        }
        if (newName == null) {
            throw new RefactoringException("");
        }

        JavaMethod jmethod = jvar.getJavaMethod();
        if (impl.existsSameNameVariableInMethod(newName, jmethod)) {
            throw new RefactoringException("already exists:"
              + " variable " + newName + " in method " + jmethod.getName());
        }

        JavaClass jclass = jvar.getJavaClass();
        if (impl.existsSameNameFieldInClass(newName, jclass)) {
            throw new RefactoringException("already exists:"
              + " field " + newName + " in class " + jclass.getName());
        }

        impl.makePDG(jvar);
    }

    protected void transform() throws RefactoringException {
        RefactoringVisitor transformer = new SplitVariableVisitor(jvar, newName);
        jfile.accept(transformer);

        PrintVisitor printer = new PrintVisitor();
        String newCode = printer.getCode(jfile);

        DisplayedFile file = new DisplayedFile(jfile.getName(), jfile.getText(), newCode);
        file.setOldHighlight(transformer.getHighlights());
        file.setNewHighlight(printer.getHighlights());
        changedFiles.add(file);
    }

    protected String getLog() {
        return "Split Local Variable: " + jvar.getName() + " and " + newName;
    }
}
