/*
 *     ReplaceDialog.java  Nov 3, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class ReplaceDialog extends JDialog {
    private JFrame frame;
    private TextPane textPane;
    private Container contentPane;
    private Font font;
    private JTextField word1Field;
    private JTextField word2Field;
    private JButton replaceButton;
    private JButton replaceAllButton;

    private int direction = UPWARD;
    private static final int UPWARD = 1;
    private static final int DOWNWARD = 2;
    private int lastFoundLocation = -1;

    public ReplaceDialog(JFrame frame, TextPane pane) {
        super(frame, "Replace", true);
        this.frame = frame;
        textPane = pane;

        createPanes();
    }

    private void createPanes() {
        contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        font = new Font("Dialog", Font.PLAIN, 14);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        contentPane.add(leftPanel, "Center");

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        leftPanel.add(box, "North");

        word1Field = createInputTextField(box, "Replacing word:");
        word1Field.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (word2Field.getText().compareTo("") == 0) {
                    word2Field.requestFocus();
                } else {
                    replaceButton.requestFocus();
                }
            }
        });

        word2Field = createInputTextField(box, "Replaced word: ");
        word2Field.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (word1Field.getText().compareTo("") == 0) {
                    word1Field.requestFocus();
                } else {
                    replaceButton.requestFocus();
                }
            }
        });

        JPanel panel = new JPanel();
        createReplaceCloseButtons(panel);
        leftPanel.add(panel, "South");

        panel = new JPanel();
        createUpDownButtons(panel);
        contentPane.add(panel, "East");

        this.pack();
        this.validate();
        word1Field.requestFocus();
    }

    private JTextField createInputTextField(JPanel box, String title) {
        JPanel panel = new JPanel();
        JLabel label = new JLabel(title); 
        panel.add(label);

        JTextField textField = new JTextField(20);
        textField.setEditable(true);
        textField.setFont(font);
        textField.setBackground(Color.white);
        textField.setText("");
        panel.add(textField);
        box.add(panel);

        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent evt) {
                updateReplaceButtonState();
            }
            public void removeUpdate(DocumentEvent evt) {
                updateReplaceButtonState();
            }
            public void changedUpdate(DocumentEvent evt) {
            }
        });

        return textField;
    }

    private void updateReplaceButtonState() {
        if (word1Field.getText().compareTo("") != 0
          && word2Field.getText().compareTo("") != 0) {
            replaceButton.setEnabled(true);
            replaceAllButton.setEnabled(true);
        } else {
            replaceButton.setEnabled(false);
            replaceAllButton.setEnabled(false);
        }
    }

    private void createUpDownButtons(JPanel panel) {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        TitledBorder border = new TitledBorder("Direction");
        border.setTitleFont(font);
        panel.setBorder(border);

        ButtonGroup group = new ButtonGroup();

        JRadioButton upButton = new JRadioButton("Upward");
        upButton.setMnemonic('U');
        panel.add(upButton);
        group.add(upButton);
        upButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                direction = UPWARD;
            }
        });

        JRadioButton downButton = new JRadioButton("Downward");
        downButton.setMnemonic('D');
        panel.add(downButton);
        group.add(downButton);
        downButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                direction = DOWNWARD;
            }
        });

        upButton.setSelected(true);
    }

    public int getDirection() {
        return direction;
    }

    private void createReplaceCloseButtons(JPanel panel) {
        replaceButton = new JButton("Replace");
        replaceButton.setEnabled(false);
        replaceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                replaceText();
            }
        });

        replaceAllButton = new JButton("Replace All");
        replaceAllButton.setEnabled(false);
        replaceAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                replaceTextAll();
                setVisible(false);
                dispose();
            }
        });

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setVisible(false);
                dispose();
            }
        });

        panel.add(replaceButton);
        panel.add(replaceAllButton);
        panel.add(closeButton);
    }

    private void replaceText() {
        int position = lastFoundLocation;
        if (getDirection() == UPWARD) {
            if (position == -1) {
                position = textPane.getCaretPosition() - 1;
            }
            position = textPane.findUpward(word1Field.getText(), position + 1);
        } else {
            if (position == -1) {
                position = textPane.getCaretPosition() + 1;
            }
            position = textPane.findDownward(word1Field.getText(), position - 1);
        }
        
        if (position != -1) {
            int start = textPane.getTextArea().getSelectionStart();
            int end = textPane.getTextArea().getSelectionEnd();
            textPane.getTextArea().replaceRange(word2Field.getText(), start, end);
            lastFoundLocation = position;
        } else {
            JOptionPane.showMessageDialog(frame, "Not found: " + word1Field.getText() + ".");
        }
    }

    private void replaceTextAll() {
        boolean isReplace = false;
        int position = 0;
        while (position != -1) {
            position = textPane.findUpward(word1Field.getText(), position);
            if (position != -1) {
                int start = textPane.getTextArea().getSelectionStart();
                int end = textPane.getTextArea().getSelectionEnd();
                textPane.getTextArea().replaceRange(word2Field.getText(), start, end);
                position = end;
                isReplace = true;
            }
        }
        
        if (!isReplace) {
            JOptionPane.showMessageDialog(frame, "Not found: " + word1Field.getText() + " in this text.");
        }
    }
}
