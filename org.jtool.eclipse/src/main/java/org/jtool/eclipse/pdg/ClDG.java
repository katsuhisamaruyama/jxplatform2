/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

import org.jtool.eclipse.cfg.CCFG;
import org.jtool.eclipse.cfg.CFGEntry;
import org.jtool.eclipse.cfg.CommonCFG;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;

/**
 * An object storing information about a class dependence graph (ClDG).
 * 
 * @author Katsuhisa Maruyama
 */
public class ClDG extends CommonPDG {
    
    protected Map<String, PDG> pdgs = new HashMap<String, PDG>();
    
    @Override
    public PDGClassEntry getEntryNode() {
        return (PDGClassEntry)entry;
    }
    
    @Override
    public CCFG getCFG() {
        CFGEntry node = (CFGEntry)entry.getCFGNode();
        CommonCFG cfg = node.getCFG();
        return cfg instanceof CCFG ? (CCFG)cfg : null;
    }
    
    public void add(PDG pdg) {
        if (!pdgs.values().contains(pdg)) {
            pdgs.put(pdg.getQualifiedName(), pdg);
        }
    }
    
    public Set<PDG> getPDGs() {
        return new HashSet<PDG>(pdgs.values());
    }
    
    public PDG getPDG(String fqn) {
        return pdgs.get(fqn);
    }
    
    @Override
    public boolean isClDG() {
        return true;
    }
    
    @Override
    public Set<PDGNode> getNodes() {
        return pdgs.values().stream()
                            .flatMap(pdg -> pdg.getNodes().stream()).collect(Collectors.toSet());
    }
    
    @Override
    public Set<Dependence> getEdges() {
        return pdgs.values().stream()
                            .flatMap(pdg -> pdg.getEdges().stream()).collect(Collectors.toSet());
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("----- ClDG (from here) -----\n");
        buf.append("Class Name = " + getQualifiedName());
        buf.append("\n");
        buf.append(getNodeInfo());
        buf.append(getEdgeInfo());
        buf.append("----- ClDG (to here) -----\n");
        return buf.toString();
    }
}
