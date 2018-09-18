/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel.builder;

import org.jtool.eclipse.cfg.builder.CFGStore;
import org.jtool.eclipse.pdg.builder.PDGStore;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.util.Logger;

/**
 * An interface for building a Java model.
 * 
 * @author Katsuhisa Maruyama
 */
public abstract class ModelBuilder {
    
    protected JavaProject currentProject;
    
    public abstract boolean isUnderPlugin();
    
    public JavaProject getCurrentProject() {
        return currentProject;
    }
    
    public void setAnalysisLevel(JavaProject jproject) {
        CFGStore.getInstance().setAnalysisLevel(jproject, false, false);
    }
    
    public void setAnalysisLevel(JavaProject jproject, boolean analyzingBytecode) {
        CFGStore.getInstance().setAnalysisLevel(jproject, analyzingBytecode, false);
    }
    
    public void setAnalysisLevel(JavaProject jproject, boolean analyzingBytecode, boolean usingBytecodeCache) {
        CFGStore.getInstance().setAnalysisLevel(jproject, analyzingBytecode, usingBytecodeCache);
    }
    
    public int getAnalysisLevel() {
        return CFGStore.getInstance().getAnalysisLevel();
    }
    
    public void setCreatingActualNodes(boolean creatingActualNodes) {
        CFGStore.getInstance().setCreatingActualNodes(creatingActualNodes);
    }
    
    public boolean creatingActualNodes() {
        return CFGStore.getInstance().creatingActualNodes();
    }
    
    public void setIgnoringJumpEdge(boolean ignoringJumpEdge) {
        PDGStore.getInstance().setIgnoringJumpEdge(ignoringJumpEdge);
    }
    
    public boolean ignoringJumpEdge() {
        return PDGStore.getInstance().ignoringJumpEdge();
    }
    
    public void unbuild() {
        CFGStore.getInstance().destroy();
        PDGStore.getInstance().destroy();
        if (currentProject != null) {
            ProjectStore.getInstance().removeProject(currentProject.getPath());
            currentProject.clear();
        }
    }
    
    public void setVisible(boolean visible) {
        Logger.getInstance().setVisible(visible);
        CFGStore.getInstance().setVisible(visible);
        PDGStore.getInstance().setVisible(visible);
    }
    
    public abstract JavaProject update();
    
    public abstract void resisterBytecodeClasses(BytecodeClassStore bytecodeClassStore);
}
