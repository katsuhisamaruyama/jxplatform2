/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.pdg;

import org.jtool.eclipse.model.cfg.CFGEntry;

/**
 * The entry node of PDGs for a class or an interface.
 * @author Katsuhisa Maruyama
 */
public class PDGClassEntry extends PDGEntry {
    
    protected PDGClassEntry() {
    }
    
    public PDGClassEntry(CFGEntry node) {
        super(node);
    }
    
    @Override
    public PDGClassEntry clone() {
        PDGClassEntry cloneNode = new PDGClassEntry((CFGEntry)getCFGNode());
        clone(cloneNode);
        return cloneNode;
    }
    
    protected void clone(PDGClassEntry cloneNode) {
        super.setClone(cloneNode);
    }
}
