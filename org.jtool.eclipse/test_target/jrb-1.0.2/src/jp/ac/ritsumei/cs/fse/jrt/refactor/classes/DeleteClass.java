/*
 *     DeleteClass.java  Jan 17, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.classes;
import jp.ac.ritsumei.cs.fse.jrt.refactor.ClassRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.List;
import javax.swing.JFrame;

public class DeleteClass extends ClassRefactoring {
    public DeleteClass() {
        super();
    }

    public DeleteClass(JFrame f, JavaClass jc, String dir) {
        super(f, jc.getJavaFile(), jc);
        setRootDir(dir);
    }

    protected void preconditions() throws RefactoringException {
        List files = impl.collectFilesUsingClass(jclass);
        if (files.size() != 0) {
            throw new RefactoringException("class " + jclass.getName() + " is used in the package");
        }
    }

    protected void transform() throws RefactoringException {
        RefactoringVisitor transformer = new DeleteClassVisitor(jclass);
        jfile.accept(transformer);

        PrintVisitor printer = new PrintVisitor();
        String newCode = printer.getCode(jfile);

        DisplayedFile file = new DisplayedFile(jfile.getName(), jfile.getText(), newCode);
        file.setOldHighlight(transformer.getHighlights());
        file.setNewHighlight(printer.getHighlights());
        changedFiles.add(file);
    }

    protected String getLog() {
        return "Delete Class: " + jclass.getName();
    }
}
