/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.slice;

import java.util.Set;

import org.jtool.eclipse.cfg.JVariable;
import org.jtool.eclipse.pdg.DD;
import org.jtool.eclipse.pdg.DependenceEdge;
import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.PDGStatement;

import java.util.HashSet;

/**
 * An object storing information about program slice.
 * @author Katsuhisa Maruyama
 */
public class Slice extends PDG {
    
    private PDGStatement criterionNode;
    private JVariable criterionVariable;
    
    public Slice(PDGStatement node, JVariable jvar) {
        criterionNode = node;
        criterionVariable = jvar;
        extract();
    }
    
    public PDGStatement getCriterionNode() {
        return criterionNode;
    }
    
    public JVariable getCriterionVariable() {
        return criterionVariable;
    }
    
    private void extract() {
        if (criterionNode.definesVariable(criterionVariable)) {
            traverseBackward(criterionNode);
        } else if (criterionNode.usesVariable(criterionVariable)) {
            add(criterionNode);
            for (PDGStatement defnode : findDefNode(criterionNode, criterionVariable)) {
                traverseBackward(defnode);
            }
        }
    }
    
    private Set<PDGStatement> findDefNode(PDGStatement anchor, JVariable jvar) {
        Set<PDGStatement> defs = new HashSet<PDGStatement>();
        
        for (DD edge : anchor.getIncomingDDEdges()) {
            if (jvar.equals(edge.getVariable())) {
                PDGStatement node = (PDGStatement)edge.getSrcNode();
                defs.add(node);
            }
        }
        return defs;
    }
    
    private void traverseBackward(PDGStatement anchor) {
        add(anchor);
        for (DependenceEdge edge : anchor.getIncomingDependeceEdges()) {
            add(edge);
            PDGStatement node = (PDGStatement)edge.getSrcNode();
            if (!getNodes().contains(node)) {
                traverseBackward(node);
            }
        }
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("----- Slice (from here) -----\n");
        buf.append("Node = " + criterionNode.getId() + "; Variable = " + criterionVariable.getName());
        buf.append("\n");
        buf.append(getNodeInfo()); 
        buf.append(getEdgeInfo());
        buf.append("----- Slice (to here) -----\n");
        return buf.toString();
    }
}
