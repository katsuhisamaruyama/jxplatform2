/*
 *     FileSystemPane.java  Nov 24, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import java.awt.Font;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.io.File;
import java.util.*;

public class FileSystemPane extends JScrollPane implements TreeSelectionListener {
    private JFrame frame;
    private TabbedTextPane tabbedTextPane;
    private FileManager fileManager;
    private FileTree fileTree;
    
    public FileSystemPane() {
        super();
    }

    public FileSystemPane(JFrame frame, FileManager fm, TabbedTextPane pane) {
        super();
        this.frame = frame;
        fileManager = fm;
        tabbedTextPane = pane;
    }

    public void init() {
        buildFileTree();
    }

    public void buildFileTree() {
        fileTree = new FileTree();
        fileTree.setFont(new Font("Dialog", Font.PLAIN, 14));
        this.setViewportView(fileTree);
        TreeSelectionModel selectionModel = fileTree.getSelectionModel();
        selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        fileTree.addTreeSelectionListener(this);

        tabbedTextPane.removeAllParsedSummaryFiles();
        tabbedTextPane.parseSummaryAllFiles();
    }

    public List getFileList() {
        return fileTree.getFiles();
    }

    public void valueChanged(TreeSelectionEvent evt) {
        TreePath path = evt.getPath();
        String name = fileTree.getPathName(path);

        if (tabbedTextPane.isOpen(name)) {
            TextPane textPane = tabbedTextPane.getTextPane(name);            
            tabbedTextPane.setSelectedComponent(textPane);
            return;
        }

        File file = new File(name);
        if (file.isFile()) {
            TextPane textPane = fileManager.openFile(file.getPath());
            textPane.getUndoManager().discardAllEdits();

            MenuPane menu = ((MainFrame)frame).getMenuPane();
            menu.buildRecentFileMenu();
            menu.updateRedoState(textPane.getUndoManager());
            menu.updateUndoState(textPane.getUndoManager());
        }
    }
}
