/*
 *     CorrespondDialog.java  Dec 20, 2001
 *
 *     Akihiko Kakimoto (kaki@fse.cs.ritsumei.ac.jp)
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.dialog;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Stack;
import java.util.HashMap;
import java.util.Iterator;

public class CorrespondDialog extends JDialog {
    private JFrame frame;
    private Container contentPane;
    private ArrayList caseList;
    private ArrayList classList;
    private Font font;
    private CorrespondListPane casePane;
    private CorrespondListPane classPane;
    private Stack caseStack = new Stack();
    private Stack classStack = new Stack();
    private Object caseSel = null;
    private Object classSel = null;
    private JButton addButton;
    private JButton undoButton;
    private JButton cancelButton;
    private HashMap correspondence = new HashMap();

    public CorrespondDialog(JFrame frame, String title, ArrayList ca, ArrayList cl) {
        super(frame, title, true);
        this.frame = frame;
        caseList = new ArrayList(ca);
        classList = new ArrayList(cl);
        createPanes("Select two corresponding items, and then push add button:");
        setVisible(true);
    }

    private void createPanes(String mesg) {
        contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        font = new Font("Dialog", Font.PLAIN, 14);

        JPanel panel = new JPanel();
        JLabel titleLabel = new JLabel(mesg, JLabel.CENTER);
        titleLabel.setForeground(Color.black);
        panel.add(titleLabel);
        contentPane.add(panel, "North");

        panel = new JPanel();
        createLists(panel);
        contentPane.add(panel, "Center");

        panel = new JPanel();
        createButtons(panel);
        contentPane.add(panel, "South");

        Point p = frame.getLocationOnScreen();
        this.pack();
        this.setLocation(p.x + frame.getSize().width - this.getSize().width - 10, p.y + 10);
        this.validate();
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                caseStack.clear();
                classStack.clear();
                setVisible(false);
                dispose();
            }
        });
    }

    private void createLists(JPanel panel) {
        casePane = new CorrespondListPane("Switch labels", caseList, 200);
        casePane.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                updateButtons();
            }
        });
        panel.add(casePane);

        classPane = new CorrespondListPane("Candidates for Subclasses", classList, 300);
        classPane.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                updateButtons();
            }
        });
        panel.add(classPane);
    }

    private void createButtons(final JPanel panel) {
        addButton = new JButton("Add");
        addButton.setEnabled(false);
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if(caseList.isEmpty()) {
                    setCorrespondence();
                    setVisible(false);
                    dispose();
                } else {
                    String newItem = new String((String)classSel + " >> " + (String)caseSel);

                    caseList.remove(caseSel);
                    caseStack.push(caseSel);
                    casePane.setListData(caseList);
                    
                    int index = classList.indexOf(classSel);
                    classList.remove(index);
                    classList.add(index, newItem);
                    classStack.push(classSel);
                    classPane.setListData(classList);
                    updateButtons();
                }
            }
        });

        undoButton = new JButton("Undo");
        undoButton.setEnabled(false);
        undoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (!caseStack.empty()) {
                    Object undoItem = getClassItem((String)classStack.peek());
                    if (undoItem == null) {
                        return;
                    }
                    int index = classList.indexOf(undoItem);

                    caseList.add(caseStack.pop());
                    casePane.setListData(caseList);

                    classList.remove(undoItem);
                    classList.add(index, classStack.pop());
                    classPane.setListData(classList);
                }
                updateButtons();
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                caseStack.clear();
                classStack.clear();
                setVisible(false);
                dispose();
            }
        });

        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        panel.add(addButton);
        panel.add(undoButton);
        panel.add(cancelButton);
    }

    private Object getClassItem(String prefix) {
        Iterator it = classList.iterator();
        while (it.hasNext()) {
            String item = (String)it.next();
            int sep = item.indexOf(" ");
            if (sep != -1 && prefix.compareTo(item.substring(0, sep)) == 0) {
                return item;
            }
        }
        return null;
    }

    private void updateButtons() {
        caseSel = casePane.getSelectedItem();
        classSel = classPane.getSelectedItem();

        if (classSel != null && ((String)classSel).indexOf(" ") != -1) {
            classSel = null;
        }

        if (caseSel != null && classSel != null) {
            addButton.setEnabled(true);
        } else {
            addButton.setEnabled(false);
        }

        if (!caseStack.empty()) {
            undoButton.setEnabled(true);
        } else {
            undoButton.setEnabled(false);
        }

        if(!caseList.isEmpty()) {
            addButton.setText("Add");
        } else {
            addButton.setText("Ok");
            addButton.setEnabled(true);
        }
    }

    private void setCorrespondence() {
        while (!caseStack.empty()) {
            correspondence.put(caseStack.pop(), classStack.pop());
        }        
    }

    public static HashMap show(JFrame frame, String title, ArrayList ca, ArrayList cl) {
        CorrespondDialog dialog = new CorrespondDialog(frame, title, ca, cl);
        return dialog.getCorrespondence();
    }

    public HashMap getCorrespondence() {
        return correspondence;
    }

    public static void main(String args[]) {
        JFrame mainFrame = new JFrame();
        mainFrame.setSize(100, 100);
        mainFrame.setVisible(true);

        ArrayList al1 = new ArrayList();
        al1.add("EUROPEAN");
        al1.add("AFRICAN");
        al1.add("NOWEGIAN_BLUE");
        ArrayList al2 = new ArrayList(al1);
        al2.add("AMERICAN");

        HashMap map = CorrespondDialog.show(mainFrame, "Correspond", al1, al2);

        Iterator it = al1.iterator();
        while (it.hasNext()) {
            String label = (String)it.next();
            String name = (String)map.get(label);
            System.out.println("LABEL/CLASS = " + label + " : " + name);
        }
        System.exit(0);
    }
}

class CorrespondListPane extends JPanel {
    private JList list;

    protected CorrespondListPane(String title, ArrayList al, int width) {
        this.setLayout(new BorderLayout());
        JLabel label = new JLabel(title);
        this.add(label, "North");

        list = new JList(al.toArray());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(10);
        list.setFixedCellWidth(width);
        list.setFixedCellHeight(18);
        JScrollPane scrollPane = new JScrollPane(list);
        this.add(scrollPane, "Center");
    }

    protected Object getSelectedItem() {
        return list.getSelectedValue();
    }

    protected void setListData(ArrayList al) {
        list.setListData(al.toArray());
    }

    protected void addListSelectionListener(ListSelectionListener l) {
        list.addListSelectionListener(l);
    }
}
