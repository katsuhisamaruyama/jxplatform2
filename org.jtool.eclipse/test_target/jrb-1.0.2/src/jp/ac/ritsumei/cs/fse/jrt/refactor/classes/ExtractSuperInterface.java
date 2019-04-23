/*
 *     ExtractSuperInterface.java  Dec 19, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.classes;
import jp.ac.ritsumei.cs.fse.jrt.refactor.ClassRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.refactor.dialog.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import javax.swing.JFrame;

public class ExtractSuperInterface extends ExtractClass {

    public ExtractSuperInterface() {
        super();
    }

    public ExtractSuperInterface(JFrame f, JavaClass jc, String dir, String name) {
        super(f, jc.getJavaFile(), jc);
        setRootDir(dir);
        newName = name;
    }

    protected void preconditions() throws RefactoringException {
        if (newName == null) {
            newName = NewNameDialog.show(frame, "Extract Super Interface");
        }

        super.preconditions();
    }

    protected void transform() throws RefactoringException {
        RefactoringVisitor transformer = new ExtractSuperInterfaceVisitor(jclass, newName);
        jfile.accept(transformer);

        PrintVisitor printer = new PrintVisitor();
        String newCode = printer.getCode(jfile);

        DisplayedFile file = new DisplayedFile(jfile.getName(), jfile.getText(), newCode);
        file.setOldHighlight(transformer.getHighlights());
        file.setNewHighlight(printer.getHighlights());
        changedFiles.add(file);
    }

    protected String getLog() {
        return "Extract superclass: " + newName + " from " + jclass.getName();
    }
}
