
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Sample12 {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Swing Sample");
        Container contentPane = frame.getContentPane();
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        contentPane.add(panel);
        
        JLabel label = new JLabel("Enjoy Java.");
        panel.add(label, BorderLayout.NORTH);
        
        JButton button = new JButton("Push");
        panel.add(button, BorderLayout.CENTER);
        
        ButtonListener bl = new ButtonListener(label);
        button.addActionListener(bl);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(200, 80);
        frame.setVisible(true);
    }
}

class ButtonListener implements ActionListener {
    private JLabel label;
    private boolean flag = true;
    
    ButtonListener(JLabel l) {
        label = l;
    }
    
    public void actionPerformed(ActionEvent e) {
        if (flag == true) {
            label.setText("Enjoy Swing.");
            flag = false;
        } else {
            label.setText("Enjoy Java.");
            flag = true;
        }
    }
}
