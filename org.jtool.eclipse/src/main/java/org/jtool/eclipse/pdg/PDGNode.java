/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.graph.GraphNode;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A node of PDGs, ClDGs, and SDG.
 * 
 * @author Katsuhisa Maruyama
 */
public class PDGNode extends GraphNode {
    
    protected CFGNode cfgnode;
    
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
        return getIncomingEdges().stream()
                                 .map(edge -> (Dependence)edge)
                                 .collect(Collectors.toSet());
    }
    
    public Set<Dependence> getOutgoingDependeceEdges() {
        return getOutgoingEdges().stream()
                                 .map(edge -> (Dependence)edge)
                                 .collect(Collectors.toSet());
    }
    
    public Set<CD> getIncomingCDEdges() {
        return getIncomingEdges().stream()
                                 .map(edge -> (Dependence)edge)
                                 .filter(edge -> edge.isCD())
                                 .map(edge -> (CD)edge)
                                 .collect(Collectors.toSet());
    }
    
    public Set<CD> getOutgoingCDEdges() {
        return getOutgoingEdges().stream()
                                 .map(edge -> (Dependence)edge)
                                 .filter(edge -> edge.isCD())
                                 .map(edge -> (CD)edge)
                                 .collect(Collectors.toSet());
    }
    
    public Set<DD> getIncomingDDEdges() {
        return getIncomingEdges().stream()
                                 .map(edge -> (Dependence)edge)
                                 .filter(edge -> edge.isDD())
                                 .map(edge -> (DD)edge)
                                 .collect(Collectors.toSet());
    }
    
    public Set<DD> getOutgoingDDEdges() {
        return getOutgoingEdges().stream()
                                 .map(edge -> (Dependence)edge)
                                 .filter(edge -> edge.isDD())
                                 .map(edge -> (DD)edge)
                                 .collect(Collectors.toSet());
    }
    
    public boolean isDominated() {
        return !getIncomingCDEdges().isEmpty();
    }
    
    public boolean isTrueDominated() {
        return getIncomingCDEdges().stream()
                                   .anyMatch(cd -> cd.isTrue());
    }
    
    public boolean isFalseDominated() {
        return getIncomingCDEdges().stream()
                                   .anyMatch(cd -> cd.isFalse());
    }
    
    public int getNumOfIncomingTrueFalseCDs() {
        return (int)getIncomingCDEdges().stream()
                                        .filter(cd -> cd.isTrue() || cd.isFalse()).count();
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
            
            @Override
            public int compare(PDGNode node1, PDGNode node2) {
                return (node2.id == node1.id) ? 0 : (node1.id > node2.id) ? 1 : -1;
            }
        });
        return nodes;
    }
    
    @Override
    public String toString() {
        return getCFGNode().toString();
    }
}
