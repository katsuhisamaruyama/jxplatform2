/*
 *     FileManager.java  Oct 22, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import jp.ac.ritsumei.cs.fse.jrt.util.SimpleEventSource;
import jp.ac.ritsumei.cs.fse.jrt.util.LogEvent;
import javax.swing.*;
import java.io.*;
import java.util.*;

public class FileManager extends SimpleEventSource {
    private JFrame frame;
    private TabbedTextPane tabbedTextPane;
    private FileSystemPane fileSystemPane;
    private File directory;
    static private final int RECENT_FILES = 5;
    private List recentFiles = new ArrayList();

    public FileManager(JFrame frame) {
        this.frame = frame;
        directory = new File(JRBProperties.getProperty("Root.Dir"));
    }

    public void setTabbedTextPane(TabbedTextPane pane) {
        tabbedTextPane = pane;
    }

    public void setFileSystemPane(FileSystemPane pane) {
        fileSystemPane = pane;
    }

    private boolean fileExists(String name) {
        File file = new File(name);
        return file.exists();
    }

    private String getFileName() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("File Chooser");
        chooser.addChoosableFileFilter(
          new ExtensionFileFilter("Java Source Files (*.java)", ".java"));
        chooser.setCurrentDirectory(directory);

        int result = chooser.showOpenDialog(frame);
        File file = chooser.getSelectedFile();
        if (file != null && result == JFileChooser.APPROVE_OPTION) {
            directory = chooser.getCurrentDirectory();
            return file.getPath();
        }
        return null;
    }

    public TextPane newFile() {
        String name = getFileName();
        if (name != null) {
            if (fileExists(name)) {
                JOptionPane.showMessageDialog(frame, "File exits: " + name + ".");
                return null;
            }
            TextPane textPane = tabbedTextPane.newFile(name);

            fireLogEvent(new LogEvent(this, "New file: " + textPane.getFileName()));
            return textPane;
        }
        return null;
    }

    public TextPane openFile() {
        String name = getFileName();
        if (name != null) {
            TextPane textPane = openFile(name);
            return textPane;
        }
        return null;
    }

    public TextPane openFile(String name) {
        TextPane textPane;
        if (tabbedTextPane.isOpen(name)) {
            textPane = tabbedTextPane.getTextPane(name);
            if (!textPane.hasChanged()) {
                JOptionPane.showMessageDialog(frame, "Already opened: " + name + ".");
                return textPane;
            } else {
                int result = JOptionPane.showConfirmDialog(frame,
                  "The changes were made to this file. Do you want to re-open the file?",
                  "Select an Option", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    textPane.setText(readFile(name));
                    tabbedTextPane.setSelectedComponent(textPane);
                    tabbedTextPane.showMethodList(textPane);
                } else {
                    return textPane;
                }
            }
        } else {
            textPane = tabbedTextPane.openFile(name, readFile(name));
            tabbedTextPane.showMethodList(textPane);
        }

        addRecentFile(name);

        textPane.setCaretPosition(0);
        textPane.validate();
        textPane.setChange(false);
        File file = new File(name);
        textPane.setLastModified(file.lastModified());
        textPane.getUndoManager().discardAllEdits();

        fireLogEvent(new LogEvent(this, "Open file: " + textPane.getFileName()));
        return textPane;
    }

    private String readFile(String name) {
        try {
            return new String(tabbedTextPane.readFile(name));
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(frame, "Failed to open: " + name + ".");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Failed to read: " + name + ".");
        }
        return new String("");
    }            

    public void renameFile() {
        TextPane textPane = tabbedTextPane.getCurrentTextPane();
        String name = new String(textPane.getFileName());

        File file = new File(name);
        if (file.exists()) {
            textPane = saveAsFile();
            if (textPane == null) {
                return;
            }
            file.delete();

        } else {
            String newName = getFileName();
            textPane.setFileName(newName);
        }
        
        fileSystemPane.buildFileTree();
        fireLogEvent(new LogEvent(this, "Rename file: "
          + "from " + name + " to " + textPane.getFileName()));
    }

    public void closeFile() {
        TextPane textPane = tabbedTextPane.getCurrentTextPane();
        closeFile(textPane);

        fireLogEvent(new LogEvent(this, "Close file: " + textPane.getFileName()));
    }

    public void closeFiles() {
        List texts = new ArrayList(tabbedTextPane.getTexts());
        Iterator it = texts.iterator();
        while (it.hasNext()) {
            TextPane textPane = (TextPane)it.next();
            if (!closeFile(textPane)) {
                return;
            }
        }
    }        

    private boolean closeFile(TextPane textPane) {
        tabbedTextPane.setSelectedComponent(textPane);
        if (textPane.hasChanged()) {
            int result = JOptionPane.showConfirmDialog(frame,
              "Do you want to save the changes you have made to this file?");
            if (result == JOptionPane.CANCEL_OPTION) {
                return false;
            }
            if (result == JOptionPane.YES_OPTION) {
                saveFile(textPane, textPane.getFileName());
            }
        }

        textPane.close();
        tabbedTextPane.closeFile();

        if (tabbedTextPane.getOpenFileNum() == 0) {
            ((MainFrame)frame).terminated();
        }
        return true;
    }

    public TextPane saveFile() {
        boolean result;
        TextPane textPane = tabbedTextPane.getCurrentTextPane();
        if (textPane.isNoTitle() && textPane.hasChanged()) {
            result = saveAsFile(textPane);
        } else {
            result = saveFile(textPane, textPane.getFileName());
        }

        if (result) {
            tabbedTextPane.showMethodList(textPane);
            fireLogEvent(new LogEvent(this, "Save file: "
              + textPane.getFileName()));
            return textPane;
        }
        return null;
    }

    public TextPane saveAsFile() {
        TextPane textPane = tabbedTextPane.getCurrentTextPane();
        String name = textPane.getFileName();

        if (saveAsFile(textPane)) {
            fileSystemPane.buildFileTree();
            tabbedTextPane.showMethodList(textPane);
            fireLogEvent(new LogEvent(this, "Save file: "
              + name + " as " + textPane.getFileName()));
            return textPane;
        }
        return null;
    }

    private boolean saveAsFile(TextPane textPane) {
        String name = getFileName();
        if (name != null) {
            if (saveFile(textPane, name)) {
                textPane.setFileName(name);
                tabbedTextPane.updateTab();
                addRecentFile(name);
                return true;
            }
        }
        return false;
    }

    private boolean saveFile(TextPane textPane, String name) {
        File file = new File(name);

        if (file.exists() && file.lastModified() != textPane.getLastModified()) {
            int result = JOptionPane.showConfirmDialog(frame,
              "The changes were made to a file on disk. Do you want to save this file?",
              "Select an Option", JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) {
                return false;
            }
        }

        try {
            FileWriter writer = new FileWriter(file);
            writer.write(textPane.getText());
            writer.close();
            
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(frame, "Failed to open: " + name + ".");
            return false;

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Failed to write: " + name + ".");
            return false;
        }

        textPane.setChange(false);
        textPane.getUndoManager().discardAllEdits();
        textPane.setLastModified(file.lastModified());
        tabbedTextPane.saveFile(textPane);

        return true;
    }

    public void insertFile() {
        String name = getFileName();        
        if (name != null) {
            TextPane textPane = tabbedTextPane.getCurrentTextPane();
            textPane.insert(readFile(name));
        }
    }

    public void addRecentFile(String name) {
        int offset = recentFiles.indexOf(name);
        if (offset == -1) {
            recentFiles.add(0, name);
            if (recentFiles.size() > RECENT_FILES) {
                recentFiles.remove(RECENT_FILES);
            }
        } else {
            recentFiles.remove(offset);
            recentFiles.add(0, name);
        }
    }

    public List getRecentFiles() {
        return recentFiles;
    }

    public void buildFileTree() {
        fileSystemPane.buildFileTree();
    }
}
