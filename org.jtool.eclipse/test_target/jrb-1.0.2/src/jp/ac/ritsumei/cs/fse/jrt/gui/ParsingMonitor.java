/*
 *     ParsingMonitor.java  Jan 7, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ParsingMonitor extends JDialog {
    private JFrame frame;
    private Container contentPane;
    private JOptionPane optionPane;
    private JLabel noteLabel;
    private JProgressBar progressBar;
    private boolean cancel = false;

    public ParsingMonitor(JFrame frame, String mesg, int min, int max) {
        this(frame, mesg);
        setMaximum(max);
        setMinimum(min);
    }

    public ParsingMonitor(JFrame frame, String mesg) {
        super(frame, "Progress...", true);
        this.frame = frame;
        createPanes(mesg);
    }

    public void setMaximum(int m) {
        progressBar.setMaximum(m);
    }

    public void setMinimum(int m) {
        progressBar.setMinimum(m);
    }
    
    public void setNote(String note) {
        noteLabel.setText(note);
    }

    public void setProgress(int nv) {
        if (nv >= progressBar.getMaximum()) {
            close();
        } else {
            progressBar.setValue(nv);
        }
    }

    public void createPanes(String mesg) {
        contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());

        noteLabel = new JLabel();
        progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
        progressBar.setValue(0);
        progressBar.setBorderPainted(true);

        Object[] message = new Object[3];
        message[0] = mesg;
        message[1] = noteLabel;
        message[2] = progressBar;
       
        Object[] options = new Object[1];
        options[0] = "Cancel";

        optionPane = new JOptionPane(message,
          JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options);
        optionPane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) {
                    cancel = true;
                    setVisible(false);
                    dispose();
                }
            }
        });
        contentPane.add(optionPane, "Center");

        this.pack();
        this.setSize(600, 150);
        this.setLocationRelativeTo(frame);
        this.validate();
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                cancel = true;
                setVisible(false);
                dispose();
            }
        });

    }

    public void close() {
        setVisible(false);
        dispose();
    }

    public boolean isCanceled() {
        return cancel;
    }
}
