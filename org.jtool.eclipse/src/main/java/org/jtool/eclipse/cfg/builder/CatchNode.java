/*
 *  Copyright 2018-2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGStatement;

/**
 * A node for a <code>catch</code> clause of a CFG.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
class CatchNode extends CFGStatement {
    
    private ITypeBinding type;
    private CFGNode parent;
    
    CatchNode() {
    }
    
    CatchNode(ASTNode node, CFGNode.Kind kind, ITypeBinding type) {
        super(node, kind);
        this.type = type;
    }
    
    ITypeBinding getType() {
        return type;
    }
    
    String getTypeName() {
        return type.getQualifiedName();
    }
    
    void setParent(CFGNode parent) {
        this.parent = parent;
    }
    
    CFGNode getParent() {
        return parent;
    }
}
