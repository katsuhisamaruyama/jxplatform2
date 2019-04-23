/*
 *     RefactoringCommand.java  Apr 10, 2003
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor;
import jp.ac.ritsumei.cs.fse.jrt.gui.Refactor;
import jp.ac.ritsumei.cs.fse.jrt.gui.RefactoringHistory;
import jp.ac.ritsumei.cs.fse.jrt.model.JavaComponent;
import jp.ac.ritsumei.cs.fse.jrt.model.JavaFile;
import java.util.List;
import javax.swing.JFrame;

public interface RefactoringCommand {
    public void execute();

    public void setRefactor(Refactor refactor);
    public void setSource(JavaFile jf, int bl, int bc, int el, int ec);
    public void setFrame(JFrame frame);
    public void setRootDir(String rootDir);

    public List getChangedFiles();
    public String getRefactoringLog();
}
