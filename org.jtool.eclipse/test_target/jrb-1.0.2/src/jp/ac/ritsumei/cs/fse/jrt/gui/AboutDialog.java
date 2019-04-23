/*
 *     AboutDialog.java  Nov 3, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AboutDialog extends JDialog {
    private JFrame frame;
    private Container contentPane;

    public AboutDialog(JFrame frame) {
        super(frame, "About", true);
        this.frame = frame;
        this.setResizable(false);

        createPanes();
    }

    private void createPanes() {
        contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.setBackground(Color.white);

        ImageIcon icon = new ImageIcon(getClass().getResource("images/about.gif"));
        JLabel image = new JLabel(icon);
        contentPane.add(image, "North");
        image.setLayout(null);

        JButton okButton = new JButton("Ok");
        okButton.setLocation(280, 15);
        okButton.setSize(okButton.getPreferredSize());
        image.add(okButton);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setVisible(false);
                dispose();
            }
        });

        JLabel versionLabel = new JLabel("Version " + Version.get());
        versionLabel.setLocation(243, 72);
        versionLabel.setSize(versionLabel.getPreferredSize());
        versionLabel.setForeground(Color.darkGray);
        image.add(versionLabel);

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        contentPane.add(panel, "Center");
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JTextArea information = new JTextArea(getInformationText());
        information.setEditable(false);
        information.setFont(new Font("Dialog", Font.BOLD, 12));
        information.setForeground(Color.darkGray);
        information.setBorder(BorderFactory.createEmptyBorder());
        information.setMargin(new Insets(5, 5, 5, 5));
        information.setOpaque(false);
        panel.add(information);

        this.pack();
        this.validate();
        okButton.requestFocus();
    }

    private String getInformationText() {
        StringBuffer buf = new StringBuffer();

        buf.append("General Manager: Katsuhisa Maruyama\n");
        buf.append("Contributors: Takashi Adachi\n");
        buf.append("                          Hisato Imanishi\n");
        buf.append("                          Tatsuya Kageyama\n");
        buf.append("                          Akihiko Kakimoto\n");
        buf.append("                          Seisuke Shimizu\n");
        buf.append("                          Katsunobu Takahashi\n");
        buf.append("                          Shota Ueno\n");
        buf.append("                          Katsuhiko Yoshikawa\n");
        buf.append("Support:\n");
        buf.append("  Information-technology Promotion Agency (IPA)\n");
        buf.append("  Research Institute of Software Engineering (RISE)\n");
        buf.append("  Ritsumeikan University (Rits)\n");
        buf.append("\n");
        buf.append("          Copyright IPA and Rits 2001-2002\n");
        buf.append("       http://refactoring.fse.cs.ritsumei.ac.jp/\n");
        buf.append("       mailto:refactoring@fse.cs.ritsumei.ac.jp");

        return buf.toString();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        AboutDialog dialog = new AboutDialog(frame);

        frame.pack();
        frame.validate();
        frame.setVisible(true);
        
        dialog.setVisible(true);
        dialog.dispose();
    }
}
