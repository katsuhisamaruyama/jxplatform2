/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.cfg.CCFG;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaMethod;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * An object that stores information on CFGs in the project.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class CFGStore {
    
    private JInfoStore infoStore;
    
    private Map<String, CFG> cfgMap = new HashMap<String, CFG>();
    private boolean creatingActualNodes = false;
    
    public CFGStore() {
        infoStore = new JInfoStore();
        CFGNode.resetId();
    }
    
    public void create(JavaProject jproject, boolean analyzingBytecode) {
        infoStore.create(this, jproject, analyzingBytecode);
    }
    
    public void destroy() {
        infoStore.destory();
        cfgMap.clear();
    }
    
    public void resetId() {
        CFGNode.resetId();
    }
    
    public JInfoStore getJInfoStore() {
        return infoStore;
    }
    
    public void setCreatingActualNodes(boolean creatingActualNodes) {
        this.creatingActualNodes = creatingActualNodes;
    }
    
    public boolean creatingActualNodes() {
        return creatingActualNodes;
    }
    
    private void addCFG(CFG cfg) {
        cfgMap.put(cfg.getQualifiedName(), cfg);
    }
    
    public CFG getCFG(String fqn) {
        return cfgMap.get(fqn);
    }
    
    public int size() {
        return cfgMap.size();
    }
    
    public JavaProject getJavaProject() {
        return infoStore.getJavaProject();
    }
    
    public CCFG getCCFG(JavaClass jclass) {
        CFG cfg = getCFG(jclass.getQualifiedName());
        if (cfg != null && cfg instanceof CCFG) {
            return (CCFG)cfg;
        }
        CCFG ccfg = build(jclass);
        return ccfg;
    }
    
    private CCFG build(JavaClass jclass) {
        CCFG ccfg = CCFGBuilder.build(jclass, infoStore);
        addCFG(ccfg);
        
        for (CFG cfg : ccfg.getStartNode().getMethods()) {
            addCFG(cfg);
        }
        for (CFG cfg : ccfg.getStartNode().getFields()) {
            addCFG(cfg);
        }
        for (CFG cfg : ccfg.getStartNode().getFields()) {
            addCFG(cfg);
        }
        for (CFG cfg : ccfg.getStartNode().getTypes()) {
            addCFG(cfg);
        }
        return ccfg;
    }
    
    public CFG getCFG(JavaMethod jmethod) {
        CFG cfg = getCFG(jmethod.getQualifiedName());
        if (cfg == null) {
            cfg = CFGMethodBuilder.build(jmethod, infoStore, new HashSet<JMethod>());
            addCFG(cfg);
        }
        return cfg;
    }
    
    CFG getCFG(JavaMethod jmethod, Set<JMethod> visitedMethods) {
        CFG cfg = getCFG(jmethod.getQualifiedName());
        if (cfg == null) {
            cfg = CFGMethodBuilder.build(jmethod, infoStore, visitedMethods);
            addCFG(cfg);
        }
        return cfg;
    }
    
    public CFG getCFG(JavaField jfield) {
        return getCFG(jfield, new HashSet<JMethod>());
    }
    
    CFG getCFG(JavaField jfield, Set<JMethod> visitedMethods) {
        CFG cfg = getCFG(jfield.getQualifiedName());
        if (cfg == null) {
            cfg = CFGFieldBuilder.build(jfield, infoStore, visitedMethods);
            addCFG(cfg);
        }
        return cfg;
    }
}
