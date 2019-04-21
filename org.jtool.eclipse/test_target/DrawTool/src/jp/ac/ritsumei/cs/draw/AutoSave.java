
package jp.ac.ritsumei.cs.draw;

/**
 * A thread that automatically saves information about figures on a given canvas.
 */
public class AutoSave extends Thread {
    
    private static final int SAVE_PERIOD = 1000 * 5;
    
    /**
     * A flag that indicates this thread is running.
     */
    private boolean isRunning;
    
    /**
     * A collection of canvases on which figures are drawn.
     */
    private TabbedCanvas tabbedCanvas;
    
    /**
     * Creates a thread that automatically saves information about figures on a given canvas.
     * @param tabbedCanvas the tabbed canvas
     */
    AutoSave(TabbedCanvas tabbedCanvas) {
        this.tabbedCanvas = tabbedCanvas;
    }
    
    /**
     * Terminates this thread.
     */
    void terminate() {
        isRunning = false;
    }
    
    /**
     * Runs this thread.
     */
    public void run() {
        isRunning = true;
        
        while (isRunning) {
            try {
                Thread.sleep(SAVE_PERIOD);
            } catch (InterruptedException e) { }
            
            for (DrawCanvas canvas : tabbedCanvas.getAllCanvases()) {
                canvas.autoSave();
            }
        }
    }
}
