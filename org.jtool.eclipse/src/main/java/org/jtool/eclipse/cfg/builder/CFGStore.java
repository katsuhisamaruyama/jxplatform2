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
import org.jtool.eclipse.cfg.JMethod;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * An object stores CFGs.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class CFGStore {
    
    private static CFGStore instance = new CFGStore();
    
    private Map<String, CFG> cfgStore = new HashMap<String, CFG>();
    private boolean creatingActualNodes;
    
    private CFGStore() {
    }
    
    public void resetId() {
        CFGNode.resetId();
    }
    
    public static CFGStore getInstance() {
        return instance;
    }
    
    public void create() {
        cfgStore.clear();
        creatingActualNodes = false;
        CFGNode.resetId();
    }
    
    public void destroy() {
        JInfoStore.getInstance().writeCache();
        cfgStore.clear();
        creatingActualNodes = false;
        CFGNode.resetId();
    }
    
    public void setAnalysisLevel(JavaProject jproject, boolean analyzingBytecode) {
        JInfoStore.getInstance().create(jproject, analyzingBytecode);
    }
    
    public void setCreatingActualNodes(boolean creatingActualNodes) {
        this.creatingActualNodes = creatingActualNodes;
    }
    
    public boolean creatingActualNodes() {
        return creatingActualNodes;
    }
    
    private void addCFG(CFG cfg) {
        cfgStore.put(cfg.getQualifiedName(), cfg);
    }
    
    public CFG getCFG(String fqn) {
        return cfgStore.get(fqn);
    }
    
    public int size() {
        return cfgStore.size();
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
        CCFG ccfg = CCFGBuilder.build(jclass);
        CFGStore.getInstance().addCFG(ccfg);
        
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
            cfg = CFGMethodBuilder.build(jmethod, new HashSet<JMethod>());
            addCFG(cfg);
        }
        return cfg;
    }
    
    CFG getCFG(JavaMethod jmethod, Set<JMethod> visitedMethods) {
        CFG cfg = getCFG(jmethod.getQualifiedName());
        if (cfg == null) {
            cfg = CFGMethodBuilder.build(jmethod, visitedMethods);
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
            cfg = CFGFieldBuilder.build(jfield, visitedMethods);
            addCFG(cfg);
        }
        return cfg;
    }
    
    public CCFG build(TypeDeclaration node) {
        return CCFGBuilder.build(node);
    }
    
    public CCFG build(AnonymousClassDeclaration node) {
        return CCFGBuilder.build(node);
    }
    
    public CCFG build(EnumDeclaration node) {
        return CCFGBuilder.build(node);
    }
    
    public CFG build(MethodDeclaration node) {
        return CFGMethodBuilder.build(node);
    }
    
    public CFG build(Initializer node) {
        return CFGMethodBuilder.build(node);
    }
    
    public CFG build(LambdaExpression node) {
        return CFGMethodBuilder.build(node);
    }
    
    public CFG build(VariableDeclaration node) {
        return CFGFieldBuilder.build(node);
    }
    
    public CFG build(VariableDeclarationFragment node) {
        return CFGFieldBuilder.build(node);
    }
    
    public CFG build(EnumConstantDeclaration node) {
        return CFGFieldBuilder.build(node);
    }
}
