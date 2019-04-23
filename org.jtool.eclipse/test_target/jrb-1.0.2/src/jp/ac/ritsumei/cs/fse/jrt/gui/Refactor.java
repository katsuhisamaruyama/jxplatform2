/*
 *     Refactor.java  Apr 10, 2003
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import jp.ac.ritsumei.cs.fse.jrt.util.SimpleEventSource;
import jp.ac.ritsumei.cs.fse.jrt.util.LogEvent;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.refactor.RefactoringCommand;
import jp.ac.ritsumei.cs.fse.jrt.refactor.ClassRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.MethodRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.FieldRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.VariableRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.MiscellaneousRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.ast.ASTSwitchStatement;
import java.util.*;
import java.io.*;
import javax.swing.JOptionPane;

public class Refactor extends SimpleEventSource {
    private static Refactor singleton = new Refactor();
    private PositionVisitor positionVisitor;
    private RefactoringCommand commander;
    private int refactoringId = 1;
    private JavaFile jfile;
    private ArrayList javaComps;
    private JavaComponent javaComp;
    private int beginLine, beginColumn;
    private int endLine, endColumn;
    private TabbedTextPane tabbedTextPane;

    private Refactor() {
        positionVisitor = new PositionVisitor();
    }

    public static Refactor getInstance() {
        return singleton;
    }

    public void setTabbedTextPane(TabbedTextPane pane) {
        tabbedTextPane = pane;
    }

    public void setJavaComponent(JavaFile jfile, int bl, int bc, int el, int ec) {
        this.jfile = jfile;
        if (jfile == null) {
            return;
        }

        beginLine = bl;
        beginColumn = bc;
        endLine = el;
        endColumn = ec;
        javaComp = positionVisitor.getTokenAt(jfile, beginLine, beginColumn, endLine, endColumn);
    }

    public boolean isClassRefactoring() {
        if (javaComp != null && javaComp.isJavaClass()) {
            JavaClass jc = (JavaClass)javaComp;
            return !jc.isInterface();
        }
        return false;
    }

    public boolean isMethodRefactoring() {
        if (javaComp != null && javaComp.isJavaMethod()) {
            JavaMethod jm = (JavaMethod)javaComp;

            if (!jm.getJavaClass().isInterface()) {
                if (jm.getName().compareTo(jm.getJavaClass().getName()) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isFieldRefactoring() {
        if (javaComp != null && javaComp.isJavaVariable()) {
            JavaVariable jv = (JavaVariable)javaComp;

            JavaStatement jst = jv.getJavaClass().getJavaField(jv);
            if (jst != null) {

                JavaVariable jvDecl = jst.getDefVariables().getFirst();
                if (jvDecl.isField()) {
                    javaComp = jvDecl;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isVariableRefactoring() {
        if (javaComp != null && javaComp.isJavaVariable()) {
            JavaVariable jv = (JavaVariable)javaComp;

            JavaMethod jm = jv.getJavaMethod();
            if (jm != null) {

                JavaVariable jvDecl = jm.getJavaVaraible(jv);
                if (jvDecl != null && jvDecl.isLocal()) {
                    ((JavaVariable)javaComp).setLocal();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isSwitchRefactoring() {
        if (javaComp != null && javaComp.isJavaStatement()) {
            SimpleNode node = javaComp.getASTNode();
            if (node instanceof ASTSwitchStatement) {
                return true;
            }
        }
        return false;
    }

    public void execute(String command) {
        TextPane textPane = tabbedTextPane.getCurrentTextPane();

        JavaFile jfile = textPane.getJavaFile();
        jfile.setText(textPane.getText());

        if (javaComp.isJavaClass()) {
            commander = ClassRefactoring.create(command);

        } else if (javaComp.isJavaMethod()) {
            commander = MethodRefactoring.create(command);

        } else if (javaComp.isJavaVariable()) {
            JavaVariable jvar = (JavaVariable)javaComp;

            if (jvar.isField()) {
                commander = FieldRefactoring.create(command);
            } else if (jvar.isLocal()) {
                commander = VariableRefactoring.create(command);
            }

        } else if (isSwitchRefactoring()) {
            commander = MiscellaneousRefactoring.create(command);
        }

        commander.setRefactor(this);
        commander.setSource(jfile, beginLine, beginColumn, endLine, endColumn);
        commander.setFrame(textPane.getFrame());
        commander.setRootDir(JRBProperties.getProperty("Root.Dir"));
        commander.execute();

        String logMessage = commander.getRefactoringLog();
        if (logMessage != null) {
            updateTextPane(command);
            fireLogEvent(new LogEvent(this, logMessage));
            refactoringId++;
        }

        tabbedTextPane.setSelectedComponent(textPane);
    }

    public String getDestinationText(String name) {
        TextPane textPane = tabbedTextPane.getTextPane(name);
        if (textPane != null) {
            return textPane.getText();
        }

        try {
            return tabbedTextPane.readFile(name);
        } catch (FileNotFoundException e) {
        } catch (IOException e) { }
        return new String("");
    }

    private void updateTextPane(String command) {
        List fileNames = getFileNameList(commander.getChangedFiles());

        Iterator it = commander.getChangedFiles().iterator();
        while (it.hasNext()) {
            DisplayedFile file = (DisplayedFile)it.next();
            String name = file.getName();
            TextPane textPane = tabbedTextPane.getTextPane(name);
            if (textPane == null) {

                File f = new File(name);
                if (f.exists()) {
                    try {
                        String content = tabbedTextPane.readFile(name);
                        textPane = tabbedTextPane.openFile(name, content);
                        textPane.setLastModified(f.lastModified());

                    } catch (FileNotFoundException e) {
                    } catch (IOException e) { }
                } else {
                    textPane = tabbedTextPane.newFile(name);
                }
                textPane.getUndoManager().discardAllEdits();
            }

            String key = getKey(command, textPane.getFileName());
            RefactoringRecord record = new RefactoringRecord(key, textPane.getFileName(), fileNames);

            textPane.getUndoManager().beginCompoundEdit();
            textPane.setText(file.getNewText());
            textPane.getUndoManager().endCompoundEdit(record);

            textPane.setChange(true);
            textPane.setCaretPosition(0);
            textPane.validate();
        }

        tabbedTextPane.updateUndoState();
        tabbedTextPane.showMethodList();
    }

    private List getFileNameList(List files) {
        List fileNames = new ArrayList();
        Iterator it = files.iterator();
        while (it.hasNext()) {
            DisplayedFile file = (DisplayedFile)it.next();
            fileNames.add(file.getName());
        }
        return fileNames;
    }

    private String getKey(String command, String fileName) {
        if (fileName.compareTo(jfile.getName()) == 0) {
            return command + "-" + String.valueOf(refactoringId) + ":src";
        } else {
            return command + "-" + String.valueOf(refactoringId) + ":dst";
        }
    }

    public boolean canUndo(TextPane textPane) {
        TextUndoManager undoManager = textPane.getUndoManager();
        RefactoringRecord record = undoManager.getLastRefactoringCommand();
        if (record == null) {
            return false;
        }
        return canUndo(record);
    }

    private boolean canUndo(RefactoringRecord record) {
        Iterator it = record.getChangedFiles().iterator();
        while (it.hasNext()) {
            String name = (String)it.next();

            TextPane textPane = tabbedTextPane.getTextPane(name);
            if (textPane == null) {
                // System.out.println("FILE NOT FOUND: " + name);
                return false;
            }
            if (!canUndo(textPane, record)) {
                // System.out.println("COMMAND NOT FOUND: " + record.getCommand());
                return false;
            }
        }
        return true;
    }

    private boolean canUndo(TextPane textPane, RefactoringRecord record) {
        TextUndoManager undoManager = textPane.getUndoManager();
        return undoManager.isLastRefactoringCommand(record.getCommand());
    }

    public void undo(TextPane textPane) {
        TextUndoManager undoManager = textPane.getUndoManager();
        RefactoringRecord record = undoManager.getLastRefactoringCommand();
        List changedFiles = record.getChangedFiles();
        if (!confirmUndo(record.getCommand(), changedFiles)) {
            return;
        }

        Iterator it = changedFiles.iterator();
        while (it.hasNext()) {
            String name = (String)it.next();
            TextPane pane = tabbedTextPane.getTextPane(name);
            TextUndoManager um = pane.getUndoManager();

            um.undoRefactoring();
            textPane.setCaretPosition(0);
            textPane.setChange(true);
        }

        tabbedTextPane.updateUndoState();
        tabbedTextPane.showMethodList();        
        fireLogEvent(new LogEvent(this, "Undo: " + record.getSimpleLog()));
    }

    private boolean confirmUndo(String command, List files) {
        int result = JOptionPane.showConfirmDialog(tabbedTextPane.getFrame(),
          "Undo " + command + ":\n"
          + "The changes were made to the following files.\n" + getPrettyFileNameList(files)
          + "Do you want to discard the changes?", "Select an Option", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            return true;
        }
        return false;
    }

    private String getPrettyFileNameList(List files) {
        StringBuffer buf = new StringBuffer();
        Iterator it = files.iterator();
        while (it.hasNext()) {
            String name = (String)it.next();
            buf.append("\"");
            buf.append(name);
            buf.append("\"\n");
        }
        return buf.toString();
    }
}
