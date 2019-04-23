/*
 *     DeleteMethod.java  Jan 17, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.methods;
import jp.ac.ritsumei.cs.fse.jrt.refactor.MethodRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.List;
import javax.swing.JFrame;

public class DeleteMethod extends MethodRefactoring {
    public DeleteMethod() {
        super();
    }

    public DeleteMethod(JFrame f, JavaMethod jm, String dir) {
        super(f, jm.getJavaClass().getJavaFile(), jm);
        setRootDir(dir);
    }

    protected void preconditions() throws RefactoringException {
        if (impl.isCalledInClass(jmethod, jmethod.getJavaClass())) {
            throw new RefactoringException("method " + jmethod.getName()
              + " is called in class " + jmethod.getJavaClass().getName());
        }

        if (!jmethod.isPrivate()) {
            List files = impl.collectFilesCallingMethod(jmethod);
            if (files.size() != 0) {
                throw new RefactoringException("method " + jmethod.getName() + " is used in the package");
            }
        }
    }

    protected void transform() throws RefactoringException {
        RefactoringVisitor transformer = new DeleteMethodVisitor(jmethod);
        jfile.accept(transformer);

        PrintVisitor printer = new PrintVisitor();
        String newCode = printer.getCode(jfile);

        DisplayedFile file = new DisplayedFile(jfile.getName(), jfile.getText(), newCode);
        file.setOldHighlight(transformer.getHighlights());
        file.setNewHighlight(printer.getHighlights());
        changedFiles.add(file);
    }

    protected String getLog() {
        return "Delete Method: " + jclass.getName();
    }
}
