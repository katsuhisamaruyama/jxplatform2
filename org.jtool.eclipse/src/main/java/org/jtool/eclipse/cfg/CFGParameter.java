/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * A node for a parameter of a method declaration.
 * 
 * @author Katsuhisa Maruyama
 */
public class CFGParameter extends CFGStatement {
    
    private int ordinal;
    private CFGNode parent;
    
    protected CFGParameter() {
        super();
    }
    
    public CFGParameter(ASTNode node, CFGNode.Kind kind, int ordinal) {
        super(node, kind);
        this.ordinal = ordinal;
    }
    
    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }
    
    public int getOrdinal() {
        return ordinal;
    }
    
    public void setParent(CFGNode node) {
        parent = node;
    }
    
    public CFGNode getParent() {
        return parent;
    }
    
    public JAccess getDefVariable() {
        return getDefVariables().get(0);
    }
    
    public JAccess getUseVariable() {
        return getUseVariables().get(0);
    }
}
