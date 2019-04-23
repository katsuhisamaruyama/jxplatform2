/*
 *     GetReachableNode.java  Oct 8, 2001
 *
 *     Katsuhiko Yoshikawa (kappy@fse.cs.ritsumei.ac.jp)
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.cfg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import java.util.Iterator;

public class GetReachableNode {
    private GraphComponentSet reachableNodes;
    private GraphComponentSet ftrack;
    private GraphComponentSet btrack;

    private GetReachableNode() {
    }

    public GetReachableNode(CFG cfg, CFGNode fromNode, CFGNode toNode) {
        ftrack = new GraphComponentSet(cfg.getForwardReachableNodes(fromNode, toNode));
        btrack = new GraphComponentSet(cfg.getBackwardReachableNodes(toNode, fromNode));
        reachableNodes = new GraphComponentSet(ftrack.intersection(btrack));
    }

    public GraphComponentSet getForwardReachableNode() {
        return ftrack;
    }

    public GraphComponentSet getBackwardReachableNode() {
        return btrack;
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
