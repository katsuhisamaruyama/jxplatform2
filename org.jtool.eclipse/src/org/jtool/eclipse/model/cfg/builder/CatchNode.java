/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.cfg.builder;

import org.eclipse.jdt.core.dom.ASTNode;
import org.jtool.eclipse.model.cfg.CFGParameter;
import org.jtool.eclipse.model.cfg.CFGStatement;

/**
 * A node for a <code>catch</code> clause of a CFG.
 * @author Katsuhisa Maruyama
 */
class CatchNode extends CFGStatement {
    
    private CFGParameter formalIn;
    
    CatchNode() {
    }
    
    CatchNode(ASTNode node, Kind kind) {
        super(node, kind);
    }
    
    void setFormalIn(CFGParameter node) {
        formalIn = node;
    }
    
    CFGParameter getFormalIn() {
        return formalIn;
    }
}
