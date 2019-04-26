/*
 *  Copyright 2018-2019
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
    
    protected CD() {
        super();
    }
    
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
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        if (kind == Kind.trueControlDependence) {
            buf.append(" T");
        } else if (kind == Kind.falseControlDependence) {
            buf.append(" F");
        } else if (kind == Kind.fallThroughControlDependence) {
            buf.append(" Fall:");
        }
        return buf.toString();
    }
}
