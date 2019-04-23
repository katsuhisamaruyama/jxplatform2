/*
 *     JRB.java  Nov 24, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import jp.ac.ritsumei.cs.fse.jrt.parser.JavaModelFactory;
import javax.swing.*;
import java.awt.event.*;
import java.util.Locale;
import java.io.File;

public class JRB {
    private MainFrame frame;

    public JRB() {
        loadProperies();
        setPathOptions();

        frame = new MainFrame(this);
        frame.pack();
        frame.validate();
        frame.setVisible(true);
        frame.init();
    }

    private void loadProperies() {
        if (!JRBProperties.load() || JRBProperties.getProperty("Application.Dir") == null) {
            JOptionPane.showMessageDialog(frame,
              "Fail to load properties. Please re-install JRB.");
            System.exit(0);
        }

        File app = new File(JRBProperties.getProperty("Application.Dir"));
        if (!app.isDirectory()) {
            JOptionPane.showMessageDialog(frame,
              "Not found the specified application directory: " + app.getPath() + ".");
            System.exit(0);
        }
    }

    private void setPathOptions() {
        while (!getProperty()) {
            OptionsDialog optionsDialog = new OptionsDialog(null);
            optionsDialog.show();
            optionsDialog.dispose();
        }
        JavaModelFactory.getInstance().setJDKFile(JRBProperties.getProperty("JDK.File"));
    }

    private boolean getProperty() {
        String JDKFile = checkedFileName(JRBProperties.getProperty("JDK.File"));
        String rootDir = checkedFileName(JRBProperties.getProperty("Root.Dir"));
        if (JDKFile != null && rootDir != null) {
            return true;
        }

        if (JDKFile == null) {
            JRBProperties.setProperty("JDK.File", "");
        }
        if (rootDir == null) {
            JRBProperties.setProperty("Root.Dir", "");
        }
        return false;
    }

    private String checkedFileName(String fileName) {
        if (fileName != null) {
            File file = new File(fileName);
            if (file != null && file.exists()) {
                return fileName;
            }
        }
        return null;
    }

    public void terminated() {
        boolean result = JRBProperties.store();
        if (!result) {
            JOptionPane.showMessageDialog(frame, "Fail to store properties.");
        }
        result = RefactoringHistory.getInstance().store();
        if (!result) {
            JOptionPane.showMessageDialog(frame, "Fail to store the history.");
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        /*
        if (System.getProperty("os.name").startsWith("Windows")) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch(Exception e) { }
        }
        */
        Locale.setDefault(Locale.ENGLISH);

        JRB browser = new JRB();
    }
}
