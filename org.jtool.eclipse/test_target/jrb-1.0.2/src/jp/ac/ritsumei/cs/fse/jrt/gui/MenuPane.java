/*
 *     MenuPane.java  Oct 22, 2001
 *
 *     Tatsuya Kageyama (kage@fse.cs.ritsumei.ac.jp)
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import jp.ac.ritsumei.cs.fse.jrt.parser.JavaModelFactory;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.Summary;
import javax.swing.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class MenuPane extends JPanel {
    private JFrame frame;
    private TabbedTextPane tabbedTextPane;
    private FileManager fileManager;
    private PrintManager printManager;
    private LogWindow logWindow;
    private WarningDialog warningDialog;
    private AllHistoryDialog hitoryDialog;
    private JMenuBar menuBar;

    private Action newAction;
    private Action openAction;
    private Action saveAction;
    private Action printAction;

    private Action undoAction;
    private Action redoAction;

    private Action cutAction;
    private Action copyAction;
    private Action pasteAction;

    private JMenuItem deleteItem;

    private JMenu fileListMenu;
    private ActionListener menuListener;

    public MenuPane(JFrame frame, TabbedTextPane pane,
      FileManager fm, PrintManager pm, LogWindow lw) {
        super(true);
        this.frame = frame;
        tabbedTextPane = pane;
        fileManager = fm;
        printManager = pm;
        logWindow = lw;

        initActions();

        menuBar = new JMenuBar();
        initMenuBar(menuBar);

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        initToolBar(toolBar);

        this.setLayout(new BorderLayout());
        this.add(menuBar, "North");
        this.add(toolBar, "South");
    }

    private void initToolBar(JToolBar toolBar) {
        JButton newButton = toolBar.add(newAction);
        newButton.setToolTipText("New");

        JButton openButton = toolBar.add(openAction);
        openButton.setToolTipText("Open");

        JButton saveButton = toolBar.add(saveAction);
        saveButton.setToolTipText("Save");

        toolBar.addSeparator();

        JButton printButton = toolBar.add(printAction);
        printButton.setToolTipText("Print");

        toolBar.addSeparator();

        JButton cutButton = toolBar.add(cutAction);
        cutButton.setToolTipText("Cut");

        JButton copyButton = toolBar.add(copyAction);
        copyButton.setToolTipText("Copy");

        JButton pasteButton = toolBar.add(pasteAction);
        pasteButton.setToolTipText("Paste");

        toolBar.addSeparator();

        JButton undoButton = toolBar.add(undoAction);
        undoButton.setToolTipText("Undo");
        undoAction.setEnabled(false);

        JButton redoButton = toolBar.add(redoAction);
        redoButton.setToolTipText("Redo");
        redoButton.setEnabled(false);
    }

    private void initMenuBar(JMenuBar menuBar) {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        menuBar.add(fileMenu);
        initFileMenu(fileMenu);

        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');
        menuBar.add(editMenu);
        initEditMenu(editMenu);
        editMenu.addMenuListener(new MenuListener() {
            public void menuSelected(MenuEvent evt) {
                TextPane textPane = tabbedTextPane.getCurrentTextPane();
                if (textPane.isSelected()) {
                    deleteItem.setEnabled(true);
                } else {
                    deleteItem.setEnabled(false);
                }
            }
            public void menuCanceled(MenuEvent evt) {
            }

            public void menuDeselected(MenuEvent evt) {
            }
        });

        JMenu refactoringMenu = new JMenu("Refcatoring");
        refactoringMenu.setMnemonic('R');
        menuBar.add(refactoringMenu);
        RefactoringMenu refs = new RefactoringMenu(frame, refactoringMenu, tabbedTextPane);
        refactoringMenu.addMenuListener(refs);

        JMenu optionsMenu = new JMenu("Options");
        optionsMenu.setMnemonic('O');
        menuBar.add(optionsMenu);
        initOptionsMenu(optionsMenu);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        menuBar.add(helpMenu);
        initHelpMenu(helpMenu);
    }

    private void initFileMenu(JMenu menu) {
        JMenuItem newItem = menu.add(newAction);
        newItem.setMnemonic('N');
        newItem.setIcon(null);
        newItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK, false));

        JMenuItem openItem = menu.add(openAction);
        openItem.setMnemonic('O');
        openItem.setIcon(null);
        openItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK, false));

        JMenuItem renameItem = new JMenuItem("Rename", 'M');
        menu.add(renameItem);
        renameItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                fileManager.renameFile();
            }
        });

        JMenuItem closeItem = new JMenuItem("Close", 'C');
        menu.add(closeItem);
        closeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                fileManager.closeFile();
            }
        });

        menu.addSeparator();

        JMenuItem saveItem = menu.add(saveAction);
        saveItem.setMnemonic('S');
        saveItem.setIcon(null);
        saveItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK, false));

        JMenuItem saveAsItem = new JMenuItem("Save As...", 'A');
        menu.add(saveAsItem);
        saveAsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                TextPane textPane = fileManager.saveAsFile();
                if (textPane != null) {
                    updateUndoState(textPane.getUndoManager());
                    updateRedoState(textPane.getUndoManager());
                }
            }
        });

        menu.addSeparator();

        fileListMenu = new JMenu("Recent Files");
        fileListMenu.setMnemonic('R');
        menu.add(fileListMenu);

        JMenuItem emptyListItem = new JMenuItem("Empty");
        emptyListItem.setEnabled(false);
        fileListMenu.add(emptyListItem);
        menuListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JMenuItem fileItem = (JMenuItem)evt.getSource();
                String name = fileItem.getActionCommand();
                TextPane textPane = fileManager.openFile(name);
                buildRecentFileMenu();
                updateUndoState(textPane.getUndoManager());
                updateRedoState(textPane.getUndoManager());
            }
        };

        menu.addSeparator();

        JMenuItem printItem = menu.add(printAction);
        printItem.setMnemonic('P');
        printItem.setIcon(null);
        printItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK, false));

        menu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit", 'X');
        menu.add(exitItem);
        exitItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK, false));
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                fileManager.closeFiles();
            }
        });
    }

    private void initEditMenu(JMenu menu) {
        JMenuItem undoItem = menu.add(undoAction);
        undoItem.setMnemonic('U');
        undoItem.setIcon(null);
        undoItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK, false));
        undoAction.setEnabled(false);

        JMenuItem redoItem = menu.add(redoAction);
        redoItem.setMnemonic('R');
        redoItem.setIcon(null);
        redoItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK, false));
        redoAction.setEnabled(false);

        menu.addSeparator();

        JMenuItem cutItem = menu.add(cutAction);
        cutItem.setMnemonic('T');
        cutItem.setIcon(null);
        cutItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK, false));
        
        JMenuItem copyItem = menu.add(copyAction);
        copyItem.setMnemonic('C');
        copyItem.setIcon(null);
        copyItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK, false));

        JMenuItem pasteItem = menu.add(pasteAction);
        pasteItem.setMnemonic('P');
        pasteItem.setIcon(null);
        pasteItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK, false));

        menu.addSeparator();

        JMenuItem insertItem = new JMenuItem("Insert", 'I');
        menu.add(insertItem);
        insertItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK, false));
        insertItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                fileManager.insertFile();
            }
        });

        deleteItem = new JMenuItem("Delete", 'L');
        menu.add(deleteItem);
        deleteItem.setEnabled(false);
        deleteItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                TextPane textPane = tabbedTextPane.getCurrentTextPane();
                int start = textPane.getTextArea().getSelectionStart();
                int end = textPane.getTextArea().getSelectionEnd();
                textPane.getTextArea().replaceRange("", start, end);
            }
        });

        menu.addSeparator();

        JMenuItem findItem = new JMenuItem("Find", 'F');
        menu.add(findItem);       
        findItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK, false));
        findItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                TextPane textPane = tabbedTextPane.getCurrentTextPane();
                FindDialog findDialog = new FindDialog(frame, textPane);
                Point point = frame.getLocationOnScreen();
                findDialog.setLocation(point.x + frame.getSize().width / 2, point.y + 10);

                findDialog.setVisible(true);
                findDialog.dispose();
            }
        });
        
        JMenuItem replaceItem = new JMenuItem("Replace", 'R');
        menu.add(replaceItem);       
        replaceItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK, false));
        replaceItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                TextPane textPane = tabbedTextPane.getCurrentTextPane();
                ReplaceDialog replaceDialog = new ReplaceDialog(frame, textPane);
                Point point = frame.getLocationOnScreen();
                replaceDialog.setLocation(point.x + frame.getSize().width / 2, point.y + 10);

                replaceDialog.setVisible(true);
                replaceDialog.dispose();
            }
        });
        
        menu.addSeparator();

        JMenuItem selectAllItem = new JMenuItem("Select All", 'A');
        menu.add(selectAllItem);
        selectAllItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK, false));
        selectAllItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                TextPane textPane = tabbedTextPane.getCurrentTextPane();
                textPane.selectAll();
            }
        });
    }

    private void initOptionsMenu(JMenu menu) {
        JMenuItem pathItem = new JMenuItem("Paths...", 'P');
        menu.add(pathItem);
        pathItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                OptionsDialog optionsDialog = new OptionsDialog(frame);
                Point point = frame.getLocationOnScreen();
                optionsDialog.setLocation(point.x + frame.getSize().width / 2, point.y + 10);

                optionsDialog.setVisible(true);
                optionsDialog.dispose();
            }
        });

        JMenuItem rebuildItem = new JMenuItem("Rebuild File Tree", 'R');
        menu.add(rebuildItem);
        rebuildItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                tabbedTextPane.removeAllParsedSummaryFiles();
                fileManager.buildFileTree();
            }
        });

        menu.addSeparator();

        JMenuItem logItem = new JMenuItem("Show Log...");
        logItem.setMnemonic('L');
        logItem.setBorderPainted(false);
        menu.add(logItem);
        logItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (!logWindow.isVisible()) {
                    Point point = frame.getLocationOnScreen();
                    logWindow.setLocation(point.x + frame.getSize().width / 2, point.y + 10);
                    logWindow.setVisible(true);
                }
            }
        });

        JMenuItem logClearItem = new JMenuItem("Clear Log");
        logClearItem.setBorderPainted(false);
        menu.add(logClearItem);
        logClearItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                logWindow.setText("");
            }
        });

        menu.addSeparator();

        hitoryDialog = new AllHistoryDialog(frame, RefactoringHistory.getInstance());
        RefactoringHistory.getInstance().setRefactoringDialog(hitoryDialog);

        JMenuItem historyItem = new JMenuItem("Show History All...");
        historyItem.setMnemonic('H');
        menu.add(historyItem);
        historyItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                TextPane textPane = tabbedTextPane.getCurrentTextPane();
                if (!hitoryDialog.isVisible()) {
                    hitoryDialog.update();
                    Point point = frame.getLocationOnScreen();
                    hitoryDialog.setLocation(point.x + frame.getSize().width / 2, point.y + 10);
                    hitoryDialog.setVisible(true);
                }
            }
       });

        JMenuItem historyClearItem = new JMenuItem("Clear History All");
        menu.add(historyClearItem);
        historyClearItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                TextPane textPane = tabbedTextPane.getCurrentTextPane();
                int result = JOptionPane.showConfirmDialog(frame,
                  "Do you want to discard the history of all refactorings?",
                  "Select an Option", JOptionPane.YES_NO_OPTION);

                if (result == JOptionPane.YES_OPTION) {
                    RefactoringHistory.getInstance().clear();
                    hitoryDialog.update();
                }
            }
        });

        menu.addSeparator();

        warningDialog = new WarningDialog(frame);
        JavaModelFactory.getInstance().addWarningEventListener(warningDialog);
        // Summary.getInstance().addWarningEventListener(warningDialog);

        final JMenuItem warningItem = new JCheckBoxMenuItem("Warn of Parse Error");
        warningItem.setMnemonic('W');
        warningItem.setBorderPainted(false);
        warningItem.setSelected(true);
        warningDialog.setAction(true);
        menu.add(warningItem);
        warningItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (warningItem.isSelected()) {
                    warningDialog.setAction(true);
                } else {
                    warningDialog.setAction(false);
                }
            }
        });
    }

    private void initHelpMenu(JMenu menu) {
        /*
        JMenuItem helpItem = new JMenuItem("Help", 'H');
        menu.add(helpItem);
        helpItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, false));
        helpItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                // showHelp();
            }
        });
        helpItem.setEnabled(false);
        */

        JMenuItem aboutItem = new JMenuItem("About...", 'A');
        menu.add(aboutItem);
        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                AboutDialog aboutDialog = new AboutDialog(frame);
                Point point = frame.getLocationOnScreen();
                aboutDialog.setLocation(point.x + frame.getSize().width / 2, point.y + 10);

                aboutDialog.setVisible(true);
                aboutDialog.dispose();
            }
        });
    }

    private void initActions() {
        newAction = new AbstractAction("New...",
          new ImageIcon(getClass().getResource("images/new.gif"))) {
            public void actionPerformed(ActionEvent evt) {
                TextPane textPane = fileManager.newFile();
                if (textPane != null) {
                    buildRecentFileMenu();
                }
            }
        };

        openAction = new AbstractAction("Open...",
          new ImageIcon(getClass().getResource("images/open.gif"))) {
            public void actionPerformed(ActionEvent evt) {
                TextPane textPane = fileManager.openFile();
                if (textPane != null) {
                    buildRecentFileMenu();
                    updateUndoState(textPane.getUndoManager());
                    updateRedoState(textPane.getUndoManager());
                }
            }
        };

        saveAction = new AbstractAction("Save",
          new ImageIcon(getClass().getResource("images/save.gif"))) {
            public void actionPerformed(ActionEvent evt) {
                TextPane textPane = fileManager.saveFile();
                if (textPane != null) {
                    updateUndoState(textPane.getUndoManager());
                    updateRedoState(textPane.getUndoManager());
                }
            }
        };

        printAction = new AbstractAction("Print...",
          new ImageIcon(getClass().getResource("images/print.gif"))) {
            public void actionPerformed(ActionEvent evt) {
                TextPane textPane = tabbedTextPane.getCurrentTextPane();
                printManager.print(textPane.getText());
            }
        };

        cutAction = new AbstractAction("Cut",
          new ImageIcon(getClass().getResource("images/cut.gif"))) {
            public void actionPerformed(ActionEvent evt) {
                TextPane textPane = tabbedTextPane.getCurrentTextPane();
                textPane.cut();
            }
        };

        copyAction = new AbstractAction("Copy",
          new ImageIcon(getClass().getResource("images/copy.gif"))) {
            public void actionPerformed(ActionEvent evt) {
                TextPane textPane = tabbedTextPane.getCurrentTextPane();
                textPane.copy();
            }
        };

        pasteAction = new AbstractAction("Paste",
          new ImageIcon(getClass().getResource("images/paste.gif"))) {
            public void actionPerformed(ActionEvent evt) {
                TextPane textPane = tabbedTextPane.getCurrentTextPane();
                textPane.paste();
            }
        };

        undoAction = new AbstractAction("Undo",
          new ImageIcon(getClass().getResource("images/undo.gif"))) {
            public void actionPerformed(ActionEvent evt) {
                TextPane textPane = tabbedTextPane.getCurrentTextPane();
                UndoManager undoManager = textPane.getUndoManager();
                try {
                    undoManager.undo();
                    textPane.setCaretPosition(0);
                } catch (CannotUndoException e) { }
                updateUndoState(undoManager);
                updateRedoState(undoManager);
            }
        };

        redoAction = new AbstractAction("Redo",
          new ImageIcon(getClass().getResource("images/redo.gif"))) {
            public void actionPerformed(ActionEvent evt) {
                TextPane textPane = tabbedTextPane.getCurrentTextPane();
                UndoManager undoManager = textPane.getUndoManager();
                try {
                    undoManager.redo();
                    textPane.setCaretPosition(0);
                } catch (CannotRedoException e) { }
                updateUndoState(undoManager);
                updateRedoState(undoManager);
            }
        };
    }

    public void buildRecentFileMenu() {
        Iterator it = fileManager.getRecentFiles().iterator();
        fileListMenu.removeAll();
        while (it.hasNext()) {
            String name = (String)it.next();
            JMenuItem fileItem = new JMenuItem(name);
            fileListMenu.add(fileItem);
            fileItem.addActionListener(menuListener);
        }
    }

    public void updateUndoState(UndoManager undoManager) {
        if (undoManager.canUndo()) {
            undoAction.setEnabled(true);
            undoAction.putValue(Action.SHORT_DESCRIPTION,
              "Undo " + undoManager.getUndoPresentationName());
        } else {
            undoAction.setEnabled(false);
            undoAction.putValue(Action.SHORT_DESCRIPTION, "Undo");
        }
    }

    public void updateRedoState(UndoManager undoManager) {
        if (undoManager.canRedo()) {
            redoAction.setEnabled(true);
            redoAction.putValue(Action.SHORT_DESCRIPTION,
              "Redo " + undoManager.getRedoPresentationName());
        } else {
            redoAction.setEnabled(false);
            redoAction.putValue(Action.SHORT_DESCRIPTION, "Redo");
        }
    }
}
