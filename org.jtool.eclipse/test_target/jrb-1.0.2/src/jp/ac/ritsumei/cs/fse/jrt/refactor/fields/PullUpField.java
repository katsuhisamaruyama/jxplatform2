/*
 *     PullUpField.java  Dec 18, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.fields;
import jp.ac.ritsumei.cs.fse.jrt.refactor.FieldRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.refactor.dialog.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class PullUpField extends FieldRefactoring {
    private String dstName = null;
    private JavaFile dfile;
    private JavaClass dst;

    public PullUpField() {
        super();
    }

    public PullUpField(JFrame f, JavaVariable jv, String dir) {
        super(f, jv.getJavaClass().getJavaFile(), jv);
        setRootDir(dir);
    }
    
    protected void preconditions() throws RefactoringException {
        if (impl.usesOtherFieldsInClassAtDeclaration(jvar)) {
            throw new RefactoringException("not move:"
              + " field " + jvar.getPrettyName() + " uses other fields of its class");
        }

        dstName = jclass.getShortSuperClassName();
        String dstClassName = impl.getClassName(dstName);
        dfile = impl.getJavaFile(dstName, jfile);
        dst = dfile.getJavaClass(dstClassName);
        if (dst == null) {
            throw new RefactoringException("not found:" + " class " + dstClassName);
        }

        if (impl.existsSameFieldInClass(jvar, dst)) {
            throw new RefactoringException("already exists:"
              + " field " + jvar.getPrettyName() + " in class " + dst.getName());
        }

        int answer = JOptionPane.showConfirmDialog(frame, "Move " + "field " + jvar.getPrettyName()
          + " into class " + dst.getName() + ": Continue?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.NO_OPTION) {
            throw new RefactoringException("");
        }
    }

    protected void transform() throws RefactoringException {
        RefactoringVisitor transformer = new PullUpFieldVisitor(jvar);
        jfile.accept(transformer);
        String dstFieldDecl = transformer.getTempCode();

        PrintVisitor printer = new PrintVisitor();
        String newCode = printer.getCode(jfile);

        DisplayedFile file = new DisplayedFile(jfile.getName(), jfile.getText(), newCode);
        file.setOldHighlight(transformer.getHighlights());
        file.setNewHighlight(printer.getHighlights());
        changedFiles.add(file);

        transformer = new InsertFieldVisitor(dstFieldDecl, dst, jvar);
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
        if (jvar.isPrivate()) {
            return;
        }

        List files = impl.collectFilesUsingField(jvar);
        files.remove(jfile);
        if (files.size() != 0) {
            showFilesUsingField(jvar, jvar.getPrettyName(), files);
        }
    }

    protected String getLog() {
        return "Pull Up Field: " + jvar.getPrettyName()
          + " from " + jclass.getName() + " to " + dst.getName();
    }
}
