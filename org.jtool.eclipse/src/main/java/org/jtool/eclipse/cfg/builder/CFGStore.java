/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.javamodel.builder.ModelBuilder;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.jtool.eclipse.cfg.CCFG;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGNode;
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
    
    public void create(JavaProject jproject, ModelBuilder builder) {
        infoStore.create(this, jproject, builder);
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
        return getCFG(jfield, new HashSet<JMethod>(), force);
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
        
        ccfg.getStartNode().getMethods().forEach(cfg -> addCFG(cfg));
        ccfg.getStartNode().getFields().forEach(cfg -> addCFG(cfg));
        ccfg.getStartNode().getTypes().forEach(ccfg2 -> {
            addCCFG(ccfg2);
            ccfg2.getCFGs().forEach(cfg -> addCFG(cfg));
        });
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
        if (cfg != null) {
            addCFG(cfg);
        }
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
        if (cfg != null) {
            addCFG(cfg);
        }
        return cfg;
    }
}
