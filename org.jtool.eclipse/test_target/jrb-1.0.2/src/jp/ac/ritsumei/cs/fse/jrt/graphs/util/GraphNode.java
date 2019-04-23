/*
 *     GraphNode.java  Sep 17, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.util;
import java.util.Iterator;

public class GraphNode implements GraphComponent, GraphNodeSort {
    private static int nodeNum = 0;
    protected int id = -1;
    protected int sort;

    // The follwings are for perfomance.
    protected GraphComponentSet incomingEdges = new GraphComponentSet();  // GraphEdge
    protected GraphComponentSet outgoingEdges = new GraphComponentSet();  // GraphEdge
    protected GraphComponentSet srcNodes = new GraphComponentSet();       // GraphNode
    protected GraphComponentSet dstNodes = new GraphComponentSet();       // GraphNode

    protected GraphNode() {
    }

    public GraphNode(int sort) {
        nodeNum++;
        id = nodeNum;
        this.sort = sort;
    }

    public int getID() {
        return id;
    }

    public static void clearID() {
        nodeNum = 0;
    }

    public void setSort(int s) {
        sort = s;
    }

    public int getSort() {
        return sort;
    }

    public void clear() {
        incomingEdges.clear();
        outgoingEdges.clear();
        srcNodes.clear();
        dstNodes.clear();
    }

    public boolean equals(GraphComponent c) {
        GraphNode node = (GraphNode)c;
        if (this == node) {
            return true;
        }
        return false;
    }

    public void addIncomingEdge(GraphEdge edge) {
        incomingEdges.add(edge);
    }

    public void addOutgoingEdge(GraphEdge edge) {
        outgoingEdges.add(edge);
    }

    public void addIncomingEdges(GraphComponentSet edges) {
        Iterator it = edges.iterator();
        while (it.hasNext()) {
            GraphEdge edge = (GraphEdge)it.next();
            incomingEdges.add(edge);
        }
    }

    public void addOutgoingEdges(GraphComponentSet edges) {
        Iterator it = edges.iterator();
        while (it.hasNext()) {
            GraphEdge edge = (GraphEdge)it.next();
            outgoingEdges.add(edge);
        }
    }

    public void removeIncomingEdge(GraphEdge edge) {
        incomingEdges.remove(edge);
    }

    public void removeOutgoingEdge(GraphEdge edge) {
        outgoingEdges.remove(edge);
    }

    public void setIncomingEdges(GraphComponentSet edges) {
        incomingEdges = edges;
    }

    public void setOutgoingEdges(GraphComponentSet edges) {
        outgoingEdges = edges;
    }

    public void clearIncomingEdges() {
        incomingEdges.clear();
    }

    public void clearOutgoingEdges() {
        outgoingEdges.clear();
    }

    public GraphComponentSet getIncomingEdges() {
        return incomingEdges;
    }

    public GraphComponentSet getOutgoingEdges() {
        return outgoingEdges;
    }

    public void setSrcNodes() {
        Iterator it = incomingEdges.iterator();
        while (it.hasNext()) {
            GraphEdge edge = (GraphEdge)it.next();
            srcNodes.add(edge.getSrcNode());
        }
    }

    public void setDstNodes() {
        Iterator it = outgoingEdges.iterator();
        while (it.hasNext()) {
            GraphEdge edge = (GraphEdge)it.next();
            dstNodes.add(edge.getDstNode());
        }
    }

    public GraphComponentSet getSrcNodes() {
        return srcNodes;
    }

    public GraphComponentSet getDstNodes() {
        return dstNodes;
    }

    public void print() {
        System.out.println(id + ": " + "sort = " + sort);
    }
}
