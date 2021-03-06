/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

/**
 * An edge represents control dependence between PDG nodes.
 * 
 * @author Katsuhisa Maruyama
 */
public class CD extends Dependence {
    
    public CD(PDGNode src, PDGNode dst) {
        super(src, dst);
    }
    
    public void setTrue() {
        kind = Kind.trueControlDependence;
    }
    
    public void setFalse() {
        kind = Kind.falseControlDependence;
    }
    
    public void setFallThrough() {
        kind = Kind.fallThroughControlDependence;
    }
    
    public void setDeclaration() {
        kind = Kind.declarationDependence;
    }
    
    public void setExceptionCatch() {
        kind = Kind.exceptionCatchDependence;
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        if (kind == Kind.trueControlDependence) {
            buf.append(" T");
        } else if (kind == Kind.falseControlDependence) {
            buf.append(" F");
        } else if (kind == Kind.fallThroughControlDependence) {
            buf.append(" FALL");
        } else if (kind == Kind.declarationDependence) {
            buf.append(" DECL");
        } else if (kind == Kind.exceptionCatchDependence) {
            buf.append(" EXCP");
        }
        return buf.toString();
    }
}
