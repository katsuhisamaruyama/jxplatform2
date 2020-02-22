/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * An object storing information about a class control flow graph (CCFG).
 * 
 * @author Katsuhisa Maruyama
 */
public class CCFG extends CommonCFG {
    
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
        return cfgs.values().stream()
                .map(cfg -> cfg.getNodes()).flatMap(l -> l.stream()).collect(Collectors.toCollection(HashSet::new));
    }
    
    @Override
    public Set<ControlFlow> getEdges() {
        return cfgs.values().stream()
                .map(cfg -> cfg.getEdges()).flatMap(l -> l.stream()).collect(Collectors.toCollection(HashSet::new));
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("----- CCFG (from here) -----\n");
        buf.append("Class Name = " + getQualifiedName());
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
