
package jp.ac.ritsumei.cs.draw;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.event.*;

import javax.swing.undo.*;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * A menu bar that contains menu items.
 */
public class DrawMenu extends JPanel {
    
    private static final long serialVersionUID = 1937943829729036145L;
    
    /**
     * The application for a drawing tool.
     */
    private DrawTool tool;
    
    /**
     * The menu bar of the application.
     */
    private JMenuBar menuBar;
    
    /**
     * The tool bar of the application.
     */
    private JToolBar toolBar;
    
    /**
     * A tabbed canvas on which a figure is drawn.
     */
    private TabbedCanvas tabbedCanvas;
    
    /**
     * The name of a directory opened by a file chooser.
     */
    private File directory;
    
    /**
     * The files that were recently opened.
     */
    private List<String> recentFiles;
    
    /**
     * The maximum number of recent files stored.
     */
    private static final int RECENT_FILES = 5;
    
    /**
     * The action when the new operation is performed.
     */
    private Action newAction;
    
    /**
     * The action when the open operation is performed.
     */
    private Action openAction;
    
    /**
     * The action when the save operation is performed.
     */
    private Action saveAction;
    
    /**
     * The action when the undo operation is performed.
     */
    private Action undoAction;
    
    /**
     * The action when the redo operation is performed.
     */
    private Action redoAction;
    
    /**
     * The action when the cut operation is performed.
     */
    private Action cutAction;
    
    /**
     * The action when the copy operation is performed.
     */
    private Action copyAction;
    
    /**
     * The action when the paste operation is performed.
     */
    private Action pasteAction;
    
    /**
     * The action when the delete operation is performed.
     */
    private Action deleteAction;
    
    /**
     * The menu when for the recent file list.
     */
    private JMenu fileListMenu;
    
    /**
     * The action listener for edit menu.
     */
    private ActionListener fileListener;
    
    /**
     * The action listener for window menu.
     */
    private ActionListener windowListener;
    
    /**
     * The figure stored in the clipboard for copy, cut, and paste actions.
     */
    private Figure clipboard;
    
    /**
     * An auto saver of information about figures.
     */
    private AutoSave autoSave;
    
    /**
     * Creates a menu.
     * @param tool the application for a drawing tool
     * @param tabbedCanvas the tabbed canvas that is the collection of a canvas on which a figure is drawn
     */
    DrawMenu(DrawTool tool, TabbedCanvas tabbedCanvas) {
        this.tool = tool;
        this.tabbedCanvas = tabbedCanvas;
        
        menuBar = new JMenuBar();
        initMenuBar(menuBar);
        
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        initToolBar(toolBar);
        
        directory = new File(".");
        recentFiles = new ArrayList<String>();
        
        clipboard = null;
        
        autoSave = new AutoSave(tabbedCanvas);
        autoSave.start();
    }
    
    /**
     * Returns the menu bar.
     * @return the menu bar
     */
    JMenuBar getMenuBar() {
        return menuBar;
    }
    
    /**
     * Returns the tool bar.
     * @return the tool bar
     */
    JToolBar getToolBar() {
        return toolBar;
    }
    
    /**
     * Creates menu items packed in a given menu bar.
     * @param menuBar the menu bar.
     */
    private void initMenuBar(JMenuBar menuBar) {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        menuBar.add(fileMenu);
        initFileActions();
        initFileMenu(fileMenu);
        
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');
        menuBar.add(editMenu);
        initEditActions();
        initEditMenu(editMenu);
        
        final JMenu windowMenu = new JMenu("Window");
        windowMenu.setMnemonic('W');
        menuBar.add(windowMenu);
        
        windowListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JMenuItem wi = (JMenuItem)evt.getSource();
                DrawCanvas canvas = tabbedCanvas.getCanvas(wi.getActionCommand());
                tabbedCanvas.selectTab(canvas);
            }
        };
        
        windowMenu.addMenuListener(new MenuListener() {
            public void menuSelected(MenuEvent evt) {
                windowMenu.removeAll();
                for (DrawCanvas canvas : tabbedCanvas.getAllCanvases()) {
                    FigureManager fmanager = canvas.getFigureManager();
                    JMenuItem windowItem = new JMenuItem(fmanager.getTitle());
                    windowMenu.add(windowItem);
                    windowItem.addActionListener(windowListener);
                }
            }
            
            public void menuCanceled(MenuEvent evt) {
            }
            
            public void menuDeselected(MenuEvent evt) {
            }
        });
    }
    
    /**
     * Creates menu items packed in a given tool bar.
     * @param menuBar the tool bar.
     */
    private void initToolBar(JToolBar toolBar) {
        JButton newButton = toolBar.add(newAction);
        newButton.setToolTipText("New");
        
        JButton openButton = toolBar.add(openAction);
        openButton.setToolTipText("Open");
        
        JButton saveButton = toolBar.add(saveAction);
        saveButton.setToolTipText("Save");
        
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
    
    /**
     * Create actions activated from the file menu.
     */
    private void initFileActions() {
        newAction = new AbstractAction("New...",
          new ImageIcon(getClass().getResource("images/new.gif"))) {
            private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent evt) {
                if (newFile()) {
                    buildRecentFileMenu();
                }
            }
        };
        
        openAction = new AbstractAction("Open...",
          new ImageIcon(getClass().getResource("images/open.gif"))) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed(ActionEvent evt) {
                if (openFile()) {
                    buildRecentFileMenu();
                    updateUndoState();
                    updateRedoState();
                }
            }
        };
        
        saveAction = new AbstractAction("Save",
          new ImageIcon(getClass().getResource("images/save.gif"))) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed(ActionEvent evt) {
                DrawCanvas canvas = tabbedCanvas.getCurrentCanvas();
                if (saveFile(canvas)) {
                    updateUndoState();
                    updateRedoState();
                }
            }
        };
    }
    
    /**
     * Creates actions activated from the edit menu.
     */
    private void initEditActions() {
        undoAction = new AbstractAction("Undo",
          new ImageIcon(getClass().getResource("images/undo.gif"))) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed(ActionEvent evt) {
                DrawCanvas canvas = tabbedCanvas.getCurrentCanvas();
                UndoManager undoManager = canvas.getUndoManager();
                try {
                    undoManager.undo();
                    updateUndoState();
                    updateRedoState();
                    canvas.clearFigure();
                } catch (CannotUndoException e) { }
            }
        };
        
        redoAction = new AbstractAction("Redo",
          new ImageIcon(getClass().getResource("images/redo.gif"))) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed(ActionEvent evt) {
                DrawCanvas canvas = tabbedCanvas.getCurrentCanvas();
                UndoManager undoManager = canvas.getUndoManager();
                try {
                    undoManager.redo();
                    updateUndoState();
                    updateRedoState();
                    canvas.clearFigure();
                } catch (CannotRedoException e) { }
            }
        };
        
        cutAction = new AbstractAction("Cut",
          new ImageIcon(getClass().getResource("images/cut.gif"))) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed(ActionEvent evt) {
                DrawCanvas canvas = tabbedCanvas.getCurrentCanvas();
                clipboard = canvas.getGrabbedFigure();
                canvas.deleteFigure();
                
                updateEditState(false);
                updateUndoState();
                updateRedoState();
            }
        };
        
        copyAction = new AbstractAction("Copy",
          new ImageIcon(getClass().getResource("images/copy.gif"))) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed(ActionEvent evt) {
                DrawCanvas canvas = tabbedCanvas.getCurrentCanvas();
                clipboard = canvas.getGrabbedFigure();
                
                updateEditState(true);
                updateUndoState();
                updateRedoState();
            }
        };
        
        pasteAction = new AbstractAction("Paste",
          new ImageIcon(getClass().getResource("images/paste.gif"))) {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed(ActionEvent evt) {
                DrawCanvas canvas = tabbedCanvas.getCurrentCanvas();
                canvas.pasteFigure(clipboard.createClone());
                
                updateEditState(true);
                updateUndoState();
                updateRedoState();
            }
        };
        
        deleteAction = new AbstractAction("Delete") {
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed(ActionEvent evt) {
                DrawCanvas canvas = tabbedCanvas.getCurrentCanvas();
                canvas.deleteFigure();
                
                updateUndoState();
                updateRedoState();
            }
        };
    }
    
    /**
     * Creates menu items packed in a given file menu.
     * @param menu the file menu.
     */
    private void initFileMenu(JMenu menu) {
        JMenuItem newItem = menu.add(newAction);
        newItem.setMnemonic('N');
        newItem.setIcon(null);
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK, false));
        
        JMenuItem openItem = menu.add(openAction);
        openItem.setMnemonic('O');
        openItem.setIcon(null);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK, false));
        
        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.setMnemonic('C');
        menu.add(closeItem);
        closeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                closeFile();
            }
        });
        
        menu.addSeparator();
        
        JMenuItem saveItem = menu.add(saveAction);
        saveItem.setMnemonic('S');
        saveItem.setIcon(null);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK, false));
        
        JMenuItem saveAsItem = new JMenuItem("Save As...");
        saveAsItem.setMnemonic('A');
        menu.add(saveAsItem);
        saveAsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                DrawCanvas canvas = tabbedCanvas.getCurrentCanvas();
                if (saveAsFile(canvas)) {
                    buildRecentFileMenu();
                    updateUndoState();
                    updateRedoState();
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
        fileListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                
                JMenuItem fileItem = (JMenuItem)evt.getSource();
                String filename = fileItem.getActionCommand();
                
                if (openFile(filename)) {
                    buildRecentFileMenu();
                    updateUndoState();
                    updateRedoState();
                }
            }
        };
        
        menu.addSeparator();
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic('X');
        menu.add(exitItem);
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                closeAllFiles();
            }
        });
    }
    
    /**
     * Creates menu items packed in a given edit menu.
     * @param menu the edit menu.
     */
    private void initEditMenu(JMenu menu) {
        JMenuItem undoItem = menu.add(undoAction);
        undoItem.setMnemonic('U');
        undoItem.setIcon(null);
        undoItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK, false));
        undoAction.setEnabled(false);
        
        JMenuItem redoItem = menu.add(redoAction);
        redoItem.setMnemonic('R');
        redoItem.setIcon(null);
        redoItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK, false));
        redoAction.setEnabled(false);
        
        menu.addSeparator();
        
        JMenuItem cutItem = menu.add(cutAction);
        cutItem.setMnemonic('T');
        cutItem.setIcon(null);
        cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK, false));
        cutItem.setEnabled(false);
        
        JMenuItem copyItem = menu.add(copyAction);
        copyItem.setMnemonic('C');
        copyItem.setIcon(null);
        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK, false));
        copyItem.setEnabled(false);
        
        JMenuItem pasteItem = menu.add(pasteAction);
        pasteItem.setMnemonic('P');
        pasteItem.setIcon(null);
        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK, false));
        pasteItem.setEnabled(false);
        
        menu.addSeparator();
        
        JMenuItem deleteItem = menu.add(deleteAction);
        deleteItem.setMnemonic('L');
        deleteItem.setIcon(null);
        deleteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false));
        deleteItem.setEnabled(false);
    }
    
    /**
     * Creates a new file that will store information about figures.
     * @return <code>true</code> if the new was successfully created, otherwise <code>false</code>
     */
    boolean newFile() {
        String filename = getFileName(false);
        if (filename == null) {
            return false;
        }
        
        if (fileExists(filename)) {
            JOptionPane.showMessageDialog(tool, "File exits: " + filename + ".");
                return false;
        }
        
        DrawCanvas canvas = tabbedCanvas.openTab(filename);
        addRecentFile(filename);
        tool.setTitle(filename);
        
        canvas.repaint();
        return true;
    }
    
    /**
     * Opens an existing file that stores information about figures.
     * @return <code>true</code> if the file was successfully opened, otherwise <code>false</code>
     */
    boolean openFile() {
        String filename = getFileName(true);
        if (filename == null) {
            return false;
        }
        return openFile(filename);
    }
    
    /**
     * Opens an existing file that stores information about figures.
     * @param filename the name of the file storing the information
     * @return <code>true</code> if the file was successfully opened, otherwise <code>false</code>
     */
    boolean openFile(String filename) {
        DrawCanvas canvas = tabbedCanvas.getCanvas(filename);
        if (tabbedCanvas.getCanvas(filename) != null) {
            if (!canvas.hasChanged()) {
                JOptionPane.showMessageDialog(tool, "Already opened: " + filename + ".");
                return false;
            } else {
                int confirm = JOptionPane.showConfirmDialog(tool,
                  "The changes were made to this file. Do you want to re-open the file?",
                  "Select an Option", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.NO_OPTION) {
                    return false;
                }
            }
        } else {
            canvas = tabbedCanvas.openTab(filename);
        }
        
        boolean result = canvas.getFigureManager().load(filename);
        if (!result) {
            JOptionPane.showMessageDialog(tool, "Cannnot open: " + filename + ".");
            return false;
        }
        
        canvas.setChanged(false);
        tool.setTitle(filename);
        
        canvas.repaint();
        return true;
    }
    
    /**
     * Saves information about figures into the temporary file.
     * @param canvas the canvas containing figures to be saved
     * @return <code>true</code> if the file was successfully saved, otherwise <code>false</code>
     */
    boolean saveTempFile(DrawCanvas canvas) {
        String tempName = canvas.getFigureManager().getFilename() + "~";
        return canvas.getFigureManager().store(tempName);
    }
    
    /**
     * Saves information about figures into the current file.
     * @param canvas the canvas containing figures to be saved
     * @return <code>true</code> if the file was successfully saved, otherwise <code>false</code>
     */
    boolean saveFile(DrawCanvas canvas) {
        return saveFile(canvas, canvas.getFigureManager().getFilename());
    }
    
    /**
     * Saves information about figures into the new file.
     * @param canvas the canvas containing figures to be saved
     * @return <code>true</code> if the file was successfully saved, otherwise <code>false</code>
     */
   boolean saveAsFile(DrawCanvas canvas) {
        String filename = getFileName(false);
        if (filename == null) {
            return false;
        }
        
        if (saveFile(canvas, filename)) {
            tabbedCanvas.updateTab(canvas);
            addRecentFile(filename);
            tool.setTitle(filename);
            return true;
        }
        return false;
    }
    
    /**
     * Saves information about figures into a file with a given name.
     * @param canvas the canvas containing figures to be saved
     * @param filename the name of the file storing the information
     * @return <code>true</code> if the file was successfully saved, otherwise <code>false</code>
     */
    private boolean saveFile(DrawCanvas canvas, String filename) {
        boolean result = canvas.getFigureManager().store(filename);
        if (!result) {
            JOptionPane.showMessageDialog(tool, "Cannot save: " + filename + ".");
            return false;
        }
        
        canvas.setChanged(false);
        return true;
    }
    
    /**
     * Closes the current file storing information about figures.
     */
    void closeFile() {
        DrawCanvas canvas = tabbedCanvas.getCurrentCanvas();
        closeFile(canvas);
    }
    
    /**
     * Closes all the files storing information about figures.
     */
    void closeAllFiles() {
        for (DrawCanvas canvas : tabbedCanvas.getAllCanvases()) {
            if (!closeFile(canvas)) {
                return;
            }
        }
    }
    
    /**
     * Closes the current file storing information about figures.
     * @param canvas the canvas containing figures to be closed
     */
    boolean closeFile(DrawCanvas canvas) {
        if (canvas.hasChanged()) {
            int result = JOptionPane.showConfirmDialog(tool,
              "Do you want to save the changes you have made to this file?");
            if (result == JOptionPane.CANCEL_OPTION) {
                return false;
                
            } else if (result == JOptionPane.YES_OPTION) {
                if (!saveFile(canvas)) {
                    int result2 = JOptionPane.showConfirmDialog(tool,
                      "Do you want to close without saving the chages?");
                    if (result2 == JOptionPane.CANCEL_OPTION) {
                        return false;
                    }
                }
            }
        }
        
        tabbedCanvas.closeTab(canvas);
        if (tabbedCanvas.getOpenFileNum() == 0) {
            tool.terminate();
        }
        return true;
    }
    
    /**
     * Tests if there exists a file with a given name.
     * @param name the name to be checked
     * @return <code>true</code> if there exists a file with the given name, otherwise <code>false</code>
     */
    private boolean fileExists(String name) {
        File file = new File(name);
        return file.exists();
    }
    
    /**
     * Obtains the name of a file specified by a user.
     * @param open <code>true</code> if a file open chooser will be activated,
     *        or <code>false</code>if a file save chooser will be activated
     * @return the name of the specified file.
     */
    private String getFileName(boolean open) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("File Chooser");
        chooser.setCurrentDirectory(directory);
        
        int result;
        if (open) {
            result = chooser.showOpenDialog(tool);
        } else {
            result = chooser.showSaveDialog(tool);
        }
        
        File file = chooser.getSelectedFile();
        if (file != null && result == JFileChooser.APPROVE_OPTION) {
            directory = chooser.getCurrentDirectory();
            return file.getPath();
        }
        return null;
    }
    
    void addRecentFile(String filename) {
        int offset = recentFiles.indexOf(filename);
        if (offset == -1) {
            recentFiles.add(0, filename);
            if (recentFiles.size() > RECENT_FILES) {
                recentFiles.remove(RECENT_FILES);
            }
        } else {
            recentFiles.remove(offset);
            recentFiles.add(0, filename);
        }
    }
    
    void buildRecentFileMenu() {
        fileListMenu.removeAll();
        for (String filename : recentFiles) {
            JMenuItem fileItem = new JMenuItem(filename);
            fileListMenu.add(fileItem);
            fileItem.addActionListener(fileListener);
        }
    }
    
    /**
     * Updates the state of edit menu items.
     * @param grabbed <code>true</code> if any figure is grabbed, otherwise <code>false</code>
     */
    void updateEditState(boolean grabbed) {
        cutAction.setEnabled(grabbed);
        copyAction.setEnabled(grabbed);
        if (clipboard != null) {
            pasteAction.setEnabled(true);
        } else {
            pasteAction.setEnabled(false);
        }
        deleteAction.setEnabled(grabbed);
    }
    
    /**
     * Updates the state of undo menu item.
     */
    void updateUndoState() {
        DrawCanvas canvas = tabbedCanvas.getCurrentCanvas();
        if (canvas == null) {
            return;
            
        }
        
        UndoManager undoManager = canvas.getUndoManager();
        if (undoManager != null && undoManager.canUndo()) {
            undoAction.setEnabled(true);
        } else {
            undoAction.setEnabled(false);
        }
    }
    
    /**
     * Updates the state of redo menu item.
     */
    public void updateRedoState() {
        DrawCanvas canvas = tabbedCanvas.getCurrentCanvas();
        if (canvas == null) {
            return;
            
        }
        
        UndoManager undoManager = canvas.getUndoManager();
        if (undoManager != null && undoManager.canRedo()) {
            redoAction.setEnabled(true);
        } else {
            redoAction.setEnabled(false);
        }
    }
    
    /**
     * Creates a popup menu.
     * @param popup the popup menu
     */
    void createPopup(JPopupMenu popup) {
        JMenuItem undoMenuItem = popup.add(undoAction);
        undoMenuItem.setIcon(null);
        popup.add(undoMenuItem);
        
        JMenuItem redoMenuItem = popup.add(redoAction);
        redoMenuItem.setIcon(null);
        popup.add(redoMenuItem);
    }
}
