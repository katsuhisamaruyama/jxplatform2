/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

import org.jtool.eclipse.cfg.CommonCFG;
import org.jtool.eclipse.javamodel.JavaClass;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * An object storing information about a system dependence graph (SDG).
 * 
 * @author Katsuhisa Maruyama
 */
public class SDG extends CommonPDG {
    
    protected Map<String, ClDG> cldgs = new HashMap<String, ClDG>();
    protected Map<String, PDG> pdgs = new HashMap<String, PDG>();
    
    @Override
    public PDGEntry getEntryNode() {
        return null;
    }
    
    @Override
    public long getId() {
        return -1;
    }
    
    @Override
    public CommonCFG getCFG() {
        return null;
    }
    
    @Override
    public String getName() {
        return "SDG";
    }
    
    @Override
    public String getQualifiedName() {
        return "SDG";
    }
    
    public void add(ClDG cldg) {
        cldgs.put(cldg.getQualifiedName(), cldg);
        
        for (PDG pdg : cldg.getPDGs()) {
            pdgs.put(pdg.getQualifiedName(), pdg);
        }
    }
    
    public Set<ClDG> getClDGs() {
        return new HashSet<ClDG>(cldgs.values());
    }
    
    public ClDG getClDG(String fqn) {
        return cldgs.get(fqn);
    }
    
    public ClDG getClDG(JavaClass jclass) {
        return cldgs.get(jclass.getQualifiedName());
    }
    
    public Set<PDG> getPDGs() {
        return new HashSet<PDG>(pdgs.values());
    }
    
    public PDG getPDG(String fqn) {
        return pdgs.get(fqn);
    }
    
    @Override
    public boolean isSDG() {
        return true;
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
        buf.append("----- SDG (from here) -----\n");
        buf.append(getNodeInfo());
        buf.append(getEdgeInfo());
        
        for (ClDG cldg : cldgs.values()) {
            buf.append(cldg.toString());
        }
        buf.append("----- SDG (to here) -----\n");
        return buf.toString();
    }
} 
