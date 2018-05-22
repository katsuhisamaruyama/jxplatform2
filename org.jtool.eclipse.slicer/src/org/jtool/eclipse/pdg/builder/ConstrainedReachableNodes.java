/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg.builder;

import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.graph.GraphElement;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Calculates constrained reachable nodes on a CFG.
 * @author Katsuhisa Maruyama
 */
public class ConstrainedReachableNodes implements Iterable<CFGNode> {
    
    private Set<CFGNode> reachableNodes;
    
    public ConstrainedReachableNodes(CFG cfg, CFGNode from, CFGNode to) {
        Set<CFGNode> W;
        W = cfg.getBackwardReachableNodes(to, from);
        Set<CFGNode> forwardCRP = new HashSet<CFGNode>(W);
        W = cfg.getForwardReachableNodes(from, cfg.getEndNode());
        GraphElement.intersection(forwardCRP, W);
        
        W = cfg.getForwardReachableNodes(from, to);
        Set<CFGNode> backwardCRP = new HashSet<CFGNode>(W);
        W = cfg.getBackwardReachableNodes(to, cfg.getStartNode());
        GraphElement.intersection(backwardCRP, W);
        
        reachableNodes = new HashSet<CFGNode>(GraphElement.union(forwardCRP, backwardCRP));
    }
    
    public boolean contains(CFGNode node) {
        return reachableNodes.contains(node);
    }
    
    public boolean isEmpty() {
        return reachableNodes.isEmpty();
    }
    
    @Override
    public Iterator<CFGNode> iterator() {
        return reachableNodes.iterator();
    }
}
