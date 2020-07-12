/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import java.util.List;
import java.util.ArrayList;

/**
 * An object storing information about a control flow graph (CFG).
 * 
 * @author Katsuhisa Maruyama
 */
public class CFG extends CommonCFG {
    
    private List<BasicBlock> basicBlocks = new ArrayList<>();
    
    public void append(CFG cfg) {
        cfg.getNodes().forEach(node -> add(node));
        cfg.getEdges().forEach(edge -> add(edge));
    }
    
    public void add(BasicBlock block) {
        basicBlocks.add(block);
    }
    
    public List<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("----- CFG of " + getQualifiedName() + "-----\n");
        buf.append(getNodeInfo()); 
        buf.append(getEdgeInfo());
        buf.append("-----------------------------------\n");
        return buf.toString();
    }
}
