/*
 * An example answer
 * Copyright 2014, Katsuhisa Maruyama
 */

package jp.ac.ritsumei.cs.draw;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Locale;

/**
 * Implements a drawing tool.
 */
public class DrawTool extends JFrame {
    
    private static final long serialVersionUID = -88007108717580447L;
    
    /**
     * The tabbed canvas that is the collection of canvases on which figures are drawn.
     */
    private TabbedCanvas tabbedCanvas;
    
    /**
     * The menu that contains menu items.
     */
    private DrawMenu menu;
    
    /**
     * The selector that allows a user to select one of the predefined figures.
     */
    private FigureSelector selector;
    
    /**
     * Creates an application for a drawing tool.
     * @param title the title of this application that are shown in the top of the application window
     */
    public DrawTool(String title) {
        super(title);
        
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        
        JPanel panel = new JPanel(new BorderLayout());
        contentPane.add(panel, BorderLayout.NORTH);
        
        selector = new FigureSelector();
        panel.add(selector, BorderLayout.EAST);
        
        tabbedCanvas = new TabbedCanvas(selector);
        contentPane.add(tabbedCanvas, BorderLayout.CENTER);
        
        menu = new DrawMenu(this, tabbedCanvas);
        setJMenuBar(menu.getMenuBar());
        panel.add(menu.getToolBar(), BorderLayout.WEST);
        
        tabbedCanvas.setDrawMenu(menu);
        tabbedCanvas.init();
    }
    
    /**
     * Intends to close the application window.
     */
    private void closeWindow() {
        if (menu != null) {
            menu.closeFile();
        }
    }
    
    /**
     * Terminates the application.
     */
    public void terminate() {
        System.exit(0);
    }
    
    public static void main(String[] args) {
        Locale.setDefault(Locale.ENGLISH);
        final DrawTool main = new DrawTool(FigureManager.UNTITLED);
        
        main.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                main.closeWindow();
            }
        });
        
        main.pack();
        main.validate();
        main.setVisible(true);
    }
}
