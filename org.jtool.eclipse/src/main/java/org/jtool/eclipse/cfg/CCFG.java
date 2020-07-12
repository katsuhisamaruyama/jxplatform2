/*
 *  Copyright 2018-2020
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
    
    protected Map<String, CFG> cfgs = new HashMap<>();
    
    @Override
    public CFGClassEntry getEntryNode() {
        return (CFGClassEntry)entry;
    }
    
    @Override
    public String getName() {
        return entry.getName();
    }
    
    @Override
    public String getQualifiedName() {
        return entry.getQualifiedName();
    }
    
    public void add(CFG cfg) {
        if (!cfgs.values().contains(cfg)) {
            cfgs.put(cfg.getQualifiedName(), cfg);
        }
    }
    
    public Set<CFG> getCFGs() {
        return new HashSet<>(cfgs.values());
    }
    
    public CFG getCFG(String fqn) {
        return cfgs.get(fqn);
    }
    
    @Override
    public Set<CFGNode> getNodes() {
        return cfgs.values()
                .stream()
                .flatMap(cfg -> cfg.getNodes().stream()).collect(Collectors.toSet());
    }
    
    @Override
    public Set<ControlFlow> getEdges() {
        return cfgs.values()
                .stream()
                .flatMap(cfg -> cfg.getEdges().stream()).collect(Collectors.toSet());
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("----- CCFG (from here) -----\n");
        buf.append("Class Name = " + getQualifiedName());
        buf.append("\n");
        cfgs.values().forEach(cfg -> buf.append(cfg.getNodeInfo()));
        cfgs.values().forEach(cfg -> buf.append(cfg.getEdgeInfo()));
        buf.append("----- CCFG (to here) -----\n");
        return buf.toString();
    }
}
