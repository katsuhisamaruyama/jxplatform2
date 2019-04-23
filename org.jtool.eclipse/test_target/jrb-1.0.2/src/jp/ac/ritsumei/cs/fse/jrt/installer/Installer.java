/*
 *     Installer.java  Jan 25, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.installer;
import jp.ac.ritsumei.cs.fse.jrt.gui.JRBProperties;
import java.util.Properties;
import javax.swing.*;
import java.io.*;

public class Installer {
    private String cwd;
    private static String BASENAME = "jrb";
    private static String JARNAME = BASENAME + ".jar";
    private static String JRBNAME = "jp.ac.ritsumei.cs.fse.jrt.gui.JRB";

    public Installer() {
    }

    private void run() {
        try {
            File file = new File(".");
            cwd = file.getCanonicalPath();
        } catch (IOException e) {
                System.exit(1);
        }
        createPropertiesFile();
        createBatchFile();
    }

    private void createPropertiesFile() {
        Properties properties = new Properties();
        properties.setProperty("Application.Dir", cwd);
        String fileName = cwd + JRBProperties.getPropertyName();
        fileName = fileName.replace('/', File.separatorChar); 

        OutputStream os = null;
        try {
            os = new FileOutputStream(fileName);
            properties.store(os, "Java Refactoring Browser properties for "
              + System.getProperty("os.name") + " (uncomplete version)");
        } catch (IOException e) {
        } finally {
            try {
                if (os != null) {
                    os.close();
                } else {
                    System.exit(1);
                }
            } catch (IOException e) { }
        }
    }
    
    private void createBatchFile() {
        String javaCommand = "java";
        String classPath = cwd + File.pathSeparator + cwd + File.separator + JARNAME;
        String content = javaCommand + " -classpath \"" + classPath + "\" " + JRBNAME;
        
        String fileName = BASENAME;
        if (System.getProperty("os.name").startsWith("Windows")) {
            fileName = fileName + ".bat";
        }

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(fileName);
            fileWriter.write(content);
        } catch (IOException e) {
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                } else {
                    System.exit(1);
                }
            } catch (IOException e) { }
        }
    }

    public static void main(String[] args) {
        Installer installer = new Installer();
        installer.run();

        if (System.getProperty("os.name").startsWith("Windows")) {
            // JOptionPane.showMessageDialog(null, "Enjoy refactoring by using JRB!");
        } else {
            System.out.println("Enjoy refactoring by using JRB!");
        }
        System.exit(0);
    }
}
