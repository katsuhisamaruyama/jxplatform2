/*
 *     RefactoringMenu.java  Nov 24, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class RefactoringMenu implements MenuListener, ActionListener {
    private JFrame frame;
    private JComponent mainMenu;
    private TabbedTextPane tabbedTextPane;
    private Refactor refactor;
    private RefactoringHistory history;
    private JMenu classesMenu;
    private JMenu methodsMenu;
    private JMenu fieldsMenu;
    private JMenu variablesMenu;
    private JMenu miscellaneousMenu;
    private JMenuItem switchItem;
    private JMenuItem undoItem;
    private JMenu guideMenu;

    public RefactoringMenu(JFrame frame, JComponent menu, TabbedTextPane pane) {
        this.frame = frame;
        mainMenu = menu;
        tabbedTextPane = pane;

        refactor = Refactor.getInstance();
        refactor.setTabbedTextPane(pane);

        history = RefactoringHistory.getInstance();
        history.setTabbedTextPane(pane);

        init(mainMenu);
    }

    private void init(JComponent menu) {
        classesMenu = initClassesMenu();
        methodsMenu = initMethodsMenu();
        fieldsMenu = initFieldsMenu(); 
        variablesMenu = initVariablesMenu();
        miscellaneousMenu = initMiscellaneousMenu();
        setMenusEnabled(false);

        menu.add(classesMenu);
        menu.add(methodsMenu);
        menu.add(fieldsMenu);
        menu.add(variablesMenu);
        menu.add(miscellaneousMenu);
        menu.add(new JSeparator());

        JMenuItem showItem = new JMenuItem("History...");
        showItem.setMnemonic('S');
        menu.add(showItem);
        showItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                TextPane textPane = tabbedTextPane.getCurrentTextPane();
                TextUndoManager undoManager = textPane.getUndoManager();

                HistoryDialog dialog = new HistoryDialog(frame);
                dialog.setText(undoManager.getText());

                Point point = frame.getLocationOnScreen();
                dialog.setLocation(point.x + frame.getSize().width / 2, point.y + 10);
                dialog.setVisible(true);
            }
        });

        initGuideMenu();
        menu.add(guideMenu);

        undoItem = new JMenuItem("Undo", 'U');
        menu.add(undoItem);
        undoItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                TextPane textPane = tabbedTextPane.getCurrentTextPane();
                refactor.undo(textPane);
            }
        });
    }

    private JMenu initClassesMenu() {
        JMenu menu = new JMenu("Class");
        menu.setMnemonic('C');

        menu.add(createMenuItem("Rename Class", "RenameClass"));
        menu.add(createMenuItem("Move Class", "MoveClass"));
        menu.add(createMenuItem("Merge Class", "MergeClass"));
        menu.add(createMenuItem("Delete Class", "DeleteClass"));
        menu.add(createMenuItem("Extract Subclass", "ExtractSubclass"));
        menu.add(createMenuItem("Extract Superclass", "ExtractSuperclass"));
        menu.add(createMenuItem("Extract Super Interface", "ExtractSuperInterface"));
        menu.add(createMenuItem("Extract Interface", "ExtractInterface"));

        return menu;
    }

    private JMenu initMethodsMenu() {
        JMenu menu = new JMenu("Method");
        menu.setMnemonic('M');

        menu.add(createMenuItem("Rename Method", "RenameMethod"));
        menu.add(createMenuItem("Move Method", "MoveMethod"));
        menu.add(createMenuItem("Delete Method", "DeleteMethod"));
        menu.add(createMenuItem("Pull Up Method", "PullUpMethod"));
        menu.add(createMenuItem("Push Down Method", "PushDownMethod"));
        // menu.add(createMenuItem("Extract Method", ExtractMethod"));

        return menu;
    }

    private JMenu initFieldsMenu() {
        JMenu menu = new JMenu("Field");
        menu.setMnemonic('F');
        
        menu.add(createMenuItem("Rename Field", "RenameField"));
        menu.add(createMenuItem("Move Field", "MoveField"));
        menu.add(createMenuItem("Delete Field", "DeleteField"));
        menu.add(createMenuItem("Pull Up Field", "PullUpField"));
        menu.add(createMenuItem("Push Down Field", "PushDownField"));
        menu.add(createMenuItem("Encapsulate Field", "EncapsulateField"));
        menu.add(createMenuItem("Self Encapsulate Field", "SelfEncapsulateField"));

        return menu;
    }

    private JMenu initVariablesMenu() {
        JMenu menu = new JMenu("Variable");
        menu.setMnemonic('V');

        menu.add(createMenuItem("Rename Variable", "RenameVariable"));
        menu.add(createMenuItem("Delete Variable", "DeleteVariable"));
        menu.add(createMenuItem("Split Variable", "SplitVariable"));
        menu.add(createMenuItem("Slice on Variable", "SliceOnVariable"));
 
        return menu;
    }

    private JMenuItem createMenuItem(String name, String command) {
        JMenuItem item = new JMenuItem(name);
        item.setActionCommand(command);
        item.addActionListener(this);
        return item;
    }        

    private JMenu initMiscellaneousMenu() {
        JMenu menu = new JMenu("Miscellaneous");
        menu.setMnemonic('I');

        switchItem = createMenuItem("Switch to Polymorphism", "SwitchToPolymorphism");
        menu.add(switchItem);
 
        return menu;
    }

    private void initGuideMenu() {
        guideMenu = new JMenu("Guide");
        guideMenu.setMnemonic('G');
        guideMenu.addMenuListener(new MenuListener() {
            public void menuSelected(MenuEvent evt) {
                TextPane textPane = tabbedTextPane.getCurrentTextPane();
                buildGuideMenu(textPane);
            }
            public void menuCanceled(MenuEvent evt) {
            }
            public void menuDeselected(MenuEvent evt) {
            }
        });
    }

    public void buildGuideMenu(TextPane textPane) {
        TextUndoManager undoManager = textPane.getUndoManager();
        RefactoringRecord record1 = undoManager.getLastRefactoringCommand(1);
        RefactoringRecord record2 = undoManager.getLastRefactoringCommand(2);

        history.retrieve(record1, record2);
        List recordsMatchedLastOne = history.getRecordsMatchedLastOne();
        List recordsMatchedLastTwo = history.getRecordsMatchedLastTwo();

        guideMenu.removeAll();
        buildRecordsMenu(recordsMatchedLastTwo, guideMenu);
        guideMenu.add(new JSeparator());
        buildRecordsMenu(recordsMatchedLastOne, guideMenu);
    }

    private void buildRecordsMenu(List collection, JMenu menu) {
        Border border = new EmptyBorder(2, 15, 2, 15);
        if (collection.size() == 0) {
            JLabel label = new JLabel("Empty", JLabel.CENTER);
            label.setBorder(border);
            menu.add(label);
        } else {
            Iterator it = collection.iterator();
            while (it.hasNext()) {
                String command = (String)it.next();
                JLabel label = new JLabel(command, JLabel.LEFT);
                label.setBorder(border);
                menu.add(label);
            }
        }
    }

    public void menuSelected(MenuEvent evt) {
        setRefactoringMenu((Point)null);
    }

    public void menuCanceled(MenuEvent evt) {
    }

    public void menuDeselected(MenuEvent evt) {
    }

    public void setRefactoringMenu(Point point) {
        TextPane textPane = tabbedTextPane.getCurrentTextPane();
        updateUndoMenu(textPane);
        updateHistoryMenu();

        tabbedTextPane.parseSummaryFiles();
        if (!textPane.parseBeforeRefactoring()) {
            setAllMenus(false);
            return;
        }
        setAllMenus(true);

        int beginLine, beginColumn;
        int endLine, endColumn;
        if (textPane.isSelected()) {
            int start = textPane.getSelectionStart();
            int end = textPane.getSelectionEnd();

            beginLine = textPane.getLine(start);
            beginColumn = textPane.getColumn(start);
            endLine = textPane.getLine(end);
            endColumn = textPane.getColumn(end);

        } else if (point != null) {
            int pos = textPane.getTextArea().viewToModel(point);
            textPane.setCaretPosition(pos);

            beginLine = textPane.getLine(pos);
            beginColumn = textPane.getColumn(pos);
            endLine = beginLine;
            endColumn = beginColumn;
        } else {
            return;
        }

        JavaFile jfile = textPane.getJavaFile();
        refactor.setJavaComponent(jfile, beginLine, beginColumn, endLine, endColumn);
        updateRefactoringsMenu();
    }

    private void setAllMenus(boolean bool) {
        classesMenu.setEnabled(bool);
        methodsMenu.setEnabled(bool);
        fieldsMenu.setEnabled(bool);
        variablesMenu.setEnabled(bool);
        miscellaneousMenu.setEnabled(bool);
    }

    private void setMenusEnabled(boolean bool) {
        setMenuEnabled(classesMenu, bool);
        setMenuEnabled(methodsMenu, bool);
        setMenuEnabled(fieldsMenu, bool);
        setMenuEnabled(variablesMenu, bool);
        setMenuEnabled(miscellaneousMenu, bool);
    }

    private void setMenuEnabled(JMenu menu, boolean bool) {
        for (int pos = 0; pos < menu.getItemCount(); pos++) {
            JMenuItem item = menu.getItem(pos);
            item.setEnabled(bool);
        }
    }

    private void updateRefactoringsMenu() {
        setMenusEnabled(false);
        if (refactor.isClassRefactoring()) {
            setMenuEnabled(classesMenu, true);

        } else if (refactor.isMethodRefactoring()) {
            setMenuEnabled(methodsMenu, true);

        } else if (refactor.isFieldRefactoring()) {
            setMenuEnabled(fieldsMenu, true);

        } else if (refactor.isVariableRefactoring()) {
            setMenuEnabled(variablesMenu, true);

        } else if (refactor.isSwitchRefactoring()) {
            miscellaneousMenu.setEnabled(true);
            switchItem.setEnabled(true);
        }
    }

    public void actionPerformed(ActionEvent evt) {
        JMenuItem menuItem = (JMenuItem)evt.getSource();
        refactor.execute(menuItem.getActionCommand());
    }

    private void updateUndoMenu(TextPane textPane) {
        undoItem.setEnabled(refactor.canUndo(textPane));
    }

    private void updateHistoryMenu() {
    }
}
