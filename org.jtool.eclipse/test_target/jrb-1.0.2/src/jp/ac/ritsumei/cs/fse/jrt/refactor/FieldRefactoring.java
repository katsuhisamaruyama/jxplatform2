/*
 *     FieldRefactoring.java  Dec 20, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.List;
import java.util.Iterator;
import javax.swing.JFrame;

public class FieldRefactoring extends Refactoring {
    private static final String packagePath = "jp.ac.ritsumei.cs.fse.jrt.refactor.fields";
    protected JavaVariable jvar;

    protected FieldRefactoring() {
    }

    public FieldRefactoring(JFrame f, JavaFile jf, JavaVariable jv) {
        setFrame(f);
        jfile = jf;
        javaComp = jv;
    }

    public static RefactoringCommand create(String command) {
        return create(command, packagePath);
    }

    protected void setUp() {
        JavaVariable jv = (JavaVariable)javaComp;
        jclass = jv.getJavaClass();
        JavaStatement jst = jclass.getJavaField(jv);
        jvar = jst.getDefVariables().getFirst();
    }

    protected void preconditions() throws RefactoringException {
    }

    protected void transform() throws RefactoringException {
    }

    protected void additionalTransformation() throws RefactoringException {
    }

    protected String getLog() {
        return "Field Refactoring: " + jvar.getName();
    }

    protected void showFilesUsingField(JavaVariable jv, String name, List files) {
        /*
        String mesg = "The following files contain the access to field " + name
            + " of class " + jclass.getName() + ". ";
        FileListDialog.show(frame, mesg, files);
        */

        Iterator it = files.iterator();
        while (it.hasNext()) {
            JavaFile dfile = (JavaFile)it.next();

            RefactoringVisitor transformer = new UsingFieldsVisitor(jv, dfile);
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
}
