/*
 *     CutCopyPasteMenu.java  Jan 10, 2002
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import javax.swing.*;
import java.awt.event.*;

public class CutCopyPasteMenu {
    private TabbedTextPane tabbedTextPane;
    private JMenuItem pasteItem;

    public CutCopyPasteMenu(JComponent menu, TabbedTextPane pane) {
        tabbedTextPane = pane;
        init(menu);
    }

    private void init(JComponent menu) {
        JMenuItem cutItem = new JMenuItem("Cut", 'T');
        cutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                TextPane textPane = tabbedTextPane.getCurrentTextPane();
                textPane.cut();
            }
        });

        JMenuItem copyItem = new JMenuItem("Copy", 'C');
        menu.add(copyItem);
        copyItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                TextPane textPane = tabbedTextPane.getCurrentTextPane();
                textPane.copy();
            }
        });

        pasteItem = new JMenuItem("Paste", 'P');
        pasteItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                TextPane textPane = tabbedTextPane.getCurrentTextPane();
                textPane.paste();
            }
        });

        menu.add(cutItem);
        menu.add(copyItem);
        menu.add(pasteItem);
        menu.add(new JSeparator());
    }
}
