/*
 *     TabbedTextPane.java  Oct 25, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import jp.ac.ritsumei.cs.fse.jrt.parser.JavaModelFactory;
import jp.ac.ritsumei.cs.fse.jrt.model.JavaFile;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;
import java.awt.Point;
import java.awt.event.*;

public class TabbedTextPane extends JTabbedPane
  implements ChangeListener, MouseListener {
    private JFrame frame;
    private MenuPane menuPane;
    private FileSystemPane fileSystemPane;
    private MethodListPane methodListPane;
    private JavaModelFactory modelFactory = JavaModelFactory.getInstance();
    private List texts = new ArrayList();  // textPane;
    private int selectedIndex = 0;
    private ProgressMonitor progressMonitor;
    private JPopupMenu popupMenu;
    private RefactoringMenu refactoringMenu;
    private RefactoringHistory history;

    public TabbedTextPane(JFrame frame) {
        super(JTabbedPane.TOP);
        this.frame = frame;

        popupMenu = new JPopupMenu("Refactoring");
        CutCopyPasteMenu ccp = new CutCopyPasteMenu(popupMenu, this);
        refactoringMenu = new RefactoringMenu(frame, popupMenu, this);

        history = RefactoringHistory.getInstance();

        init();
        this.addChangeListener(this);
    }

    public void setMenuPane(MenuPane pane) {
        menuPane = pane;
    }

    public void setFileSystemPane(FileSystemPane pane) {
        fileSystemPane = pane;
    }

    public void setMethodListPane(MethodListPane pane) {
        methodListPane = pane;
    }

    public JFrame getFrame() {
        return frame;
    }

    public void init() {
        TextPane textPane = new TextPane(frame);
        textPane.getTextArea().addMouseListener(this);
        textPane.setMenuPane(menuPane);

        this.addTab("No Title", null, textPane, "No Title");
        texts.add(textPane);
    }

    public TextPane newFile(String name) {
        return openFile(name, "");
    }

    public TextPane openFile(String name, String content) {
        TextPane textPane = new TextPane(frame, name);
        textPane.getTextArea().addMouseListener(this);
        textPane.setMenuPane(menuPane);
        textPane.setText(content);
        textPane.setChange(false);
        textPane.getUndoManager().discardAllEdits();

        if (content.length() != 0) {
            ((MainFrame)frame).getFileManager().addRecentFile(name);
        }

        this.addTab(textPane.getTabName(), null, textPane, name);
        this.setSelectedComponent(textPane);
        texts.add(textPane);

        TextPane firstPane = (TextPane)texts.get(0);
        if (firstPane.isNoTitle() && !firstPane.hasChanged()) {
            texts.remove(0);
            this.remove(0);
        }
        return textPane;
    }

    public void saveFile(TextPane textPane) {
        history.memorize(textPane);
    }

    public void closeFile() {
        texts.remove(selectedIndex);
        this.remove(selectedIndex);
    }

    public String readFile(String name) throws FileNotFoundException, IOException {
        StringBuffer content;
        
        File file = new File(name);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        content = new StringBuffer();
        while (reader.ready()) {
            String oneLineString = reader.readLine();
            if (oneLineString == null) {
                break;
            }
            content.append(oneLineString + "\n");
        }
        reader.close();
        return new String(content);
    }

    public boolean isOpen(String name) {
        if (getTextPane(name) != null) {
            return true;
        }
        return false;
    }

    public boolean hasChanged(String name) {
        return getTextPane(name).hasChanged();
    }

    public TextPane getTextPane(String name) {
        Iterator it = texts.iterator();
        while (it.hasNext()) {
            TextPane textPane = (TextPane)it.next();
            if (name.compareTo(textPane.getFileName()) == 0) {
                return textPane;
            }
        }
        return null;
    }

    public void updateTab() {
        TextPane textPane = getCurrentTextPane();
        setTitleAt(selectedIndex, textPane.getTabName());
        setToolTipTextAt(selectedIndex, textPane.getFileName());
    }

    public int getOpenFileNum() {
        return texts.size();
    }

    public List getTexts() {
        return texts;
    }

    public void stateChanged(ChangeEvent evt) {
        JTabbedPane src = (JTabbedPane)evt.getSource();
        selectedIndex = src.getSelectedIndex();

        updateUndoState();
        if (getCurrentTextPane() != null) {
            showMethodList(getCurrentTextPane());
        }
    }

    public TextPane getCurrentTextPane() {
        if (texts.size() != 0 && selectedIndex < texts.size()) {
            TextPane textPane = (TextPane)texts.get(selectedIndex);
            return textPane;
        }
        return null;
    }

    public void showMethodList() {
        showMethodList(getCurrentTextPane());
    }

    public void showMethodList(TextPane textPane) {
        if (textPane != null) {
            methodListPane.buildMethodList(textPane);
        }
    }

    public void updateUndoState() {
        if (getCurrentTextPane() != null) {
            TextUndoManager undoManager = getCurrentTextPane().getUndoManager();

            menuPane.updateUndoState(undoManager);
            menuPane.updateRedoState(undoManager);
        }
    }

    public void mousePressed(MouseEvent evt) {
        showPopup(evt);
    }

    public void mouseReleased (MouseEvent evt) {
        showPopup(evt);
    }

    public void mouseClicked (MouseEvent evt) {
        showPopup(evt);
    }

    public void mouseEntered(MouseEvent evt) {
    }

    public void mouseExited(MouseEvent evt) {
    }

    private void showCursor(Point point) {
        TextPane textPane = getCurrentTextPane();
        textPane.showCursor(point);
    }

    private void showPopup(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            Point point = evt.getPoint();
            showCursor(point);

            refactoringMenu.setRefactoringMenu(point);
            popupMenu.show(evt.getComponent(), point.x, point.y + 10);
        }
    }

    public void removeAllParsedSummaryFiles() {
        modelFactory.removeAllParsedSummaryFiles();
    }

    public void parseSummaryAllFiles() {
        final List files = fileSystemPane.getFileList();
        final ParsingMonitor monitor = new ParsingMonitor(frame,
          "Parsing Java files:", 0, files.size());

        Thread parser = new Thread() {
            private String name;
            private int num = 1;

            public void run() {
                Iterator it = files.iterator();
                while (it.hasNext()) {
                    name = (String)it.next();
                    parseSummaryFile(name);

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            monitor.setNote(name);
                            monitor.setProgress(num);
                        }
                    });
                    num++;
                }       
                monitor.close();
            }
        };
        parser.start();
        monitor.setVisible(true);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) { }
    }

    public void parseSummaryFiles() {
        boolean parsed = false;
        List fileList = fileSystemPane.getFileList();

        Iterator it = fileList.iterator();
        while (it.hasNext()) {
            String name = (String)it.next();
            boolean check = parseSummaryFile(name);
            parsed = parsed || check;
        }
        if (parsed) {
            modelFactory.collectInformation();
        }
    }

    private boolean parseSummaryFile(String name) {
        if (!modelFactory.existsInParsedSummaryFiles(name)) {
            TextPane textPane = getTextPane(name);
            if (textPane != null) {
                modelFactory.parseEachSummaryFile(name, textPane.getText());
            } else {
                modelFactory.parseEachSummaryFile(name);
            }
            return true;
        }
        return false;
    }
}
