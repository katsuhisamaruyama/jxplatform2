
import javax.swing.*;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Sample15 {
    public static void main(String[] args) {
        
        JFrame frame = new JFrame("Swing Sample");
        Container contentPane = frame.getContentPane();
        
        JLabel label = new JLabel("Enjoy Swing.", JLabel.CENTER);
        contentPane.add(label);
        
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                System.out.println("Bye!");
            }
        });
        
        frame.setSize(200, 100);
        frame.setVisible(true);
    }
}
