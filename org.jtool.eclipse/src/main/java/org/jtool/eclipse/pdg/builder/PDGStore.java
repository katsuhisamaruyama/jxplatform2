/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg.builder;

import org.jtool.eclipse.cfg.CCFG;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.builder.CFGMethodBuilder;
import org.jtool.eclipse.cfg.builder.CFGStore;
import org.jtool.eclipse.cfg.builder.CCFGBuilder;
import org.jtool.eclipse.cfg.builder.CFGFieldBuilder;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.jtool.eclipse.javamodel.builder.ProjectStore;
import org.jtool.eclipse.pdg.ClDG;
import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.SDG;

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
    
    private static PDGStore instance = new PDGStore();
    
    protected SDG currentSDG = null;
    protected Map<String, PDG> pdgStore = new HashMap<String, PDG>();
    
    private boolean ignoringJumpEdge = false;
    
    private boolean visible = true;
    
    private PDGStore() {
    }
    
    public static PDGStore getInstance() {
        return instance;
    }
    
    public void destroy() {
        pdgStore.clear();
        currentSDG = null;
    }
    
    public void setIgnoringJumpEdge(boolean ignoringJumpEdge) {
        this.ignoringJumpEdge = ignoringJumpEdge;
    }
    
    public boolean ignoringJumpEdge() {
        return ignoringJumpEdge;
    }
    
    public int size() {
        return pdgStore.size();
    }
    
    private void addPDG(PDG pdg) {
        pdgStore.put(pdg.getEntryNode().getQualifiedName(), pdg);
    }
    
    public PDG getPDG(String fqn) {
        return pdgStore.get(fqn);
    }
    
    public PDG getPDG(CFG cfg) {
        if (visible) {
            System.out.print(" - " + cfg.getStartNode().getQualifiedName() + " - PDG\n");
        }
        
        PDG pdg = PDGBuilder.buildPDG(cfg);
        addPDG(pdg);
        if (CFGStore.getInstance().creatingActualNodes()) {
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
        if (CFGStore.getInstance().creatingActualNodes()) {
            SDG sdg = getSDG(jmethod.getDeclaringClass());
            return sdg.getPDG(jmethod.getQualifiedName());
        } else {
            return getPDG(jmethod);
        }
    }
    
    public PDG getPDGWithinSDG(JavaField jfield) {
        if (CFGStore.getInstance().creatingActualNodes()) {
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
        
        if (visible) {
            System.out.print(" - " + jclass.getQualifiedName() + " - ClDG\n");
        }
        
        CCFG ccfg = CCFGBuilder.build(jclass);
        ClDG cldg = PDGBuilder.buildClDG(ccfg);
        if (CFGStore.getInstance().creatingActualNodes()) {
            PDGBuilder.connectParameters(cldg);
        }
        return cldg;
    }
    
    public ClDG getClDGWithinSDG(JavaClass jclass) {
        if (CFGStore.getInstance().creatingActualNodes()) {
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
        if (CFGStore.getInstance().creatingActualNodes()) {
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
            ClDG cldg = PDGBuilder.buildClDG(ccfg);
            sdg.add(cldg);
            sdg.setCCFG(ccfg);
        }
        return sdg;
    }
    
    public SDG getSDG(JavaProject jproject) {
        if (!jproject.getPath().equals(ProjectStore.getInstance().getCurrentProject().getPath())) {
            if (visible) {
                System.out.print(" - " + jproject.getName() + " - SDG\n");
            }
            
            pdgStore.clear();
            currentSDG = getSDG(jproject.getClasses());
            for (PDG pdg : currentSDG.getPDGs()) {
                addPDG(pdg);
            }
            if (CFGStore.getInstance().creatingActualNodes()) {
                PDGBuilder.connectParameters(jproject.getClasses(), currentSDG);
            }
        }
        return currentSDG;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public boolean isVisible() {
        return visible;
    }
}
