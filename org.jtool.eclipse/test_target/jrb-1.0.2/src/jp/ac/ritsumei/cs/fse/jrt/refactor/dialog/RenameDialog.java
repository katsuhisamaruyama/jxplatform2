/*
 *     RenameDialog.java  Dec 20, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.dialog;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class RenameDialog extends RefactoringDialog {
    private JTextField oldNameField;
    private JTextField newNameField;
    private String newName;

    public RenameDialog(JFrame frame, String title, String oldName) {
        super(frame, title, "Input new class, method, field, or variable name:");
        oldNameField.setText(oldName);
        setVisible(true);
    }

    protected JPanel createCenterPane() {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        oldNameField = createInputTextField(box, "Old name:   ");
        oldNameField.setEditable(false);

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
        
    public static String show(JFrame frame, String title, String oldName) {
        RenameDialog dialog = new RenameDialog(frame, title, oldName);
        return dialog.getNewName();
    }

    public String getNewName() {
        return newName;
    }

    public static void main(String args[]) {
        JFrame mainFrame = new JFrame();
        mainFrame.setSize(100, 100);
        mainFrame.setVisible(true);

        String name = RenameDialog.show(mainFrame, "Rename", "OldName");
        System.out.println("NAME = " + name);
        System.exit(0);
    }
}
