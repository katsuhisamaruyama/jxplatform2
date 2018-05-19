/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.cfg.builder;

import org.jtool.eclipse.model.cfg.BasicBlock;
import org.jtool.eclipse.model.cfg.CFG;
import org.jtool.eclipse.model.cfg.CFGNode;
import org.jtool.eclipse.model.cfg.ControlFlow;

/**
 * Calculates and stores basic blocks of a CFG.
 * @author Katsuhisa Maruyama
 */
public class BasicBlockBuilder {
    
    public static void create(CFG cfg) {
        CFGNode start = cfg.getStartNode();
        CFGNode[] nodes = CFGNode.toArray(start.getSuccessors());
        CFGNode first = (CFGNode)nodes[0];
        for (CFGNode node : cfg.getNodes()) {
            if (node.equals(first) || node.isJoin() || (node.isNextToBranch() && !node.equals(start))) {
                BasicBlock block = new BasicBlock(node);
                cfg.add(block);
                block.add(node);
            }
        }
        
        for (BasicBlock block : cfg.getBasicBlocks()) {
            collectNodesInBlock(block, cfg);
        }
    }
    
    private static void collectNodesInBlock(BasicBlock block, CFG cfg) {
        CFGNode node = getTrueSucc(block.getLeader());
        while (node != null && !node.isLeader() && !node.equals(cfg.getEndNode())) {
            block.add(node);   
            node = getTrueSucc(node);
        }
    }
    
    private static CFGNode getTrueSucc(CFGNode node) {
        for (ControlFlow edge : node.getOutgoingFlows()) {
            if (edge.isTrue()) {
                return edge.getDstNode();
            }
        }
        return null;
    }
}
