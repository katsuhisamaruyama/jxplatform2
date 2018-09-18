/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel.builder;

import org.jtool.eclipse.cfg.CCFG;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.builder.CFGStore;
import org.jtool.eclipse.cfg.builder.JInfoStore;
import org.jtool.eclipse.pdg.ClDG;
import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.SDG;
import org.jtool.eclipse.pdg.builder.PDGStore;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaMethod;
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
    
    public CFG getCFG(String fqn) {
        return CFGStore.getInstance().getCFG(fqn);
    }
    
    public int sizeOfCFG() {
        return CFGStore.getInstance().size();
    }
    
    public CCFG getCCFG(JavaClass jclass) {
        return CFGStore.getInstance().getCCFG(jclass);
    }
    
    public CFG getCFG(JavaMethod jmethod) {
        return CFGStore.getInstance().getCFG(jmethod);
    }
    
    public CFG getCFG(JavaField jfield) {
        return CFGStore.getInstance().getCFG(jfield);
    }
    
    public int sizeOfPDG() {
        return PDGStore.getInstance().size();
    }
    
    public PDG getPDG(String fqn) {
        return PDGStore.getInstance().getPDG(fqn);
    }
    
    public PDG getPDG(CFG cfg) {
        return PDGStore.getInstance().getPDG(cfg);
    }
    
    public PDG getPDG(JavaMethod jmethod) {
        return PDGStore.getInstance().getPDG(jmethod);
    }
    
    public PDG getPDG(JavaField jfield) {
        return PDGStore.getInstance().getPDG(jfield);
    }
    
    public PDG getPDGWithinSDG(JavaMethod jmethod) {
        return PDGStore.getInstance().getPDGWithinSDG(jmethod);
    }
    
    public PDG getPDGWithinSDG(JavaField jfield) {
        return PDGStore.getInstance().getPDGWithinSDG(jfield);
    }
    
    public ClDG getClDG(JavaClass jclass) {
        return PDGStore.getInstance().getClDG(jclass);
    }
    
    public ClDG getClDGWithinSDG(JavaClass jclass) {
        return PDGStore.getInstance().getClDGWithinSDG(jclass);
    }
    
    public ClDG getClDG(JavaMethod jmethod) {
        return PDGStore.getInstance().getClDG(jmethod);
    }
    
    public ClDG getClDGWithinSDG(JavaMethod jmethod) {
        return PDGStore.getInstance().getClDGWithinSDG(jmethod);
    }
    
    public ClDG getClDG(JavaField jfield) {
        return PDGStore.getInstance().getClDG(jfield);
    }
    
    public ClDG getClDGWithinSDG(JavaField jfield) {
        return PDGStore.getInstance().getClDGWithinSDG(jfield);
    }
    
    public SDG getSDG(JavaClass jclass) {
        return PDGStore.getInstance().getSDG(jclass);
    }
    
    public SDG getSDG(JavaProject jproject) {
        return PDGStore.getInstance().getSDG(jproject);
    }
    
    public void setVisible(boolean visible) {
        Logger.getInstance().setVisible(visible);
    }
    
    public abstract JavaProject update();
    
    public abstract void resisterBytecodeClasses(BytecodeClassStore bytecodeClassStore);
}
