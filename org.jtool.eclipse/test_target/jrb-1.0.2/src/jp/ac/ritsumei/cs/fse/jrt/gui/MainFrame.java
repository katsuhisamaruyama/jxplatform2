/*
 *     MainFrame.java  Oct 21, 2001
 *
 *     Tatsuya Kageyama (kage@fse.cs.ritsumei.ac.jp)
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {
    private JRB browser;
    private Container contentPane;
    private FileManager fileManager;
    private PrintManager printManager;
    private LogWindow logWindow;

    private TabbedTextPane tabbedTextPane;
    private FileSystemPane fileSystemPane;
    private MethodListPane methodListPane;
    private MenuPane menuPane;

    public MainFrame(JRB browser) {
        super("Java Refactoring Browser");
        this.browser = browser;
        createPanes();

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                closeWindow();
            }
        });
    }

    private void createPanes() {
        contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());

        fileManager = new FileManager(this);
        printManager = new PrintManager(this);
        logWindow = new LogWindow();

        JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainPane.setContinuousLayout(true);
        mainPane.setDividerSize(2);
        mainPane.setDividerLocation(200);
        contentPane.add(mainPane, "Center");

        tabbedTextPane = new TabbedTextPane(this);
        mainPane.setRightComponent(tabbedTextPane);
        fileManager.setTabbedTextPane(tabbedTextPane);
        fileManager.addLogEventListener(logWindow);

        JSplitPane filePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        filePane.setContinuousLayout(true);
        filePane.setDividerSize(2);
        filePane.setDividerLocation(450);
        mainPane.setLeftComponent(filePane);

        fileSystemPane = new FileSystemPane(this, fileManager, tabbedTextPane);
        filePane.setTopComponent(fileSystemPane);
        fileManager.setFileSystemPane(fileSystemPane);

        methodListPane = new MethodListPane(this);
        filePane.setBottomComponent(methodListPane);
        tabbedTextPane.setMethodListPane(methodListPane);

        menuPane = new MenuPane(this, tabbedTextPane, fileManager, printManager, logWindow);
        contentPane.add(menuPane, "North");
        tabbedTextPane.setMenuPane(menuPane);
        tabbedTextPane.setFileSystemPane(fileSystemPane);

        Refactor.getInstance().addLogEventListener(logWindow);
    }

    public void init() {
        fileSystemPane.init();
    }

    public FileManager getFileManager() {
        return fileManager;
    }
    
    public TabbedTextPane getTabbedTextPane() {
        return tabbedTextPane;
    }

    public FileSystemPane getFileSystemPane() {
        return fileSystemPane;
    }

    public MethodListPane getMethodListPane() {
        return methodListPane;
    }

    public MenuPane getMenuPane() {
        return menuPane;
    }

    public void closeFiles() {
        fileManager.closeFiles();
    }

    public void closeWindow() {
        fileManager.closeFiles();
    }

    public void terminated() {
        browser.terminated();
    }

    public void closedLogWindow() {
        System.out.println("CLOSED");
    }
}
