/*
 *     Graph.java  Sep 17, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.util;
import java.util.Iterator;

public class Graph implements java.io.Serializable {
    protected GraphComponentSet nodes = new GraphComponentSet();  // GraphNode
    protected GraphComponentSet edges = new GraphComponentSet();  // GraphEdge

    public Graph() {
    }

    public void setNodes(GraphComponentSet set) {
        nodes = set;
    }

    public GraphComponentSet getNodes() {
        return nodes;
    }

    public void setEdges(GraphComponentSet set) {
        edges = set;
    }

    public GraphComponentSet getEdges() {
        return edges;
    }

    public void clear() {
        nodes.clear();
        edges.clear();
    }

    public void add(GraphNode node) {
        nodes.add(node);
    }

    public void add(GraphEdge edge) {
        edges.add(edge);
    }

    public void removeNode(GraphNode node) {
        nodes.remove(node);
    }

    public void removeNode(GraphEdge edge) {
        edges.remove(edge);
    }

    public boolean contains(GraphNode node) {
        return nodes.contains(node);
    }

    public boolean contains(GraphEdge edge) {
        return edges.contains(edge);
    }

    public void setSrcDstNodes() {
        Iterator it = nodes.iterator();
        while (it.hasNext()) {
            GraphNode node = (GraphNode)it.next();
            node.setSrcNodes();
            node.setDstNodes();
        }
    }

    public void print() {
    }

    public void printNodes() {
    }

    public void printEdges() {
    }

    public boolean equals(Graph graph) {
        if (this == graph) {
            return true;
        }
        if (nodes.equals(graph.getNodes()) && edges.equals(graph.getEdges())) {
            return true;
        }
        return false;
    }
}
