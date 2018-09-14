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
 * An edge of PDGs, ClDGs, and SDGs.
 * 
 * @author Katsuhisa Maruyama
 */
public class Dependence extends GraphEdge {
    
    protected Kind kind;
    
    public enum Kind {
        controlDependence,               // Control dependence in general
        trueControlDependence,           // Control dependence with respect to a true-branch flow
        falseControlDependence,          // Control dependence with respect to a false-branch flow
        fallThroughControlDependence,    // Control dependence with respect to a fall-through flow
        
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
        call,                            // Connection between a caller and its callee
    }
    
    protected Dependence() {
        super();
    }
    
    protected Dependence(PDGNode src, PDGNode dst) {
        super(src, dst);
    }
    
    public void setKind(Kind kind) {
        this.kind = kind;
    }
    
    public boolean isCD() {
        return kind == Kind.trueControlDependence ||
               kind == Kind.falseControlDependence ||
               kind == Kind.fallThroughControlDependence;
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
        if (elem instanceof Dependence) {
            return equals((Dependence)elem);
        }
        return false;
    }
    
    public boolean equals(Dependence dependence) {
        if (dependence == null) {
            return false;
        }
        return super.equals((GraphEdge)dependence) && kind == dependence.kind;
    }
    
    @Override
    public int hashCode() {
        return Long.valueOf(src.getId() + dst.getId()).hashCode();
    }
    
    @Override
    public Dependence clone() {
        Dependence cloneEdge = new Dependence(getSrcNode(), getDstNode());
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
    
    public static List<Dependence> sortDependenceEdges(Collection<? extends Dependence> co) {
        List<Dependence> edges = new ArrayList<Dependence>(co);
        Collections.sort(edges, new Comparator<Dependence>() {
            
            public int compare(Dependence edge1, Dependence edge2) {
                if (edge2.src.getId() == edge1.src.getId()) {
                    if (edge2.dst.getId() == edge1.dst.getId()) {
                        return edge2.kind.toString().compareTo(edge1.kind.toString());
                    } else if (edge1.dst.getId() > edge2.dst.getId()) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else if (edge1.src.getId() > edge2.src.getId()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return edges;
    }
}