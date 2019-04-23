/*
 *     FindDialog.java  Nov 3, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class FindDialog extends JDialog {
    private JFrame frame;
    private TextPane textPane;
    private Container contentPane;
    private Font font;
    private JTextField wordField;
    private JButton findButton;

    private int direction;
    private static final int UPWARD = 1;
    private static final int DOWNWARD = 2;
    private int lastFoundLocation = -1;

    public FindDialog(JFrame frame, TextPane pane) {
        super(frame, "Search", true);
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

        JPanel panel = new JPanel();
        wordField = createInputTextField(panel, "Serch word:");
        wordField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                findText();
            }
        });
        leftPanel.add(panel, "North");

        panel = new JPanel();
        createFindCloseButtons(panel);
        leftPanel.add(panel, "South");

        panel = new JPanel();
        createUpDownButtons(panel);
        contentPane.add(panel, "East");

        this.pack();
        this.validate();
        wordField.requestFocus();
    }

    private JTextField createInputTextField(JPanel panel, String title) {
        JLabel label = new JLabel(title); 
        panel.add(label);

        JTextField textField = new JTextField(20);
        textField.setEditable(true);
        textField.setFont(font);
        textField.setBackground(Color.white);
        textField.setText("");
        panel.add(textField);

        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent evt) {
                updateFindButtonState();
            }
            public void removeUpdate(DocumentEvent evt) {
                updateFindButtonState();
            }
            public void changedUpdate(DocumentEvent evt) {
            }
        });

        return textField;
    }

    private void updateFindButtonState() {
        if (wordField.getText().compareTo("") != 0) {
            findButton.setEnabled(true);
        } else {
            findButton.setEnabled(false);
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

        direction = DOWNWARD;
        downButton.setSelected(true);
    }

    public int getDirection() {
        return direction;
    }

    private void createFindCloseButtons(JPanel panel) {
        findButton = new JButton("Find");
        findButton.setEnabled(false);
        findButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                findText();
            }
        });

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setVisible(false);
                dispose();
            }
        });

        panel.add(findButton);
        panel.add(closeButton);
    }

    private void findText() {
        int position = lastFoundLocation;
        if (getDirection() == UPWARD) {
            if (position == -1) {
                position = textPane.getCaretPosition() + 1;
            }
            position = textPane.findUpward(wordField.getText(), position - 1);
        } else {
            if (position == -1) {
                position = textPane.getCaretPosition() - 1;
            }
            position = textPane.findDownward(wordField.getText(), position + 1);
        }
        
        if (position != -1) {
            lastFoundLocation = position;
        } else {
            JOptionPane.showMessageDialog(frame, "Not found: " + wordField.getText() + ".");
        }
    }
}
