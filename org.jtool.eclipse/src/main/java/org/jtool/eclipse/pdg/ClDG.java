/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * An object storing information about a class dependence graph (ClDG).
 * 
 * @author Katsuhisa Maruyama
 */
public class ClDG extends PDG {
    
    protected Map<String, PDG> pdgs = new HashMap<String, PDG>();
    
    public ClDG() {
        super();
    }
    
    public void setEntryNode(PDGClassEntry node) {
        entry = node;
    }
    
    @Override
    public PDGClassEntry getEntryNode() {
        return (PDGClassEntry)entry;
    }
    
    @Override
    public long getId() {
        return entry.getId();
    }
    
    @Override
    public String getName() {
        return entry.getName();
    }
    
    @Override
    public String getQualifiedName() {
        return entry.getQualifiedName();
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
        
        for (PDG pdg : pdgs.values()) {
            buf.append(pdg.getNodeInfo());
        }
        for (PDG pdg : pdgs.values()) {
            buf.append(pdg.getEdgeInfo());
        }
        buf.append("----- ClDG (to here) -----\n");
        return buf.toString();
    }
}
