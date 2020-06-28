/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

import org.jtool.eclipse.cfg.JReference;
import org.jtool.eclipse.graph.GraphElement;

/**
 * An edge that represents data dependence between PDG nodes.
 * 
 * @author Katsuhisa Maruyama
 */
public class DD extends Dependence {
    
    protected JReference jvar;
    private PDGNode loopCarriedNode = null;
    
    public DD(PDGNode src, PDGNode dst) {
        super(src, dst);
    }
    
    public DD(PDGNode src, PDGNode dst, JReference jvar) {
        super(src, dst);
        this.jvar = jvar;
    }
    
    public void setVariable(JReference jvar) {
        this.jvar = jvar;
    }
    
    public JReference getVariable() {
        return jvar;
    }
    
    public void setLoopCarriedNode(PDGNode node) {
        loopCarriedNode = node;
    }
    
    public PDGNode getLoopCarriedNode() {
        return loopCarriedNode;
    }
    
    public boolean isLoopCarried() {
        return loopCarriedNode != null;
    }
    
    public boolean isLoopIndependent() {
        return loopCarriedNode == null;
    }
    
    public boolean isDefUse() {
        return isLIDD() || isLCDD();
    }
    
    public void setLIDD() {
        kind = Kind.loopIndependentDefUseDependence;
    }
    
    public void setLCDD() {
        kind = Kind.loopCarriedDefUseDependence;
    }
    
    public void setDefOrder() {
        kind = Kind.defOrderDependence;
    }
    
    public void setOutput() {
        kind = Kind.outputDependence;
    }
    
    public void setAnti() {
        kind = Kind.antiDependence;
    }
    
    public void setParameterIn() {
        kind = Kind.parameterIn;
    }
    
    public void setParameterOut() {
        kind = Kind.parameterOut;
    }
    
    public void setFieldAccess() {
        kind = Kind.fieldAccess;
    }
    
    public void setSummary() {
        kind = Kind.summary;
    }
    
    @Override
    public boolean equals(GraphElement elem) {
        return (elem instanceof DD) ? equals((DD)elem) : false;
    }
    
    public boolean equals(DD edge) {
        return edge != null && (super.equals((Dependence)edge) && jvar.equals(edge.jvar));
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public DD clone() {
        DD cloneEdge = new DD(getSrcNode(), getDstNode());
        super.setClone(cloneEdge);
        setClone(cloneEdge);
        return cloneEdge;
    }
    
    protected void setClone(DD cloneEdge) {
        cloneEdge.setVariable(getVariable());
        cloneEdge.setLoopCarriedNode(getLoopCarriedNode());
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        buf.append(" [ ");
        buf.append(jvar.getReferenceName());
        if (isLoopCarried()) {
            buf.append(" LC = ");
            buf.append(getLoopCarriedNode().getId());
        }
        buf.append(" ]");
        if (kind == Kind.loopIndependentDefUseDependence) {
            buf.append(" LIDD");
        } else if (kind == Kind.loopCarriedDefUseDependence) {
            buf.append(" LCDD");
        } else if (kind == Kind.defOrderDependence) {
            buf.append(" DO");
        } else if (kind == Kind.outputDependence) {
            buf.append(" OD");
        } else if (kind == Kind.antiDependence) {
            buf.append(" AD");
        } else if (kind == Kind.parameterIn) {
            buf.append(" PIN");
        } else if (kind == Kind.parameterOut) {
            buf.append(" POUT");
        } else if (kind == Kind.fieldAccess) {
            buf.append(" FACC");
        } else if (kind == Kind.summary) {
            buf.append(" SUMM");
        }
        return buf.toString();
    }
}
