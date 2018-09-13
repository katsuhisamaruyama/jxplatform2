/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.graph.GraphEdge;
import org.jtool.eclipse.graph.GraphNode;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/**
 * A node of PDGs, ClDGs, and SDG.
 * 
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
    
    public Set<Dependence> getIncomingDependeceEdges() {
        Set<Dependence> edges = new HashSet<Dependence>();
        for (GraphEdge edge : getIncomingEdges()) {
            edges.add((Dependence)edge);
        }
        return edges;
    }
    
    public Set<Dependence> getOutgoingDependeceEdges() {
        Set<Dependence> edges = new HashSet<Dependence>();
        for (GraphEdge edge : getOutgoingEdges()) {
            edges.add((Dependence)edge);
        }
        return edges;
    }
    
    public Set<CD> getIncomingCDEdges() {
        Set<CD> edges = new HashSet<CD>();
        for (GraphEdge edge : getIncomingEdges()) {
            Dependence dependence = (Dependence)edge;
            if (dependence.isCD()) {
                edges.add((CD)dependence);
            }
        }
        return edges;
    }
    
    public Set<CD> getOutgoingCDEdges() {
        Set<CD> edges = new HashSet<CD>();
        for (GraphEdge edge : getOutgoingEdges()) {
            Dependence dependence = (Dependence)edge;
            if (dependence.isCD()) {
                edges.add((CD)dependence);
            }
        }
        return edges;
    }
    
    public Set<DD> getIncomingDDEdges() {
        Set<DD> edges = new HashSet<DD>();
        for (GraphEdge edge : getIncomingEdges()) {
            Dependence dependence = (Dependence)edge;
            if (dependence.isDD()) {
                edges.add((DD)dependence);
            }
        }
        return edges;
    }
    
    public Set<DD> getOutgoingDDEdges() {
        Set<DD> edges = new HashSet<DD>();
        for (GraphEdge edge : getOutgoingEdges()) {
            Dependence dependence = (Dependence)edge;
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
    
    public boolean equals(PDGNode node) {
        return super.equals((GraphNode)node);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public PDGNode clone() {
        PDGNode cloneNode = new PDGNode(getCFGNode());
        super.setClone(cloneNode);
        return cloneNode;
    }
    
    public void print() {
        System.out.println(toString());
    }
    
    public static List<PDGNode> sortPDGNodes(Collection<? extends PDGNode> co) {
        List<PDGNode> nodes = new ArrayList<PDGNode>(co);
        Collections.sort(nodes, new Comparator<PDGNode>() {
            public int compare(PDGNode node1, PDGNode node2) {
                if (node2.id == node1.id) {
                    return 0;
                } else if (node1.id > node2.id) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return nodes;
    }
    
    @Override
    public String toString() {
        return getCFGNode().toString();
    }
}
