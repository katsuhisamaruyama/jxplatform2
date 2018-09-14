/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

import org.jtool.eclipse.pdg.builder.PDGBuilder;
import org.jtool.eclipse.cfg.CCFG;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGStore;
import org.jtool.eclipse.cfg.builder.CFGMethodBuilder;
import org.jtool.eclipse.cfg.builder.CCFGBuilder;
import org.jtool.eclipse.cfg.builder.CFGFieldBuilder;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.jtool.eclipse.javamodel.builder.ProjectStore;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
 * An object stores PDGs.
 * 
 * @author Katsuhisa Maruyama
 */
public class PDGStore {
    
    private static PDGStore instance = new PDGStore();
    
    protected SDG currentSDG = null;
    protected Map<String, PDG> pdgStore = new HashMap<String, PDG>();
    
    private boolean visible = true;
    
    private PDGStore() {
    }
    
    public static PDGStore getInstance() {
        return instance;
    }
    
    public void destroy() {
        pdgStore.clear();
        currentSDG = null;
        CFGStore.getInstance().destroy();
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
            System.out.print(" - " + cfg.getStartNode().getQualifiedName() + " - PDG");
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
            System.out.print(" - " + jclass.getQualifiedName() + " - ClDG");
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
    
    private SDG getSDG(List<JavaClass> jclasses) {
        SDG sdg = new SDG();
        for (JavaClass jclass : jclasses) {
            CCFG ccfg = CCFGBuilder.build(jclass);
            ClDG cldg = PDGBuilder.buildClDG(ccfg);
            sdg.add(cldg);
        }
        return sdg;
    }
    
    public SDG getSDG(JavaProject jproject) {
        if (!jproject.getPath().equals(ProjectStore.getInstance().getCurrentProject().getPath())) {
            if (visible) {
                System.out.print(" - " + jproject.getName() + " - SDG");
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
    
    public PDG updatePDG(JavaProject jproject, JavaMethod jmethod) {
        removePDGsWithJavaModel(jproject, jproject.collectDanglingClasses(jmethod.getDeclaringClass()));
        return getPDG(jmethod);
    }
    
    public PDG updatePDG(JavaProject jproject, JavaField jfield) {
        removePDGsWithJavaModel(jproject, jproject.collectDanglingClasses(jfield.getDeclaringClass()));
        return getPDG(jfield);
    }
    
    public ClDG updatePDG(JavaProject jproject, JavaClass jclass) {
        removePDGsWithJavaModel(jproject,jproject.collectDanglingClasses(jclass));
        return getClDG(jclass);
    }
    
    public PDG updatePDGWithinSDG(JavaProject jproject, JavaMethod jmethod) {
        removePDGsWithJavaModel(jproject, jproject.collectDanglingClasses(jmethod.getDeclaringClass()));
        return getPDGWithinSDG(jmethod);
    }
    
    public PDG updatePDGWithinSDG(JavaProject jproject, JavaField jfield) {
        removePDGsWithJavaModel(jproject, jproject.collectDanglingClasses(jfield.getDeclaringClass()));
        return getPDGWithinSDG(jfield);
    }
    
    public ClDG updatePDGWithinSDG(JavaProject jproject, JavaClass jclass) {
        removePDGsWithJavaModel(jproject, jproject.collectDanglingClasses(jclass));
        return getClDGWithinSDG(jclass);
    }
    
    public void removePDGsWithJavaModel(JavaProject jproject, Set<JavaClass> classes) {
        removePDGs(jproject, classes);
        CFGStore.getInstance().removeCFGs(jproject, classes);
        jproject.removeClasses(classes);
    }
    
    public void removePDGs(JavaProject jproject, Set<JavaClass> classes) {
        for (JavaClass jclass : classes) {
            for (JavaMethod jmethod : jclass.getMethods()) {
                pdgStore.remove(jmethod.getQualifiedName());
            }
            for (JavaField jfeild : jclass.getFields()) {
                pdgStore.remove(jfeild.getQualifiedName());
            }
            pdgStore.remove(jclass.getQualifiedName());
        }
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public ClDG[] buildPDGsForTest(List<JavaClass> jclasses) {
        int size = jclasses.size();
        ClDG[] cldgs = new ClDG[size];
        if (visible) {
            System.out.println();
            System.out.println("** Building PDGs of " + size + " classes ");
        }
        int count = 1;
        for (JavaClass jclass : jclasses) {
            cldgs[count - 1] = getClDG(jclass);
            if (visible) {
                System.out.println(" (" + count + "/" + size + ")");
            }
            count++;
        }
        return cldgs;
    }
}
