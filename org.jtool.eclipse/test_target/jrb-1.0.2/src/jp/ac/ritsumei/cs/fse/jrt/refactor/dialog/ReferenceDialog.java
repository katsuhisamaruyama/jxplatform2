/*
 *     ReferenceDialog.java  Dec 20, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.dialog;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ReferenceDialog extends RefactoringDialog {
    private JTextField refNameField;
    private String newName;

    public ReferenceDialog(JFrame frame, String title) {
        super(frame, title, "Determine how to reference to the target object:");
        setVisible(true);
    }

    protected JPanel createCenterPane() {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        refNameField = createInputTextField(box, "Reference name: ");
        refNameField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                okButton.requestFocus();
            }
        });
        refNameField.requestFocus();
        return box;
    }

    protected void okAction() {
        newName = refNameField.getText();
        if (newName.equals("") || newName.indexOf(" ") != -1) {
            newName = null;
        }
        setVisible(false);
        dispose();
    }

    protected void cancelAction() {
        newName = null;
        setVisible(false);
        dispose();
    }

    public static String show(JFrame frame, String title) {
        ReferenceDialog dialog = new ReferenceDialog(frame, title);
        return dialog.getNewName();
    }

    public String getNewName() {
        return newName;
    }

    public static void main(String args[]) {
        JFrame mainFrame = new JFrame();
        mainFrame.setSize(100, 100);
        mainFrame.setVisible(true);

        String name = ReferenceDialog.show(mainFrame, "Reference name");
        System.out.println("NAME = " + name);
        System.exit(0);
    }
}
