/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

import java.util.Set;
import java.util.HashSet;

/**
 * An object storing information about a class dependence graph (ClDG).
 * @author Katsuhisa Maruyama
 */
public class ClDG extends PDG {
    
    protected PDGClassEntry entry;
    private Set<PDG> pdgs = new HashSet<PDG>();
    
    public ClDG() {
        super();
    }
    
    public void setEntryNode(PDGClassEntry node) {
        entry = node;
    }
    
    @Override
    public PDGClassEntry getEntryNode() {
        return entry;
    }
    
    @Override
    public long getId() {
        return entry.getId();
    }
    
    @Override
    public String getName() {
        return entry.getName();
    }
    
    public void add(PDG pdg) {
        if (!pdgs.contains(pdg)) {
            pdgs.add(pdg);
            
            for (PDGNode node : pdg.getNodes()) {
                add(node);
            }
            for (DependenceEdge edge : pdg.getEdges()) {
                add(edge);
            }
            
            ClassMemberEdge edge = new ClassMemberEdge(entry, pdg.getEntryNode());
            edge.setKind(DependenceEdge.Kind.classMember);
            add(edge);
        }
    }
    
    public Set<PDG> getPDGs() {
        return pdgs;
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
