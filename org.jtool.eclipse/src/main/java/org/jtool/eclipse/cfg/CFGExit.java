/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * The exit node of a CFG.
 * 
 * @author Katsuhisa Maruyama
 */
public class CFGExit extends CFGNode {
    
    public CFGExit(ASTNode node, CFGNode.Kind kind) {
        super(node, kind);
    }
}
