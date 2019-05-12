/*
 *  Copyright 2018-2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

import org.jtool.eclipse.cfg.CFGEntry;

/**
 * The entry node of a PDG or a ClDG.
 * 
 * @author Katsuhisa Maruyama
 */
public class PDGEntry extends PDGNode {
    
    private CommonPDG pdg = null;
    
    protected PDGEntry() {
        super();
    }
    
    public PDGEntry(CFGEntry node) {
        super(node);
    }
    
    public void setPDG(CommonPDG pdg) {
        this.pdg = pdg;
    }
    
    public CommonPDG getPDG() {
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
}
