/*
 *  Copyright 2018-2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.cfg.CommonCFG;
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
    
    private Map<String, CommonCFG> cfgMap = new HashMap<String, CommonCFG>();
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
    
    private void addCFG(CommonCFG graph) {
        cfgMap.put(graph.getQualifiedName(), graph);
    }
    
    public CommonCFG getControlFlowGraph(String fqn) {
        return cfgMap.get(fqn);
    }
    
    public int size() {
        return cfgMap.size();
    }
    
    public JavaProject getJavaProject() {
        return infoStore.getJavaProject();
    }
    
    public CCFG getCCFG(JavaClass jclass) {
        CommonCFG graph = getControlFlowGraph(jclass.getQualifiedName());
        if (graph != null && graph instanceof CCFG) {
            return (CCFG)graph;
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
        for (CCFG ccfg2 : ccfg.getStartNode().getTypes()) {
            addCFG(ccfg2);
            for (CFG cfg : ccfg2.getCFGs()) {
                addCFG(cfg);
            }
        }
        return ccfg;
    }
    
    public CFG getCFG(JavaMethod jmethod) {
        CommonCFG graph = getControlFlowGraph(jmethod.getQualifiedName());
        if (graph != null && graph instanceof CFG) {
            return (CFG)graph;
        }
        CFG cfg = CFGMethodBuilder.build(jmethod, infoStore, new HashSet<JMethod>());
        addCFG(cfg);
        return cfg;
    }
    
    public CFG getCFG(JavaField jfield) {
        return getCFG(jfield, new HashSet<JMethod>());
    }
    
    CFG getCFG(JavaMethod jmethod, Set<JMethod> visited) {
        CommonCFG graph = getControlFlowGraph(jmethod.getQualifiedName());
        if (graph != null && graph instanceof CFG) {
            return (CFG)graph;
        }
        CFG cfg = CFGMethodBuilder.build(jmethod, infoStore, visited);
        addCFG(cfg);
        return cfg;
    }
    
    CFG getCFG(JavaField jfield, Set<JMethod> visited) {
        CommonCFG graph = getControlFlowGraph(jfield.getQualifiedName());
        if (graph != null && graph instanceof CFG) {
            return (CFG)graph;
        }
        CFG cfg = CFGFieldBuilder.build(jfield, infoStore, visited);
        addCFG(cfg);
        return cfg;
    }
}
