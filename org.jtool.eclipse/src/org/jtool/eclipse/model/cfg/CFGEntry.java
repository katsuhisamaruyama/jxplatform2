/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.cfg;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * The entry node of a CFG.
 * @author Katsuhisa Maruyama
 */
public abstract class CFGEntry extends CFGNode {
    
    private String name;
    private String fqn;
    private CFG cfg = null;
    
    protected CFGEntry() {
    }
    
    protected CFGEntry(ASTNode node, CFGNode.Kind kind, String name, String fqn) {
        super(node, kind);
        this.name = name;
        this.fqn = fqn;
    }
    
    public void setCFG(CFG g) {
        cfg = g;
    }
    
    public CFG getCFG() {
        return cfg;
    }
    
    public String getName() {
        return name;
    }
    
    public String getQualifiedName() {
        return fqn;
    }
}
