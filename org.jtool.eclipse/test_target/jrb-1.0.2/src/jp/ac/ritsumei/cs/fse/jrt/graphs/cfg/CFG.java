/*
 *     CFG.java  Oct 16, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.cfg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.JavaFile;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.OutputStream;

public class CFG extends Graph {
    private CFGMethodEntryNode start;
    private CFGNode end;
    private List blocks = new ArrayList();  // BasicBlock
    transient private GraphComponentSet track = new GraphComponentSet();  // CFGNode

    public CFG() {
        super();
    }

    public void setStartNode(CFGMethodEntryNode node) {
        start = node;
    }

    public CFGMethodEntryNode getStartNode() {
        return start;
    }

    public void setEndNode(CFGNode node) {
        end = node;
    }

    public CFGNode getEndNode() {
        return end;
    }

    public String getName() {
        return start.getName();
    }

    public void add(CFGNode node) {
        super.add(node);
    }

    public void add(Flow edge) {
        super.add(edge);
    }

    public void add(BasicBlock block) {
        blocks.add(block);
    }

    public List getBasicBlocks() {
        return blocks;
    }

    public boolean isBranchNode(CFGNode node) {
        return node.isBranch();
    }

    public boolean isLoopNode(CFGNode node) {
        return node.isLoop();
    }

    public boolean isJoinNode(CFGNode node) {
        return node.isJoin();
    }

    public Flow getFlow(CFGNode src, CFGNode dst) {
        Iterator it = edges.iterator();
        while (it.hasNext()) {
            Flow edge = (Flow)it.next();
            if (edge.getSrcNode() == src && edge.getDstNode() == dst) {
                return edge;
            }
        }
        return null;
    }

    public GraphComponentSet getFlowsTo(CFGNode dst) {
        GraphComponentSet set = new GraphComponentSet();
        Iterator it = edges.iterator();
        while (it.hasNext()) {
            Flow edge = (Flow)it.next();
            if (edge.getDstNode() == dst) {
                set.add(edge);
            }
        }
        return set;
    }

    public GraphComponentSet getFlowsFrom(CFGNode src) {
        GraphComponentSet set = new GraphComponentSet();
        Iterator it = edges.iterator();
        while (it.hasNext()) {
            Flow edge = (Flow)it.next();
            if (edge.getSrcNode() == src) {
                set.add(edge);
            }
        }
        return set;
    }

    public GraphComponentSet getSrcNodes(CFGNode dst) {
        GraphComponentSet set = new GraphComponentSet();
        Iterator it = edges.iterator();
        while (it.hasNext()) {
            Flow edge = (Flow)it.next();
            if (edge.getDstNode() == dst) {
                set.add(edge.getSrcNode());
            }
        }
        return set;
    }

    public GraphComponentSet getDstNodes(CFGNode src) {
        GraphComponentSet set = new GraphComponentSet();
        Iterator it = edges.iterator();
        while (it.hasNext()) {
            Flow edge = (Flow)it.next();
            if (edge.getSrcNode() == src) {
                set.add(edge.getDstNode());
            }
        }
        return set;
    }

    public GraphNode getTrueSuccessor(CFGNode node) {
        Iterator it = edges.iterator();
        while (it.hasNext()) {
            Flow edge = (Flow)it.next();
            if (edge.getSrcNode() == node && edge.isTrue()) {
                return edge.getDstNode();
            }
        }
        return null;
    }

    public GraphNode getFalseSuccessor(CFGNode node) {
        Iterator it = edges.iterator();
        while (it.hasNext()) {
            Flow edge = (Flow)it.next();
            if (edge.getSrcNode() == node && edge.isFalse()) {
                return edge.getDstNode();
            }
        }
        return null;
    }

    public GraphComponentSet getCallNodes() {
        GraphComponentSet set = new GraphComponentSet();
        Iterator it = nodes.iterator();
        while (it.hasNext()) {
            CFGNode node = (CFGNode)it.next();
            if (node.isCallSt()) {
                set.add(node);
            }
        }
        return set;
    }

    public GraphComponentSet getForwardReachableNodes(CFGNode fromNode, CFGNode toNode) {
        track.clear();
        walkForward(fromNode, toNode, true);
        return track;
    }

    public GraphComponentSet getForwardReachableNodesWithoutLoopback(CFGNode fromNode,
                                                                     CFGNode toNode) {
        track.clear();
        walkForward(fromNode, toNode, false);
        return track;
    }

    public GraphComponentSet getBackwardReachableNodes(CFGNode fromNode, CFGNode toNode) {
        track.clear();
        walkBackward(fromNode, toNode, true);
        return track;
    }

    public GraphComponentSet getBackwardReachableNodesWithoutLoopback(CFGNode fromNode,
                                                                      CFGNode toNode) {
        track.clear();
        walkBackward(fromNode, toNode, false);
        return track;
    }

    private void walkForward(GraphNode fromNode, GraphNode toNode, boolean loopOk) {
        if (fromNode.equals(toNode) && !track.isEmpty()) {
            track.add(fromNode);
            return;
        }
        track.add(fromNode);

        Iterator it = fromNode.getOutgoingEdges().iterator();
        while (it.hasNext()) {
            Flow flow = (Flow)it.next();
            if (loopOk || !flow.isLoopBack()) {

                GraphNode succ = flow.getDstNode();
                if (!track.contains(succ)) {
                    walkForward(succ, toNode, loopOk);
                }
            }
        }
    }

    private void walkBackward(GraphNode toNode, GraphNode fromNode, boolean loopOk) {
        if (toNode.equals(fromNode) && !track.isEmpty()) {
            track.add(toNode);
            return;
        }
        track.add(toNode);

        Iterator it = toNode.getIncomingEdges().iterator();
        while (it.hasNext()) {
            Flow flow = (Flow)it.next();
            if (loopOk || !flow.isLoopBack()) {

                GraphNode pred = flow.getSrcNode();
                if (!track.contains(pred)) {
                    walkBackward(pred, fromNode, loopOk);
                }
            }
        }
    }

    /* @deprecated
    public CFGNode getNode(int id) {
        Iterator it = nodes.iterator();
        while(it.hasNext()) {
            CFGNode cfgNode = (CFGNode)it.next();
            if(id == cfgNode.getID()) {
                return cfgNode;
            }
        }
        return null;
    }
    */

    public void createBasicBlock() {
        BBFactory.getInstance().create(this);
    }

    public void print() {
        System.out.println("----- CFG (from here) -----");
        printNodes();
        printEdges();
        System.out.println("----- CFG (to here) -----");
    }

    public void printNodes() {
        Iterator it = nodes.iterator();
        while (it.hasNext()) {
            CFGNode node = (CFGNode)it.next();
            node.print();
        }
    }

    public void printEdges() {
        Iterator it = edges.iterator();
        while (it.hasNext()) {
            Flow edge = (Flow)it.next();
            edge.print();
        }
    }
}
