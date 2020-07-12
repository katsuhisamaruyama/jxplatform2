/*
 *  Copyright 2019-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.slice;

import org.jtool.eclipse.pdg.PDGNode;
import java.util.List;
import java.util.ArrayList;

/**
 * An object storing information about a closure created by traversing only the control dependence.
 * 
 * @author Katsuhisa Maruyama
 */
public class CDClosure {
    
    public static List<PDGNode> getForwardCDClosure(PDGNode anchor) {
        List<PDGNode> nodes = new ArrayList<>();
        traverseForwardCD(anchor, nodes);
        return nodes;
    }
    
    private static void traverseForwardCD(PDGNode node, List<PDGNode> nodes) {
        if (nodes.contains(node)) {
            return;
        }
        nodes.add(node);
        
        node.getOutgoingCDEdges().forEach(edge -> {
            PDGNode next = edge.getSrcNode();
            traverseForwardCD(next, nodes);
        });
    }
    
    public static List<PDGNode> getBackwardCDClosure(PDGNode anchor) {
        List<PDGNode> nodes = new ArrayList<>();
        traverseBackwardCD(anchor, nodes);
        return nodes;
    }
    
    private static void traverseBackwardCD(PDGNode node, List<PDGNode> nodes) {
        if (nodes.contains(node)) {
            return;
        }
        nodes.add(node);
        
        node.getIncomingCDEdges().forEach(edge -> {
            PDGNode next = edge.getSrcNode();
            traverseBackwardCD(next, nodes);
        });
    }
}
