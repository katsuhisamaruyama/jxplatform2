/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

import org.jtool.eclipse.cfg.JVariable;
import org.jtool.eclipse.graph.GraphElement;

/**
 * An edge of a PDG, which represents data dependence between PDG nodes.
 * @author Katsuhisa Maruyama
 */
public class DD extends DependenceEdge {
    
    protected JVariable jvar;
    private PDGNode loopCarriedNode = null;
    
    protected DD() {
        super();
    }
    
    public DD(PDGNode src, PDGNode dst) {
        super(src, dst);
    }
    
    public DD(PDGNode src, PDGNode dst, JVariable jvar) {
        super(src, dst);
        this.jvar = jvar;
    }
    
    public void setVariable(JVariable jvar) {
        this.jvar = jvar;
    }
    
    public JVariable getVariable() {
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
    
    public boolean isLIDD() {
        return kind == Kind.loopIndependentDefUseDependence;
    }
    
    public void setLCDD() {
        kind = Kind.loopCarriedDefUseDependence;
    }
    
    public boolean isLCDD() {
        return kind == Kind.loopCarriedDefUseDependence;
    }
    
    public void setDefOrder() {
        kind = Kind.defOrderDependence;
    }
    
    public boolean isDefOrder() {
        return kind == Kind.defOrderDependence;
    }
    
    public void setOutput() {
        kind = Kind.outputDependence;
    }
    
    public boolean isOutput() {
        return kind == Kind.outputDependence;
    }
    
    public void setAnti() {
        kind = Kind.antiDependence;
    }
    
    public boolean isAnti() {
        return kind == Kind.antiDependence;
    }
    
    public void setParameterIn() {
        kind = Kind.parameterIn;
    }
    
    public boolean isParameterIn() {
        return kind == Kind.parameterIn;
    }
    
    public void setParameterOut() {
        kind = Kind.parameterOut;
    }
    
    public boolean isParameterOut() {
        return kind == Kind.parameterOut;
    }
    
    public void setSummary() {
        kind = Kind.summary;
    }
    
    public boolean isSummary() {
        return kind == Kind.summary;
    }
    
    @Override
    public boolean equals(GraphElement elem) {
        if (elem == null || !(elem instanceof DD)) {
            return false;
        }
        DD edge = (DD)elem;
        return super.equals(elem) && jvar.equals(edge.jvar);
    }
    
    @Override
    public int hashCode() {
        return Long.valueOf(src.getId() + dst.getId()).hashCode();
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
        buf.append(getVariable().getName());
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
        } else if (kind == Kind.summary) {
            buf.append(" SUMM");
        }
        return buf.toString();
    }
}
