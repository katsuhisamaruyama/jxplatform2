/*
 *     GetConstrainedReachableNode.java  Oct 8, 2001
 *
 *     Katsuhiko Yoshikawa (kappy@fse.cs.ritsumei.ac.jp)
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.cfg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import java.util.Iterator;

public class GetConstrainedReachableNode {
    private GraphComponentSet reachableNodes;

    private GetConstrainedReachableNode() {
    }

    public GetConstrainedReachableNode(CFG cfg, CFGNode fromNode, CFGNode toNode) {
        GraphComponentSet W;

        W = cfg.getBackwardReachableNodes(toNode, fromNode);
        GraphComponentSet forwardCRP = new GraphComponentSet(W);
        W = cfg.getForwardReachableNodes(fromNode, cfg.getEndNode());
        forwardCRP.intersection(W);

        W = cfg.getForwardReachableNodes(fromNode, toNode);
        GraphComponentSet backwardCRP = new GraphComponentSet(W);
        W = cfg.getBackwardReachableNodes(toNode, cfg.getStartNode());
        backwardCRP.intersection(W);

        reachableNodes = new GraphComponentSet(forwardCRP.union(backwardCRP));
    }

    public boolean contains(GraphNode node) {
        return reachableNodes.contains(node);
    }

    public boolean isEmpty() {
        return reachableNodes.isEmpty();
    }

    public Iterator iterator() {
        return reachableNodes.iterator();
    }

    public void printNodes() {
        Iterator it = reachableNodes.iterator();
        while (it.hasNext()) {
            CFGNode node = (CFGNode)it.next();
            node.print();
        }
    }
}
