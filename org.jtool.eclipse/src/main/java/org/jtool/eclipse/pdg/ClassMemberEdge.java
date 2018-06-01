/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

/**
 * Constructs a class member edge in a class dependence graph (ClDG).
 * @author Katsuhisa Maruyama
 */
public class ClassMemberEdge extends DependenceEdge {
    
    protected ClassMemberEdge() {
        super();
    }
    
    public ClassMemberEdge(PDGNode src, PDGNode dst) {
        super(src, dst);
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        buf.append("MEMBER");
        return buf.toString();
    }
}
