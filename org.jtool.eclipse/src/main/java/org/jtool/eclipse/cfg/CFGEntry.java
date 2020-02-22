/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * The entry node of a CFG.
 * 
 * @author Katsuhisa Maruyama
 */
public abstract class CFGEntry extends CFGNode {
    
    private String name;
    private String signature;
    private String fqn;
    private CommonCFG cfg = null;
    
    protected CFGEntry(ASTNode node, CFGNode.Kind kind, String name, String signature, String fqn) {
        super(node, kind);
        this.name = name;
        this.signature = signature;
        this.fqn = fqn;
    }
    
    public void setCFG(CommonCFG cfg) {
        this.cfg = cfg;
    }
    
    public CommonCFG getCFG() {
        return cfg;
    }
    
    public String getName() {
        return name;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public String getQualifiedName() {
        return fqn;
    }
}
