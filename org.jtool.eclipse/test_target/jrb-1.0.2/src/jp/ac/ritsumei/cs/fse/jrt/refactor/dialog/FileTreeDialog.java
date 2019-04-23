/*
 *     FileTreeDialog.java  Dec 20, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.dialog;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.File;

public class FileTreeDialog extends TreeDialog {

    public FileTreeDialog(JFrame frame, String title, String dir) {
        super(frame, title, dir);
        setSize(400, 400);
        setVisible(true);
    }

    protected JTree createTree(String dir) {
        return new FileTree(dir);
    }

    public void valueChanged(TreeSelectionEvent evt) {
        TreePath path = evt.getPath();
        selName = ((FileTree)tree).getPathName(path);
        if (selName != null) {
            setVisible(false);
            dispose();
        }
    }

    public static String show(JFrame frame, String title, String dir) {
        FileTreeDialog dialog = new FileTreeDialog(frame, title, dir);
        return dialog.getSelName();
    }

    public String getSelName() {
        return selName;
    }

    public static void main(String args[]) {
        String dir = ".";
        if (args.length == 1) {
            dir = args[0];
        }

        JFrame mainFrame = new JFrame();
        mainFrame.setSize(100, 100);
        mainFrame.setVisible(true);

        String name = FileTreeDialog.show(mainFrame, "File Selection", dir);
        System.out.println("NAME = " + name);
        System.exit(0);
    }
}

class FileTree extends JTree {
    private FileTreeNode rootNode;

    public FileTree(String rootDir) {
        super((TreeModel)null);
        this.putClientProperty("JTree.lineStyle", "Angled");

        rootNode = new FileTreeNode(null, rootDir);
        rootNode.populateDirectories();
        this.setModel(new DefaultTreeModel(rootNode));
    }

    public String getPathName(TreePath path) {
        Object obj = path.getLastPathComponent();
        FileTreeNode node = (FileTreeNode)obj;
        if (node.isLeaf()) {
            return node.getFullName();
        }
        return null;
    }
}

class FileTreeNode extends DefaultMutableTreeNode {
    private String name;
    private String fullName;
    private boolean isDir;

    protected FileTreeNode() {
        super();
    }

    public FileTreeNode(String parent, String name) {
        super();

        this.name = name;
        if (parent == null) {
            fullName = name;
        } else {
            fullName = parent + File.separator + name;
        }

        File file = new File(fullName);
        isDir = file.isDirectory();

        if (!isDir && !file.isFile()) {  // Windows Drive (C:\) is a directory
            isDir = true;
        }			
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isLeaf() {
        return !isDir;
    }

    public boolean getAllowsChildren() {
        return isDir;
    }

    public String toString() {			
        return name;
    }

    public void populateDirectories() {
        File directory = new File(fullName);
        String[] names = directory.list();
        for (int i = 0; i < names.length; i++) {
            File dir = new File(fullName, names[i]);
            if (dir.isDirectory()) {
                FileTreeNode node = new FileTreeNode(fullName, names[i]);
                this.add(node);
                node.populateDirectories();
            }
        }

        for (int i = 0; i < names.length; i++) {
            File file = new File(fullName, names[i]);
            if (file.isFile() && names[i].endsWith(".java")) {
                FileTreeNode node = new FileTreeNode(fullName, names[i]);
                this.add(node);
            }
        }
    }
}
