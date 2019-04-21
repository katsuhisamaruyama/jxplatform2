
package jp.ac.ritsumei.cs.draw;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;

import java.util.List;
import java.util.ArrayList;

public class TabbedCanvas extends JTabbedPane implements ChangeListener {
    
    private static final long serialVersionUID = -8288277746120385281L;
    
    /**
     * The collection of canvases.
     */
    private List<DrawCanvas> canvases;
    
    /**
     * The index indicating a tab the user has selected.
     */
    private int selectedTab = 0;
    
    /**
     * The menu binding to a canvas.
     */
    private DrawMenu menu;
    
    /**
     * The selector binding to a canvas.
     */
    private FigureSelector selector;
    
    /**
     * Creates a tabbed canvas.
     * @param selector the selector binding to a canvas
     */
    TabbedCanvas(FigureSelector selector) {
        super(JTabbedPane.TOP);
        
        this.selector = selector;
        canvases = new ArrayList<DrawCanvas>();
    }
    
    /**
     * Sets a menu binding to a canvas.
     * @param menu the menu
     */
    void setDrawMenu(DrawMenu menu) {
        this.menu = menu;
    }
    
    /**
     * Returns the canvas currently selected. 
     * @return the currently-selected canvas
     */
    DrawCanvas getCurrentCanvas() {
        if (canvases.size() != 0 && selectedTab < canvases.size()) {
            return (DrawCanvas)canvases.get(selectedTab);
        }
        return null;
    }
    
    /**
     * Obtains all the canvases managed by this tabbed canvas.
     * @return the collection of all the canvases.
     */
    List<DrawCanvas> getAllCanvases() {
        return canvases;
    }
    
    /**
     * Retrieves a canvas binding to a given file name.
     * @param filename the file name to be chacked
     * @return the found canvas, or <code>null</code> if there is no canvas found
     */
    DrawCanvas getCanvas(String filename) {
        for (DrawCanvas canvas : canvases) {
            if (canvas.getFigureManager().getFilename().equals(filename)) {
                return canvas;
            }
        }
        return null;
    }
    
    /**
     * Initializes this tabbled canvas.
     */
    void init() {
        newTab(null);
        addChangeListener(this);
    }
    
    /**
     * Opens a new canvas.
     * @param filename the name of a file binding to the opened canvas
     * @return the opened canvas
     */
    DrawCanvas openTab(String filename) {
        return newTab(filename);
    }
    
    /**
     * Creates a new canvas and its tab.
     * @param filename the name of a file binding to the created canvas
     */
    private DrawCanvas newTab(String filename) {
        DrawCanvas canvas = new DrawCanvas(menu, selector, filename);
        UndoManager undoManager = new UndoManager();
        canvas.addUndoableEditListener(undoManager);
        canvas.setUndoManager(undoManager);
        
        FigureManager fmanager = canvas.getFigureManager();
        addTab(fmanager.getTitle(), null, canvas, fmanager.getFilename());
        canvases.add(canvas);
        selectTab(canvas);
        
        return canvas;
    }
    
    /**
     * Closes a given canvas.
     * @param canvas to be closed
     */
    public void closeTab(DrawCanvas canvas) {
        selectTab(canvas);
        canvas.getFigureManager().clear();
        
        canvases.remove(selectedTab);
        remove(selectedTab);
    }
    
    /**
     * Returns the number of opened canvas.
     * @return the number of opened canvas
     */
    int getOpenFileNum() {
        return canvases.size();
    }
    
    /**
     * Selects a given canvas.
     * @param canvas the canvas to be selected
     */
    void selectTab(DrawCanvas canvas) {
        setSelectedComponent(canvas);
        selectedTab = getSelectedIndex();
    }
    
    /**
     * Updates the title displayed on the tab of the selected canvas.
     * @param canvas the selected canvas 
     */
    void updateTab(DrawCanvas canvas) {
        setTitleAt(selectedTab, canvas.getFigureManager().getTitle());
        setToolTipTextAt(selectedTab, canvas.getFigureManager().getFilename());
    }
    
    /**
     * Do something when the target of the listener has changed its state.
     */
    public void stateChanged(ChangeEvent evt) {
        JTabbedPane src = (JTabbedPane)evt.getSource();
        selectedTab = src.getSelectedIndex();
        menu.updateUndoState();
    }
}
