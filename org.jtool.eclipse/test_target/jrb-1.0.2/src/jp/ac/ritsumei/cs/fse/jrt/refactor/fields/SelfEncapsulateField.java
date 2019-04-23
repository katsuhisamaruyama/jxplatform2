/*
 *     SelfEncapsulateField.java  Dec 17, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.fields;
import jp.ac.ritsumei.cs.fse.jrt.refactor.FieldRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.refactor.dialog.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import javax.swing.JFrame;

public class SelfEncapsulateField extends FieldRefactoring {
    private JavaMethod getter = null;
    private JavaMethod setter = null;
    private JavaMethod jg;
    private JavaMethod js;

    public SelfEncapsulateField() {
        super();
    }

    public SelfEncapsulateField(JFrame f, JavaVariable jv, String dir, String name) {
        super(f, jv.getJavaClass().getJavaFile(), jv);
        setRootDir(dir);
        if (name != null) {
            getter = impl.getGetter("get" + name, jv);
            setter = impl.getSetter("set" + name, jv);
        }
    }

    protected void preconditions() throws RefactoringException {
        if (!jvar.isPrivate()) {
            throw new RefactoringException("not change:"
              + " field " + jvar.getPrettyName() + " is not private");
        }

        if (!impl.isAllDefinedInSimpleAssignments(jvar, jclass)) {
            throw new RefactoringException("not change:" 
              + " field " + jvar.getPrettyName() + " is not defined in a simple assignment");
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

        transformer = new SelfEncapsulateFieldVisitor(jvar, getter, setter);
        jfile.accept(transformer);

        PrintVisitor printer = new PrintVisitor();
        String newCode = printer.getCode(jfile);

        DisplayedFile file = new DisplayedFile(jfile.getName(), jfile.getText(), newCode);
        file.setOldHighlight(transformer.getHighlights());
        file.setNewHighlight(printer.getHighlights());
        changedFiles.add(file);
    }

    protected String getLog() {
        return "Self Encapsulate Field: " + jvar.getPrettyName() + " through accessors "
          + getter.getName() + " and " + setter.getName();
    }
}
