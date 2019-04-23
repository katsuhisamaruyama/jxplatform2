/*
 *     RefactoringRecord.java  Jan 5, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import javax.swing.undo.CompoundEdit;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.Serializable;
import java.sql.Timestamp;

public class RefactoringRecord implements Serializable {
    private String command;
    private String fileName;
    private Timestamp timestamp;
    private String userName;
    transient private boolean inProgress = false;
    transient private List changedFiles;
    transient private CompoundEdit edit;
    
    private RefactoringRecord() {
    }

    public RefactoringRecord(String cmd, String fname, List files) {
        command = new String(cmd);
        fileName = new String(fname);
        changedFiles = new ArrayList(files);
        timestamp = new Timestamp(System.currentTimeMillis());
        userName = new String(System.getProperty("user.name"));
        inProgress = true;
    }

    public String getCommand() {
        return command;
    }

    public String getCommandWithoutID() {
        String prefix = command.substring(0, command.indexOf("-"));
        String suffix = command.substring(command.indexOf(":"));
        return prefix + suffix;
    }

    public String getFileName() {
        return fileName;
    }

    public String getUseName() {
        return userName;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public List getChangedFiles() {
        return changedFiles;
    }

    public void setEdit(CompoundEdit edit) {
        this.edit = edit;
    }

    public CompoundEdit getEdit() {
        return edit;
    }

    public String getLog() {
        String cmd = command.substring(0, command.indexOf("-"));
        return cmd + "  " + fileName + " by " + userName + " at " + timestamp.toString();
    }

    public String getSimpleLog() {
        return command + " at " + timestamp.toString();
    }
}
