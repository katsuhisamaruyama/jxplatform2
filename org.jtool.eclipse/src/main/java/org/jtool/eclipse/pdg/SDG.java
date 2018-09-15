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
 * An object storing information about a system dependence graph (SDG).
 * 
 * @author Katsuhisa Maruyama
 */
public class SDG extends PDG {
    
    private Set<PDGEntry> entries = new HashSet<PDGEntry>();
    
    protected Map<String, ClDG> cldgs = new HashMap<String, ClDG>();
    protected Map<String, PDG> pdgs = new HashMap<String, PDG>();
    
    public SDG() {
        super();
    }
    
    public void addEntryNode(PDGClassEntry node) {
        entries.add(node);
    }
    
    public Set<PDGEntry> getEntries() {
        return entries;
    }
    
    public void add(ClDG cldg) {
        if (!cldgs.values().contains(cldg)) {
            cldgs.put(cldg.getQualifiedName(), cldg);
            
            for (PDG pdg : cldg.getPDGs()) {
                pdgs.put(pdg.getQualifiedName(), pdg);
            }
        }
    }
    
    public Set<ClDG> getClDGs() {
        return new HashSet<ClDG>(cldgs.values());
    }
    
    public ClDG getClDG(String fqn) {
        return cldgs.get(fqn);
    }
    
    public Set<PDG> getPDGs() {
        return new HashSet<PDG>(pdgs.values());
    }
    
    public PDG getPDG(String fqn) {
        return pdgs.get(fqn);
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("----- SDG (from here) -----\n");
        for (PDG pdg : pdgs.values()) {
            buf.append(pdg.getNodeInfo());
        }
        for (PDG pdg : pdgs.values()) {
            buf.append(pdg.getEdgeInfo());
        }
        buf.append("----- SDG (to here) -----\n");
        return buf.toString();
    }
} 
