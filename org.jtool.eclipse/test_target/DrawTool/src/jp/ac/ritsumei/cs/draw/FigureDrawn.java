
package jp.ac.ritsumei.cs.draw;

import javax.swing.undo.*;

public class FigureDrawn extends FigureEdit {
    
    private static final long serialVersionUID = 836544759807433999L;
    
    /**
     * Creates a new, empty object.
     */
    FigureDrawn() {
    }
    
    /**
     * Creates an undoable edit for a drawn figure.
     * @param canvas the canvas on which the edited figure was drawn
     * @param figure the figure that was drawn.
     */
    FigureDrawn(DrawCanvas canvas, Figure figure) {
        super(canvas, figure);
    }
    
    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        FigureManager fmanager = canvas.getFigureManager();
        fmanager.remove(figure);
        
        canvas.repaint();
    }
    
    @Override
    public void redo() throws CannotUndoException {
        super.redo();
        FigureManager fmanager = canvas.getFigureManager();
        fmanager.add(figure);
        
        canvas.repaint();
    }
}
