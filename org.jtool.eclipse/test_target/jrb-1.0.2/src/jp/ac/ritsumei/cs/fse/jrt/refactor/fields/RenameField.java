/*
 *     RenameField.java  Dec 20, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.fields;
import jp.ac.ritsumei.cs.fse.jrt.refactor.FieldRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.refactor.dialog.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JFrame;

public class RenameField extends FieldRefactoring {
    private String newName = null;

    public RenameField() {
        super();
    }

    public RenameField(JFrame f, JavaVariable jv, String dir, String name) {
        super(f, jv.getJavaClass().getJavaFile(), jv);
        setRootDir(dir);
        newName = name;
    }

    protected void preconditions() throws RefactoringException {
        String cname = jvar.getName().substring(0, jvar.getName().lastIndexOf("."));

        if (newName == null) {
            newName = RenameDialog.show(frame, "Rename Field", jvar.getPrettyName());
        }
        if (newName == null) {
            throw new RefactoringException("");
        }

        if (impl.existsSameNameFieldInClass(newName, jclass)) {
            throw new RefactoringException("already exists:"
              + " field " + newName + " in class " + jclass.getName());
        }
    }

    protected void transform() throws RefactoringException {
        RefactoringVisitor transformer = new RenameFieldVisitor(jvar, newName);
        jfile.accept(transformer);

        PrintVisitor printer = new PrintVisitor();
        String newCode = printer.getCode(jfile);

        DisplayedFile file = new DisplayedFile(jfile.getName(), jfile.getText(), newCode);
        file.setOldHighlight(transformer.getHighlights());
        file.setNewHighlight(printer.getHighlights());
        changedFiles.add(file);
    }

    protected void additionalTransformation() throws RefactoringException {
        if (jvar.isPrivate()) {
            return;
        }

        List files = impl.collectFilesUsingField(jvar);
        files.remove(jfile);
        if (files.size() == 0) {
            return;
        }

        String mesg = "The following files contain the access to field " + jvar.getPrettyName()
                    + " of class " + jclass.getName() + ". "
                    + "Select some to be changed among the files.";
        List filesToBeChanged = FileListDialog.show(frame, mesg, files);

        Iterator it = filesToBeChanged.iterator();
        while (it.hasNext()) {
            JavaFile dfile = (JavaFile)it.next();

            RefactoringVisitor transformer = new RenameFieldVisitor(jvar, newName, dfile);
            dfile.accept(transformer);

            PrintVisitor printer = new PrintVisitor();
            String dstCode = printer.getCode(dfile);

            DisplayedFile file = new DisplayedFile(dfile.getName(), dfile.getText(), dstCode);
            file.setOldHighlight(transformer.getHighlights());
            file.setNewHighlight(printer.getHighlights());
            changedFiles.add(file);
        }
    }

    protected String getLog() {
        return "Rename Field: " + "from " + jvar.getPrettyName() + " to " + newName;
    }
}
