/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.slice;

import org.jtool.eclipse.pdg.DD;
import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.PDGStatement;

/**
 * An object storing information about a closure created by traversing only the control dependence.
 * 
 * @author Katsuhisa Maruyama
 */
public class DDClosure extends PDG {
    
    public DDClosure(PDGStatement node) {
        traverseBackward(node);
    }
    
    private void traverseBackward(PDGStatement anchor) {
        add(anchor);
        
        for (DD edge : anchor.getIncomingDDEdges()) {
            add(edge);
            PDGStatement node = (PDGStatement)edge.getSrcNode();
            
            if (!getNodes().contains(node)) {
                traverseBackward(node);
            }
        }
    }
}
