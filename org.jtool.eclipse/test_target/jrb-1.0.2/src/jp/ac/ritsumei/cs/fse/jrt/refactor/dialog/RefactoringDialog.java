/*
 *     RefactoringDialog.java  Dec 20, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.dialog;
import jp.ac.ritsumei.cs.fse.jrt.gui.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class RefactoringDialog extends JDialog {
    protected JFrame frame;
    protected Container contentPane;
    protected JButton okButton;
    protected JButton cancelButton;

    protected RefactoringDialog(JFrame frame, String title, String mesg) {
        super(frame, title, true);
        this.frame = frame;
        createPanes(mesg, null);
    }

    protected RefactoringDialog(JFrame frame, String title, String mesg, String dir) {
        super(frame, title, true);
        this.frame = frame;
        createPanes(mesg, dir);
    }

    protected void createPanes(String mesg, String dir) {
        contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());

        JPanel topPane = new JPanel();
        JLabel label = new JLabel(mesg, JLabel.CENTER);
        label.setForeground(Color.black);
        topPane.add(label);
        contentPane.add(topPane, "North");

        JPanel centerPane;
        if (dir == null) {
            centerPane = createCenterPane();
        } else {
            centerPane = createCenterPane(dir);
        }
        contentPane.add(centerPane, "Center");

        JPanel bottomPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        createOkButton(bottomPane);
        createCancelButton(bottomPane);
        contentPane.add(bottomPane, "South");

        Point p = frame.getLocationOnScreen();
        this.pack();
        this.setLocation(p.x + frame.getSize().width - this.getSize().width - 10, p.y + 10);
        this.validate();
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                cancelAction();
            }
        });
    }

    protected JPanel createCenterPane() {
        return new JPanel();
    }

    protected JPanel createCenterPane(final String dir) {
        return new JPanel();
    }

    protected JTextField createTextField(JPanel panel, String title) {
        JLabel label = new JLabel(title);
        panel.add(label);

        JTextField textField = new JTextField(20);
        textField.setEditable(true);
        textField.setFont(new Font("Dialog", Font.PLAIN, 14));
        textField.setBackground(Color.white);
        textField.setText("");
        panel.add(textField);

        return textField;
    }

    protected JTextField createInputTextField(JPanel box, String title) {
        JPanel panel = new JPanel();
        JTextField textField = createTextField(panel, title);

        box.add(panel);
        return textField;
    }

    protected JTextField createInputTextField(JPanel box, String title, JButton button) {
        JPanel panel = new JPanel();
        JTextField textField = createTextField(panel, title);

        panel.add(button);
        box.add(panel);
        return textField;
    }

    protected JTextField createInputTextField(JPanel box, String title, int space) {
        JPanel panel = new JPanel();
        JTextField textField = createTextField(panel, title);
        panel.add(Box.createHorizontalStrut(space));

        box.add(panel);
        return textField;
    }

    protected void createOkButton(JPanel panel) {
        okButton = new JButton("Ok");
        panel.add(okButton);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                okAction();
            }
        });
    }

    protected void okAction() {
    }

    protected void createCancelButton(JPanel panel) {
        cancelButton = new JButton("Cancel");
        panel.add(cancelButton);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cancelAction();
            }
        });
    }

    protected void cancelAction() {
    }
}
