/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.graph;

import java.util.Set;
import java.util.HashSet;

/**
 * A graph object which is either a CFG or PDG.
 * @author Katsuhsa Maruyama
 */
public class Graph<N extends GraphNode, E extends GraphEdge> {
    
    private Set<N> nodes = new HashSet<>();
    private Set<E> edges = new HashSet<>();
    
    public Graph() {
        super();
    }
    
    public void setNodes(Set<N> set) {
        nodes = set;
    }
    
    public Set<N> getNodes() {
        return nodes;
    }
    
    public void setEdges(Set<E> set) {
        edges = set;
    }
    
    public Set<E> getEdges() {
        return edges;
    }
    
    public void clear() {
        nodes.clear();
        edges.clear();
    }
    
    public void add(N node) {
        nodes.add(node);
    }
    
    public void add(E edge) {
        edges.add(edge);
    }
    
    public void remove(N node) {
        nodes.remove(node);
        new HashSet<E>(getEdges()).stream()
                                  .filter(edge -> edge.getSrcNode().equals(node) || edge.getDstNode().equals(node))
                                  .forEach(edge -> remove(edge));
    }
    
    public void remove(E edge) {
        edges.remove(edge);
        edge.getSrcNode().removeOutgoingEdge(edge);
        edge.getDstNode().removeIncomingEdge(edge);
    }
    
    public boolean contains(N node) {
        return nodes.contains(node);
    }
    
    public boolean contains(E edge) {
        return edges.contains(edge);
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("----- Graph (from here) -----\n");
        buf.append(getNodeInfo());
        buf.append(getEdgeInfo());
        buf.append("----- Graph (to here) -----\n");
        return buf.toString();
    }
    
    protected String getNodeInfo() {
        StringBuilder buf = new StringBuilder();
        GraphNode.sortGraphNode(getNodes()).forEach(node -> {
            buf.append(node.toString());
            buf.append("\n");
        });
        return buf.toString();
    }
    
    protected String getEdgeInfo() {
        StringBuilder buf = new StringBuilder();
        GraphEdge.sortGrapgEdge(getEdges()).forEach(edge -> {
            buf.append(edge.toString());
            buf.append("\n");
        });
        return buf.toString();
    }
}
