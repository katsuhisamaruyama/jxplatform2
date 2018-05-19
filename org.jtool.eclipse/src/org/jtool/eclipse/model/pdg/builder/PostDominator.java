/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.pdg.builder;

import org.jtool.eclipse.model.cfg.CFG;
import org.jtool.eclipse.model.cfg.CFGNode;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Calculates post-dominator nodes on a CFG.
 * @author Katsuhisa Maruyama
 */
public class PostDominator implements Iterable<CFGNode> {
    
    private Set<CFGNode> postDominator = new HashSet<CFGNode>();
    
    private Set<CFGNode> track = new HashSet<CFGNode>();
    
    public PostDominator(CFG cfg, CFGNode anchor) {
        for (CFGNode node : cfg.getNodes()) {
            if (!anchor.equals(node)) {
                track.clear();
                track = cfg.getForwardReachableNodes(anchor, node);
                if (track.contains(node) && !track.contains(cfg.getEndNode())) {
                    add(node);
                }
            }
        }
    }
    
    protected boolean add(CFGNode node) {
        return postDominator.add(node);
    }
    
    public boolean remove(CFGNode node) {
        return postDominator.remove(node);
    }
    
    public boolean contains(CFGNode node) {
        return postDominator.contains(node);
    }
    
    public boolean isEmpty() {
        return postDominator.isEmpty();
    }
    
    public Iterator<CFGNode> iterator() {
        return postDominator.iterator();
    }
    
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (CFGNode node : postDominator) {
            buf.append(node.getId());
            buf.append(", ");
        }
        if (buf.length() != 0) {
            return buf.substring(0, buf.length() - 2);
        } else {
            return "";
        }
    }
}
