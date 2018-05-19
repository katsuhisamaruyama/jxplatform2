/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.cfg;

/**
 * An object storing information about a class control flow graph (CCFG).
 * @author Katsuhisa Maruyama
 */
public class CCFG extends CFG {
    
    public CFGClassEntry getStartNode() {
        return (CFGClassEntry)start;
    }
}
