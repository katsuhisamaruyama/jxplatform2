/*
 *     DeleteField.java  Jan 17, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.fields;
import jp.ac.ritsumei.cs.fse.jrt.refactor.FieldRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.List;
import javax.swing.JFrame;

public class DeleteField extends FieldRefactoring {
    public DeleteField() {
        super();
    }

    public DeleteField(JFrame f, JavaVariable jv, String dir) {
        super(f, jv.getJavaClass().getJavaFile(), jv);
        setRootDir(dir);
    }

    protected void preconditions() throws RefactoringException {
        if (impl.isUsedInClass(jvar, jvar.getJavaClass())) {
            throw new RefactoringException("field " + jvar.getPrettyName()
              + " is used in class " + jvar.getJavaClass().getName());
        }

        if (!jvar.isPrivate()) {
            List files = impl.collectFilesUsingField(jvar);
            if (files.size() != 0) {
                throw new RefactoringException("field " + jvar.getPrettyName() + " is used in the package");
            }
        }
    }

    protected void transform() throws RefactoringException {
        RefactoringVisitor transformer = new DeleteFieldVisitor(jvar);
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
