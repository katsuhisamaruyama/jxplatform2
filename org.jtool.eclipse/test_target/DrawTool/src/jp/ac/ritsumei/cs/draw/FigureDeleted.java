
package jp.ac.ritsumei.cs.draw;

import javax.swing.undo.*;

public class FigureDeleted extends FigureEdit {
    
    private static final long serialVersionUID = -9180996813190895506L;
    
    /**
     * Creates a new, empty object.
     */
    FigureDeleted() {
    }
    
    /**
     * Creates an undoable edit for a deleted figure.
     * @param canvas the canvas on which the edited figure was drawn
     * @param figure the figure that was deleted.
     */
    FigureDeleted(DrawCanvas canvas, Figure figure) {
        super(canvas, figure);
    }
    
    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        FigureManager fmanager = canvas.getFigureManager();
        fmanager.add(figure);
        
        canvas.repaint();
    }
    
    @Override
    public void redo() throws CannotUndoException {
        super.redo();
        FigureManager fmanager = canvas.getFigureManager();
        fmanager.remove(figure);
        
        canvas.repaint();
    }
}
