/*
 *     MethodListPane.java  Nov 1, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.Color;
import java.awt.Font;
import java.util.Vector;
import java.util.Iterator;

public class MethodListPane extends JScrollPane implements ListSelectionListener {
    private JFrame frame;
    private JList methodList;
    private Vector list = new Vector();

    public MethodListPane() {
        super();
    }

    public MethodListPane(JFrame frame) {
        super();
        this.frame = frame;
        methodList = new JList();
        methodList.setFont(new Font("Dialog", Font.PLAIN, 14));
        methodList.setFixedCellHeight(18);
        this.setViewportView(methodList);

        // methodList.addListSelectionListener(this);
    }

    public void buildMethodList(TextPane textPane) {
        SummaryJavaFile jfile = textPane.getSummaryJavaFile();
        if (jfile == null) {
            return;
        }        

        list.clear();
        Iterator cit = jfile.getJavaClasses().iterator();
        while (cit.hasNext()) {
            SummaryJavaClass jclass = (SummaryJavaClass)cit.next();

            Iterator mit = jclass.getJavaMethods().iterator();
            while (mit.hasNext()) {
                SummaryJavaMethod jmethod = (SummaryJavaMethod)mit.next();
                list.add(new MethodName(jmethod));
            }
        }

        methodList.setListData(list);
        methodList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public void valueChanged(ListSelectionEvent evt) {
        /*
        JList src = (JList)evt.getSource();
        MethodName name = (MethodName)src.getSelectedValue();
        SummaryJavaMethod jmethod = name.getJavaMethod();
        */
    }
}

class MethodName {
    private SummaryJavaMethod jmethod;

    public MethodName(SummaryJavaMethod jm) {
        jmethod = jm;
    }

    public SummaryJavaMethod getJavaMethod() {
        return jmethod;
    }

    public String toString() {
        return jmethod.getJavaClass().getName() + "#" + jmethod.getMethodInfo();
    }
}
