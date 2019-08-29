/*
 *  Copyright 2018-2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.eclipse.jdt.core.dom.ASTNode;
import org.jtool.eclipse.cfg.CFGStatement;

/**
 * A node for a <code>catch</code> clause of a CFG.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
class CatchNode extends CFGStatement {
    
    private String exceptionType;
    
    CatchNode() {
    }
    
    CatchNode(ASTNode node, Kind kind) {
        super(node, kind);
    }
    
    void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }
    
    String getExceptionType() {
        return exceptionType;
    }
}
