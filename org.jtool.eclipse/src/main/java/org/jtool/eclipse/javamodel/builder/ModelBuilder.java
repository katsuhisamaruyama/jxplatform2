/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel.builder;

import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.cfg.CCFG;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.builder.CFGStore;
import org.jtool.eclipse.pdg.ClDG;
import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.SDG;
import org.jtool.eclipse.pdg.builder.PDGStore;
import org.jtool.eclipse.util.Logger;

/**
 * An interface for building a Java model.
 * 
 * @author Katsuhisa Maruyama
 */
public abstract class ModelBuilder {
    
    protected JavaProject currentProject;
    
    protected boolean analyzingBytecode;
    protected CFGStore cfgStore;
    protected PDGStore pdgStore;
    
    public abstract boolean isUnderPlugin();
    
    protected ModelBuilder(boolean analyzingBytecode) {
        this.analyzingBytecode = analyzingBytecode;
        cfgStore = new CFGStore();
        pdgStore = new PDGStore(cfgStore);
    }
    
    public JavaProject getCurrentProject() {
        return currentProject;
    }
    
    public CFGStore getCFGStore() {
        return cfgStore;
    }
    
    public PDGStore getPDGStore() {
        return pdgStore;
    }
    
    public void setCreatingActualNodes(boolean creatingActualNodes) {
        cfgStore.setCreatingActualNodes(creatingActualNodes);
    }
    
    public void setIgnoringJumpEdge(boolean ignoringJumpEdge) {
        pdgStore.setIgnoringJumpEdge(ignoringJumpEdge);
    }
    
    public void unbuild() {
        cfgStore.destroy();
        pdgStore.destroy();
        if (currentProject != null) {
            ProjectStore.getInstance().removeProject(currentProject.getPath());
            currentProject.clear();
        }
    }
    
    public CFG getCFG(String fqn) {
        return cfgStore.getCFG(fqn);
    }
    
    public CCFG getCCFG(JavaClass jclass) {
        return cfgStore.getCCFG(jclass);
    }
    
    public CFG getCFG(JavaMethod jmethod) {
        return cfgStore.getCFG(jmethod);
    }
    
    public CFG getCFG(JavaField jfield) {
        return cfgStore.getCFG(jfield);
    }
    
    public PDG getPDG(String fqn) {
        return pdgStore.getPDG(fqn);
    }
    
    public PDG getPDG(CFG cfg) {
        return pdgStore.getPDG(cfg);
    }
    
    public PDG getPDG(JavaMethod jmethod) {
        return pdgStore.getPDG(jmethod);
    }
    
    public PDG getPDG(JavaField jfield) {
        return pdgStore.getPDG(jfield);
    }
    
    public PDG getPDGWithinSDG(JavaMethod jmethod) {
        return pdgStore.getPDGWithinSDG(jmethod);
    }
    
    public PDG getPDGWithinSDG(JavaField jfield) {
        return pdgStore.getPDGWithinSDG(jfield);
    }
    
    public ClDG getClDG(JavaClass jclass) {
        return pdgStore.getClDG(jclass);
    }
    
    public ClDG getClDGWithinSDG(JavaClass jclass) {
        return pdgStore.getClDGWithinSDG(jclass);
    }
    
    public ClDG getClDG(JavaMethod jmethod) {
        return pdgStore.getClDG(jmethod);
    }
    
    public ClDG getClDGWithinSDG(JavaMethod jmethod) {
        return pdgStore.getClDGWithinSDG(jmethod);
    }
    
    public ClDG getClDG(JavaField jfield) {
        return pdgStore.getClDG(jfield);
    }
    
    public ClDG getClDGWithinSDG(JavaField jfield) {
        return pdgStore.getClDGWithinSDG(jfield);
    }
    
    public SDG getSDG(JavaClass jclass) {
        return pdgStore.getSDG(jclass);
    }
    
    public SDG getSDG() {
        return pdgStore.getSDG();
    }
    
    public void setVisible(boolean visible) {
        Logger.getInstance().setVisible(visible);
    }
    
    public abstract JavaProject update();
    
    public abstract void resisterBytecodeClasses(BytecodeClassStore bytecodeClassStore);
}
