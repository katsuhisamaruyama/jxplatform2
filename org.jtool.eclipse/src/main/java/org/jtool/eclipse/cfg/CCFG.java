/*
 *  Copyright 2018-2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * An object storing information about a class control flow graph (CCFG).
 * 
 * @author Katsuhisa Maruyama
 */
public class CCFG extends CFG {
    
    protected Map<String, CFG> cfgs = new HashMap<String, CFG>();
    
    @Override
    public CFGClassEntry getStartNode() {
        return (CFGClassEntry)start;
    }
    
    @Override
    public String getName() {
        return start.getName();
    }
    
    @Override
    public String getQualifiedName() {
        return start.getQualifiedName();
    }
    
    public void add(CFG cfg) {
        if (!cfgs.values().contains(cfg)) {
            cfgs.put(cfg.getQualifiedName(), cfg);
        }
    }
    
    public Set<CFG> getCFGs() {
        return new HashSet<CFG>(cfgs.values());
    }
    
    public CFG getCFG(String fqn) {
        return cfgs.get(fqn);
    }
    
    @Override
    public Set<CFGNode> getNodes() {
        Set<CFGNode> nodes = new HashSet<CFGNode>();
        for (CFG cfg : cfgs.values()) {
            nodes.addAll(cfg.getNodes());
        }
        return nodes;
    }
    
    @Override
    public Set<ControlFlow> getEdges() {
        Set<ControlFlow> edges = new HashSet<ControlFlow>();
        for (CFG cfg : cfgs.values()) {
            edges.addAll(cfg.getEdges());
        }
        return edges;
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("----- CCFG (from here) -----\n");
        buf.append("Name = " + getName());
        buf.append("\n");
        for (CFG cfg : cfgs.values()) {
            buf.append(cfg.getNodeInfo());
        }
        for (CFG cfg : cfgs.values()) {
            buf.append(cfg.getEdgeInfo());
        }
        buf.append("----- CCFG (to here) -----\n");
        return buf.toString();
    }
}
