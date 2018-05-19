/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.pdg;

/**
 * Constructs a call edge in a class dependence graph (ClDG).
 * @author Katsuhisa Maruyama
 */
public class CallEdge extends DependenceEdge {
    
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
