/*
 *     NullCommand.java  Apr 10, 2003
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor;
import jp.ac.ritsumei.cs.fse.jrt.gui.Refactor;
import jp.ac.ritsumei.cs.fse.jrt.model.JavaComponent;
import jp.ac.ritsumei.cs.fse.jrt.model.JavaFile;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JFrame;

public class NullCommand implements RefactoringCommand {
    public NullCommand() {
    }

    public void execute() {
        System.err.println("Unsupported command.");
    }

    public void setRefactor(Refactor refactor) {
    }

    public void setSource(JavaFile jf, int bl, int bc, int el, int ec) {
    }

    public void setFrame(JFrame frame) {
    }

    public void setRootDir(String rootDir) {
    }

    public List getChangedFiles() {
        return new ArrayList();
    }

    public String getRefactoringLog() {
        return null;
    }
}
