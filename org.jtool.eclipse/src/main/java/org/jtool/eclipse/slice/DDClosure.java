/*
 *  Copyright 2019-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.slice;

import org.jtool.eclipse.pdg.PDGNode;
import org.jtool.eclipse.pdg.DD;
import org.jtool.eclipse.cfg.JReference;
import java.util.List;
import java.util.ArrayList;

/**
 * An object storing information about a closure created by traversing only the data dependence.
 * 
 * @author Katsuhisa Maruyama
 */
public class DDClosure {
    
    public static List<PDGNode> getForwardCDClosure(PDGNode anchor, JReference jv) {
        List<PDGNode> nodes = new ArrayList<PDGNode>();
        for (DD edge : anchor.getOutgoingDDEdges()) {
            if (edge.getVariable().equals(jv)) {
                PDGNode next = edge.getSrcNode();
                traverseForwardDD(next, nodes);
            }
        }
        return nodes;
    }
    
    private static void traverseForwardDD(PDGNode node, List<PDGNode> nodes) {
        if (nodes.contains(node)) {
            return;
        }
        nodes.add(node);
        
        node.getOutgoingDDEdges().forEach(edge -> {
            PDGNode next = edge.getSrcNode();
            traverseForwardDD(next, nodes);
        });
    }
    
    public static List<PDGNode> getBackwardCDClosure(PDGNode anchor, JReference jv) {
        List<PDGNode> nodes = new ArrayList<PDGNode>();
        for (DD edge : anchor.getIncomingDDEdges()) {
            if (edge.getVariable().equals(jv)) {
                PDGNode next = edge.getSrcNode();
                traverseBackwardDD(next, nodes);
            }
        }
        return nodes;
    }
    
    private static void traverseBackwardDD(PDGNode node, List<PDGNode> nodes) {
        if (nodes.contains(node)) {
            return;
        }
        nodes.add(node);
        
        node.getIncomingDDEdges().forEach(edge -> {
            PDGNode next = edge.getSrcNode();
            traverseBackwardDD(next, nodes);
        });
    }
}
