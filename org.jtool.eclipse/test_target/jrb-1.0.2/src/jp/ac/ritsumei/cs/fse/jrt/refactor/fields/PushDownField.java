/*
 *     PushDownField.java  Dec 21, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.fields;
import jp.ac.ritsumei.cs.fse.jrt.refactor.FieldRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.refactor.dialog.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class PushDownField extends FieldRefactoring {
    private List dsts = new ArrayList();  // JavaClass

    public PushDownField() {
        super();
    }

    public PushDownField(JFrame f, JavaVariable jv, String dir) {
        super(f, jv.getJavaClass().getJavaFile(), jv);
        setRootDir(dir);
    }
    
    protected void preconditions() throws RefactoringException {
        if (impl.isDirectlyUsedInClass(jvar, jclass)) {
            throw new RefactoringException("not move:"
              + " field " + jvar.getPrettyName() + " is directly accessed in" + " class " + jclass.getName());
        }

        if (impl.refersToPrivateField(jvar)) {
            throw new RefactoringException("not move:"
              + " field " + jvar.getPrettyName() + " uses private fields in class " + jclass.getName());
        }

        dsts = impl.collectChildrenUsingField(jvar);
        int answer;
        if (!dsts.isEmpty()) {
            answer = JOptionPane.showConfirmDialog(frame, "Move" + " field " + jvar.getPrettyName()
              + " into class " + dsts + ": Continue?", "Confirm", JOptionPane.YES_NO_OPTION);
        } else {
            dsts = impl.collectChildren(jclass);
            answer = JOptionPane.showConfirmDialog(frame, "No class uses field " + jvar.getPrettyName()
              + ": Continue to move it into all children " + dsts + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        }
        if (answer == JOptionPane.NO_OPTION) {
            throw new RefactoringException("");
        }

        Iterator iv = impl.collectUsedFieldsInClassOrAncestors(jvar).iterator();

        Iterator it = dsts.iterator();
        while (it.hasNext()) {
            JavaClass dst = (JavaClass)it.next();

            if (impl.existsSameFieldInClass(jvar, dst)) {
                throw new RefactoringException("already exists:"
                  + " field " + jvar.getPrettyName() + " in class " + dst.getName());
            }
            
            while (iv.hasNext()) {
                JavaVariable jv = (JavaVariable)iv.next();
                if (impl.existsSameNameFieldInClass(jv.getName(), dst)) {
                    throw new RefactoringException("already exists:"
                      + " field " + jv.getPrettyName() + " in class " + dst.getName());
                }
            }
        }
    }

    protected void transform() throws RefactoringException {
        RefactoringVisitor transformer = new PushDownFieldVisitor(jvar);
        jfile.accept(transformer);
        String dstFieldDecl = transformer.getTempCode();

        PrintVisitor printer = new PrintVisitor();
        String newCode = printer.getCode(jfile);

        DisplayedFile file = new DisplayedFile(jfile.getName(), jfile.getText(), newCode);
        file.setOldHighlight(transformer.getHighlights());
        file.setNewHighlight(printer.getHighlights());
        changedFiles.add(file);

        Iterator it = dsts.iterator();
        while (it.hasNext()) {
            JavaClass dst = (JavaClass)it.next();

            transformer = new InsertFieldVisitor(dstFieldDecl, dst, jvar);
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
        return "Push Down Field: " + jvar.getPrettyName()
          + " from " + jclass.getName() + " to " + dsts;
    }
}
