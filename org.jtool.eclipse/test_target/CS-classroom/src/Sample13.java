
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Sample13 {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Swing Sample");
        Container contentPane = frame.getContentPane();
        
        PanelA panel = new PanelA();
        contentPane.add(panel);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(200, 80);
        frame.setVisible(true);
    }
}

class PanelA extends JPanel implements ActionListener {
    private JLabel label;
    private boolean flag = true;
    
    PanelA() {
        setLayout(new BorderLayout());
        
        label = new JLabel("Enjoy Java.");
        add(label, BorderLayout.NORTH);
        
        JButton button = new JButton("Push");
        add(button, BorderLayout.CENTER);
        
        button.addActionListener(this);
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
