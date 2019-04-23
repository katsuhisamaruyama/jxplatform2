/*
 *     VariableRefactoring.java  Dec 20, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import javax.swing.JFrame;

public class VariableRefactoring extends Refactoring {
    private static final String packagePath = "jp.ac.ritsumei.cs.fse.jrt.refactor.variables";
    protected JavaVariable jvar;

    protected VariableRefactoring() {
    }

    public VariableRefactoring(JFrame f, JavaFile jf, JavaVariable jv) {
        setFrame(f);
        jfile = jf;
        javaComp = jv;
    }

    public static RefactoringCommand create(String command) {
        return create(command, packagePath);
    }

    protected void setUp() {
        jvar = (JavaVariable)javaComp;
        jclass = jvar.getJavaClass();
    }

    protected void preconditions() throws RefactoringException {
    }

    protected void transform() throws RefactoringException {
    }

    protected void additionalTransformation() throws RefactoringException {
    }

    protected String getLog() {
        return "Variable Refactoring: " + jvar.getName();
    }
}
