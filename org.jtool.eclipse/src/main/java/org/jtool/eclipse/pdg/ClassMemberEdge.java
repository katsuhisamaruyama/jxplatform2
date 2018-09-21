/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

/**
 * An edge that represents a class and its members in ClDGs.
 * 
 * @author Katsuhisa Maruyama
 */
public class ClassMemberEdge extends Dependence {
    
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
        buf.append(" MEMBER");
        return buf.toString();
    }
}
