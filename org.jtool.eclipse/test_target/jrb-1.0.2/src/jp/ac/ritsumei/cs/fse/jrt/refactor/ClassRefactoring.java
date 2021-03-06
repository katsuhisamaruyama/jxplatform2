/*
 *     ClassRefactoring.java  Dec 20, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import javax.swing.JFrame;

public class ClassRefactoring extends Refactoring {
    private static final String packagePath = "jp.ac.ritsumei.cs.fse.jrt.refactor.classes";
    protected ClassRefactoring() {
    }

    protected ClassRefactoring(JFrame f, JavaFile jf, JavaClass jc) {
        setFrame(f);
        jfile = jf;
        javaComp = jc;
    }

    public static RefactoringCommand create(String command) {
        return create(command, packagePath);
    }

    protected void setUp() {
        jclass = (JavaClass)javaComp;
    }

    protected void preconditions() throws RefactoringException {
    }

    protected void transform() throws RefactoringException {
    }

    protected void additionalTransformation() throws RefactoringException {
    }

    protected String getLog() {
        return "Class Refactoring: " + jclass.getName();
    }
}
