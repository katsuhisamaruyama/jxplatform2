/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.graph.GraphEdge;
import org.jtool.eclipse.graph.GraphElement;
import org.jtool.eclipse.graph.GraphNode;
import java.util.Set;
import java.util.HashSet;

/**
 * A node of a PDG.
 * @author Katsuhisa Maruyama
 */
public class PDGNode extends GraphNode {
    
    protected CFGNode cfgnode;
    
    protected PDGNode() {
        super();
    }
    
    protected PDGNode(CFGNode node) {
        super(node.getId());
        cfgnode = node;
        cfgnode.setPDGNode(this);
    }
    
    public CFGNode getCFGNode() {
        return cfgnode;
    }
    
    public boolean isStatement() {
        return cfgnode.isStatement();
    }
    
    public boolean isParameter() {
        return cfgnode.isParameter();
    }
    
    public boolean isLoop() {
        return cfgnode.isLoop();
    }
    
    public boolean isBranch() {
        return cfgnode.isBranch();
    }
    
    public Set<DependenceEdge> getIncomingDependeceEdges() {
        Set<DependenceEdge> edges = new HashSet<DependenceEdge>();
        for (GraphEdge edge : getIncomingEdges()) {
            edges.add((DependenceEdge)edge);
        }
        return edges;
    }
    
    public Set<DependenceEdge> getOutgoingDependeceEdges() {
        Set<DependenceEdge> edges = new HashSet<DependenceEdge>();
        for (GraphEdge edge : getOutgoingEdges()) {
            edges.add((DependenceEdge)edge);
        }
        return edges;
    }
    
    public Set<CD> getIncomingCDEdges() {
        Set<CD> edges = new HashSet<CD>();
        for (GraphEdge edge : getIncomingEdges()) {
            DependenceEdge dependence = (DependenceEdge)edge;
            if (dependence.isCD()) {
                edges.add((CD)dependence);
            }
        }
        return edges;
    }
    
    public Set<CD> getOutgoingCDEdges() {
        Set<CD> edges = new HashSet<CD>();
        for (GraphEdge edge : getOutgoingEdges()) {
            DependenceEdge dependence = (DependenceEdge)edge;
            if (dependence.isCD()) {
                edges.add((CD)dependence);
            }
        }
        return edges;
    }
    
    public Set<DD> getIncomingDDEdges() {
        Set<DD> edges = new HashSet<DD>();
        for (GraphEdge edge : getIncomingEdges()) {
            DependenceEdge dependence = (DependenceEdge)edge;
            if (dependence.isDD()) {
                edges.add((DD)dependence);
            }
        }
        return edges;
    }
    
    public Set<DD> getOutgoingDDEdges() {
        Set<DD> edges = new HashSet<DD>();
        for (GraphEdge edge : getOutgoingEdges()) {
            DependenceEdge dependence = (DependenceEdge)edge;
            if (dependence.isDD()) {
                edges.add((DD)dependence);
            }
        }
        return edges;
    }
    
    public boolean isDominated() {
        return !getIncomingCDEdges().isEmpty();
    }
    
    public boolean isTrueDominated() {
        for (CD cd : getIncomingCDEdges()) {
            if (cd.isTrue()) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isFalseDominated() {
        for (CD cd : getIncomingCDEdges()) {
            if (cd.isFalse()) {
                return true;
            }
        }
        return false;
    }
    
    public int getNumOfIncomingTrueFalseCDs() {
        int num = 0;
        for (CD cd : getIncomingCDEdges()) {
            if (cd.isTrue() || cd.isFalse()) {
                num++;
            }
        }
        return num;
    }
    
    @Override
    public PDGNode clone() {
        PDGNode cloneNode = new PDGNode(getCFGNode());
        super.setClone(cloneNode);
        return cloneNode;
    }
    
    @Override
    public boolean equals(GraphElement obj) {
        if (obj == null || !(obj instanceof PDGNode)) {
            return false;
        }
        PDGNode node = (PDGNode)obj;
        return this == node || id == node.id;
    }
    
    @Override
    public int hashCode() {
        return Long.valueOf(id).hashCode();
    }
    
    public void print() {
        System.out.println(toString());
    }
    
    @Override
    public String toString() {
        return getCFGNode().toString();
    }
}
