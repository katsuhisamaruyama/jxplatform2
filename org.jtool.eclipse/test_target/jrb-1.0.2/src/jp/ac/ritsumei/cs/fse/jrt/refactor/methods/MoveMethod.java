/*
 *     MoveMethod.java  Dec 15, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.methods;
import jp.ac.ritsumei.cs.fse.jrt.refactor.MethodRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.refactor.dialog.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class MoveMethod extends MethodRefactoring {
    private String dstName = null;
    private JavaFile dfile;
    private JavaClass dst;
    private String refName = null;

    public MoveMethod() {
        super();
    }

    public MoveMethod(JFrame f, JavaMethod jm, String dir, String name) {
        super(f, jm.getJavaClass().getJavaFile(), jm);
        setRootDir(dir);
        dstName = name;
    }

    protected void preconditions() throws RefactoringException {
        List superList = impl.collectSameMethodInSuperclasses(jmethod);
        List subList = impl.collectSameMethodInSubclasses(jmethod);

        if (!superList.isEmpty() || !subList.isEmpty()) {
            int answer = JOptionPane.showConfirmDialog(frame,
              "Exists the same method declaration in superclasses " + superList
              + " and/or subclasses " + subList + ": Continue?",
              "Confirm", JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.NO_OPTION) {
                throw new RefactoringException("");
            }
        }

        if (impl.callsMethodsInClassOrAncestors(jmethod)) {
            throw new RefactoringException("not move:" + " method " + jmethod.getName()
              + " calls other methods in class/superclass of " + jclass.getName());
        }

        if (dstName == null) {
            dstName = MoveClassBodyDialog.show(frame, "Move Method: " + jmethod.getName(),
              rootDir, jclass.getName());
        }
        if (dstName == null) {
            throw new RefactoringException("");
        }

        String dstClassName = impl.getClassName(dstName);
        dfile = impl.getJavaFile(dstName, jfile);
        dst = dfile.getJavaClass(dstClassName);
        if (dst == null) {
            throw new RefactoringException("not found:" + " class " + dstClassName);
        }
    
        if (impl.existsSameMethodInClass(jmethod, dst)) {
            throw new RefactoringException("already exists:"
              + " method " + jmethod.getName() + " in class " + dst.getName());
        }

        JavaVariableList refList = impl.collectVariablesReferringToClass(jmethod, dst);
        if (impl.isAllSame(refList)) {
            refName = refList.getFirst().getName();
        }

        String useName = null;
        JavaVariableList useList = impl.collectUsedFieldsInClassOrAncestors(jmethod);
        if (impl.isAllSame(useList)) {
            useName = useList.getFirst().getName();
        }

        if (useName == null && !useList.isEmpty() || useName != null && useName != refName) {
            throw new RefactoringException("not move:"
              + " method " + jmethod.getName() + " uses fields except the object referring to class "
              + jclass.getName());
        }

        if (refName == null && impl.isCalledInClass(jmethod, jclass)) {
            throw new RefactoringException("not move:"
              + " method " + jmethod.getName() + " has no/multiple objects referring to class " + dst.getName());
        }
    }

    protected void transform() throws RefactoringException {
        RefactoringVisitor transformer = new MoveMethodVisitor(jmethod, refName);
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
        return "Move Method: " + jmethod.getName() + " from " + jclass.getName() + " to " + dst.getName();
    }
}
