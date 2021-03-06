/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg.builder;

import org.jtool.eclipse.pdg.ClDG;
import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.SDG;
import org.jtool.eclipse.cfg.CCFG;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.builder.CFGStore;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaMethod;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * An object that stores information on PDGs in the project.
 * 
 * @author Katsuhisa Maruyama
 */
public class PDGStore {
    
    private CFGStore cfgStore;
    
    private Map<String, PDG> pdgMap = new HashMap<>();
    private Map<String, ClDG> cldgMap = new HashMap<>();
    
    private SDG currentSDG;
    
    public PDGStore(CFGStore cfgStore) {
        this.cfgStore = cfgStore;
    }
    
    public void destroy() {
        pdgMap.clear();
        cfgStore = null;
    }
    
    private void addPDG(PDG pdg) {
        pdgMap.put(pdg.getEntryNode().getQualifiedName(), pdg);
    }
    
    private void addClDG(ClDG cldg) {
        cldgMap.put(cldg.getEntryNode().getQualifiedName(), cldg);
    }
    
    public PDG findPDG(String fqn) {
        return pdgMap.get(fqn);
    }
    
    public ClDG findClDG(String fqn) {
        return cldgMap.get(fqn);
    }
    
    public SDG findSDG() {
        return currentSDG;
    }
    
    public PDG getPDG(CFG cfg, boolean force) {
        PDG pdg = getPDGCore(cfg, force);
        PDGBuilder.connectMethodCallsConservatively(pdg);
        return pdg;
    }
    
    public PDG getPDG(JavaMethod jmethod, boolean force) {
        CFG cfg = cfgStore.getCFG(jmethod, force);
        return getPDG(cfg, force);
    }
    
    public PDG getPDG(JavaField jfield, boolean force) {
        CFG cfg = cfgStore.getCFG(jfield, force);
        return getPDG(cfg, force);
    }
    
    private PDG getPDGCore(CFG cfg, boolean force) {
        if (!force) {
            PDG pdg = pdgMap.get(cfg.getQualifiedName());
            if (pdg != null) {
                return pdg;
            }
        }
        PDG pdg = PDGBuilder.buildPDG(cfg);
        addPDG(pdg);
        return pdg;
    }
    
    public ClDG getClDG(CCFG ccfg, boolean force) {
        ClDG cldg = getClDGCore(ccfg, force);
        PDGBuilder.connectMethodCalls(cldg);
        return cldg;
    }
    
    public ClDG getClDG(JavaClass jclass, boolean force) {
        CCFG ccfg = cfgStore.getCCFG(jclass, force);
        return getClDG(ccfg, force);
    }
    
    private ClDG getClDGCore(CCFG ccfg, boolean force) {
        if (!force) {
            ClDG cldg = cldgMap.get(ccfg.getQualifiedName());
            if (cldg != null) {
                return cldg;
            }
        }
        ClDG cldg = PDGBuilder.buildClDG(ccfg);
        return cldg;
    }
    
    public SDG getSDG(JavaClass jclass, boolean force) {
        Set<JavaClass> classes = new HashSet<>();
        classes.add(jclass);
        return getSDG(classes, force);
    }
    
    public SDG getSDG(Set<JavaClass> classes, boolean force) {
        Set<JavaClass> allClasses = new HashSet<>();
        for (JavaClass jc : classes) {
            collectEfferentClasses(jc, allClasses);
        }
        
        SDG sdg = getSDGForClasses(allClasses, force);
        return sdg;
    }
    
    public SDG getSDG(boolean force) {
        if (!force && currentSDG != null) {
            return currentSDG;
        }
        Set<JavaClass> classes = new HashSet<>(cfgStore.getJavaProject().getClasses());
        currentSDG = getSDGForClasses(classes, force);
        return currentSDG;
    }
    
    public SDG getSDGForClasses(Set<JavaClass> classes, boolean force) {
        SDG sdg = new SDG();
        for (JavaClass jc : classes) {
            CCFG ccfg = cfgStore.getCCFG(jc, force);
            ClDG cldg = getClDGCore(ccfg, force);
            
            sdg.add(cldg);
            addClDG(cldg);
        }
        for (PDG pdg : sdg.getPDGs()) {
            addPDG(pdg);
        }
        PDGBuilder.connectMethodCalls(classes, sdg);
        PDGBuilder.connectFieldAccesses(sdg);
        return sdg;
    }
    
    public PDG getPDGWithinSDG(JavaMethod jmethod, boolean force) {
        getSDG(jmethod.getDeclaringClass(), force);
        return pdgMap.get(jmethod.getQualifiedName());
    }
    
    public PDG getPDGWithinSDG(JavaField jfield, boolean force) {
        getSDG(jfield.getDeclaringClass(), force);
        return pdgMap.get(jfield.getQualifiedName());
    }
    
    public ClDG getClDGWithinSDG(JavaClass jclass, boolean force) {
        getSDG(jclass, force);
        return cldgMap.get(jclass.getQualifiedName());
    }
    
    private void collectEfferentClasses(JavaClass jclass, Set<JavaClass> classes) {
        if (classes.contains(jclass)) {
            return;
        }
        classes.add(jclass);
        classes.addAll(collectDescendantClasses(jclass));
        
        for (JavaClass jc : jclass.getEfferentClassesInProject()) {
            collectEfferentClasses(jc, classes);
        }
    }
    
    private Set<JavaClass> collectDescendantClasses(JavaClass jclass) {
        return jclass.getDescendants().stream()
                     .filter(descendant -> descendant.isInProject())
                     .collect(Collectors.toSet());
    }
}
