/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

import org.jtool.eclipse.pdg.builder.PDGBuilder;
import org.jtool.eclipse.pdg.builder.ClDGBuilder;
import org.jtool.eclipse.pdg.builder.SDGBuilder;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGStore;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.jtool.eclipse.javamodel.builder.ProjectStore;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

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
    
    public PDG getPDG(String fqn) {
        return pdgStore.get(fqn);
    }
    
    public PDG getPDGWithoutCache(JavaClass jclass) {
        if (visible) {
            System.out.print(" - " + jclass.getQualifiedName() + " - PDG");
        }
        
        ClDG cldg = ClDGBuilder.build(jclass);
        if (CFGStore.getInstance().creatingActualNodes()) {
            for (PDG pdg : cldg.getPDGs()) {
                PDGBuilder.connectParametersConservatively(pdg);
            }
        }
        return cldg;
    }
    
    public PDG getPDGWithoutCache(JavaMethod jmethod) {
        if (visible) {
            System.out.print(" - " + jmethod.getQualifiedName() + " - PDG");
        }
        
        PDG pdg = PDGBuilder.build(jmethod);
        if (CFGStore.getInstance().creatingActualNodes()) {
            PDGBuilder.connectParametersConservatively(pdg);
        }
        return pdg;
    }
    
    public PDG getPDGWithoutCache(JavaField jfield) {
        if (visible) {
            System.out.print(" - " + jfield.getQualifiedName() + " - PDG");
        }
        
        PDG pdg = PDGBuilder.build(jfield);
        if (CFGStore.getInstance().creatingActualNodes()) {
            PDGBuilder.connectParametersConservatively(pdg);
        }
        return pdg;
    }
    
    public PDG getPDGWithoutCache(CFG cfg) {
        if (visible) {
            System.out.print(" - " + cfg.getStartNode().getQualifiedName() + " - PDG");
        }
        
        PDG pdg = PDGBuilder.build(cfg);
        if (CFGStore.getInstance().creatingActualNodes()) {
            PDGBuilder.connectParametersConservatively(pdg);
        }
        return pdg;
    }
    
    public SDG getSDG(JavaProject jproject) {
        if (!jproject.getPath().equals(ProjectStore.getInstance().getCurrentProject().getPath())) {
            currentSDG = SDGBuilder.build(jproject.getClasses());
        }
        return currentSDG;
    }
    
    public ClDG getClDG(JavaClass jclass) {
        PDG found = getPDG(jclass.getQualifiedName());
        if (found != null && found instanceof ClDG) {
            return (ClDG)found;
        }
        
        if (visible) {
            System.out.print(" - " + jclass.getQualifiedName() + " - ClDG");
        }
        
        if (CFGStore.getInstance().creatingActualNodes()) {
            SDG sdg = SDGBuilder.build(jclass);
            for (PDG pdg : sdg.getPDGs()) {
                addPDG(pdg);
            }
            SDGBuilder.connectParameters(sdg);
            return sdg.getClDG(jclass.getQualifiedName());
        } else {
            return ClDGBuilder.build(jclass);
        }
    }
    
    public PDG getPDG(JavaMethod jmethod) {
        PDG found = getPDG(jmethod.getQualifiedName());
        if (found != null && found instanceof PDG) {
            return found;
        }
        
        if (visible) {
            System.out.print(" - " + jmethod.getQualifiedName() + " - PDG");
        }
        
        if (CFGStore.getInstance().creatingActualNodes()) {
            SDG sdg = SDGBuilder.build(jmethod);
            for (PDG pdg : sdg.getPDGs()) {
                addPDG(pdg);
            }
            SDGBuilder.connectParameters(sdg);
            return sdg.getPDG(jmethod.getQualifiedName());
            
        } else {
            ClDG cldg = ClDGBuilder.build(jmethod);
            for (PDG pdg : cldg.getPDGs()) {
                addPDG(pdg);
            }
            ClDGBuilder.connectParameters(cldg);
            return cldg.getPDG(jmethod.getQualifiedName());
        }
    }
    
    public PDG getPDG(JavaField jfield) {
        PDG found = getPDG(jfield.getQualifiedName());
        if (found != null && found instanceof PDG) {
            return found;
        }
        
        if (visible) {
            System.out.print(" - " + jfield.getQualifiedName() + " - PDG");
        }
        
        if (CFGStore.getInstance().creatingActualNodes()) {
            SDG sdg = SDGBuilder.build(jfield);
            for (PDG pdg : sdg.getPDGs()) {
                addPDG(pdg);
            }
            SDGBuilder.connectParameters(sdg);
            return sdg.getPDG(jfield.getQualifiedName());
            
        } else {
            ClDG cldg = ClDGBuilder.build(jfield);
            for (PDG pdg : cldg.getPDGs()) {
                addPDG(pdg);
            }
            ClDGBuilder.connectParameters(cldg);
            return cldg.getPDG(jfield.getQualifiedName());
        }
    }
    
    private void addPDG(PDG pdg) {
        pdgStore.put(pdg.getEntryNode().getQualifiedName(), pdg);
    }
    
    public int size() {
        return pdgStore.size();
    }
    
    public void buildPDGs(List<JavaClass> jclasses) {
        int size = jclasses.size();
        if (visible) {
            System.out.println();
            System.out.println("** Building PDGs of " + size + " classes ");
        }
        int count = 1;
        for (JavaClass jclass : jclasses) {
            getPDGWithoutCache(jclass);
            if (visible) {
                System.out.println(" (" + count + "/" + size + ")");
            }
            count++;
        }
        PDGStore.getInstance().destroy();
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public boolean isVisible() {
        return visible;
    }
}
