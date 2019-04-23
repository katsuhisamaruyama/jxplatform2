/*
 *     AllHistoryDialog.java  Jan 15, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import jp.ac.ritsumei.cs.fse.jrt.util.LogEvent;
import jp.ac.ritsumei.cs.fse.jrt.util.LogEventListener;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Comparator;

public class AllHistoryDialog extends HistoryDialog implements LogEventListener {
    private RefactoringHistory history;
    private List sequences;
    private Object[] refactorings;
    private JRadioButton sequenceButton;
    private JRadioButton commandButton;
    private JRadioButton fileButton;    
    private JRadioButton userButton;
    private JRadioButton timeButton;    

    public AllHistoryDialog(JFrame frame, RefactoringHistory hist) {
        super(frame, "History", false);
        this.frame = frame;
        history = hist;

        contentPane = this.getContentPane();
        font = new Font("Dialog", Font.PLAIN, 12);

        JScrollPane scPane = createTextPane(20, 80);
        contentPane.add(scPane, "Center");

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        createSelectionButtons(panel);
        createCloseButton(panel);
        contentPane.add(panel, "South");
        
        this.pack();
        this.validate();
        
        updateSequence();
        setSequences();
    }

    public void printMessage(LogEvent evt) {
        update();
    }

    public void update() {
        updateSequence();
        if (sequenceButton.isSelected()) {
            setSequences();
        } else if (commandButton.isSelected()) {
            updateByCommand();
        } else if (fileButton.isSelected()) {
            updateByFile();
        } else if (userButton.isSelected()) {
            updateByUser();
        } else {
            updateByTime();
        }
    }

    private void updateSequence() {
        sequences = history.collectAllSequences();

        List collection = new ArrayList();
        Iterator it = sequences.iterator();
        while (it.hasNext()) {
            LinkedList sequence = (LinkedList)it.next();
            collection.addAll(sequence);
        }
        refactorings = collection.toArray();
    }

    private void setSequences() {
        StringBuffer buf = new StringBuffer();
        Iterator it = sequences.iterator();
        while (it.hasNext()) {
            LinkedList sequence = (LinkedList)it.next();

            Iterator ir = sequence.iterator();
            if (ir.hasNext()) {
                RefactoringRecord record = (RefactoringRecord)ir.next();
                buf.append(record.getCommandWithoutID());
            }
            while (ir.hasNext()) {
                RefactoringRecord record = (RefactoringRecord)ir.next();
                buf.append(" -> ");
                buf.append(record.getCommandWithoutID());
            }
            buf.append("\n");
        }
        setText(buf.toString());
    }

    private void setRecords() {
        StringBuffer buf = new StringBuffer();
        for (int index = 0; index < refactorings.length; index++) {
            RefactoringRecord record = (RefactoringRecord)refactorings[index];
            if (!record.isInProgress()) {
                buf.append("*");
            }
            buf.append(record.getLog());
            buf.append("\n");
        }
        setText(buf.toString());
    }

    private void updateByCommand() {
        Arrays.sort(refactorings, new ComparatorByCommand());
        setRecords();
    }

    private void updateByFile() {
        Arrays.sort(refactorings, new ComparatorByFile());
        setRecords();
    }

    private void updateByUser() {
        Arrays.sort(refactorings, new ComparatorByUser());
        setRecords();
    }

    private void updateByTime() {
        Arrays.sort(refactorings, new ComparatorByTime());
        setRecords();
    }

    private void createSelectionButtons(JPanel panel) {
        ButtonGroup grp = new ButtonGroup();

        sequenceButton = new JRadioButton("Sequence");
        sequenceButton.setMnemonic('S');
        panel.add(sequenceButton);
        grp.add(sequenceButton);
        sequenceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setSequences();
            }
        });

        commandButton = new JRadioButton("Command");
        commandButton.setMnemonic('C');
        panel.add(commandButton);
        grp.add(commandButton);
        commandButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateByCommand();
            }
        });

        fileButton = new JRadioButton("File");
        fileButton.setMnemonic('F');
        panel.add(fileButton);
        grp.add(fileButton);
        fileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateByFile();
            }
        });

        userButton = new JRadioButton("User");
        userButton.setMnemonic('U');
        panel.add(userButton);
        grp.add(userButton);
        userButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateByUser();
            }
        });

        timeButton = new JRadioButton("Time");
        timeButton.setMnemonic('T');
        panel.add(timeButton);
        grp.add(timeButton);
        timeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateByTime();
            }
        });

        sequenceButton.setSelected(true);
    }

    private void createCloseButton(JPanel panel) {
        JButton closeButton = new JButton("Close");
        panel.add(closeButton);
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setVisible(false);
            }
        });

        closeButton.requestFocus();
    }
}

class ComparatorByCommand implements Comparator {
    public int compare(Object obj1, Object obj2) {
        RefactoringRecord record1 = (RefactoringRecord)obj1;
        RefactoringRecord record2 = (RefactoringRecord)obj2;
        return record1.getCommand().compareTo(record2.getCommand());
    }
}

class ComparatorByFile implements Comparator {
    public int compare(Object obj1, Object obj2) {
        RefactoringRecord record1 = (RefactoringRecord)obj1;
        RefactoringRecord record2 = (RefactoringRecord)obj2;
        return record1.getFileName().compareTo(record2.getFileName());
    }        
}

class ComparatorByUser implements Comparator {

    public int compare(Object obj1, Object obj2) {
        RefactoringRecord record1 = (RefactoringRecord)obj1;
        RefactoringRecord record2 = (RefactoringRecord)obj2;
        return record1.getUseName().compareTo(record2.getUseName());
    }        
}

class ComparatorByTime implements Comparator {
    public int compare(Object obj1, Object obj2) {
        RefactoringRecord record1 = (RefactoringRecord)obj1;
        RefactoringRecord record2 = (RefactoringRecord)obj2;

        if (record1.getTimestamp().equals(record2.getTimestamp())) {
            return 0;
        } else if (record1.getTimestamp().before(record2.getTimestamp())) {
            return -1;
        } else {
            return 1;
        }
    }        
}
