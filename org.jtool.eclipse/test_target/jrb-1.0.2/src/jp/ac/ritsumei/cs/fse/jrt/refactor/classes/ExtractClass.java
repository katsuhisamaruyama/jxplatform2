/*
 *     ExtractClass.java  Dec 19, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.classes;
import jp.ac.ritsumei.cs.fse.jrt.refactor.ClassRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.refactor.dialog.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import javax.swing.JFrame;

public class ExtractClass extends ClassRefactoring {
    protected String newName = null;

    public ExtractClass() {
        super();
    }

    public ExtractClass(JFrame f, JavaFile jf, JavaClass jc) {
        super(f, jf, jc);
    }

    protected void preconditions() throws RefactoringException {
        if (newName == null) {
            throw new RefactoringException("");
        }

        String fname = impl.findSameNameFileInPackage(newName, jfile);
        if (fname != null) {
            throw new RefactoringException("already exists:"
              + " class/interface " + newName + " in file " + fname);
        }

        String thisFileName = jfile.getShortFileName() + ".java";
        if (impl.existsSameNameClassInFile(newName, jfile)) {
            throw new RefactoringException("already exists:"
              + " class/interface " + newName + " in file " + thisFileName);
        }
    }
}
