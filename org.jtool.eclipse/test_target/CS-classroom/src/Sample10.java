
import javax.swing.*;
import java.awt.*;

public class Sample10 {
    public static void main(String[] args) {
        
        JFrame frame = new JFrame("Swing Sample");
        Container contentPane = frame.getContentPane();
        
        JLabel label = new JLabel("Enjoy Swing.", JLabel.CENTER);
        contentPane.add(label);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(200, 100);
        frame.setVisible(true);
    }
}
