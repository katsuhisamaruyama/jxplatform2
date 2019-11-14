
import javax.swing.*;
import java.awt.*;

public class Sample16 {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Swing Sample");
        Container contentPane = frame.getContentPane();
        
        CanvasA canvas = new CanvasA();
        contentPane.add(canvas);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}

class CanvasA extends JPanel {
    Font font;
    
    CanvasA() {
        setBackground(Color.white);
        setPreferredSize(new Dimension(700, 500));
        font = new Font("SanSerif", Font.ITALIC, 18);
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.setColor(Color.red);
        g.setFont(font);
        g.drawString("Java", 200, 100);
        
        g.setColor(Color.blue);
        g.drawLine(10, 10, 690, 490);
    }
}
