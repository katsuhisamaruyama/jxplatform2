
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Sample19 {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Swing Sample");
        Container contentPane = frame.getContentPane();
        
        final DialogA dialog = new DialogA(frame, "Swing9 Dialog", "Can you see me?");
        
        JButton button = new JButton("Question");
        contentPane.add(button);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(true);
            }
        });
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        
        frame.setSize(300, 100);
        frame.setVisible(true);
    }
}

class DialogA extends JDialog {
    DialogA(Frame frame, String title, String msg) {
        super(frame, title, true);
        Container contentPane = getContentPane();
        
        JButton yesButton = new JButton("Yes");
        contentPane.add(yesButton, BorderLayout.EAST);
        yesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        
        JLabel label = new JLabel(msg, JLabel.CENTER);
        contentPane.add(label, BorderLayout.CENTER);
        pack();
    }
}
