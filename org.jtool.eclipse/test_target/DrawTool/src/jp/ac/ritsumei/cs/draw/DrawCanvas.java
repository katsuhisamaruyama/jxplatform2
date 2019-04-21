
package jp.ac.ritsumei.cs.draw;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;

import java.util.ListIterator;

/**
 * Implements a canvas on which figures are drawn.
 */
public class DrawCanvas extends JPanel implements MouseListener, MouseMotionListener {
    
    private static final long serialVersionUID = 6136134230542448464L;
    
    /**
     * The menu binding to this canvas.
     */
    private DrawMenu menu;
    
    /**
     * The selector binding to this canvas.
     */
    private FigureSelector selector;
    
    /**
     * The figure manager binding to this canvas.
     */
    private FigureManager figureManager;
    
    /**
     * A figure a user currently selects.
     */
    private Figure currentFigure = null;
    
    /**
     * A figure a user currently grabs.
     */
    private Figure grabbedFigure = null;
    
    /**
     * A flag indicating any figure is currently grabbed.
     */
    private boolean isGrabMode;
    
    /**
     * The x-coordinate of the current position of the mouse cursor.
     */
    private int curX;
    
    /**
     * The y-coordinate of the current position of the mouse cursor.
     */
    private int curY;
    
    /**
     * A flag indicating there exits a figure that was not saved.
     */
    private boolean changed;
    
    /**
     * The undoable edit support for this canvas.
     */
    private UndoableEditSupport support = new UndoableEditSupport();
    
    /**
     * The undo manager of this canvas.
     */
    private UndoManager undoManager;
    
    /**
     * Creates a new canvas on which a figure is drawn.
     * @param menu the menu for activation of actions
     * @param selector the selector for a figure currently drawn
     * @param name the name of a file that stores information about figures on this canvas
     */
    DrawCanvas(DrawMenu menu, FigureSelector selector, String filename) {
        this.menu = menu;
        this.selector = selector;
        if (filename != null) {
            figureManager = new FigureManager(filename);
        } else {
            figureManager = new FigureManager();
        }
        setChanged(false);
        
        setBackground(Color.white);
        setPreferredSize(new Dimension(700, 500));
        
        addMouseListener(this);
        addMouseMotionListener(this);
    }
    
    /**
     * Tests if there exits a figure that was not saved.
     * @return <code>true</code> if there exits a figure that was not saved, otherwise <code>false</code>
     */
    boolean hasChanged() {
        return changed;
    }
    
    /**
     * Sets the value of the flag indicating there exits a figure that was not saved.
     * @param bool <code>true</code> if there exits a figure that was not saved, otherwise <code>false</code>
     */
    synchronized void setChanged(boolean bool) {
        changed = bool;
    }
    
    /**
     * Returns the manager that manages figures drawn on the canvas.
     * @return the figure manager binding to this canvas.
     */
    FigureManager getFigureManager() {
        return figureManager;
    }
    
    /**
     * Returns the figure a user currently selects.
     * @return the currently selected figure
     */
    Figure getCurrentFigure() {
        return currentFigure;
    }
    
    /**
     * Sets the figure a user is currently drawing.
     * @param figure the figure currently drawn
     */
    synchronized void setCurrentFigure(Figure figure) {
        currentFigure = figure;
        notifyAll();
    }
    
    /**
     * Returns the figure a user currently grabs.
     * @return the currently grabbed figure
     */
    Figure getGrabbedFigure() {
        return grabbedFigure;
    }
    
    /**
     * Automatically saves figure information.
     */
    synchronized void autoSave() {
        if (menu != null) {
            while (getCurrentFigure() != null) {
                try {
                    wait();
                } catch (InterruptedException e) { }
            }
            
            if (hasChanged()) {
                menu.saveTempFile(this);
            }
        }
    }
    
    /**
     * Do anything when a mouse button has been pressed on a component.
     * @param e an event indicating that a mouse action occurred in a component
     */
    public void mousePressed(MouseEvent e) {
        Graphics g = getGraphics();
        
        isGrabMode = selector.isGrabMode();
        if (isGrabMode) {
            setCurrentFigure(getGrabbedFigure(e.getX(), e.getY()));
            if (getCurrentFigure() != null) {
                if (grabbedFigure == null) {
                    grabbedFigure = getCurrentFigure().createClone();
                    drawGrabbedFigure(g, grabbedFigure);
                }
                curX = e.getX();
                curY = e.getY();
            }
            
        } else {
            setCurrentFigure(selector.createFigure());
            getCurrentFigure().setStart(e.getX(), e.getY());
            getCurrentFigure().setEnd(e.getX(), e.getY());
            drawRubber(g, getCurrentFigure());
        }
    }
    
    /**
     * Do anything when a mouse button has been released on a component.
     * @param e an event indicating that a mouse action occurred in a component
     */
    public void mouseReleased(MouseEvent e) {
        if (getCurrentFigure() != null) {
            Graphics g = getGraphics();
            
            if (isGrabMode) {
                if (getCurrentFigure().getLeft() != grabbedFigure.getLeft() ||
                    getCurrentFigure().getTop() != grabbedFigure.getTop()) {
                    support.postEdit(new FigureMoved(this, getCurrentFigure(), grabbedFigure));
                    menu.updateUndoState();
                    menu.updateRedoState();
                    
                    setChanged(true);
                    setCurrentFigure(null);
                    grabbedFigure = null;
                    
                    repaint();
                } else {
                    menu.updateEditState(true);
                }
                
            } else {
                drawRubber(g, getCurrentFigure());
                
                if (getCurrentFigure().getLeft() != e.getX() || getCurrentFigure().getTop() != e.getY()) {
                    support.postEdit(new FigureDrawn(this, getCurrentFigure()));
                    menu.updateUndoState();
                    menu.updateRedoState();
                    
                    getCurrentFigure().setEnd(e.getX(), e.getY());
                    figureManager.add(getCurrentFigure());
                    drawFigure(g, getCurrentFigure());
                    setChanged(true);
                    
                    repaint();
                }
                setCurrentFigure(null);
                grabbedFigure = null;
            }
        }
    }
    
    /**
     * Do nothing when the mouse enters a component.
     * @param e an event indicating that a mouse action occurred in a component
     */
    public void mouseEntered(MouseEvent e) {
    }
    
    /**
     * Do nothing when the mouse exits a component.
     * @param e an event indicating that a mouse action occurred in a component
     */
    public void mouseExited(MouseEvent e) {
    }
    
    /**
     * Do nothing when the mouse button has been clicked (pressed and released) on a component.
     * @param e an event indicating that a mouse action occurred in a component
     */
    public void mouseClicked(MouseEvent e) {
    }
    
    /**
     * Do anything when a mouse button is pressed on a component and then dragged.
     * @param e an event indicating that a mouse action occurred in a component
     */
    public void mouseDragged(MouseEvent e) {
        if (getCurrentFigure() != null) {
            Graphics g = getGraphics();
            
            if (isGrabMode) {
                if (e.getX() != curX || e.getY() != curY) {
                    drawOutline(g, getCurrentFigure());
                    getCurrentFigure().move(e.getX() - curX, e.getY() - curY);
                    curX = e.getX();
                    curY = e.getY();
                    drawOutline(g, getCurrentFigure());
                }
                
            } else {
                drawRubber(g, getCurrentFigure());
                getCurrentFigure().setEnd(e.getX(), e.getY());
                drawRubber(g, getCurrentFigure());
            }
        }
    }
    
    /**
     * Do noting when the mouse cursor has been moved onto a component but no buttons have been pushed.
     * @param e an event indicating that a mouse action occurred in a component
     */
    public void mouseMoved(MouseEvent e) {
    }
    
    /**
     * Retrieves a figure that is currently grabbed.
     * @param x the x-coordinate of the grabbed point on the canvas
     * @param y the y-coordinate of the grabbed point on the canvas
     * @return the found figure, or <code>null</code> if no figure was grabbed
     */
    private Figure getGrabbedFigure(int x, int y) {
        ListIterator<Figure> it = figureManager.getFigures().listIterator();
        while (it.hasNext()) {
            it.next();
        }
        
        while (it.hasPrevious()) {
            Figure figure = (Figure)it.previous();
            if (figure.isGrabbed(x, y)) {
                return figure;
            }
        }
        return null;
    }
    
    /**
     * Draws a given figure.
     * @param g the graphics context
     * @param figure the figure to be drawn
     */
    private void drawFigure(Graphics g, Figure figure) {
        Color c = g.getColor();
        g.setPaintMode();
        figure.draw(g);
        g.setColor(c);
    }
    
    /**
     * Draws a rubber representing a given figure while drawing it.
     * @param g the graphics context
     * @param figure the figure represented by the drawn rubber
     */
    private void drawRubber(Graphics g, Figure figure) {
        Color c = g.getColor();
        g.setXORMode(getBackground());
        figure.drawRubber(g);
        g.setColor(c);
    }
    
    /**
     * Draws the outline of a given figure while moving it.
     * @param g the graphics context
     * @param figure the figure having the drawn outline
     */
    private void drawOutline(Graphics g, Figure figure) {
        Color c = g.getColor();
        g.setXORMode(getBackground());
        figure.drawOutline(g);
        g.setColor(c);
    }
    
    /**
     * Draws a given figure while grabbing it.
     * @param g the graphics context
     * @param figure the figure to be drawn while grabbing it
     */
    private void drawGrabbedFigure(Graphics g, Figure figure) {
        Color c = g.getColor();
        g.setXORMode(getBackground());
        figure.drawGrabbedFigure(g);
        g.setColor(c);
    }
    
    /**
     * Draws all the figures on the canvas and the grabbed figure when the repaint is occurred.
     * @param g the graphics context
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        for (Figure figure : figureManager.getFigures()) {
            drawFigure(g, figure);
        }
        
        if (getCurrentFigure() != null) {
            drawGrabbedFigure(g, getCurrentFigure());
        }
    }
    
    /**
     * Pastes a figure stored in the clipboard.
     * @param the figure stored in the clipboard
     */
    void pasteFigure(Figure figure) {
        support.postEdit(new FigureDrawn(this, figure));
        menu.updateUndoState();
        menu.updateRedoState();
        menu.updateEditState(true);
        
        while (isOverlapped(figure)) {
            figure.move(10, 10);
        }
        
        figureManager.add(figure);
        setChanged(true);
        clearFigure();
        
        repaint();
    }
    
    /**
     * Deletes the current figure.
     */
    void deleteFigure() {
        support.postEdit(new FigureDeleted(this, grabbedFigure));
        menu.updateUndoState();
        menu.updateRedoState();
        menu.updateEditState(false);
        
        figureManager.remove(getCurrentFigure());
        setChanged(true);
        clearFigure();
        
        repaint();
    }
    
    /**
     * Clears figures currently selected and grabbed.
     */
    void clearFigure() {
        setCurrentFigure(null);
        grabbedFigure = null;
    }
    
    /**
     * Checks if a given figure is overlapped on any of existing figures.
     * @param figure the figure to be checked
     * @return <code>true</code> if the given figure is overlapped, otherwise <code>false</code>
     */
    private boolean isOverlapped(Figure figure) {
        for (Figure fig : figureManager.getFigures()) {
            if (figure.isOverlapped(fig)) {
                return true;
            }
        }
        return false;
    }
    
    public void addUndoableEditListener(UndoableEditListener listener) {
        support.addUndoableEditListener(listener);
    }
    
    public void removeUndoableEditListener(UndoableEditListener listener) {
        support.removeUndoableEditListener(listener);
    }
    
    public void setUndoManager(UndoManager undoManger) {
        this.undoManager = undoManger;
    }
    
    public UndoManager getUndoManager() {
        return undoManager;
    }
}
