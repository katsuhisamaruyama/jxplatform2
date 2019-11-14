
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Sample17 {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Swing Sample");
        Container contentPane = frame.getContentPane();
        
        CanvasB canvas = new CanvasB();
        contentPane.add(canvas);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}

class CanvasB extends JPanel implements MouseListener {
    final static int RADIUS = 20;
    private int x = -1;
    private int y;
    
    CanvasB() {
        setBackground(Color.white);
        setPreferredSize(new Dimension(700, 500));
        addMouseListener(this);
    }
    
    public void mouseEntered(MouseEvent e) { }
    
    public void mouseExited(MouseEvent e) { }
    
    public void mousePressed(MouseEvent e) { }
    
    public void mouseReleased(MouseEvent e) { }
    
    public void mouseClicked(MouseEvent e) {
        x = e.getX();
        y = e.getY();
        repaint();
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (x != -1) {
            g.setColor(Color.blue);
            g.drawOval(x - RADIUS, y - RADIUS, RADIUS * 2, RADIUS * 2);
        }
    }
}
