/*
 *     MoveClassDialog.java  Dec 20, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.dialog;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class MoveClassDialog extends RefactoringDialog {
    private JTextField srcNameField;
    private JTextField dstNameField;
    private String newName;
    private String rootDir;

    public MoveClassDialog(JFrame frame, String title, String dir, String srcName) {
        super(frame, title, "Input targe file name:", dir);
        srcNameField.setText(srcName);
        rootDir = dir;
        setVisible(true);
    }

    protected JPanel createCenterPane(final String dir) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        srcNameField = createInputTextField(box, "Source name: ", 60);
        srcNameField.setEditable(false);

        JButton selButton = new JButton("File");
        selButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                dstNameField.setText(FileTreeDialog.show(frame, "File Selection", dir));
            }
        });
        dstNameField = createInputTextField(box, "Target name: ", selButton);
        dstNameField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                okButton.requestFocus();
            }
        });

        dstNameField.requestFocus();
        return box;
    }

    protected void okAction() {
        String name = dstNameField.getText();
        if (name.startsWith(rootDir)) {
            try {
                File file = new File(name);
                newName = file.getCanonicalPath();
                setVisible(false);
                dispose();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "No such file: " + name);
            }
        } else {
            JOptionPane.showMessageDialog(frame,
              " File " + name + " exists outside directories under " + rootDir);
        }
    }

    protected void cancelAction() {
        newName = null;
        setVisible(false);
        dispose();
    }

    public static String show(JFrame frame, String title, String dir, String srcName) {
        MoveClassDialog dialog = new MoveClassDialog(frame, title, dir, srcName);
        return dialog.getNewName();
    }

    public String getNewName() {
        return newName;
    }

    public static void main(String args[]) {
        String dir = ".";
        if (args.length == 1) {
            dir = args[0];
        }

        JFrame mainFrame = new JFrame();
        mainFrame.setSize(100, 100);
        mainFrame.setVisible(true);

        String name = MoveClassDialog.show(mainFrame, "Move", dir, "Source.java");
        System.out.println("NAME = " + name);
        System.exit(0);
    }
}
