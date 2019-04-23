/*
 *     TextPane.java  Oct 27, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import jp.ac.ritsumei.cs.fse.jrt.parser.JavaModelFactory;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.SummaryJavaFile;
import jp.ac.ritsumei.cs.fse.jrt.model.JavaFile;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class TextPane extends JPanel
  implements DocumentListener, CaretListener, FocusListener {
    private JFrame frame;
    private JTextArea textArea;
    private TextUndoManager undoManager;

    private JTextField positionPane;
    private JTextField fileNamePane;
    private Font font;
    private static final String NO_TITLE = "No Title";

    private JavaModelFactory modelFactory = JavaModelFactory.getInstance();
    private JavaFile jfile;
    private String fileName;
    private String tabName;
    private boolean textChanged = false;
    private long lastModifiedTime;

    private int selectionStart = 0;
    private int selectionEnd = 0;
    private Object cursor = null;

    public TextPane(JFrame frame) {
        super(true);
        this.frame = frame;
        fileName = NO_TITLE;
        setTabName(NO_TITLE);
        createPanes();
    }

    public TextPane(JFrame frame, String name) {
        super(true);
        this.frame = frame;
        fileName = name;
        setTabName(name);
        createPanes();
    }

    private void createPanes() {
        this.setLayout(new BorderLayout());
        undoManager = new TextUndoManager();
        
        font = new Font("Monospaced", Font.PLAIN, 14);
        textArea = new JTextArea(28, 80);
        textArea.setText("");
        textArea.setEditable(true);
        textArea.setFont(font);
        textArea.setSelectionColor(Color.yellow);
        textArea.setBackground(Color.white);
        textArea.setTabSize(4);
        textArea.setLineWrap(true);
        textArea.setMargin(new Insets(5, 5, 5, 5));
        textArea.getDocument().addDocumentListener(this);
        textArea.getDocument().addUndoableEditListener(undoManager);
        textArea.addCaretListener(this);
        textArea.addFocusListener(this);

        JScrollPane scTextArea = new JScrollPane(textArea);
        scTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scTextArea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.add(scTextArea, "Center");

        JPanel statusPane = new JPanel();
        statusPane.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        positionPane = new JTextField();
        positionPane.setEditable(false);
        positionPane.setText("(0,0)");
        positionPane.setBorder(BorderFactory.createEmptyBorder());
        positionPane.setFont(new Font("Dialog", Font.PLAIN, 14));
        positionPane.setOpaque(false);
        statusPane.add(positionPane);

        String ft = fileName;
        if (ft.length() > 80) {
            ft = "/... " + ft.substring(ft.length() - 80);
        }
        fileNamePane = new JTextField();
        fileNamePane.setEditable(false);
        fileNamePane.setText(ft);
        fileNamePane.setBorder(BorderFactory.createEmptyBorder());
        fileNamePane.setFont(new Font("Dialog", Font.PLAIN, 14));
        fileNamePane.setOpaque(false);
        statusPane.add(fileNamePane);

        this.add(statusPane, "South");
    }

    public TextUndoManager getUndoManager() {
        return undoManager;
    }

    public void setMenuPane(MenuPane pane) {
        undoManager.setMenuPane(pane);
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    public JFrame getFrame() {
        return frame;
    }

    public void setTabName(String name) {
        tabName = name.substring(name.lastIndexOf(File.separator) + 1);
    }

    public String getTabName() {
        return tabName;
    }

    public void setFileName(String name) {
        fileName = name;
        setTabName(name);

        String ft = fileName;
        if (ft.length() > 80) {
            ft = "/... " + ft.substring(ft.length() - 80);
        }
        fileNamePane.setText(ft);
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isNoTitle() {
        if (getFileName().compareTo(NO_TITLE) == 0) {
            return true;
        }
        return false;
    }

    public void setText(String text) {
        textArea.setText(text);
    }

    public String getText() {
        return textArea.getText();
    }

    public JavaFile getJavaFile() {
        return jfile;
    }

    public SummaryJavaFile getSummaryJavaFile() {
        return modelFactory.getSummaryJavaFile(fileName);
    }

    public void insert(String text) {
        textArea.insert(text, getCaretPosition());
    }

    public void setCaretPosition(int pos) {
        textArea.setCaretPosition(pos);
    }

    public int getCaretPosition() {
        return textArea.getCaretPosition();
    }

    /*
    public void setCaretPosition(int line, int column) {
        textArea.setCaretPosition(getPosition(line, column));
    }            

    private int getPosition(int line, int column) {
        int pos = 0;
        if (getText().startsWith("\n")) {
            line--;
            column++;
        }
        for (int i = 1; i < line; i++) {
            if (pos < getText().length()) {
                pos = getText().indexOf("\n", pos + 1);
            }
        }
        int end = pos + column - 1;
        for (int i = pos; i < end; i++) {
            if (getText().charAt(i) == '\t') {
                pos = pos + 4;
            } else {
                pos = pos + 1;
            }
        }
        return pos;
    }
    */

    public void showCursor(Point point) {
        if (isSelected()) {
            return;
        }

        int offset = textArea.viewToModel(point);
        Highlighter h = textArea.getHighlighter();
        if (cursor != null) {
            h.removeHighlight(cursor);
            cursor = null;
        }
        DefaultHighlighter.DefaultHighlightPainter p;
        p = new DefaultHighlighter.DefaultHighlightPainter(Color.gray);
        try {
            cursor = h.addHighlight(offset, offset + 1, p);
        } catch (BadLocationException e) { }
    }

    public void setLastModified(long time) {
        lastModifiedTime = time;
    }

    public long getLastModified() {
        return lastModifiedTime;
    }

    public void cut() {
        textArea.cut();
        textArea.requestFocus();
    }

    public void copy() {
        textArea.copy();
        textArea.requestFocus();
    }

    public void paste() {
        textArea.paste();
        textArea.requestFocus();
    }

    public void selectAll() {
        textArea.selectAll();
        textArea.requestFocus();
    }

    public int findUpward(String word, int from) {
        String content = getText();
        int location = content.lastIndexOf(word, from);
        if (location != -1) {
            textArea.select(location, location + word.length());
            textArea.requestFocus();
        }
        return location;
    }

    public int findDownward(String word, int from) {
        String content = getText();
        int location = content.indexOf(word, from);
        if (location != -1) {
            textArea.select(location, location + word.length());
            textArea.requestFocus();
        }
        return location;
    }

    public void setChange(boolean bool) {
        textChanged = bool;
        if (textChanged) {
            modelFactory.removeParsedSummaryFile(fileName);
            modelFactory.parseEachSummaryFile(fileName, getText());
            jfile = null;
        }
    }

    public boolean hasChanged() {
        return textChanged;
    }

    public void insertUpdate(DocumentEvent evt) {
        setChange(true);
    }

    public void removeUpdate(DocumentEvent evt) {
        setChange(true);
    }

    public void changedUpdate(DocumentEvent evt) {
    }

    public void focusGained(FocusEvent evt) {
        Highlighter h = textArea.getHighlighter();
        h.removeAllHighlights();
    }

    public void focusLost(FocusEvent evt) {
        Highlighter h = textArea.getHighlighter();
        DefaultHighlighter.DefaultHighlightPainter p;
        p = new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);
        try {
            h.addHighlight(textArea.getSelectionStart(), textArea.getSelectionEnd(), p);
        } catch (BadLocationException e) { }
    }

    public void caretUpdate(CaretEvent evt) {
        int caretDot = evt.getDot();
        int caretMark = evt.getMark();

        if (caretDot == caretMark) {
            selectionStart = caretDot;
            selectionEnd = caretDot;
        } else {
            selectionStart = textArea.getSelectionStart();
            selectionEnd = textArea.getSelectionEnd();
        }

        positionPane.setText("(" + getLine(caretDot) + "," + getColumn(caretDot) + ")");
        this.validate();
    }

    public int getLine(int pos) {
        int line = -1;
        try {
            line = textArea.getLineOfOffset(pos) + 1;
        } catch(BadLocationException e) { }
        return line;
    }

    public int getColumn(int pos) {
        int column = -1;
        try {
            int line = textArea.getLineOfOffset(pos);
            column = pos - textArea.getLineStartOffset(line) + 1;
        } catch(BadLocationException e) { }
        return column;
    }

    public int getSelectionStart() {
        return selectionStart;
    }

    public int getSelectionEnd() {
        return selectionEnd;
    }

    public boolean isSelected() {
        if (getSelectionStart() != getSelectionEnd()) {
            return true;
        }
        return false;
    }

    public void close() {
        modelFactory.removeParsedSummaryFile(fileName);
    }

    public boolean parseBeforeRefactoring() {
        if (jfile == null) {
            jfile = modelFactory.parseEachFile(fileName, getText());
        }
        return jfile.isValid();
    }
}
