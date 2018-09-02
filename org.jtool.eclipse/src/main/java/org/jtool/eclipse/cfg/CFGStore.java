/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.jtool.eclipse.cfg.builder.JInfoStore;
import org.jtool.eclipse.cfg.builder.CFGClassBuilder;
import org.jtool.eclipse.cfg.builder.CFGFieldBuilder;
import org.jtool.eclipse.cfg.builder.CFGMethodBuilder;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaMethod;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * An object representing a virtual project.
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
        if (jproject != null) {
            analysisLevel = 1;
            JInfoStore.getInstance().build(jproject, analyzingBytecode);
            if (analyzingBytecode) {
                analysisLevel = 2;
            }
        }
    }
    
    public int getAnalsisLevel() {
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
    
    public CFG getCFG(JavaClass jclass) {
        CFG ccfg = getCFG(jclass.getQualifiedName());
        if (ccfg == null) {
            if (visible) {
                System.out.print(" - " + jclass.getQualifiedName() + " - CFG");
            }
            ccfg = build(jclass);
        }
        return ccfg;
    }
    
    public CFG getCFG(JavaMethod jmethod) {
        CFG cfg = getCFG(jmethod.getQualifiedName());
        if (cfg == null) {
            if (visible) {
                System.out.print(" - " + jmethod.getQualifiedName() + " - CFG");
            }
            cfg = build(jmethod);
        }
        return cfg;
    }
    
    public CFG getCFG(JavaField jfield) {
        CFG cfg = getCFG(jfield.getQualifiedName());
        if (cfg == null) {
            if (visible) {
                System.out.print(" - " + jfield.getQualifiedName() + " - CFG");
            }
            cfg = build(jfield);
        }
        return cfg;
    }
    
    private CCFG build(JavaClass jclass) {
        CCFG ccfg = CFGClassBuilder.build(jclass);
        CFGStore.getInstance().addCFG(ccfg);
        
        for (CFG cfg : ccfg.getStartNode().getMethods()) {
            addCFG(cfg);
        }
        for (CFG cfg : ccfg.getStartNode().getFields()) {
            addCFG(cfg);
        }
        /*
        for (CFG cfg : ccfg.getStartNode().getTypes()) {
            addCFG(cfg);
        }
        */
        return ccfg;
    }
    
    private CFG build(JavaMethod jmethod) {
        CFG cfg = CFGMethodBuilder.build(jmethod);
        addCFG(cfg);
        return cfg;
    }
    
    private CFG build(JavaField jfield) {
        
        CFG cfg = CFGFieldBuilder.build(jfield);
        addCFG(cfg);
        return cfg;
    }
    
    public void buildCFGs(List<JavaClass> jclasses) {
        int size = jclasses.size();
        int count = 1;
        System.out.println();
        System.out.println("** Building CFGs of " + size + " classes ");
        for (JavaClass jclass : jclasses) {
            CFGStore.getInstance().getCFG(jclass);
            System.out.println(" (" + count + "/" + size + ")");
            count++;
        }
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public boolean isVisible() {
        return visible;
    }
}
