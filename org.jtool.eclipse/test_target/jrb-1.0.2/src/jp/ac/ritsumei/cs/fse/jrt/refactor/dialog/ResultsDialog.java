/*
 *     ResultsDialog.java  Dec 22, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.dialog;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.DisplayedFile;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.HighlightToken;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class ResultsDialog extends JDialog {
    private JFrame frame;
    private Container contentPane;
    private JButton okButton;
    private JButton cancelButton;
    private boolean okFlag = false;

    public ResultsDialog(JFrame frame, String title, List files) {
        super(frame, title, true);
        this.frame = frame;
        createPanes("Confirm changes made to the following texts:", files);
        setVisible(true);
    }

    public static boolean show(JFrame frame, String title, List files) {
        ResultsDialog dialog = new ResultsDialog(frame, title, files);
        return dialog.isOk();
    }

    public boolean isOk() {
        return okFlag;
    }

    private void createPanes(String mesg, List files) {
        contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        JLabel titleLabel = new JLabel(mesg, JLabel.CENTER);
        titleLabel.setForeground(Color.black);
        panel.add(titleLabel);
        contentPane.add(panel, "North");

        ChangeTextsPane changeTextPane = new ChangeTextsPane(files);
        contentPane.add(changeTextPane, "Center");

        panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        createOkButton(panel);
        createCancelButton(panel);
        contentPane.add(panel, "South");

        okButton.requestFocus();

        Point p = frame.getLocationOnScreen();
        this.setLocation(p.x + 100, p.y + 100);
        this.pack();
        this.validate();
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                okFlag = false;
                setVisible(false);
                dispose();
            }
        });
    }

    private void createOkButton(JPanel panel) {
        okButton = new JButton("Ok");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                okFlag = true;
                setVisible(false);
                dispose();
            }
        });
    }

    protected void createCancelButton(JPanel panel) {
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                okFlag = false;
                setVisible(false);
                dispose();
            }
        });

        panel.add(okButton);
        panel.add(cancelButton);
    }

    public static void main(String args[]) {
        JFrame mainFrame = new JFrame();
        mainFrame.setSize(100, 100);
        mainFrame.setVisible(true);

        ArrayList al = new ArrayList();
        al.add(new DisplayedFile("/cs/fse/jrt/refactor/name1", "TEXT 1", "NEW TEXT 1"));
        al.add(new DisplayedFile("/cs/fse/jrt/name2", "TEXT 2\nTEXT 2", "NEW TEXT 2"));
        al.add(new DisplayedFile("/cs/fse/name3", "TEXT 3\nTEXT 3\nTEXT 3", "NEW TEXT 3"));

        boolean confirm = ResultsDialog.show(mainFrame, "Confirm Changes", al);
        System.out.println("Ok = " + confirm);
        System.exit(0);
    }
}

class ChangeTextsPane extends JTabbedPane implements ChangeListener {
    private List files;  // DisplayedFile

    public ChangeTextsPane(List files) {
        super(JTabbedPane.TOP);
        this.files = files;
        init();

        this.addChangeListener(this);
    }

    public void init() {
        Iterator it = files.iterator();
        while (it.hasNext()) {
            DisplayedFile file = (DisplayedFile)it.next();

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());

            JSplitPane textPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            textPane.setContinuousLayout(true);
            textPane.setDividerSize(2);
            panel.add(textPane, "Center");

            JTextArea oldTextPane = createTextPane(file.getOldText());
            setHighlights(oldTextPane, file.getOldHighlight());
            textPane.setLeftComponent(createScrollTextPane(oldTextPane, "Before"));

            JTextArea newTextPane = createTextPane(file.getNewText());
            setHighlights(newTextPane, file.getNewHighlight());
            textPane.setRightComponent(createScrollTextPane(newTextPane, "After"));

            JTextField filenamePane = new JTextField();
            filenamePane.setText("  " + file.getName());
            filenamePane.setEditable(false);
            filenamePane.setBorder(BorderFactory.createEmptyBorder());
            filenamePane.setFont(new Font("Dialog", Font.PLAIN, 14));
            filenamePane.setOpaque(false);
            panel.add(filenamePane, "South");

            this.addTab(file.getShortName(), panel);
        }
    }

    private JTextArea createTextPane(String text) {
        JTextArea textArea = new JTextArea(28, 70);
        textArea.setText(text);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setSelectionColor(Color.gray);
        textArea.setBackground(Color.white);
        textArea.setTabSize(4);
        textArea.setLineWrap(true);
        textArea.setMargin(new Insets(5, 5, 5, 5));
        textArea.setCaretPosition(0);

        return textArea;
    }

    private JScrollPane createScrollTextPane(JTextArea textArea, String title) {
        JScrollPane scTextArea = new JScrollPane(textArea);
        scTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scTextArea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        TitledBorder border = new TitledBorder(title);
        border.setTitlePosition(TitledBorder.ABOVE_TOP);
        border.setTitleColor(Color.red);
        scTextArea.setBorder(border);

        return scTextArea;
    }

    private void setHighlights(JTextArea textArea, List highlights) {
        Highlighter h = textArea.getHighlighter();
        DefaultHighlighter.DefaultHighlightPainter p;

        Iterator it = highlights.iterator();
        while (it.hasNext()) {
            HighlightToken ht = (HighlightToken)it.next();
            p = new DefaultHighlighter.DefaultHighlightPainter(ht.getColor());

            try {
                h.addHighlight(ht.getBegin(), ht.getEnd(), p);
            } catch (BadLocationException e) { }
        }
    }

    public void stateChanged(ChangeEvent evt) {
        JTabbedPane src = (JTabbedPane)evt.getSource();
        int sel = src.getSelectedIndex();
    }
}
