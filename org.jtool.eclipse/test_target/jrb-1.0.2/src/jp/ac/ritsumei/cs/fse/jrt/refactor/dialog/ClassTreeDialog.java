/*
 *     ClassTreeDialog.java  Dec 19, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.dialog;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.Summary;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.SummaryJavaFile;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.SummaryJavaClass;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.Iterator;
import java.io.File;

public class ClassTreeDialog extends TreeDialog {

    public ClassTreeDialog(JFrame frame, String title, String dir) {
        super(frame, title, dir);
        setSize(400, 400);
        setVisible(true);
    }

    protected JTree createTree(String dir) {
        return new ClassTree(dir);
    }

    public void valueChanged(TreeSelectionEvent evt) {
        TreePath path = evt.getPath();
        selName = ((ClassTree)tree).getPathName(path);
        if (selName != null) {
            setVisible(false);
            dispose();
        }
    }

    public static String show(JFrame frame, String title, String dir) {
        ClassTreeDialog dialog = new ClassTreeDialog(frame, title, dir);
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
        Summary.getInstance().parse(dir);

        JFrame mainFrame = new JFrame();
        mainFrame.setSize(100, 100);
        mainFrame.setVisible(true);

        String name = ClassTreeDialog.show(mainFrame, "Class Selection", dir);
        System.out.println("NAME = " + name);
        System.exit(0);
    }
}

class ClassTree extends JTree {
    private ClassTreeNode rootNode;

    public ClassTree(String rootDir) {
        super((TreeModel)null);
        this.putClientProperty("JTree.lineStyle", "Angled");

        rootNode = new ClassTreeNode(null, rootDir);
        rootNode.populateDirectories();
        this.setModel(new DefaultTreeModel(rootNode));
    }

    public String getPathName(TreePath path) {
        Object obj = path.getLastPathComponent();
        ClassTreeNode node = (ClassTreeNode)obj;
        if (node.isLeaf()) {
            return node.getFullName();
        }
        return null;
    }
}

class ClassTreeNode extends DefaultMutableTreeNode {
    private String name;
    private String fullName;
    private boolean isClass;

    public ClassTreeNode(String parent, String name) {
        super();

        this.name = name;
        if (parent == null) {
            fullName = name;
        } else {
            fullName = parent + File.separator + name;
        }
        isClass = false;
    }

    public ClassTreeNode(String fileName, String sep, String name) {
        super();
        this.name = name;
        fullName = fileName + sep + name;
        isClass = true;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isLeaf() {
        return isClass;
    }

    public boolean getAllowsChildren() {
        return !isClass;
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
                ClassTreeNode node = new ClassTreeNode(fullName, names[i]);
                this.add(node);
                node.populateDirectories();
            }
        }

        for (int i = 0; i < names.length; i++) {
            File file = new File(fullName, names[i]);
            if (file.isFile() && names[i].endsWith(".java")) {
                populateClasses(fullName, names[i]);
            }
        }
    }

    public void populateClasses(String fullName, String name) {
        String fileName = fullName + File.separator + name;
        SummaryJavaFile jfile = Summary.getInstance().getJavaFile(fileName);
        if (jfile == null) {
            return;
        }

        Iterator it = jfile.getJavaClasses().iterator();
        while (it.hasNext()) {
            SummaryJavaClass jclass = (SummaryJavaClass)it.next();
            ClassTreeNode node = new ClassTreeNode(fileName, "#", jclass.getName());
            this.add(node);
        }
    }
}
