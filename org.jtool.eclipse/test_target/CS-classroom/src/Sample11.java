
import javax.swing.*;
import java.awt.*;

public class Sample11 {
    public static void main(String[] args) {
        
        JFrame frame = new JFrame("Swing Sample");
        Container contentPane = frame.getContentPane();
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        contentPane.add(panel);
        
        JLabel label = new JLabel("Enjoy Swing.");
        panel.add(label, BorderLayout.NORTH);
        
        JButton button = new JButton("Push");
        panel.add(button, BorderLayout.CENTER);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(200, 80);
        frame.setVisible(true);
    }
}
