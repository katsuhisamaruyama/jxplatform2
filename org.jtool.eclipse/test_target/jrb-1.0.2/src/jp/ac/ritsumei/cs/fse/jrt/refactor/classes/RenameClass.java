/*
 *     RenameClass.java  Dec 1, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.classes;
import jp.ac.ritsumei.cs.fse.jrt.refactor.ClassRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.refactor.dialog.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import javax.swing.JFrame;
import java.util.List;
import java.util.Iterator;

public class RenameClass extends ClassRefactoring {
    private String newName = null;

    public RenameClass() {
        super();
    }

    public RenameClass(JFrame f, JavaClass jc, String dir, String name) {
        super(f, jc.getJavaFile(), jc);
        setRootDir(dir);
        newName = name;
    }

    protected void preconditions() throws RefactoringException {
        if (newName == null) {
            newName = RenameDialog.show(frame, "Rename Class", jclass.getName());
        }
        if (newName == null) {
            throw new RefactoringException("");
        }

        if (impl.existsSameNameClassInFile(newName, jfile)) {
            throw new RefactoringException("already exists:"
              + " class/interface " + newName + " in file " + jfile.getName());
        }

        if (impl.isUsedInFile(newName, jfile)) {
            throw new RefactoringException("already used:"
              + " type " + newName + " in file " + jfile.getName());
        }

        String fname = impl.findSameNameFileInPackage(newName, jfile);
        if (fname != null) {
            throw new RefactoringException("already exists:"
              + " class/interface " + newName + " in file " + fname);
        }
    }

    protected void transform() throws RefactoringException {
        RefactoringVisitor transformer = new RenameClassVisitor(jclass, newName);
        jfile.accept(transformer);

        PrintVisitor printer = new PrintVisitor();
        String newCode = printer.getCode(jfile);

        DisplayedFile file = new DisplayedFile(jfile.getName(), jfile.getText(), newCode);
        file.setOldHighlight(transformer.getHighlights());
        file.setNewHighlight(printer.getHighlights());
        changedFiles.add(file);
    }

    protected void additionalTransformation() throws RefactoringException {
        List files = impl.collectFilesUsingClass(jclass);
        files.remove(jfile);
        if (files.size() == 0) {
            return;
        }

        String mesg = "The following files use class " + jclass.getName() + ". "
                    + "Select some to be changed among the files.";
        List filesToBeChanged = FileListDialog.show(frame, mesg, files);

        Iterator it = filesToBeChanged.iterator();
        while (it.hasNext()) {
            JavaFile dfile = (JavaFile)it.next();

            RefactoringVisitor transformer = new RenameClassVisitor(jclass, newName, dfile);
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
        return "Rename Class: " + "from " + jclass.getName() + " to " + newName;
    }
}
