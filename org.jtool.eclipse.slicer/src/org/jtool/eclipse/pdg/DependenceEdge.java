/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

import org.jtool.eclipse.graph.GraphEdge;
import org.jtool.eclipse.graph.GraphElement;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * An edge of a PDG, which represents dependence between PDG nodes.
 * @author Katsuhisa Maruyama
 */
public class DependenceEdge extends GraphEdge {
    
    protected Kind kind;
    
    public enum Kind {
        controlDependence,               // Control dependence in general
        trueControlDependence,           // Control dependence with respect to a true-branch flow
        falseControlDependence,          // Control dependence with respect to a false-branch flow
        fallControlDependence,           // Control dependence with respect to a fall-through flow
        
        dataDependence,                  // Data dependence in general
        loopIndependentDefUseDependence, // Data dependence with respect to a loop-independent variable
        loopCarriedDefUseDependence,     // Data dependence with respect to a loop-carried variable
        defOrderDependence,              // Data dependence based on the order of definitions of variables
        outputDependence,                // Data dependence based on the order of outputs of variables
        antiDependence,                  // Data dependence based on the order of use and definition of variables
        parameterIn,                     // Data dependence with respect to incoming parameter passing
        parameterOut,                    // Data dependence with respect to outgoing parameter passing
        summary,                         // Data dependence between actual-in and actual-out nodes
        
        classMember,                     // Connection between a class and its members
    }
    
    protected DependenceEdge() {
        super();
    }
    
    protected DependenceEdge(PDGNode src, PDGNode dst) {
        super(src, dst);
    }
    
    public void setKind(Kind kind) {
        this.kind = kind;
    }
    
    public boolean isCD() {
        return kind == Kind.trueControlDependence ||
               kind == Kind.falseControlDependence ||
               kind == Kind.fallControlDependence;
    }
    
    public boolean isDD() {
        return kind == Kind.loopIndependentDefUseDependence ||
               kind == Kind.loopCarriedDefUseDependence ||
               kind == Kind.defOrderDependence ||
               kind == Kind.outputDependence ||
               kind == Kind.antiDependence ||
               kind == Kind.parameterIn ||
               kind == Kind.parameterOut ||
               kind == Kind.summary;
    }
    
    @Override
    public PDGNode getSrcNode() {
        return (PDGNode)src;
    }
    
    @Override
    public PDGNode getDstNode() {
        return (PDGNode)dst;
    }
    
    @Override
    public boolean equals(GraphElement elem) {
        if (elem == null || !(elem instanceof DependenceEdge)) {
            return false;
        }
        DependenceEdge edge = (DependenceEdge)elem;
        return this == edge || (src.equals(edge.src) && dst.equals(dst) && kind == edge.kind);
    }
    
    @Override
    public int hashCode() {
        return Long.valueOf(src.getId() + dst.getId()).hashCode();
    }
    
    @Override
    public DependenceEdge clone() {
        DependenceEdge cloneEdge = new DependenceEdge(getSrcNode(), getDstNode());
        super.setClone(cloneEdge);
        setClone(cloneEdge);
        return cloneEdge;
    }
    
    public void print() {
        System.out.println(toString());
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(src.getId());
        buf.append(" -> ");
        buf.append(dst.getId());
        return buf.toString();
    }
    
    protected List<DependenceEdge> sortControlFlows(Collection<? extends DependenceEdge> co) {
        List<DependenceEdge> edges = new ArrayList<DependenceEdge>(co);
        Collections.sort(edges, new Comparator<DependenceEdge>() {
            
            public int compare(DependenceEdge edge1, DependenceEdge edge2) {
                if (edge2.src.getId() == edge1.src.getId()) {
                    if (edge2.dst.getId() == edge1.dst.getId()) {
                        return edge2.kind.toString().compareTo(edge1.kind.toString());
                    } else if (edge2.dst.getId() > edge1.dst.getId()) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else if (edge2.src.getId() > edge1.src.getId()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return edges;
    }
}
