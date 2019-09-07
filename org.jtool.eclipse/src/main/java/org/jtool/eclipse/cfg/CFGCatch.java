/*
 *  Copyright 2018-2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * A node for a <code>catch</code> clause or <code>throws</code> of method in a CFG.
 * 
 * @author Katsuhisa Maruyama
 */
public class CFGCatch extends CFGNode {
    
    private ITypeBinding type;
    private CFGNode parent;
    
    protected CFGCatch() {
    }
    
    public CFGCatch(ASTNode node, CFGNode.Kind kind, ITypeBinding type) {
        super(node, kind);
        this.type = type;
    }
    
    public ITypeBinding getType() {
        return type;
    }
    
    public String getTypeName() {
        return type.getQualifiedName();
    }
    
    public void setParent(CFGNode parent) {
        this.parent = parent;
    }
    
    public CFGNode getParent() {
        return parent;
    }
}
