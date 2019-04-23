/*
 *     EncapsulateField.java  Dec 20, 2001
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

public class EncapsulateField extends FieldRefactoring {
    private JavaMethod getter = null;
    private JavaMethod setter = null;
    private JavaMethod jg;
    private JavaMethod js;

    public EncapsulateField() {
        super();
    }

    public EncapsulateField(JFrame f, JavaVariable jv, String dir, String name) {
        super(f, jv.getJavaClass().getJavaFile(), jv);
        setRootDir(dir);
        if (name != null) {
            getter = impl.getGetter("get" + name, jv);
            setter = impl.getSetter("set" + name, jv);
        }
    }

    protected void preconditions() throws RefactoringException {
        if (jvar.isPrivate()) {
            throw new RefactoringException("not change:" + " field "
              + jvar.getPrettyName() + " is private");
        }

        if (getter == null || setter == null) {
            AccessorDialog dialog = AccessorDialog.show(frame,
              "Create/Specify setter and getter", jvar.getPrettyName());
            String gname = dialog.getGetterName();
            String sname = dialog.getSetterName();
            getter = impl.getGetter(gname, jvar);
            setter = impl.getSetter(sname, jvar);
        }
        if (getter == null || setter == null) {
            throw new RefactoringException("");
        }

        if (impl.existsSameMethodInClass(getter, jclass)) {
            jg = null;
        } else {
            jg = getter;
        }
        if (impl.existsSameMethodInClass(setter, jclass)) {
            js = null;
        } else {
            js = setter;
        }
    }

    protected void transform() throws RefactoringException {
        RefactoringVisitor transformer = new EncapsulateFieldVisitor(jvar, jg, js);
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

        String mesg = "The following files directly use field " + jvar.getName() + ". "
                    + "Select some to be changed among the files.";
        List filesToBeChanged = FileListDialog.show(frame, mesg, files);

        Iterator it = filesToBeChanged.iterator();
        while (it.hasNext()) {
            JavaFile dfile = (JavaFile)it.next();

            RefactoringVisitor transformer = new SelfEncapsulateFieldVisitor(jvar, getter, setter, dfile);
            dfile.accept(transformer);

            PrintVisitor printer = new PrintVisitor();
            String dstCode = printer.getCode(dfile);

            DisplayedFile file = getChangedFile(dfile.getName());
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

    protected String getLog() {
        return "Encapsulate Field: " + jvar.getPrettyName() + " through accessors "
          + getter.getName() + " and " + setter.getName();
    }
}
