/*
 *     NewNameDialog.java  Dec 20, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.dialog;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class NewNameDialog extends RefactoringDialog {
    private JTextField newNameField;
    private String newName;

    public NewNameDialog(JFrame frame, String title) {
        super(frame, title, "Input new class or method name:");
        setVisible(true);
    }

    protected JPanel createCenterPane() {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        newNameField = createInputTextField(box, "New name: ");
        newNameField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                okButton.requestFocus();
            }
        });
        newNameField.requestFocus();
        return box;
    }

    protected void okAction() {
        newName = newNameField.getText();
        if (newName.equals("") || newName.indexOf(" ") != -1) {
            JOptionPane.showMessageDialog(frame, "Invalid string: \"" + newName + "\"");
        } else {
            setVisible(false);
            dispose();
        }
    }

    protected void cancelAction() {
        newName = null;
        setVisible(false);
        dispose();
    }

    public static String show(JFrame frame, String title) {
        NewNameDialog dialog = new NewNameDialog(frame, title);
        return dialog.getNewName();
    }

    public String getNewName() {
        return newName;
    }

    public static void main(String args[]) {
        JFrame mainFrame = new JFrame();
        mainFrame.setSize(100, 100);
        mainFrame.setVisible(true);

        String name = NewNameDialog.show(mainFrame, "New name");
        System.out.println("NAME = " + name);
        System.exit(0);
    }
}
