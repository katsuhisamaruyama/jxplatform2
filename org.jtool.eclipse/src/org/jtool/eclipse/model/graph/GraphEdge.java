/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.graph;

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
    
    protected GraphEdge() {
    }
    
    protected GraphEdge(GraphNode src, GraphNode dst) {
        this();
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
    
    public boolean equals(GraphElement obj) {
        if (obj == null || !(obj instanceof GraphEdge)) {
            return false;
        }
        GraphEdge edge = (GraphEdge)obj;
        return this == edge || (src.equals(edge.src) && dst.equals(edge.dst));
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
    
    protected List<GraphEdge> sortGraphEdges(Collection<? extends GraphEdge> co) {
        List<GraphEdge> edges = new ArrayList<GraphEdge>(co);
        Collections.sort(edges, new Comparator<GraphEdge>() {
            public int compare(GraphEdge edge1, GraphEdge edge2) {
                if (edge2.src.id == edge1.src.id) {
                    if (edge2.dst.id == edge1.dst.id) {
                        return 0;
                    } else if (edge2.dst.id > edge1.dst.id) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else if (edge2.src.id > edge1.src.id) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return edges;
    }
}
