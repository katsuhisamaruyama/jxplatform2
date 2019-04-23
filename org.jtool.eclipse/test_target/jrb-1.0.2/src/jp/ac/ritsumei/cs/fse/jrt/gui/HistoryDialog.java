/*
 *     HistoryDialog.java  Jan 15, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class HistoryDialog extends JDialog {
    protected JFrame frame;
    protected Container contentPane;
    protected Font font;
    protected JTextArea textArea;

    protected HistoryDialog(JFrame frame, String title, boolean modal) {
        super(frame, title, modal);
    }

    public HistoryDialog(JFrame frame) {
        super(frame, "History", true);
        this.frame = frame;

        contentPane = this.getContentPane();
        font = new Font("Dialog", Font.PLAIN, 12);

        JScrollPane scPane = createTextPane(15, 50);
        contentPane.add(scPane, "Center");

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        createCloseButton(panel);
        contentPane.add(panel, "South");

        this.pack();
        this.validate();
    }

    public void setText(String text) {
        if (text.length() == 0) {
            text = "---- Empty ----";
        }
        textArea.setText(text);
    }

    protected JScrollPane createTextPane(int line, int column) {
        textArea = new JTextArea(line, column);
        textArea.setText("");
        textArea.setEditable(false);
        textArea.setFont(font);
        textArea.setBackground(Color.white);
        textArea.setMargin(new Insets(5, 5, 5, 5));

        JScrollPane scTextArea = new JScrollPane(textArea);
        scTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scTextArea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        return scTextArea;
    }

    private void createCloseButton(JPanel panel) {
        JButton closeButton = new JButton("Close");
        panel.add(closeButton);
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setVisible(false);
                dispose();
            }
        });

        closeButton.requestFocus();
    }
}
