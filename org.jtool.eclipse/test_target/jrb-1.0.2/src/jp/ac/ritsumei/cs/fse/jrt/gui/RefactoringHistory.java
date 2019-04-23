/*
 *     RefactoringHistory.java  Jan 15, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import java.util.*;
import java.io.*;

public class RefactoringHistory {
    private static RefactoringHistory singleton = new RefactoringHistory();
    private static final String HISTORY_NAME = "JRB.history";
    private TabbedTextPane tabbedTextPane;
    private AllHistoryDialog historyDialog;
    private List records;
    private LinkedList memories;
    private HashMap memoriesMap;
    private List recordsMatched1 = new ArrayList();
    private List recordsMatched2 = new ArrayList();

    private RefactoringHistory() {
        if (!load()) {
            // System.out.println("NOT FOUND HISTORY");
            records = new ArrayList();
        }
        memories = new LinkedList();
        memoriesMap = new HashMap();
    }

    public static RefactoringHistory getInstance() {
        return singleton;
    }

    public void setTabbedTextPane(TabbedTextPane pane) {
        tabbedTextPane = pane;
    }

    public void setRefactoringDialog(AllHistoryDialog dialog) {
        historyDialog = dialog;
    }

    public void clear() {
        records.clear();
        memories.clear();
    }

    private List getDisplyedRecords(List collection) {
        HashMap maps = new HashMap();
        Iterator it = collection.iterator();
        while (it.hasNext()) {
            RefactoringRecord record = (RefactoringRecord)it.next();
            String command = record.getCommandWithoutID().intern();

            DisplayedCommand dcommand = (DisplayedCommand)maps.get(command);
            if (dcommand != null) {
                dcommand.increment();
            } else {
                maps.put(command, new DisplayedCommand(command));
            }
        }

        return sortDisplayedCommand(maps.values());
    }

    private List sortDisplayedCommand(Collection collection) {
        Object[] commands = collection.toArray();
        Arrays.sort(commands, new ComparatorByNum());

        List dcommands = new ArrayList();
        for (int index = 0; index < commands.length && index < 5; index++) {
            DisplayedCommand dcommand = (DisplayedCommand)commands[index];
            dcommands.add(dcommand.getCommand());
        }
        return dcommands;
    }

    public List getRecordsMatchedLastOne() {
        return getDisplyedRecords(recordsMatched1);
    }

    public List getRecordsMatchedLastTwo() {
        return getDisplyedRecords(recordsMatched2);
    }

    public void memorize(TextPane textPane) {
        TextUndoManager undoManager = textPane.getUndoManager();

        LinkedList sequance = (LinkedList)memoriesMap.get(undoManager);
        if (sequance != null) {
            memories.remove(sequance);
        }

        LinkedList nsequance = undoManager.getRefactoringCommands();
        if (nsequance.size() != 0) {
            memories.add(new LinkedList(nsequance));
            memoriesMap.put(undoManager, nsequance);
        }

        historyDialog.update();
    }

    private void recordAllMomories() {
        Iterator it = memories.iterator();
        while (it.hasNext()) {
            LinkedList sequence = (LinkedList)it.next();
            records.add(sequence);
        }
    }

    public List collectAllSequences() {
        List sequences = new ArrayList();
        // collectRecordsInProgress(sequences);
        sequences.addAll(records);
        sequences.addAll(memories);
        return sequences;
    }

    /*
    private void collectRecordsInProgress(List collection) {
        Iterator it = tabbedTextPane.getTexts().iterator();
        while (it.hasNext()) {
            TextPane textPane = (TextPane)it.next();
            TextUndoManager undoManager = textPane.getUndoManager();
            LinkedList sequence = undoManager.getRefactoringCommands();
            if (sequence.size() != 0) {
                collection.add(undoManager.getRefactoringCommands());
            }
        }
    }
    */

    public void retrieve(RefactoringRecord record1, RefactoringRecord record2) {
        recordsMatched1.clear();
        recordsMatched2.clear();

        Iterator it = collectAllSequences().iterator();
        while (it.hasNext()) {
            LinkedList sequence = (LinkedList)it.next();
            retrieveRecordInSequence(sequence, record1, record2);
        }
    }

    private void retrieveRecordInSequence(LinkedList sequence,
      RefactoringRecord record1, RefactoringRecord record2) {
        RefactoringRecord last1 = null;
        RefactoringRecord last2 = null;

        Iterator it = sequence.iterator();
        while (it.hasNext()) {
            RefactoringRecord record = (RefactoringRecord)it.next();
            if (record1 != null && last1 != null && match(record1, last1)) {
                if (record2 != null && last2 != null && match(record2, last2)) {
                    recordsMatched2.add(record);
                } else {
                    recordsMatched1.add(record);
                }
            }
            last2 = last1;
            last1 = record;
        }
    }
       
    private boolean match(RefactoringRecord ra, RefactoringRecord rb) {
        if (ra.getCommandWithoutID().compareTo(rb.getCommandWithoutID()) == 0) {
            return true;
        }
        return false;
    }

    public boolean load() {
        try {
            String historyName = JRBProperties.getProperty("Application.Dir")
              + File.separator + HISTORY_NAME;
            InputStream is = new BufferedInputStream(new FileInputStream(historyName));
            ObjectInputStream ois = new ObjectInputStream(is);
            records = (ArrayList)ois.readObject();
            is.close();

        } catch (ClassNotFoundException e) {
            // System.out.println("CLASS NOT FOUND");
            return false;
        } catch (IOException e) {
            // System.out.println("IO EXCEPTION");
            return false;
        }
        return true;
    }        

    public boolean store() {
        recordAllMomories();

        try {
            String historyName = JRBProperties.getProperty("Application.Dir")
              + File.separator + HISTORY_NAME;
            OutputStream os = new BufferedOutputStream(new FileOutputStream(historyName));
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(records);
            oos.flush();
            os.close();

        } catch (IOException e) {
            // System.out.println("IO EXCEPTION");
            return false;
        }
        return true;
    }        
}

class DisplayedCommand {
    private String command;
    private int num;

    DisplayedCommand(String command) {
        this.command = command;
        num = 0;
    }

    void increment() {
        num++;
    }

    String getCommand() {
        return command;
    }

    int getNum() {
        return num;
    }

    public String toString() {
        return command + " " + num;
    }
}

class ComparatorByNum implements Comparator {
    public int compare(Object obj1, Object obj2) {
        DisplayedCommand command1 = (DisplayedCommand)obj1;
        DisplayedCommand command2 = (DisplayedCommand)obj2;

        return command2.getNum() - command1.getNum();
    }
}
