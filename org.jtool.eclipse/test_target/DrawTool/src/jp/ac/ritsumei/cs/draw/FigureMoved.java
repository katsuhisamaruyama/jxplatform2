
package jp.ac.ritsumei.cs.draw;

import javax.swing.undo.*;

public class FigureMoved extends FigureEdit {
    
    private static final long serialVersionUID = 153285208113194136L;
    
    /**
     * The figure before moving.
     */
    private Figure undoFigure;
    
    /**
     * The copy of the figure after moving.
     */
    private Figure redoFigure;
    
    /**
     * Creates a new, empty object.
     */
    FigureMoved() {
    }
    
    /**
     * Creates an undoable edit for a moved figure.
     * @param canvas the canvas on which the edited figure was moved
     * @param figure the figure after moving.
     * @param prevFigure the figure before moving.
     */
    FigureMoved(DrawCanvas canvas, Figure figure, Figure prevFigure) {
        super(canvas, figure);
        undoFigure = prevFigure;
        redoFigure = figure.createClone();
    }
    
    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        figure.replace(undoFigure);
        
        canvas.repaint();
    }
    
    @Override
    public void redo() throws CannotUndoException {
        super.redo();
        figure.replace(redoFigure);
        
        canvas.repaint();
    }
}
