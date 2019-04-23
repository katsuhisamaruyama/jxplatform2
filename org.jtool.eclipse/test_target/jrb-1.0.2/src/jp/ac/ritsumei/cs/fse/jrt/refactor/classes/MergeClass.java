/*
 *     MergeClass.java  Dec 19, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.classes;
import jp.ac.ritsumei.cs.fse.jrt.refactor.ClassRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.refactor.dialog.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.ArrayList;
import java.io.File;
import javax.swing.JFrame;

public class MergeClass extends ClassRefactoring {
    private String dstName = null;
    private JavaFile dfile;
    private JavaClass dst;

    public MergeClass() {
        super();
    }

    public MergeClass(JFrame f, JavaClass jc, String dir, String name) {
        super(f, jc.getJavaFile(), jc);
        setRootDir(dir);
        dstName = name;
    }

    protected void preconditions() throws RefactoringException {
        if (dstName == null) {
            dstName = MergeClassDialog.show(frame, "Merge Class: " + jclass.getName(), rootDir);
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

        if (impl.existsSameMethodBetweenClasses(jclass, dst)) {
            throw new RefactoringException("method conflict exists:"
              + " classes " + jclass.getName() + " and " + dst.getName());
        }

        if (impl.existsSameFieldBetweenClasses(jclass, dst)) {
            throw new RefactoringException("field conflict exists:"
              + " classes " + jclass.getName() + " and " + dst.getName());
        }
    }

    protected void transform() throws RefactoringException {
        RefactoringVisitor transformer = new MergeClassVisitor(jclass, dst);
        jfile.accept(transformer);
        String dstClassDecl = transformer.getTempCode();

        PrintVisitor printer = new PrintVisitor();
        String newCode = printer.getCode(jfile);

        DisplayedFile file = new DisplayedFile(jfile.getName(), jfile.getText(), newCode);
        file.setOldHighlight(transformer.getHighlights());
        file.setNewHighlight(printer.getHighlights());
        changedFiles.add(file);
        
        transformer = new InsertClassBodyVisitor(dstClassDecl, dst, jclass);
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
        // showCallingMethods(jm);
        // showUsingFields(jv);
    }

    protected String getLog() {
        return "Merge Class: " + jclass.getName() + " into " + dst.getName();
    }
}
