/*
 *     MethodRefactoring.java  Dec 20, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.List;
import java.util.Iterator;
import javax.swing.JFrame;

public class MethodRefactoring extends Refactoring {
    private static final String packagePath = "jp.ac.ritsumei.cs.fse.jrt.refactor.methods";
    protected JavaMethod jmethod;

    protected MethodRefactoring() {
    }

    public MethodRefactoring(JFrame f, JavaFile jf, JavaMethod jm) {
        setFrame(f);
        jfile = jf;
        javaComp = jm;
    }

    public static RefactoringCommand create(String command) {
        return create(command, packagePath);
    }

    protected void setUp() {
        jmethod = (JavaMethod)javaComp;
        jclass = jmethod.getJavaClass();
    }

    protected void preconditions() throws RefactoringException {
    }

    protected void transform() throws RefactoringException {
    }

    protected void additionalTransformation() throws RefactoringException {
    }

    protected String getLog() {
        return "Method Refactoring: " + jmethod.getName();
    }

    protected void showFilesCallingMethod(JavaMethod jm, List files) {
        /*
        String mesg = "The following files contain the method invocation to method " + jm.getName()
                    + " of class " + jclass.getName() + ". ";
        FileListDialog.show(frame, mesg, files);
        */

        Iterator it = files.iterator();
        while (it.hasNext()) {
            JavaFile dfile = (JavaFile)it.next();

            RefactoringVisitor transformer = new CallingMethodsVisitor(jm, dfile);
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
