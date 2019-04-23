/*
 *     AccessorDialog.java  Dec 20, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.dialog;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AccessorDialog extends RefactoringDialog {
    private JTextField getterField;
    private JTextField setterField;
    private String getterName;
    private String setterName;

    public AccessorDialog(JFrame frame, String title, String name) {
        super(frame, title, "Create accessors of field " + name + ":");
        int index = 0;
        while (name.charAt(index) == '_') {
            index++;
        }
        String topChar = name.substring(index, index + 1);
        String suffixName = topChar.toUpperCase() + name.substring(index + 1);

        getterField.setText("get" + suffixName);
        setterField.setText("set" + suffixName);
        setVisible(true);
    }

    protected JPanel createCenterPane() {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        getterField = createInputTextField(box, "Getter name: ");
        getterField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (setterField.getText().compareTo("") == 0) {
                    setterField.requestFocus();
                } else {
                    okButton.requestFocus();
                }
            }
        });

        setterField = createInputTextField(box, "Setter name: ");
        setterField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (getterField.getText().compareTo("") == 0) {
                    getterField.requestFocus();
                } else {
                    okButton.requestFocus();
                }
            }
        });

        getterField.requestFocus();
        return box;
    }

    protected void okAction() {
        getterName = getterField.getText();
        setterName = setterField.getText();

        if (getterName.equals("") || setterName.equals("")
          || getterName.indexOf(" ") != -1 || setterName.indexOf(" ") != -1) {
            JOptionPane.showMessageDialog(frame, "Invalid string: \""
              + getterName + "\" and/or \"" + setterName + "\"");
        } else {
            setVisible(false);
            dispose();
        }
    }

    protected void cancelAction() {
        getterName = null;
        setterName = null;
        setVisible(false);
        dispose();
    }

    public static AccessorDialog show(JFrame frame, String title, String name) {
        AccessorDialog dialog = new AccessorDialog(frame, title, name);
        return dialog;
    }

    public String getSetterName() {
        return setterName;
    }

    public String getGetterName() {
        return getterName;
    }

    public static void main(String args[]) {
        JFrame mainFrame = new JFrame();
        mainFrame.setSize(100, 100);
        mainFrame.setVisible(true);

        AccessorDialog dialog = AccessorDialog.show(mainFrame, "Accessor", "name");
        System.out.println("Getter = " + dialog.getGetterName());
        System.out.println("Setter = " + dialog.getSetterName());
        System.exit(0);
    }
}
