/*
 *     TextUndoManager.java  Oct 28, 2001
 *
 *     Katsunobu Takahashi (bashi@fse.cs.ritsumei.ac.jp)
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import java.util.*;

public class TextUndoManager extends UndoManager
  implements UndoableEditListener {
    private MenuPane menuPane;
    private CompoundEdit compoundEdit;
    private HashMap presentationNames = new HashMap();
    private LinkedList commands = new LinkedList();  // RefactoringRecord
    private Stack redoCommands = new Stack();        // RefactoringRecord

    public TextUndoManager() {
        super();
        setLimit(100);
    }

    public void setMenuPane(MenuPane pane) {
        menuPane = pane;
    }

    public void undoableEditHappened(UndoableEditEvent evt) {
        UndoableEdit edit = evt.getEdit();

        if (compoundEdit != null && compoundEdit.isInProgress()) {
            compoundEdit.addEdit(edit);
        } else {
            this.addEdit(edit);
            presentationNames.put(edit, edit.getPresentationName());
        }

        menuPane.updateUndoState(this);
        menuPane.updateRedoState(this);
    }

    public void beginCompoundEdit() {
        compoundEdit = new CompoundEdit();
    }

    public void endCompoundEdit(RefactoringRecord record) {
        compoundEdit.end();
        if (compoundEdit.canUndo()) {
            this.addEdit(compoundEdit);
            presentationNames.put(compoundEdit, record.getCommand());

            record.setEdit(compoundEdit);
            append(record);
        }

        menuPane.updateUndoState(this);
        menuPane.updateRedoState(this);
    }

    public String getUndoPresentationName() {
        return (String)presentationNames.get(editToBeUndone());
    }        

    public String getRedoPresentationName() {
        return (String)presentationNames.get(editToBeRedone());
    }        

    public void append(RefactoringRecord record) {
        commands.add(record);
        if (!redoCommands.empty()) {
            redoCommands.clear();
        }
    }

    public void undo() {
        if (isRefactoringCommand(getUndoPresentationName())) {
            if (!commands.isEmpty()) {
                redoCommands.push(commands.removeLast());
            }
        }
        super.undo();
    }

    public void redo() {
        if (isRefactoringCommand(getRedoPresentationName())) {
            if (!redoCommands.empty()) {
                commands.add(redoCommands.pop());
            }
        }
        super.redo();
    }

    private boolean isRefactoringCommand(String name) {
        if (name != null && name.indexOf(":") != -1) {
            return true;
        }
        return false;
    }

    public RefactoringRecord getLastRefactoringCommand(int index) {
        int len = commands.size();
        if (len > index - 1) {
            return (RefactoringRecord)commands.get(len - index);
        }
        return null;
    }

    public RefactoringRecord getLastRefactoringCommand() {
        if (commands.size() != 0) {
            return (RefactoringRecord)commands.getLast();
        }
        return null;
    }

    public boolean isLastRefactoringCommand(String name) {
        RefactoringRecord record = getLastRefactoringCommand();
        if (record != null) {
            String pname = name.substring(0, name.indexOf(":"));
            String rname = record.getCommand().substring(0, record.getCommand().indexOf(":"));
            if (pname.compareTo(rname) == 0) {
                return true;
            }
        }
        return false;
    }

    public void undoRefactoring() {
        while (canUndo() && !isRefactoringCommand(getUndoPresentationName())) {
            undo();
        }
        undo();  // Undo refcatoring
    }

    public LinkedList getRefactoringCommands() {
        return commands;
    }

    public String getText() {
        StringBuffer buf = new StringBuffer();

        Iterator it = commands.iterator();
        while (it.hasNext()) {
            RefactoringRecord record = (RefactoringRecord)it.next();
            buf.append(record.getSimpleLog());
            buf.append("\n");
        }

        if (buf.length() == 0) {
            buf.append("---- Empty ----");
        }
        return buf.toString();
    }
}
