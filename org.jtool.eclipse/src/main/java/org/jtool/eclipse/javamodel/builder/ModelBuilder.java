/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel.builder;

import org.jtool.eclipse.cfg.builder.CFGStore;
import org.jtool.eclipse.pdg.builder.PDGStore;
import org.jtool.eclipse.cfg.builder.JInfoStore;
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
        CFGStore.getInstance().setAnalysisLevel(jproject, false);
    }
    
    public void setAnalysisLevel(JavaProject jproject, boolean analyzingBytecode) {
        CFGStore.getInstance().setAnalysisLevel(jproject, analyzingBytecode);
    }
    
    public void setCreatingActualNodes(boolean creatingActualNodes) {
        CFGStore.getInstance().setCreatingActualNodes(creatingActualNodes);
    }
    
    public void setIgnoringJumpEdge(boolean ignoringJumpEdge) {
        PDGStore.getInstance().setIgnoringJumpEdge(ignoringJumpEdge);
    }
    
    public boolean ignoringJumpEdge() {
        return PDGStore.getInstance().ignoringJumpEdge();
    }
    
    public void build() {
        CFGStore.getInstance().create();
        PDGStore.getInstance().create();
        JInfoStore.getInstance().create();
    }
    
    public void unbuild() {
        CFGStore.getInstance().destroy();
        PDGStore.getInstance().destroy();
        JInfoStore.getInstance().destory();
        if (currentProject != null) {
            ProjectStore.getInstance().removeProject(currentProject.getPath());
            currentProject.clear();
        }
    }
    
    public void setVisible(boolean visible) {
        Logger.getInstance().setVisible(visible);
    }
    
    public abstract JavaProject update();
    
    public abstract void resisterBytecodeClasses(BytecodeClassStore bytecodeClassStore);
}
