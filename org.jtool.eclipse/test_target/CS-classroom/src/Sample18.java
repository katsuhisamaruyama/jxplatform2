
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Sample18 {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Swing Sample");
        Container contentPane = frame.getContentPane();
        
        JMenuBar menubar = new JMenuBar();
        frame.setJMenuBar(menubar);
        
        JMenu fileMenu = new JMenu("File");
        menubar.add(fileMenu);
        
        JMenuItem openItem = new JMenuItem("Open...");
        fileMenu.add(openItem);
        JMenuItem saveItem = new JMenuItem("Save");
        fileMenu.add(saveItem);
        
        fileMenu.addSeparator();
        
        JMenuItem exitItem = new JMenuItem("Exit");
        fileMenu.add(exitItem);
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        
        JPanel panel = new JPanel();
        panel.setBackground(Color.white);
        contentPane.add(panel);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);
        frame.setVisible(true);
    }
}
