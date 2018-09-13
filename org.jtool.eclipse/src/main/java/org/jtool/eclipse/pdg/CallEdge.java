/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

/**
 * An edge that represents a relationship between caller and callee in ClDGs and SDGs.
 * 
 * @author Katsuhisa Maruyama
 */
public class CallEdge extends Dependence {
    
    protected CallEdge() {
        super();
    }
    
    public CallEdge(PDGNode src, PDGNode dst) {
        super(src, dst);
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        buf.append("CALL");
        return buf.toString();
    }
}
