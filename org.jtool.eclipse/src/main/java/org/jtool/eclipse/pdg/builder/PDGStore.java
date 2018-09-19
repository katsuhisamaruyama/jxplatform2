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
 * An object stores PDGs.
 * 
 * @author Katsuhisa Maruyama
 */
public class PDGStore {
    
    private CFGStore cfgStore;
    
    private Map<String, PDG> pdgMap = new HashMap<String, PDG>();
    private boolean ignoringJumpEdge = false;
    
    private SDG currentSDG;
    
    public PDGStore(CFGStore cfgStore) {
        this.cfgStore = cfgStore;
    }
    
    public void destroy() {
        pdgMap.clear();
        cfgStore = null;
    }
    
    public void setIgnoringJumpEdge(boolean ignoringJumpEdge) {
        this.ignoringJumpEdge = ignoringJumpEdge;
    }
    
    public boolean ignoringJumpEdge() {
        return ignoringJumpEdge;
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
        PDG pdg = PDGBuilder.buildPDG(cfg, ignoringJumpEdge);
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
        
        CFG cfg = CFGMethodBuilder.build(jmethod);
        return getPDG(cfg);
    }
    
    public PDG getPDG(JavaField jfield) {
        PDG pdg = getPDG(jfield.getQualifiedName());
        if (pdg != null && pdg instanceof PDG) {
            return pdg;
        }
        
        CFG cfg = CFGFieldBuilder.build(jfield);
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
        
        CCFG ccfg = CCFGBuilder.build(jclass);
        ClDG cldg = PDGBuilder.buildClDG(ccfg, ignoringJumpEdge);
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
        List<JavaClass> jclasses = new ArrayList<JavaClass>();
        collectEfferentClasses(jclass, jclasses);
        collectDescendantClasses(jclasses);
        
        SDG sdg = getSDG(jclasses);
        for (PDG pdg : sdg.getPDGs()) {
            addPDG(pdg);
        }
        if (cfgStore.creatingActualNodes()) {
            PDGBuilder.connectParameters(jclasses, sdg);
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
            CCFG ccfg = CCFGBuilder.build(jclass);
            ClDG cldg = PDGBuilder.buildClDG(ccfg, ignoringJumpEdge);
            sdg.add(cldg);
            sdg.setCCFG(ccfg);
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
