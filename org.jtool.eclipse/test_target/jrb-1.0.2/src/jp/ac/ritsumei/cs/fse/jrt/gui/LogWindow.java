/*
 *     LogWindow.java  Nov 3, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import jp.ac.ritsumei.cs.fse.jrt.util.LogEvent;
import jp.ac.ritsumei.cs.fse.jrt.util.LogEventListener;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;

public class LogWindow extends JFrame implements LogEventListener {
    private Container contentPane;
    private Font font;
    private JTextArea textArea;

    public LogWindow() {
        super("Log");

        contentPane = this.getContentPane();
        font = new Font("Dialog", Font.PLAIN, 12);

        JScrollPane scPane = createTextPane(20, 65);
        contentPane.add(scPane, "Center");

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        createCloseButton(panel);
        contentPane.add(panel, "South");

        this.pack();
        this.validate();
    }

    public void setText(String text) {
        textArea.setText(text);
    }

    public void printMessage(LogEvent evt) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        textArea.append(evt.getMessage());        
        textArea.append("  ");
        textArea.append(timestamp.toString());
        textArea.append("\n");
    }

    private JScrollPane createTextPane(int line, int column) {
        contentPane = this.getContentPane();
        font = new Font("Dialog", Font.PLAIN, 12);

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
