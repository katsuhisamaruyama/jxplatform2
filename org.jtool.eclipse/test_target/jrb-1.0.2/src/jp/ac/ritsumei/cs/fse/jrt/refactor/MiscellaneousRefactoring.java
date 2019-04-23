/*
 *     MiscellaneousRefactoring.java  Dec 20, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import javax.swing.JFrame;

public class MiscellaneousRefactoring extends Refactoring {
    private static final String packagePath = "jp.ac.ritsumei.cs.fse.jrt.refactor.miscellaneous";

    protected MiscellaneousRefactoring() {
    }

    protected MiscellaneousRefactoring(JFrame f, JavaFile jf, JavaComponent j) {
        setFrame(f);
        jfile = jf;
        javaComp = j;
    }

    public static RefactoringCommand create(String command) {
        return create(command, packagePath);
    }

    protected void setUp() {
    }

    protected void preconditions() throws RefactoringException {
    }

    protected void transform() throws RefactoringException {
    }

    protected void additionalTransformation() throws RefactoringException {
    }

    protected String getLog() {
        return "Miscellaneous Refactoring";
    }
}
