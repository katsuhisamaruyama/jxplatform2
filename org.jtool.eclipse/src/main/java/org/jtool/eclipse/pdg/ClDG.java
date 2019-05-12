/*
 *  Copyright 2018-2019
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
import java.util.HashSet;

/**
 * An object storing information about a class dependence graph (ClDG).
 * 
 * @author Katsuhisa Maruyama
 */
public class ClDG extends CommonPDG {
    
    protected Map<String, PDG> pdgs = new HashMap<String, PDG>();
    
    public ClDG() {
        super();
    }
    
    @Override
    public PDGClassEntry getEntryNode() {
        return (PDGClassEntry)entry;
    }
    
    public CCFG getCFG() {
        CFGEntry node = (CFGEntry)entry.getCFGNode();
        CommonCFG cfg = node.getCFG();
        if (cfg instanceof CCFG) {
            return (CCFG)cfg;
        }
        return null;
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
    public Set<PDGNode> getNodes() {
        Set<PDGNode> nodes = new HashSet<PDGNode>();
        for (PDG pdg : pdgs.values()) {
            nodes.addAll(pdg.getNodes());
        }
        return nodes;
    }
    
    @Override
    public Set<Dependence> getEdges() {
        Set<Dependence> edges = new HashSet<Dependence>();
        for (PDG pdg : pdgs.values()) {
            edges.addAll(pdg.getEdges());
        }
        return edges;
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("----- ClDG (from here) -----\n");
        buf.append("Name = " + getName());
        buf.append("\n");
        buf.append(getNodeInfo());
        buf.append(getEdgeInfo());
        buf.append("----- ClDG (to here) -----\n");
        return buf.toString();
    }
}
