/*
 *     MoveClassBodyDialog.java  Dec 19, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.dialog;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.Summary;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class MoveClassBodyDialog extends RefactoringDialog {
    private JTextField srcNameField;
    private JTextField dstNameField;
    private String newName;
    private String rootDir;

    public MoveClassBodyDialog(JFrame frame, String title, String dir, String srcName) {
        super(frame, title, "Input targe class name:", dir);
        srcNameField.setText(srcName);
        rootDir = dir;
        setVisible(true);
    }

    protected JPanel createCenterPane(final String dir) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        srcNameField = createInputTextField(box, "Source name: ", 70);
        srcNameField.setEditable(false);

        JButton selButton = new JButton("Class");
        selButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                dstNameField.setText(ClassTreeDialog.show(frame, "Class Selection", dir));
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
        int sep = name.indexOf("#");
        if (sep == -1) {
            if (name.equals("") || name.indexOf(" ") != -1) {
                JOptionPane.showMessageDialog(frame, "Invalid string: \"" + name + "\"");
            } else {
                newName = name;
                setVisible(false);
                dispose();
            }
        } else {
            String fileName = name.substring(0, sep);
            if (fileName.startsWith(rootDir)) {
                try {
                    File file = new File(fileName);
                    newName = file.getCanonicalPath() + name.substring(sep);
                    setVisible(false);
                    dispose();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(frame, "No such file: " + fileName);
                }
            } else {
                JOptionPane.showMessageDialog(frame,
                  " File " + fileName + " exists outside directories under " + rootDir);
            }
        }
    }

    protected void cancelAction() {
        newName = null;
        setVisible(false);
        dispose();
    }

    public static String show(JFrame frame, String title, String dir, String srcName) {
        MoveClassBodyDialog dialog = new MoveClassBodyDialog(frame, title, dir, srcName);
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
        Summary.getInstance().parse(dir);

        JFrame mainFrame = new JFrame();
        mainFrame.setSize(100, 100);
        mainFrame.setVisible(true);

        String name = MoveClassBodyDialog.show(mainFrame, "Move", dir, "SourceClass");
        System.out.println("NAME = " + name);
        System.exit(0);
    }
}
