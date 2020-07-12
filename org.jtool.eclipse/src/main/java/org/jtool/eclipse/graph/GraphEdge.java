/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.graph;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * An edge object for graph.
 * @author Katsuhsa Maruyama
 */
public abstract class GraphEdge extends GraphElement {
    
    protected GraphNode src;
    protected GraphNode dst;
    
    protected GraphEdge(GraphNode src, GraphNode dst) {
        this.src = src;
        this.dst = dst;
        src.addOutgoingEdge(this);
        dst.addIncomingEdge(this);
    }
    
    public void setSrcNode(GraphNode node) {
        src.removeOutgoingEdge(this);
        dst.removeIncomingEdge(this);
        src = node;
        src.addOutgoingEdge(this);
        dst.addIncomingEdge(this);
    }
    
    public void setDstNode(GraphNode node) {
        src.removeOutgoingEdge(this);
        dst.removeIncomingEdge(this);
        dst = node;
        src.addOutgoingEdge(this);
        dst.addIncomingEdge(this);
    }
    
    public GraphNode getSrcNode() {
        return src;
    }
    
    public GraphNode getDstNode() {
        return dst;
    }
    
    @Override
    public boolean equals(GraphElement elem) {
        return (elem instanceof GraphEdge) ? equals((GraphEdge)elem) : false;
    }
    
    public boolean equals(GraphEdge edge) {
        return edge != null && (this == edge || (src.equals(edge.src) && dst.equals(edge.dst)));
    }
    
    @Override
    public int hashCode() {
        return Long.valueOf(src.id + dst.id).hashCode();
    }
    
    protected void setClone(GraphEdge cloneEdge) {
        cloneEdge.setSrcNode(src);
        cloneEdge.setDstNode(dst);
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("Edge: " + src.getId() + " -> " + dst.getId());
        return buf.toString();
    }
    
    protected static List<GraphEdge> sortGrapgEdge(Collection<? extends GraphEdge> co) {
        List<GraphEdge> edges = new ArrayList<>(co);
        Collections.sort(edges, new Comparator<>() {
            
            @Override
            public int compare(GraphEdge edge1, GraphEdge edge2) {
                if (edge2.src.id == edge1.src.id) {
                    return edge2.dst.id == edge1.dst.id ? 0 : edge1.dst.id > edge2.dst.id ? 1 : -1;
                } else if (edge1.src.id > edge2.src.id) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return edges;
    }
}
