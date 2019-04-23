/*
 *     WarningDialog.java  Nov 3, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import jp.ac.ritsumei.cs.fse.jrt.util.WarningEvent;
import jp.ac.ritsumei.cs.fse.jrt.util.WarningEventListener;
import javax.swing.*;
import java.io.*;

public class WarningDialog extends JDialog implements WarningEventListener {
    private JFrame frame;
    private boolean action = false;

    public void setAction(boolean bool) {
        action = bool;
    }

    public WarningDialog(JFrame frame) {
        super(frame, "Log", true);
        this.frame = frame;
    }

    public void printMessage(WarningEvent evt) {
        if (action) {
            String mesg = evt.getMessage();
            if (mesg.indexOf("\n") != -1) {
                mesg = mesg.substring(0, mesg.indexOf("\n"));
            }
            JOptionPane.showMessageDialog(frame, mesg, null, JOptionPane.ERROR_MESSAGE);
        }
    }
}
