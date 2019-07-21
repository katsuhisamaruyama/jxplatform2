/*
 *  Copyright 2018-2019
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

/**
 * An object that stores information on PDGs in the project.
 * 
 * @author Katsuhisa Maruyama
 */
public class PDGStore {
    
    private CFGStore cfgStore;
    boolean containingFallThroughEdge = true;
    
    private Map<String, PDG> pdgMap = new HashMap<String, PDG>();
    private Map<String, ClDG> cldgMap = new HashMap<String, ClDG>();
    
    private SDG currentSDG;
    
    public PDGStore(CFGStore cfgStore) {
        this.cfgStore = cfgStore;
    }
    
    public void destroy() {
        pdgMap.clear();
        cfgStore = null;
    }
    
    public void setContainingFallThroughEdge(boolean containingFallThroughEdge) {
        this.containingFallThroughEdge = containingFallThroughEdge;
    }
    
    public boolean isContainingFallThroughEdge() {
        return containingFallThroughEdge;
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
        if (!force) {
            PDG pdg = pdgMap.get(cfg.getQualifiedName());
            if (pdg != null) {
                return pdg;
            }
        }
        PDG pdg = PDGBuilder.buildPDG(cfg, containingFallThroughEdge);
        PDGBuilder.connectParametersConservatively(pdg);
        addPDG(pdg);
        return pdg;
    }
    
    public ClDG getClDG(CCFG ccfg, boolean force) {
        if (!force) {
            ClDG cldg = cldgMap.get(ccfg.getQualifiedName());
            if (cldg != null) {
                return cldg;
            }
        }
        ClDG cldg = PDGBuilder.buildClDG(ccfg, containingFallThroughEdge);
        
        PDGBuilder.connectParameters(cldg);
        return cldg;
    }
    
    public PDG getPDG(JavaMethod jmethod, boolean force) {
        CFG cfg = cfgStore.getCFG(jmethod, force);
        return getPDG(cfg, force);
    }
    
    public PDG getPDG(JavaField jfield, boolean force) {
        CFG cfg = cfgStore.getCFG(jfield, force);
        return getPDG(cfg, force);
    }
    
    public PDG getPDGWithinSDG(JavaMethod jmethod, boolean force) {
        getSDG(jmethod.getDeclaringClass(), force);
        return pdgMap.get(jmethod.getQualifiedName());
    }
    
    public PDG getPDGWithinSDG(JavaField jfield, boolean force) {
        getSDG(jfield.getDeclaringClass(), force);
        return pdgMap.get(jfield.getQualifiedName());
    }
    
    public ClDG getClDG(JavaClass jclass, boolean force) {
        CCFG ccfg = cfgStore.getCCFG(jclass, force);
        return getClDG(ccfg, force);
    }
    
    public ClDG getClDGWithinSDG(JavaClass jclass, boolean force) {
        getSDG(jclass, force);
        return cldgMap.get(jclass.getQualifiedName());
    }
    
    public SDG getSDG(Set<JavaClass> classes, boolean force) {
        Set<JavaClass> allClasses = new HashSet<JavaClass>();
        for (JavaClass jc : classes) {
            collectEfferentClasses(jc, allClasses);
        }
        
        SDG sdg = getSDGForClasses(allClasses, force);
        return sdg;
    }
    
    public SDG getSDGForClasses(Set<JavaClass> classes, boolean force) {
        SDG sdg = new SDG();
        for (JavaClass jc : classes) {
            CCFG ccfg = cfgStore.getCCFG(jc, force);
            ClDG cldg = getClDG(ccfg, force);
            
            sdg.add(cldg);
            addClDG(cldg);
        }
        for (PDG pdg : sdg.getPDGs()) {
            addPDG(pdg);
        }
        PDGBuilder.connectParameters(classes, sdg);
        PDGBuilder.connectFieldAccesses(sdg);
        return sdg;
    }
    
    public SDG getSDG(JavaClass jclass, boolean force) {
        Set<JavaClass> classes = new HashSet<JavaClass>();
        classes.add(jclass);
        return getSDG(classes, force);
    }
    
    public SDG getSDG(boolean force) {
        if (!force && currentSDG != null) {
            return currentSDG;
        }
        Set<JavaClass> classes = new HashSet<JavaClass>(cfgStore.getJavaProject().getClasses());
        currentSDG = getSDGForClasses(classes, force);
        return currentSDG;
    }
    
    private void collectEfferentClasses(JavaClass jclass, Set<JavaClass> classes) {
        if (classes.contains(jclass)) {
            return;
        }
        classes.add(jclass);
        
        for (JavaClass jc : jclass.getEfferentClassesInProject()) {
            collectEfferentClasses(jc, classes);
        }
    }
    
    @SuppressWarnings("unused")
    private void collectDescendantClasses(JavaClass jclass, Set<JavaClass> jclasses) {
        for (JavaClass descendant : jclass.getDescendants()) {
            if (descendant.isInProject() && !jclasses.contains(descendant)) {
                jclasses.add(descendant);
            }
        }
    }
}
