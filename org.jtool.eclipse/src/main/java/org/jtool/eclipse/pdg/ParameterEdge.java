/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

import org.jtool.eclipse.cfg.JReference;

/**
 * An edge that represents a relationship between parameters of a caller and its callee in PDGs.
 * 
 * @author Katsuhisa Maruyama
 */
public class ParameterEdge extends DD {
    
    protected ParameterEdge() {
        super();
    }
    
    public ParameterEdge(PDGNode src, PDGNode dst) {
        super(src, dst);
    }
    
    public ParameterEdge(PDGNode src, PDGNode dst, JReference jvar) {
        super(src, dst);
        this.jvar = jvar;
    }
    
    @Override
    public JReference getVariable() {
        return jvar;
    }
    
    @Override
    public void setParameterIn() {
        kind = Kind.parameterIn;
    }
    
    @Override
    public boolean isParameterIn() {
        return kind == Kind.parameterIn;
    }
    
    @Override
    public void setParameterOut() {
        kind = Kind.parameterOut;
    }
    
    @Override
    public boolean isParameterOut() {
        return kind == Kind.parameterOut;
    }
}
