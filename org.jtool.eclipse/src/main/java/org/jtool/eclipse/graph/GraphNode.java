/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.graph;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * A node object for graph.
 * @author Katsuhsa Maruyama
 */
public abstract class GraphNode extends GraphElement {
    
    protected long id = 0;
    
    private Set<GraphEdge> incomingEdges = new HashSet<>();
    private Set<GraphEdge> outgoingEdges = new HashSet<>();
    private Set<GraphNode> srcNodes = new HashSet<>();
    private Set<GraphNode> dstNodes = new HashSet<>();
    
    protected GraphNode(long id) {
        this.id = id;
    }
    
    public void clear() {
        incomingEdges.clear();
        outgoingEdges.clear();
        srcNodes.clear();
        dstNodes.clear();
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public long getId() {
        return id;
    }
    
    public void addIncomingEdge(GraphEdge edge) {
        if (incomingEdges.add(edge)) {
            srcNodes.add(edge.getSrcNode());
        }
    }
    
    public void addOutgoingEdge(GraphEdge edge) {
        if (outgoingEdges.add(edge)) {
            dstNodes.add(edge.getDstNode());
        }
    }
    
    public void addIncomingEdges(Set<GraphEdge> edges) {
        edges.forEach(edge -> addIncomingEdge(edge));
    }
    
    public void addOutgoingEdges(Set<GraphEdge> edges) {
        edges.forEach(edge -> addOutgoingEdge(edge));
    }
    
    public void removeIncomingEdge(GraphEdge edge) {
        incomingEdges.remove(edge);
        srcNodes.remove(edge.getSrcNode());
    }
    
    public void removeOutgoingEdge(GraphEdge edge) {
        outgoingEdges.remove(edge);
        dstNodes.remove(edge.getDstNode());
    }
    
    public void clearIncomingEdges() {
        incomingEdges.clear();
    }
    
    public void clearOutgoingEdges() {
        outgoingEdges.clear();
    }
    
    public void setIncomingEdges(Set<GraphEdge> edges) {
        incomingEdges = edges;
    }
    
    public void setOutgoingEdges(Set<GraphEdge> edges) {
        outgoingEdges = edges;
    }
    
    public Set<GraphEdge> getIncomingEdges() {
        return incomingEdges;
    }
    
    public Set<GraphEdge> getOutgoingEdges() {
        return outgoingEdges;
    }
    
    public Set<GraphNode> getSrcNodes() {
        return srcNodes;
    }
    
    public Set<GraphNode> getDstNodes() {
        return dstNodes;
    }
    
    @Override
    public boolean equals(GraphElement elem) {
        return (elem instanceof GraphNode) ? equals((GraphNode)elem) : false;
    }
    
    public boolean equals(GraphNode node) {
        return node != null && (this == node || id == node.id);
    }
    
    @Override
    public int hashCode() {
        return Long.valueOf(id).hashCode();
    }
    
    protected void setClone(GraphNode cloneNode) {
        cloneNode.addIncomingEdges(incomingEdges);
        cloneNode.addOutgoingEdges(outgoingEdges);
    }
    
    public String toString() {
        StringBuilder buf = new StringBuilder(); 
        buf.append("Node: " + getIdString() + "\n");
        Set<GraphEdge> outgoing = getOutgoingEdges();
        buf.append("  Outgoing :");
        outgoing.forEach(edge -> buf.append("  " + edge.getDstNode().getId()));
        buf.append("\n");
        Set<GraphEdge> incoming = getIncomingEdges();
        buf.append("  Incoming :");
        incoming.forEach(edge -> buf.append("  " + edge.getSrcNode().getId()));
        return buf.toString();
    }
    
    public String getIdString() {
        StringBuilder buf = new StringBuilder();
        long id = getId();
        if (id < 10) {
            buf.append("   ");
        } else if (id < 100) {
            buf.append("  ");
        } else if (id < 1000) {
            buf.append(" ");
        }
        buf.append(String.valueOf(id));
        return buf.toString();
    }
    
    public static List<GraphNode> sortGraphNode(Collection<? extends GraphNode> co) {
        List<GraphNode> nodes = new ArrayList<>(co);
        Collections.sort(nodes, new Comparator<>() {
            
            @Override
            public int compare(GraphNode node1, GraphNode node2) {
                return node2.id == node1.id ? 0 : node1.id > node2.id ? 1 : -1;
            }
        });
        return nodes;
    }
}
