/*
 *     GraphEdge.java  Sep 10, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.util;

public class GraphEdge implements GraphComponent, GraphEdgeSort {
    protected GraphNode src;
    protected GraphNode dst;
    protected int sort;

    protected GraphEdge() {
    }

    public GraphEdge(GraphNode src, GraphNode dst) {
        this.src = src;
        this.dst = dst;
        src.addOutgoingEdge(this);
        dst.addIncomingEdge(this);
    }

    public boolean equals(GraphComponent c) {
        GraphEdge edge = (GraphEdge)c;
        if (this == edge) {
            return true;
        }
        if (src.equals(edge.getSrcNode()) && dst.equals(edge.getDstNode())) {
            return true;
        }
        return false;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public void setSrcNode(GraphNode node) {
        src.removeOutgoingEdge(this);
        src = node;
        src.addOutgoingEdge(this);
    }

    public void setDstNode(GraphNode node) {
        dst.removeIncomingEdge(this);        
        dst = node;
        dst.addIncomingEdge(this);
    }

    public GraphNode getSrcNode() {
        return src;
    }

    public GraphNode getDstNode() {
        return dst;
    }

    public void print() {
        System.out.println("Edge: " + src.getID() + " -> " + dst.getID());
    }
}
