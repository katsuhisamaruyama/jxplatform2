/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

import org.jtool.eclipse.cfg.CFGClassEntry;

/**
 * The entry node of a PDG for a class or an interface.
 * 
 * @author Katsuhisa Maruyama
 */
public class PDGClassEntry extends PDGEntry {
    
    public PDGClassEntry(CFGClassEntry node) {
        super(node);
    }
    
    public CFGClassEntry getCFGClassEntry() {
        return (CFGClassEntry)getCFGNode();
    }
    
    @Override
    public String getName() {
        return getCFGClassEntry().getName();
    }
    
    @Override
    public String getQualifiedName() {
        return getCFGClassEntry().getQualifiedName();
    }
}
