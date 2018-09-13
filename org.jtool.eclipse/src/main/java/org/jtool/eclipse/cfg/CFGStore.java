/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.jtool.eclipse.cfg.builder.JInfoStore;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.jtool.eclipse.cfg.builder.CCFGBuilder;
import org.jtool.eclipse.cfg.builder.CFGFieldBuilder;
import org.jtool.eclipse.cfg.builder.CFGMethodBuilder;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaMethod;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

/**
 * An object stores CFGs.
 * 
 * @author Katsuhisa Maruyama
 */
public class CFGStore {
    
    private static CFGStore instance = new CFGStore();
    
    private Map<String, CFG> cfgStore = new HashMap<String, CFG>();
    
    private int analysisLevel = 0;
    
    private boolean creatingActualNodes = false;
    
    private boolean visible = true;
    
    private CFGStore() {
        CFGNode.resetId();
    }
    
    public void resetId() {
        CFGNode.resetId();
    }
    
    public static CFGStore getInstance() {
        return instance;
    }
    
    public void setAnalysisLevel(JavaProject jproject, boolean analyzingBytecode) {
        JInfoStore.getInstance().build(jproject, analyzingBytecode);
        if (jproject != null) {
            analysisLevel = 1;
            if (analyzingBytecode) {
                analysisLevel = 2;
            }
        }
    }
    
    public int getAnalysisLevel() {
        return analysisLevel;
    }
    
    public void setCreatingActualNodes(boolean creatingActualNodes) {
        this.creatingActualNodes = creatingActualNodes;
    }
    
    public void destroy() {
        cfgStore.clear();
        CFGNode.resetId();
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
        
        if (visible) {
            System.out.print(" - " + jclass.getQualifiedName() + " - CCFG");
        }
        CCFG ccfg = build(jclass);
        return ccfg;
    }
    
    private CCFG build(JavaClass jclass) {
        CCFG ccfg = CCFGBuilder.build(jclass);
        CFGStore.getInstance().addCFG(ccfg);
        
        for (CFG cfg : ccfg.getStartNode().getMethods()) {
            ccfg.add(cfg);
            addCFG(cfg);
        }
        for (CFG cfg : ccfg.getStartNode().getFields()) {
            ccfg.add(cfg);
            addCFG(cfg);
        }
        return ccfg;
    }
    
    public CFG getCFG(JavaMethod jmethod) {
        return getCFG(jmethod, new HashSet<JMethod>());
    }
    
    public CFG getCFG(JavaMethod jmethod, Set<JMethod> visitedMethods) {
        CFG cfg = getCFG(jmethod.getQualifiedName());
        if (cfg == null) {
            if (visible) {
                System.out.print(" - " + jmethod.getQualifiedName() + " - CFG");
            }
            
            cfg = CFGMethodBuilder.build(jmethod, visitedMethods);
            addCFG(cfg);
        }
        return cfg;
    }
    
    public CFG getCFG(JavaField jfield) {
        return getCFG(jfield, new HashSet<JMethod>());
    }
    
    public CFG getCFG(JavaField jfield, Set<JMethod> visitedMethods) {
        CFG cfg = getCFG(jfield.getQualifiedName());
        if (cfg == null) {
            if (visible) {
                System.out.print(" - " + jfield.getQualifiedName() + " - CFG");
            }
            
            cfg = CFGFieldBuilder.build(jfield, visitedMethods);
            addCFG(cfg);
        }
        return cfg;
    }
    
    public void buildCFGs(List<JavaClass> jclasses) {
        int size = jclasses.size();
        int count = 1;
        System.out.println();
        System.out.println("** Building CFGs of " + size + " classes ");
        for (JavaClass jclass : jclasses) {
            getCCFG(jclass);
            System.out.println(" (" + count + "/" + size + ")");
            count++;
        }
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
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public boolean isVisible() {
        return visible;
    }
}
