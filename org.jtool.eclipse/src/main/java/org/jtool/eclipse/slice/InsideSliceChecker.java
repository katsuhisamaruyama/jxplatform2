/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.slice;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import java.util.Set;

/**
 * Visits a Java program and extracts statements contained in a slice.
 * 
 * @author Katsuhisa Maruyama
 */
public class InsideSliceChecker extends ASTVisitor {
    
    private Set<ASTNode> sliceNodes;
    private int origOffset;
    private int newOffset;
    
    private boolean inSlice = false;
    
    public InsideSliceChecker(Set<ASTNode> sliceNodes, int origOffset, int newOffset) {
        this.sliceNodes = sliceNodes;
        this.origOffset = origOffset;
        this.newOffset = newOffset;
    }
    
    private boolean contains(ASTNode node) {
        for (ASTNode n : sliceNodes) {
            if (n.getNodeType() == node.getNodeType() &&
                n.getStartPosition() - origOffset == node.getStartPosition() - newOffset) {
                return true;
            }
        }
        return false;
    }
    
    public boolean existsSliceNode() {
        return inSlice;
    }
    
    @Override
    public void preVisit(ASTNode node) {
        if (contains(node)) {
            inSlice = true;
        }
    }
}
