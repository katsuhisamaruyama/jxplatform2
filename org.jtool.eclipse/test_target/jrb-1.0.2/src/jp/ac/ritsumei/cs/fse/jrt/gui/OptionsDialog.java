/*
 *     OptionsDialog.java  Oct 25, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class OptionsDialog extends JDialog {
    private JFrame frame;
    private Container contentPane;
    private Font font;
    private JTextField jdkFile;
    private JTextField rootDir;
    private JButton okButton;
    private JButton cancelButton;

    public OptionsDialog(JFrame frame) {
        super(frame, "Options", true);
        this.frame = frame;
        createPanes();
    }

    private void createPanes() {
        contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        font = new Font("Dialog", Font.PLAIN, 14);

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        jdkFile = createOptionTextField(box,
                    "JDK file location:", JFileChooser.FILES_ONLY);
        jdkFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (rootDir.getText().compareTo("") == 0) {
                    rootDir.requestFocus();
                } else {
                    okButton.requestFocus();
                }
            }
        });

        rootDir = createOptionTextField(box,
                    "Browse root directory:", JFileChooser.DIRECTORIES_ONLY);
        rootDir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (jdkFile.getText().compareTo("") == 0) {
                    jdkFile.requestFocus();
                } else {
                    okButton.requestFocus();
                }
            }
        });
        contentPane.add(box, "Center");

        JPanel panel = new JPanel();
        createOkCancelButtons(panel);
        contentPane.add(panel, "South");

        this.pack();
        this.validate();

        updateButtonState();
        jdkFile.requestFocus();
    }

    private JTextField createOptionTextField(JPanel box, String title, final int mode) {
        JPanel panel = new JPanel();
        TitledBorder border = new TitledBorder(title);
        border.setTitleFont(font);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(border);

        final JTextField textField = new JTextField(30);
        textField.setEditable(true);
        textField.setFont(font);
        textField.setBackground(Color.white);
        panel.add(textField);
        panel.add(Box.createHorizontalStrut(5));

        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent evt) {
                updateButtonState();
            }
            public void removeUpdate(DocumentEvent evt) {
                updateButtonState();
            }
            public void changedUpdate(DocumentEvent evt) {
            }
        });

        JButton fileButton = new JButton("File");
        panel.add(fileButton);
        box.add(panel);

        fileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showFileChooser(textField, mode);
            }
        });
        /*
        fileButton.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    showFileChooser(textField, mode);
                }
            }
            public void keyReleased(KeyEvent evt) {
            }
            public void keyTyped(KeyEvent evt) {
            }
        });
        */

        return textField;
    }

    private void showFileChooser(JTextField textField, int mode) {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setDialogTitle("File Chooser");
        chooser.setFileSelectionMode(mode);
        File dir = new File(textField.getText());
        if (dir.exists()) {
            chooser.setCurrentDirectory(dir);
        }

        int result = chooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            textField.setText(file.getPath());
        }
    }

    private void createOkCancelButtons(JPanel panel) {
        okButton = new JButton("Ok");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (!checkOption(jdkFile.getText())) {
                    return;
                }
                if (!checkOption(rootDir.getText())) {
                    return;
                }

                JRBProperties.setProperty("JDK.File", jdkFile.getText());
                String dir = JRBProperties.getProperty("Root.Dir");
                JRBProperties.setProperty("Root.Dir", rootDir.getText());
                setVisible(false);

                if (frame != null && dir != null && rootDir.getText().compareTo(dir) != 0) { 
                    ((MainFrame)frame).getFileSystemPane().buildFileTree();
                }
                dispose();
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setVisible(false);
                dispose();
                if (frame == null) {
                    System.exit(0);
                }
            }
        });

        panel.add(okButton);
        panel.add(cancelButton);
    }

    private boolean checkOption(String name) {
        File file = new File(name);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(frame, "Failed to find: " + name + ".");
            return false;
        }
        return true;
    }

    private void updateButtonState() {
        if (rootDir.getText().length() != 0 && jdkFile.getText().length() != 0) {
            okButton.setEnabled(true);
        } else {
            okButton.setEnabled(false);
        }
    }

    public void show() {
        jdkFile.setText(JRBProperties.getProperty("JDK.File"));
        rootDir.setText(JRBProperties.getProperty("Root.Dir"));
        super.show();
    }
}
