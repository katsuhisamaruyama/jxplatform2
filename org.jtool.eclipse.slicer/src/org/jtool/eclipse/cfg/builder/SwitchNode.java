/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGStatement;
import org.jtool.eclipse.cfg.ControlFlow;
import org.jtool.eclipse.graph.GraphEdge;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * A node for a <code>switch</code> statement of a CFG.
 * @author Katsuhisa Maruyama
 */
class SwitchNode extends CFGStatement {
    
    private CFGNode defaultStartNode = null;
    private CFGNode defaultEndNode = null;
    
    SwitchNode() {
    }
    
    SwitchNode(ASTNode node, CFGNode.Kind kind) {
        super(node, kind);
    }
    
    void setDefaultStartNode(CFGNode node) {
        defaultStartNode = node;
    }
    
    CFGNode getDefaultStartNode() {
        return defaultStartNode;
    }
    
    void setDefaultEndNode(CFGNode node) {
        defaultEndNode = node;
    }
    
    CFGNode getDefaultEndNode() {
        return defaultEndNode;
    }
    
    CFGNode getPredecessorOfDefault() {
        for (GraphEdge edge : defaultStartNode.getIncomingEdges()) {
            ControlFlow flow = (ControlFlow)edge;
            if (flow.isFalse()) {
                return (CFGNode)flow.getSrcNode();
            }
        }
        return null;
    }
    
    CFGNode getSuccessorOfDefault() {
        for (GraphEdge edge : defaultStartNode.getOutgoingEdges()) {
            ControlFlow flow = (ControlFlow)edge;
            if (flow.isFalse()) {
                return (CFGNode)flow.getDstNode();
            }
        }
        return null;
    }
    
    boolean hasDefault() {
        return defaultStartNode != null;
    }
}
