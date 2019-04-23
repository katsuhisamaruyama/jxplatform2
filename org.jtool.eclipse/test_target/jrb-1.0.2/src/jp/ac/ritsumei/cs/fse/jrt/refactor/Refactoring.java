/*
 *     Refactoring.java  Apr 10, 2003
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor;
import jp.ac.ritsumei.cs.fse.jrt.gui.Refactor;
import jp.ac.ritsumei.cs.fse.jrt.gui.PositionVisitor;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.refactor.dialog.ResultsDialog;
import jp.ac.ritsumei.cs.fse.jrt.refactor.dialog.FileListDialog;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public abstract class Refactoring implements RefactoringCommand {
    protected RefactoringImpl impl = new RefactoringImpl();
    protected JFrame frame;
    protected String rootDir;
    protected JavaFile jfile;
    protected JavaClass jclass;
    protected JavaComponent javaComp;
    protected List changedFiles = new ArrayList();  // DisplayedFile
    private String logMessage = null;
    private JavaFile originalJFile;

    public static RefactoringCommand create(String command, String packagePath) {
        try {
            Class cl = Class.forName(packagePath + "." + command);
            return (RefactoringCommand)cl.newInstance();

        } catch (ClassNotFoundException e) {
            // e.printStackTrace();
        } catch (InstantiationException e) {
            // e.printStackTrace();
        } catch (IllegalAccessException e) {
            // e.printStackTrace();
        }
        return new NullCommand();
    }

    public void setRefactor(Refactor refactor) {
        impl.setRefactor(refactor);
    }

    public void setSource(JavaFile jf, int bl, int bc, int el, int ec) {
        originalJFile = jf;
        jfile = impl.getClone(originalJFile);
        jfile.setText(jf.getText());

        PositionVisitor positionVisitor = new PositionVisitor();
        javaComp = positionVisitor.getTokenAt(jfile, bl, bc, el, ec);
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
        impl.setFrame(frame);
    }
    
    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    public List getChangedFiles() {
        return changedFiles;
    }

    public DisplayedFile getChangedFile(String name) {
        Iterator it = changedFiles.iterator();
        while (it.hasNext()) {
            DisplayedFile file = (DisplayedFile)it.next();
            if (name.compareTo(file.getName()) == 0) {
                return file;
            }
        }
        return null;
    }

    public String getRefactoringLog() {
        return logMessage;
    }

    public void execute() {
        try {
            setUp();
            preconditions();
            transform();
            additionalTransformation();

            if (confirm()) {
                logMessage = getLog();
                clean();
            } else {
                logMessage = null;
                changedFiles.clear();
                restore();
            }

        } catch (RefactoringException e) {
            String mesg = e.getMessage();
            if (mesg.length() != 3) {
                JOptionPane.showMessageDialog(frame, "Refactoring" + mesg);
            }
            logMessage = null;
            changedFiles.clear();
            restore();
        }
    }

    protected abstract void setUp();

    protected abstract void preconditions() throws RefactoringException;

    protected abstract void transform() throws RefactoringException;

    protected abstract void additionalTransformation() throws RefactoringException;
    
    protected abstract String getLog();

    protected boolean confirm() {
        boolean result = ResultsDialog.show(frame, "Confirm Changes", changedFiles);
        return result;
    }

    private void restore() {
        impl.removeParsedFile(jfile);
        impl.addParsedFile(originalJFile);
    }

    private void clean() {
        if (jfile != null) {
            impl.removeParsedFile(jfile);
        }
        Iterator it = changedFiles.iterator();
        while (it.hasNext()) {
            DisplayedFile file = (DisplayedFile)it.next();
            impl.removeParsedFile(file.getName());
        }
    }
}
