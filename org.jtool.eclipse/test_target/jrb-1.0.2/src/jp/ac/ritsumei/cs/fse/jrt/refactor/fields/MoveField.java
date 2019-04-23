/*
 *     MoveField.java  Dec 20, 2001
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

public class MoveField extends FieldRefactoring {
    private String dstName = null;
    private JavaFile dfile;
    private JavaClass dst;
    private JavaMethod getter = null;
    private JavaMethod setter = null;
    private String refName;

    public MoveField() {
        super();
    }

    public MoveField(JFrame f, JavaVariable jv, String dir, String name) {
        super(f, jv.getJavaClass().getJavaFile(), jv);
        setRootDir(dir);
        dstName = name;
    }

    protected void preconditions() throws RefactoringException {
        if (impl.usesOtherFieldsInAncestorsAtDeclaration(jvar)) {
            throw new RefactoringException("not move:"
              + " field " + jvar.getPrettyName() + " uses other fields of its ancestors");
        }

        if (!jvar.isPrivate()) {
            throw new RefactoringException("not move:"
              + " field " + jvar.getPrettyName() + " is not private");
        }

        if (dstName == null) {
            dstName = MoveClassBodyDialog.show(frame,
              "Move Field: " + jvar.getPrettyName(), rootDir, jclass.getName());
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

        if (impl.existsSameFieldInClass(jvar, dst)) {
            throw new RefactoringException("already exists:"
              + " field " + jvar.getPrettyName() + " in class " + dst.getName());
        }

        if (getter == null || setter == null) {
            AccessorDialog dialog = AccessorDialog.show(frame,
              "Sepcify setter and getter", jvar.getPrettyName());
            String gname = dialog.getGetterName();
            String sname = dialog.getSetterName();
            getter = impl.getGetter(gname, jvar);
            setter = impl.getSetter(sname, jvar);
        }
        if (getter == null || setter == null) {
            throw new RefactoringException("");
        }

        if (!impl.existsSameMethodInClass(getter, jclass)
            || !impl.existsSameMethodInClass(setter, jclass)) {
            throw new RefactoringException("not found:"  + " accessor(s) "
              + getter.getName() + " and/or " + setter.getName() + " in class " + jclass.getName());
        }

        if (impl.existsSameMethodInClass(getter, dst)
          || impl.existsSameMethodInClass(setter, dst)) {
            throw new RefactoringException("already exist(s):"  + " accessor(s) "
              + getter.getName() + " and/or " + setter.getName() + " in class " + dst.getName());
        }

        if (impl.isDirectlyUsedInClass(jvar, jclass, getter, setter)) {
            throw new RefactoringException("not move:" + " field " + jvar.getPrettyName()
              + " is directly accessed in" + " class " + jclass.getName());
        }

        refName = ReferenceDialog.show(frame, "Reference name");
        if (impl.existsSameNameFieldInClass(refName, jclass)) {
            throw new RefactoringException("already exists:"
              + " field " + refName + " in class " + jclass.getName());
        }

        JavaMethod gm = jclass.getJavaMethod(getter.getSignature());
        if (impl.existsSameNameVariableInMethod(refName, gm)) {
            throw new RefactoringException("already exists:"
              + " variable " + refName + " in method " + gm.getName());
        }

        JavaMethod sm = jclass.getJavaMethod(setter.getSignature());
        if (impl.existsSameNameVariableInMethod(refName, sm)) {
            throw new RefactoringException("already exists:"
              + " variable " + refName + " in method " + sm.getName());
        }
    }

    protected void transform() throws RefactoringException {
        RefactoringVisitor transformer = new MoveFieldVisitor(jvar, refName, dst.getName(), getter, setter);
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
        return "Move Field: " + jvar.getPrettyName()
          + " from " + jclass.getName() + " to " + dst.getName();
    }
}
