/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.pdg;

import org.jtool.eclipse.model.cfg.CFGEntry;

/**
 * The entry node of a PDG.
 * @author Katsuhisa Maruyama
 */
public class PDGEntry extends PDGNode {
    
    private PDG pdg = null;
    
    protected PDGEntry() {
        super();
    }
    
    public PDGEntry(CFGEntry node) {
        super(node);
    }
    
    public void setPDG(PDG g) {
        pdg = g;
    }
    
    public PDG getPDG() {
        return pdg;
    }
    
    public CFGEntry getCFGEntry() {
        return (CFGEntry)getCFGNode();
    }
    
    public String getName() {
        return getCFGEntry().getName();
    }
    
    public String getQualifiedName() {
        return getCFGEntry().getQualifiedName();
    }
    
    @Override
    public PDGEntry clone() {
        PDGEntry cloneNode = new PDGEntry(getCFGEntry());
        super.setClone(cloneNode);
        return cloneNode;
    }
}
