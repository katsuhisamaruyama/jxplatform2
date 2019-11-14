
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Sample14 {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Swing Sample");
        Container contentPane = frame.getContentPane();
        
        PanelB panel = new PanelB();
        contentPane.add(panel);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(200, 80);
        frame.setVisible(true);
    }
}

class PanelB extends JPanel {
    PanelB() {
        setLayout(new BorderLayout());
        
        final JLabel label = new JLabel("Enjoy Java.");
        add(label, BorderLayout.NORTH);
        
        JButton button = new JButton("Push");
        add(button, BorderLayout.CENTER);
        
        button.addActionListener(new ActionListener() {
            private boolean flag = true;
            public void actionPerformed(ActionEvent e) {
                if (flag == true) {
                    label.setText("Enjoy Swing.");
                    flag = false;
                } else {
                    label.setText("Enjoy Java.");
                    flag = true;
                }
            }
        });
    }
}
