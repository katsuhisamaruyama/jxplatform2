/*
 *     FileTree.java  Nov 1, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.io.File;
import java.util.*;

public class FileTree extends JTree {
    private FileTreeNode rootNode;
    private List files = new ArrayList();

    public FileTree() {
        super((TreeModel)null);
        this.putClientProperty("JTree.lineStyle", "Angled");

        String rootDir = JRBProperties.getProperty("Root.Dir");
        if (rootDir.endsWith(File.separator)) {
            rootDir = rootDir.substring(0, rootDir.length() - 1);
        }
        rootNode = new FileTreeNode(null, rootDir);
        rootNode.populateDirectories();
        this.setModel(new DefaultTreeModel(rootNode));

        Enumeration e = rootNode.preorderEnumeration();
        while (e.hasMoreElements()) {
            FileTreeNode node = (FileTreeNode)e.nextElement();
            if (node.getFullName().endsWith(".java")) {
                files.add(node.getFullName());
            }
        }
    }

    public FileTreeNode getRootNode() {
        return rootNode;
    }

    public List getFiles() {
        return files;
    }

    public String getPathName(TreePath path) {
        Object obj = path.getLastPathComponent();
        if (obj instanceof FileTreeNode) {
            return ((FileTreeNode)obj).getFullName();
        }
        return null;
    }
}

class FileTreeNode extends DefaultMutableTreeNode {
    private String name;
    private String fullName;
    private boolean isDir;

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
