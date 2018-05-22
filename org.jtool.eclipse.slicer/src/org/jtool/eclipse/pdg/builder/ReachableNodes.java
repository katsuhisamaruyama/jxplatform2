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
 * Calculates reachable nodes on a CFG.
 * @author Katsuhisa Maruyama
 */
public class ReachableNodes implements Iterable<CFGNode> {
    
    private Set<CFGNode> reachableNodes;
    
    private Set<CFGNode> ftrack;
    private Set<CFGNode> btrack;
    
    public ReachableNodes(CFG cfg, CFGNode from, CFGNode to) {
        ftrack = new HashSet<CFGNode>(cfg.getForwardReachableNodes(from, to));
        btrack = new HashSet<CFGNode>(cfg.getBackwardReachableNodes(to, from));
        reachableNodes = new HashSet<CFGNode>(GraphElement.intersection(ftrack, btrack));
    }
    
    public Set<CFGNode> getForwardReachableNodes() {
        return ftrack;
    }
    
    public Set<CFGNode> getBackwardReachableNodes() {
        return btrack;
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
