/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

import org.jtool.eclipse.cfg.CFGEntry;

/**
 * The entry node of PDGs for a method or a constructor.
 * @author Katsuhisa Maruyama
 */
public class PDGMethodEntry extends PDGEntry {
    
    protected PDGMethodEntry() {
        super();
    }
    
    public PDGMethodEntry(CFGEntry node) {
        super(node);
    }
    
    @Override
    public PDGMethodEntry clone() {
        PDGMethodEntry cloneNode = new PDGMethodEntry((CFGEntry)getCFGNode());
        super.setClone(cloneNode);
        return cloneNode;
    }
}
