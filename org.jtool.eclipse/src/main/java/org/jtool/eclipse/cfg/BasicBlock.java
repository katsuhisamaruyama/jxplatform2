/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import java.util.Set;
import java.util.HashSet;

/**
 * An object storing information about a basic block of a CFG.
 * 
 * @author Katsuhisa Maruyama
 */
public class BasicBlock {
    
    private int id;
    private CFGNode leader;
    private Set<CFGNode> nodes = new HashSet<CFGNode>();
    
    private static int num = 0;
    
    protected BasicBlock() {
    }
    
    public BasicBlock(CFGNode node) {
        num++;
        id = num;
        leader = node;
    }
    
    public int getId() {
        return id;
    }
    
    public CFGNode getLeader() {
        return leader;
    }
    
    public void add(CFGNode node) {
        nodes.add(node);
        node.setBasicBlock(this);
    }
    
    public Set<CFGNode> getNodes() {
        return nodes;
    }
    
    public boolean contains(CFGNode node) {
        return nodes.contains(node);
    }
    
    public boolean isEmpty() {
        return nodes.isEmpty();
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("----- Basic Block (from here) -----\n");
        buf.append(printNodes());
        buf.append("----- Basic Block (to here) -----\n");
        return buf.toString();
    }
    
    private String printNodes() {
        StringBuilder buf = new StringBuilder();
        for (CFGNode node : getNodes()) {
            buf.append(node.toString());
            buf.append("\n");
        }
        return buf.toString();
    }
}
