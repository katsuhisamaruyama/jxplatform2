/*
 *     PullUpMethod.java  Dec 21, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.methods;
import jp.ac.ritsumei.cs.fse.jrt.refactor.MethodRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.refactor.dialog.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.List;
import java.util.Iterator;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class PullUpMethod extends MethodRefactoring {
    private String dstName = null;
    private JavaFile dfile;
    private JavaClass dst;
    private List callings;
    private boolean isCalled;

    public PullUpMethod() {
        super();
    }

    public PullUpMethod(JFrame f, JavaMethod jm, String dir) {
        super(f, jm.getJavaClass().getJavaFile(), jm);
        setRootDir(dir);
    }

    protected void preconditions() throws RefactoringException {
        dstName = jclass.getSuperClassName();

        if (impl.usesFieldsInClass(jmethod, jclass)) {
            throw new RefactoringException("not move:"
              + " method " + jmethod.getName() + " uses fields in" + " class " + jclass.getName());
        }

        String dstClassName = impl.getClassName(dstName);
        dfile = impl.getJavaFile(dstName + ".java", jfile);
        dst = dfile.getJavaClass(dstClassName);
        if (dst == null) {
            throw new RefactoringException("not found:" + " class " + dstClassName);
        }

        if (impl.existsSameMethodInClass(jmethod, dst)) {
            throw new RefactoringException("already exists:"
              + " method " + jmethod.getName() + " in class " + dst.getName());
        }

        callings = impl.collectCalledMethodInClass(jmethod, jclass);
        Iterator it = callings.iterator();
        while (it.hasNext()) {
            JavaMethod jm = (JavaMethod)it.next();
            if (impl.existsSameMethodInClass(jm, dst)) {
                throw new RefactoringException("cannot create"
                 + " abstract method " + jm.getName() + " called by " + jmethod.getName());
            }
        }

        int answer = JOptionPane.showConfirmDialog(frame, "Move " + "method " + jmethod.getName()
          + " into class " + dst.getName() + ": Continue?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.NO_OPTION) {
            throw new RefactoringException("");
        }

        isCalled = impl.isCalledInClass(jmethod, jclass);
    }

    protected void transform() throws RefactoringException {
        RefactoringVisitor transformer = new PullUpMethodVisitor(jmethod, callings, isCalled);
        jfile.accept(transformer);
        String dstMethodDecl = transformer.getTempCode();

        PrintVisitor printer = new PrintVisitor();
        String newCode = printer.getCode(jfile);

        DisplayedFile file = new DisplayedFile(jfile.getName(), jfile.getText(), newCode);
        file.setOldHighlight(transformer.getHighlights());
        file.setNewHighlight(printer.getHighlights());
        changedFiles.add(file);

        transformer = new InsertMethodVisitor(dstMethodDecl, dst, jmethod);
        dfile.accept(transformer);

        printer = new PrintVisitor();
        String dstCode = printer.getCode(dfile);

        file = getChangedFile(dfile.getName());
        if (file == null) {
            file = new DisplayedFile(dfile.getName(), dfile.getText(), dstCode);
            file.setOldHighlight(transformer.getHighlights());
            file.setNewHighlight(printer.getHighlights());
            changedFiles.add(file);
        } else {
            file.setNewText(dstCode);
            file.addNewHighlight(printer.getHighlights());
        }
    }

    protected void additionalTransformation() throws RefactoringException {
        if (jmethod.isPrivate()) {
            return;
        }

        List files = impl.collectFilesCallingMethod(jmethod);
        files.remove(jfile);
        if (files.size() != 0) {
            showFilesCallingMethod(jmethod, files);
        }
    }

    protected String getLog() {
        return "Pull Up Method: " + jmethod.getName() + " from " + jclass.getName() + " to " + dst.getName();
    }
}
