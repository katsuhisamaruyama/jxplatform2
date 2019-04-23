/*
 *     FileListDialog.java  Jan 6, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.dialog;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class FileListDialog extends RefactoringDialog {
    private JList list;

    public FileListDialog(JFrame frame, String mesg, List files) {
        super(frame, "File list", mesg);
        list.setListData(files.toArray());
        setVisible(true);
    }

    protected JPanel createCenterPane() {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        list = new JList();
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setVisibleRowCount(10);
        list.setFixedCellWidth(400);
        list.setFixedCellHeight(18);
        list.setFont(new Font("Dialog", Font.PLAIN, 14));
        list.setBackground(Color.white);

        JScrollPane scrollPane = new JScrollPane(list);
        box.add(scrollPane);

        list.requestFocus();
        return box;
    }

    protected void okAction() {
        setVisible(false);
        dispose();
    }

    protected void cancelAction() {
        list.clearSelection();
        setVisible(false);
        dispose();
    }

    public static List show(JFrame frame, String mesg, List files) {
        FileListDialog dialog = new FileListDialog(frame, mesg, files);
        return dialog.getSelectedFiles();
    }

    public List getSelectedFiles() {
        List files = new ArrayList();
        Object[] selectedValues = list.getSelectedValues(); 
        for (int i = 0; i < selectedValues.length; i++) {
            files.add(selectedValues[i]);
        }
        return files;
    }

    public static void main(String args[]) {
        JFrame mainFrame = new JFrame();
        mainFrame.setSize(100, 100);
        mainFrame.setVisible(true);

        List f = new ArrayList();
        f.add("test1");
        f.add("test1/test2");
        f.add("test1/test2/text3");

        List files = FileListDialog.show(mainFrame, "Files to be changed", f);
        System.out.println("NAME = " + files);
        System.exit(0);
    }
}
