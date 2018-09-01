/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

import org.jtool.eclipse.cfg.CCFG;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGStore;
import org.jtool.eclipse.cfg.builder.CFGClassBuilder;
import org.jtool.eclipse.cfg.builder.CFGFieldBuilder;
import org.jtool.eclipse.cfg.builder.CFGMethodBuilder;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.jtool.eclipse.pdg.builder.PDGBuilder;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * An object representing a virtual project.
 * @author Katsuhisa Maruyama
 */
public class PDGStore {
    
    private static PDGStore instance = new PDGStore();
    
    protected Map<String, PDG> pdgStore = new HashMap<String, PDG>();
    
    private boolean visible = true;
    
    private PDGStore() {
    }
    
    public static PDGStore getInstance() {
        return instance;
    }
    
    public void destroy() {
        pdgStore.clear();
        CFGStore.getInstance().destroy();
    }
    
    public PDG getPDG(CFG cfg) {
        return PDGBuilder.build(cfg);
    }
    
    public PDG getPDG(JavaClass jclass) {
        PDG cldg = getPDG(jclass.getQualifiedName());
        if (cldg == null) {
            if (visible) {
                System.out.print(" - " + jclass.getQualifiedName() + " - PDG");
            }
            cldg = buildPDG(jclass);
        }
        return cldg;
    }
    
    public PDG getPDG(JavaMethod jmethod) {
        PDG pdg = getPDG(jmethod.getQualifiedName());
        if (pdg == null) {
            if (visible) {
                System.out.print(" - " + jmethod.getQualifiedName() + " - PDG");
            }
            pdg = buildPDG(jmethod);
        }
        return pdg;
    }
    
    public PDG getPDG(JavaField jfield) {
        PDG pdg = getPDG(jfield.getQualifiedName());
        if (pdg == null) {
            if (visible) {
                System.out.print(" - " + jfield.getQualifiedName() + " - PDG");
            }
            pdg = buildPDG(jfield);
        }
        return pdg;
    }
    
    private void addPDG(PDG pdg) {
        pdgStore.put(pdg.getEntryNode().getQualifiedName(), pdg);
    }
    
    public PDG getPDG(String fqn) {
        return pdgStore.get(fqn);
    }
    
    public int size() {
        return pdgStore.size();
    }
    
    private ClDG buildPDG(JavaClass jclass) {
        ClDG cldg = new ClDG();
        CCFG ccfg = CFGClassBuilder.build(jclass);
        PDGClassEntry pdgentry = new PDGClassEntry(ccfg.getStartNode());
        cldg.setEntryNode(pdgentry);
        
        for (JavaMethod jm : jclass.getMethods()) {
            PDG pdg = buildPDG(jm);
            addPDG(pdg);
            cldg.add(pdg);
        }
        for (JavaField jf : jclass.getFields()) {
            PDG pdg = buildPDG(jf);
            addPDG(pdg);
            cldg.add(pdg);
        }
        /*
        for (JavaClass jt : jclass.getInnerClasses()) {
            PDG pdg = buildPDG(jt);
            addPDG(pdg);
            cldg.add(pdg);
        }
        */
        return cldg;
    }
    
    private PDG buildPDG(JavaMethod jmethod) {
        CFG cfg = CFGMethodBuilder.build(jmethod);
        PDG pdg = PDGBuilder.build(cfg);
        if (CFGStore.getInstance().creatingActualNodes()) {
            PDGBuilder.connectActualParameters(pdg);
        }
        return pdg;
    }
    
    private PDG buildPDG(JavaField jfield) {
        CFG cfg = CFGFieldBuilder.build(jfield);
        PDG pdg = PDGBuilder.build(cfg);
        return pdg;
    }
    
    public void buildPDGs(List<JavaClass> jclasses) {
        int size = jclasses.size();
        if (visible) {
            System.out.println();
            System.out.println("** Building PDGs of " + size + " classes ");
        }
        int count = 1;
        for (JavaClass jclass : jclasses) {
            PDGStore.getInstance().getPDG(jclass);
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
