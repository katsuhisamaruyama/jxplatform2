/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg;

import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGEntry;
import org.jtool.eclipse.graph.Graph;

/**
 * An object storing information about a program dependence graph (PDG).
 * 
 * @author Katsuhisa Maruyama
 */
public class PDG extends Graph<PDGNode, Dependence> {
    
    protected PDGEntry entry;
    
    public PDG() {
        super();
    }
    
    public void setEntryNode(PDGEntry node) {
        entry = node;
        entry.setPDG(this);
    }
    
    public PDGEntry getEntryNode() {
        return entry;
    }
    
    public long getId() {
        return entry.getId();
    }
    
    public CFG getCFG() {
        CFGEntry node = (CFGEntry)entry.getCFGNode();
        return node.getCFG();
    }
    
    public String getName() {
        return entry.getName();
    }
    
    public String getQualifiedName() {
        return entry.getQualifiedName();
    }
    
    public void append(PDG pdg) {
        for (PDGNode node : pdg.getNodes()) {
            add(node);
        }
        for (Dependence edge : pdg.getEdges()) {
            add(edge);
        }
    }
    
    @Override
    public void add(PDGNode node) {
        super.add(node);
    }
    
    @Override
    public void add(Dependence edge) {
        super.add(edge);
    }
    
    public PDGNode getNode(int id) {
        for (PDGNode node : getNodes()) {
            if (id == node.getId()) {
                return node;
            }
        }
        return null;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PDG) {
            return equals((PDG)obj);
        }
        return false;
    }
    
    public boolean equals(PDG pdg) {
        if (pdg == null) {
            return false;
        }
        return this == pdg || getQualifiedName().equals(pdg.getQualifiedName());
    }
    
    @Override
    public int hashCode() {
        return getQualifiedName().hashCode();
    }
    
    public void print() {
        System.out.println(toString());
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("----- PDG (from here) -----\n");
        buf.append("Name = " + getName());
        buf.append("\n");
        buf.append(getNodeInfo()); 
        buf.append(getEdgeInfo());
        buf.append("----- PDG (to here) -----\n");
        return buf.toString();
    }
    
    @Override
    protected String getNodeInfo() {
        return super.getNodeInfo();
    }
    
    @Override
    protected String getEdgeInfo() {
        StringBuffer buf = new StringBuffer();
        int index = 1;
        for (Dependence edge : Dependence.sortDependenceEdges(getEdges())) {
            buf.append(String.valueOf(index));
            buf.append(": ");
            buf.append(edge.toString());
            buf.append("\n");
            index++;
        }
        return buf.toString();
    }
    
    public String printCDG() {
        StringBuilder buf = new StringBuilder();
        buf.append("----- CDG (from here) -----\n");
        buf.append("Name = " + getName());
        buf.append("\n");
        buf.append(getNodeInfo()); 
        buf.append(getCDEdgeInfo());
        buf.append("----- CDG (to here) -----\n");
        return buf.toString();
    }
    
    public String printDDG() {
        StringBuilder buf = new StringBuilder();
        buf.append("----- DDG (from here) -----\n");
        buf.append("Name = " + getName());
        buf.append("\n");
        buf.append(getNodeInfo()); 
        buf.append(getDDEdgeInfo());
        buf.append("----- DDG (to here) -----\n");
        return buf.toString();
    }
    
    protected String getCDEdgeInfo() {
        StringBuffer buf = new StringBuffer();
        for (Dependence edge : Dependence.sortDependenceEdges(getEdges())) {
            if (edge.isCD()) {
                buf.append(edge.toString());
                buf.append("\n");
            }
        }
        return buf.toString();
    }
    
    protected String getDDEdgeInfo() {
        StringBuffer buf = new StringBuffer();
        for (Dependence edge : Dependence.sortDependenceEdges(getEdges())) {
            if (edge.isDD()) {
                buf.append(edge.toString());
                buf.append("\n");
            }
        }
        return buf.toString();
    }
}
