/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * A merge node of a CFG.
 * 
 * @author Katsuhisa Maruyama
 */
public class CFGMerge extends CFGNode {
    
    private CFGStatement branch;
    
    public CFGMerge(ASTNode node, CFGStatement branch) {
        super(node, branch.getKind());
        this.branch = branch;
    }
    
    public void setBranch(CFGStatement branch) {
        this.branch = branch;
    }
    
    public CFGStatement getBranch() {
        return branch;
    }
    
    @Override
    public String toString() {
        if (getKind() != null) {
            return super.getIdString()  + " merge-" + branch.getKind().toString() + "(" + branch.getId() + ")";
        } else {
            return super.getIdString();
        }
    }
}
