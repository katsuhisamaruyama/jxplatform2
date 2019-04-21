
package jp.ac.ritsumei.cs.draw;

import javax.swing.undo.*;

/**
 * An undoable edit that stored undo manager.
 */
public class FigureEdit extends AbstractUndoableEdit {
    
    private static final long serialVersionUID = -3799593553159906088L;
    
    /**
     * The canvas on which the edited figure was drawn.
     */
    protected DrawCanvas canvas;
    
    /**
     * The figure that was edited.
     */
    protected Figure figure;
    
    /**
     * Creates a new, empty object.
     */
    FigureEdit() {
    }
    
    /**
     * Creates an undoable edit for an edited figure.
     * @param canvas the canvas on which the edited figure was drawn
     * @param figure the figure that was edited.
     */
    protected FigureEdit(DrawCanvas canvas, Figure figure) {
        this.canvas = canvas;
        this.figure = figure;
    }
    
    /**
     * Undoes the current undoable edit.
     * @exception CannotUndoException if the undo operation failed.
     */
    public void undo() throws CannotUndoException {
        super.undo();
    }
    
    /**
     * Redoes the current undoable edit.
     * @exception CannotUndoException if the redo operation failed.
     */
    public void redo() throws CannotUndoException {
        super.redo();
    }
}
