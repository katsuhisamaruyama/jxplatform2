/*
 *     RenameMethod.java  Dec 12, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.methods;
import jp.ac.ritsumei.cs.fse.jrt.refactor.MethodRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.refactor.dialog.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.Summary;

public class RenameMethod extends MethodRefactoring {
    private String newName = null;
    private JavaMethod nmethod;

    public RenameMethod() {
        super();
    }

    public RenameMethod(JFrame f, JavaMethod jm, String dir, String name) {
        super(f, jm.getJavaClass().getJavaFile(), jm);
        setRootDir(dir);
        newName = name;
    }

    protected void preconditions() throws RefactoringException {
        if (newName == null) {
            newName = RenameDialog.show(frame, "Rename Method", jmethod.getName());
        }
        if (newName == null) {
            throw new RefactoringException("");
        }

        nmethod = new JavaMethod(jmethod);
        nmethod.setName(newName);
        if (impl.existsSameMethodInClass(nmethod, jclass)) {
            throw new RefactoringException("already exists:"
              + " method " + newName + " in class " + jclass.getName());
        }
    }

    protected void transform() throws RefactoringException {
        RefactoringVisitor transformer = new RenameMethodVisitor(jmethod, newName);
        jfile.accept(transformer);

        PrintVisitor printer = new PrintVisitor();
        String newCode = printer.getCode(jfile);

        DisplayedFile file = new DisplayedFile(jfile.getName(), jfile.getText(), newCode);
        file.setOldHighlight(transformer.getHighlights());
        file.setNewHighlight(printer.getHighlights());
        changedFiles.add(file);
    }

    protected void additionalTransformation() throws RefactoringException {
        if (jmethod.isPrivate()) {
            return;
        }

        List files = impl.collectFilesCallingMethod(jmethod);
        files.remove(jfile);
        if (files.size() == 0) {
            return;
        }

        String mesg = "The following files contain the method invocation to method " + jmethod.getName()
                    + " of class " + jclass.getName() + ". "
                    + "Select some to be changed among the files.";
        List filesToBeChanged = FileListDialog.show(frame, mesg, files);

        Iterator it = filesToBeChanged.iterator();
        while (it.hasNext()) {
            JavaFile dfile = (JavaFile)it.next();

            RefactoringVisitor transformer = new RenameMethodVisitor(jmethod, newName, dfile);
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
        return "Rename Method: " + "from " + jmethod.getName() + " to " + newName;
    }
}
