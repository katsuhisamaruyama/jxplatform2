/*
 *     TreeDialog.java  Dec 20, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.dialog;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public abstract class TreeDialog extends JDialog implements TreeSelectionListener {
    protected JFrame frame;
    protected Container contentPane;
    protected JTree tree;
    protected String selName;

    protected TreeDialog(JFrame frame, String title, String dir) {
        super(frame, title, true);
        this.frame = frame;
        createPanes(dir);
    }

    protected abstract JTree createTree(String dir);

    public abstract void valueChanged(TreeSelectionEvent evt);

    protected void createPanes(String dir) {
        contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());

        tree = createTree(dir);
        tree.setFont(new Font("Dialog", Font.PLAIN, 14));
        tree.setSize(10, 10);

        TreeSelectionModel selectionModel = tree.getSelectionModel();
        selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);

        JScrollPane panel = new JScrollPane();
        panel.setViewportView(tree);
        contentPane.add(panel, "Center");

        JPanel bottomPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        createCancelButton(bottomPane);
        contentPane.add(bottomPane, "South");

        Point p = frame.getLocationOnScreen();
        this.pack();
        this.setLocation(p.x + frame.getSize().width - this.getSize().width - 10, p.y + 10);
        this.validate();
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                selName = null;
                setVisible(false);
                dispose();
            }
        });
    }

    protected void createCancelButton(JPanel panel) {
        JButton cancelButton = new JButton("Cancel");
        panel.add(cancelButton);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                selName = null;
                setVisible(false);
                dispose();
            }
        });
    }
}
