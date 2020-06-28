/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.BasicBlock;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGNode;

/**
 * Calculates and stores basic blocks of a CFG.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class BasicBlockBuilder {
    
    public static void create(CFG cfg) {
        CFGNode start = cfg.getEntryNode();
        CFGNode first = start.getSuccessors().iterator().next();
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
        while (node != null && !node.isLeader() && !node.equals(cfg.getExitNode())) {
            block.add(node);   
            node = getTrueSucc(node);
        }
    }
    
    private static CFGNode getTrueSucc(CFGNode node) {
        return node.getOutgoingFlows().stream().filter(edge -> edge.isTrue()).map(edge -> edge.getDstNode()).findFirst().orElse(null);
    }
}
