/*
 *  Copyright 2018-2019
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
    private Map<String, CCFG> ccfgMap = new HashMap<String, CCFG>();
    
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
    
    public JavaProject getJavaProject() {
        return infoStore.getJavaProject();
    }
    
    private void addCFG(CFG cfg) {
        cfgMap.put(cfg.getQualifiedName(), cfg);
    }
    
    private void addCCFG(CCFG ccfg) {
        ccfgMap.put(ccfg.getQualifiedName(), ccfg);
    }
    
    public CFG findCFG(String fqn) {
        return cfgMap.get(fqn);
    }
    
    public CCFG findCCFG(String fqn) {
        return ccfgMap.get(fqn);
    }
    
    public CFG getCFG(JavaMethod jmethod, boolean force) {
        return getCFG(jmethod, new HashSet<JMethod>(), force);
    }
    
    public CFG getCFG(JavaField jfield, boolean force) {
        return  getCFG(jfield, new HashSet<JMethod>(), force);
    }
    
    public CCFG getCCFG(JavaClass jclass, boolean force) {
        if (!force) {
            CCFG ccfg = ccfgMap.get(jclass.getQualifiedName());
            if (ccfg != null) {
                return ccfg;
            }
        }
        
        CCFG ccfg = CCFGBuilder.build(jclass, force, infoStore);
        addCCFG(ccfg);
        
        for (CFG cfg : ccfg.getStartNode().getMethods()) {
            addCFG(cfg);
        }
        for (CFG cfg : ccfg.getStartNode().getFields()) {
            addCFG(cfg);
        }
        for (CCFG ccfg2 : ccfg.getStartNode().getTypes()) {
            addCCFG(ccfg2);
            for (CFG cfg : ccfg2.getCFGs()) {
                addCFG(cfg);
            }
        }
        return ccfg;
    }
    
    CFG getCFG(JavaMethod jmethod, Set<JMethod> visited, boolean force) {
        if (!force) {
            CFG cfg = cfgMap.get(jmethod.getQualifiedName());
            if (cfg != null) {
                return cfg;
            }
        }
        CFG cfg = CFGMethodBuilder.build(jmethod, infoStore, visited);
        addCFG(cfg);
        return cfg;
    }
    
    CFG getCFG(JavaField jfield, Set<JMethod> visited, boolean force) {
        if (!force) {
            CFG cfg = cfgMap.get(jfield.getQualifiedName());
            if (cfg != null) {
                return cfg;
            }
        }
        
        CFG cfg = CFGFieldBuilder.build(jfield, infoStore, visited);
        addCFG(cfg);
        return cfg;
    }
}
