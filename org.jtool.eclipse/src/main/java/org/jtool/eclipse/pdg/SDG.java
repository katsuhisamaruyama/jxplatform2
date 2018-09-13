/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

import org.jtool.eclipse.graph.Graph;
import java.util.Set;
import java.util.HashSet;

/**
 * An object storing information about a system dependence graph (SDG).
 * 
 * @author Katsuhisa Maruyama
 */
public class SDG extends Graph<PDGNode, Dependence> {
    
    private Set<PDGEntry> entries = new HashSet<PDGEntry>();
    
    private Set<PDG> pdgs = new HashSet<PDG>();
    
    public SDG() {
        super();
    }
    
    public void addEntryNode(PDGClassEntry node) {
        entries.add(node);
    }
    
    public Set<PDGEntry> getEntries() {
        return entries;
    }
    
    public void add(PDG pdg) {
        if (!pdgs.contains(pdg)) {
            pdgs.add(pdg);
            entries.add(pdg.getEntryNode());
            for (PDGNode node : pdg.getNodes()) {
                add(node);
            }
            for (Dependence edge : pdg.getEdges()) {
                add(edge);
            }
        }
    }
    
    public Set<PDG> getPDGs() {
        return pdgs;
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("----- SDG (from here) -----\n");
        buf.append(getNodeInfo()); 
        buf.append(getEdgeInfo());
        buf.append("----- SDG (to here) -----\n");
        return buf.toString();
    }
} 
