/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg.builder;

import org.jtool.eclipse.pdg.ClDG;
import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.SDG;
import org.jtool.eclipse.cfg.CCFG;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.builder.CFGMethodBuilder;
import org.jtool.eclipse.cfg.builder.CFGStore;
import org.jtool.eclipse.cfg.builder.CCFGBuilder;
import org.jtool.eclipse.cfg.builder.CFGFieldBuilder;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaMethod;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * An object that stores information on PDGs in the project.
 * 
 * @author Katsuhisa Maruyama
 */
public class PDGStore {
    
    private CFGStore cfgStore;
    
    private Map<String, PDG> pdgMap = new HashMap<String, PDG>();
    boolean containingFallThroughEdge = true;
    
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
    
    public int size() {
        return pdgMap.size();
    }
    
    private void addPDG(PDG pdg) {
        pdgMap.put(pdg.getEntryNode().getQualifiedName(), pdg);
    }
    
    public PDG getPDG(String fqn) {
        return pdgMap.get(fqn);
    }
    
    public PDG getPDG(CFG cfg) {
        PDG pdg = PDGBuilder.buildPDG(cfg, containingFallThroughEdge);
        addPDG(pdg);
        if (cfgStore.creatingActualNodes()) {
            PDGBuilder.connectParametersConservatively(pdg);
        }
        return pdg;
    }
    
    public PDG getPDG(JavaMethod jmethod) {
        PDG pdg = getPDG(jmethod.getQualifiedName());
        if (pdg != null && pdg instanceof PDG) {
            return pdg;
        }
        
        CFG cfg = CFGMethodBuilder.build(jmethod, cfgStore.getJInfoStore());
        return getPDG(cfg);
    }
    
    public PDG getPDG(JavaField jfield) {
        PDG pdg = getPDG(jfield.getQualifiedName());
        if (pdg != null && pdg instanceof PDG) {
            return pdg;
        }
        
        CFG cfg = CFGFieldBuilder.build(jfield, cfgStore.getJInfoStore());
        return getPDG(cfg);
    }
    
    public PDG getPDGWithinSDG(JavaMethod jmethod) {
        if (cfgStore.creatingActualNodes()) {
            SDG sdg = getSDG(jmethod.getDeclaringClass());
            return sdg.getPDG(jmethod.getQualifiedName());
        } else {
            return getPDG(jmethod);
        }
    }
    
    public PDG getPDGWithinSDG(JavaField jfield) {
        if (cfgStore.creatingActualNodes()) {
            SDG sdg = getSDG(jfield.getDeclaringClass());
            return sdg.getPDG(jfield.getQualifiedName());
            
        } else {
            return getPDG(jfield);
        }
    }
    
    public ClDG getClDG(JavaClass jclass) {
        PDG pdg = getPDG(jclass.getQualifiedName());
        if (pdg != null && pdg instanceof ClDG) {
            return (ClDG)pdg;
        }
        
        CCFG ccfg = CCFGBuilder.build(jclass, cfgStore.getJInfoStore());
        ClDG cldg = PDGBuilder.buildClDG(ccfg, containingFallThroughEdge);
        if (cfgStore.creatingActualNodes()) {
            PDGBuilder.connectParameters(cldg);
        }
        return cldg;
    }
    
    public ClDG getClDGWithinSDG(JavaClass jclass) {
        if (cfgStore.creatingActualNodes()) {
            SDG sdg = getSDG(jclass);
            return sdg.getClDG(jclass.getQualifiedName());
            
        } else {
            return getClDG(jclass);
        }
    }
    
    public ClDG getClDG(JavaMethod jmethod) {
        return getClDG(jmethod.getDeclaringClass());
    }
    
    public ClDG getClDGWithinSDG(JavaMethod jmethod) {
        return getClDGWithinSDG(jmethod.getDeclaringClass());
    }
    
    public ClDG getClDG(JavaField jfield) {
        return getClDG(jfield.getDeclaringClass());
    }
    
    public ClDG getClDGWithinSDG(JavaField jfield) {
        return getClDGWithinSDG(jfield.getDeclaringClass());
    }
    
    public SDG getSDG(JavaClass jclass) {
        List<JavaClass> classes = new ArrayList<JavaClass>();
        collectEfferentClasses(jclass, classes);
        collectDescendantClasses(classes);
        
        SDG sdg = getSDG(classes);
        for (PDG pdg : sdg.getPDGs()) {
            addPDG(pdg);
        }
        if (cfgStore.creatingActualNodes()) {
            PDGBuilder.connectParameters(classes, sdg);
            PDGBuilder.connectFieldAccesses(sdg);
        }
        return sdg;
    }
    
    private void collectEfferentClasses(JavaClass jclass, List<JavaClass> classes) {
        if (classes.contains(jclass)) {
            return;
        }
        classes.add(jclass);
        
        for (JavaClass jc : jclass.getEfferentClassesInProject()) {
            collectEfferentClasses(jc, classes);
        }
    }
    
    private void collectDescendantClasses(List<JavaClass> jclasses) {
        for (JavaClass jc : new ArrayList<JavaClass>(jclasses)) {
            for (JavaClass descendant : jc.getDescendants()) {
                if (descendant.isInProject() && !jclasses.contains(descendant)) {
                    jclasses.add(descendant);
                }
            }
        }
    }
    
    private SDG getSDG(List<JavaClass> classes) {
        SDG sdg = new SDG();
        for (JavaClass jclass : classes) {
            CCFG ccfg = CCFGBuilder.build(jclass, cfgStore.getJInfoStore());
            ClDG cldg = PDGBuilder.buildClDG(ccfg, containingFallThroughEdge);
            sdg.add(cldg);
        }
        return sdg;
    }
    
    public SDG getSDG() {
        if (currentSDG == null) {
            currentSDG = getSDG(cfgStore.getJavaProject().getClasses());
            for (PDG pdg : currentSDG.getPDGs()) {
                addPDG(pdg);
            }
            if (cfgStore.creatingActualNodes()) {
                PDGBuilder.connectParameters(cfgStore.getJavaProject().getClasses(), currentSDG);
            }
        }
        return currentSDG;
    }
}
