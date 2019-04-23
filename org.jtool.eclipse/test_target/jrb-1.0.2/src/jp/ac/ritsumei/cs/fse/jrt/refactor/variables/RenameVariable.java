/*
 *     RenameVariable.java  Dec 25, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.variables;
import jp.ac.ritsumei.cs.fse.jrt.refactor.VariableRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.refactor.dialog.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import javax.swing.JFrame;

public class RenameVariable extends VariableRefactoring {
    private String newName;

    public RenameVariable() {
        super();
    }

    public RenameVariable(JFrame f, JavaVariable jv, String dir, String name) {
        super(f, jv.getJavaClass().getJavaFile(), jv);
        setRootDir(dir);
        newName = name;
    }

    protected void preconditions() throws RefactoringException {
        if (newName == null) {
            newName = RenameDialog.show(frame, "Rename Variable", jvar.getName());
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
    }

    protected void transform() throws RefactoringException {
        RefactoringVisitor transformer = new RenameVariableVisitor(jvar, newName);
        jfile.accept(transformer);

        PrintVisitor printer = new PrintVisitor();
        String newCode = printer.getCode(jfile);

        DisplayedFile file = new DisplayedFile(jfile.getName(), jfile.getText(), newCode);
        file.setOldHighlight(transformer.getHighlights());
        file.setNewHighlight(printer.getHighlights());
        changedFiles.add(file);
    }

    protected String getLog() {
        return "Rename Local Variable: " + "from " + jvar.getName() + " to " + newName;
    }
}
