/*
 *     PushDownMethod.java  Dec 14, 2001
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
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class PushDownMethod extends MethodRefactoring {
    private List dsts;  // JavaClass

    public PushDownMethod() {
        super();
    }

    public PushDownMethod(JFrame f, JavaMethod jm, String dir) {
        super(f, jm.getJavaClass().getJavaFile(), jm);
        setRootDir(dir);
    }

    protected void preconditions() throws RefactoringException {
        if (impl.isCalledInClass(jmethod, jclass)) {
            throw new RefactoringException("not move:"
              + " method " + jmethod.getName() + " is called by other methods in class " + jclass.getName());
        }

        if (impl.callsPrivateMethods(jmethod)) {
            throw new RefactoringException("not move:"
              + " method " + jmethod.getName() + " calls private methods in class " + jclass.getName());
        }                                                

        if (impl.refersToPrivateField(jmethod)) {
            throw new RefactoringException("not move:"
              + " method " + jmethod.getName() + " uses private fields in class " + jclass.getName());
        }

        dsts = impl.collectChildrenCallingMethod(jmethod);
        int answer;
        if (!dsts.isEmpty()) {
            answer = JOptionPane.showConfirmDialog(frame, "Move" + " method " + jmethod.getName()
              + " into class " + dsts + ": Continue?", "Confirm", JOptionPane.YES_NO_OPTION);
        } else {
            dsts = impl.collectChildren(jclass);
            answer = JOptionPane.showConfirmDialog(frame, "No subclass calls method " + jmethod.getName()
              + ": Continue to move it into all children " + dsts + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        }
        if (answer == JOptionPane.NO_OPTION) {
                throw new RefactoringException("");
        }

        Iterator iv = impl.collectUsedFieldsInClassOrAncestors(jmethod).iterator();
        Iterator im = impl.collectCalledMethodsInClassOrAncestors(jmethod).iterator();
        Iterator it = dsts.iterator();
        while (it.hasNext()) {
            JavaClass dst = (JavaClass)it.next();

            if (impl.existsSameMethodInClass(jmethod, dst)) {
                throw new RefactoringException("already exists:"
                  + " method " + jmethod.getName() + " in class " + dst.getName());
            }

            while (iv.hasNext()) {
                JavaVariable jv = (JavaVariable)iv.next();
                if (impl.existsSameNameFieldInClass(jv.getName(), dst)) {
                    throw new RefactoringException("already exists:"
                      + " field " + jv.getPrettyName() + " in class " + dst.getName());
                }
            }

            while (im.hasNext()) {
                JavaMethod jm = (JavaMethod)im.next();
                if (impl.existsSameMethodInClass(jm, dst)) {
                    throw new RefactoringException("already exists:"
                      + " method " + jm.getName() + " in class " + dst.getName());
                }
            }
        }
    }

    protected void transform() throws RefactoringException {
        RefactoringVisitor transformer = new PushDownMethodVisitor(jmethod);
        jfile.accept(transformer);
        String dstMethodDecl = transformer.getTempCode();

        PrintVisitor printer = new PrintVisitor();
        String newCode = printer.getCode(jfile);

        DisplayedFile file = new DisplayedFile(jfile.getName(), jfile.getText(), newCode);
        file.setOldHighlight(transformer.getHighlights());
        file.setNewHighlight(printer.getHighlights());
        changedFiles.add(file);

        Iterator it = dsts.iterator();
        while (it.hasNext()) {
            JavaClass dst = (JavaClass)it.next();

            transformer = new InsertMethodVisitor(dstMethodDecl, dst, jmethod);
            JavaFile dfile = dst.getJavaFile();
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
        return "Push Down Method: " + jmethod.getName() + " from " + jclass.getName() + " to " + dsts;
    }
}
