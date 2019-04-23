/*
 *     History2Text.java  Jan 18, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

import jp.ac.ritsumei.cs.fse.jrt.gui.RefactoringRecord;
import java.util.*;
import java.io.*;

public class History2Text {
    private static final String HISTORY_NAME = "JRB.history";
    private List records;

    public static void main(String args[]) {
        if (args.length > 0) {
            History2Text exe = new History2Text(args[0]);
        } else {
            History2Text exe = new History2Text();
        }
    }

    public History2Text() {
        print(HISTORY_NAME);
    }

    public History2Text(String name) {
        print(name);
    }

    private void print(String fileName) {
        if (!load(fileName)) {
            System.out.println("Fail to load history information" + fileName);
        }

        Iterator it = records.iterator();
        while (it.hasNext()) {
            LinkedList sequence = (LinkedList)it.next();
            printRecordInSequence(sequence);
        }
    }

    private void printRecordInSequence(LinkedList sequence) {
        Iterator it = sequence.iterator();
        while (it.hasNext()) {
            RefactoringRecord record = (RefactoringRecord)it.next();
            System.out.println(record.getLog());
        }
    }

    public boolean load(String fileName) {
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(fileName));
            ObjectInputStream ois = new ObjectInputStream(is);
            records = (ArrayList)ois.readObject();
            is.close();
        } catch (ClassNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }        
}
