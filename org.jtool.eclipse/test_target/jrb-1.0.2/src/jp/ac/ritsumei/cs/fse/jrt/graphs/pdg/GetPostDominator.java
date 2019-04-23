/*
 *     GetPostDominator.java  Oct 8, 2001
 *
 *     Shota Ueno (mi@fse.cs.ritsumei.ac.jp)
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.pdg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.cfg.*;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import java.util.Iterator;

public class GetPostDominator {
    private GraphComponentSet postDominator = new GraphComponentSet();  // CFGNode
    transient private GraphComponentSet track = new GraphComponentSet();  // CFGNode

    private GetPostDominator() {
    }

    public GetPostDominator(CFG cfg, CFGNode anchor) {
        Iterator it = cfg.getNodes().iterator();
        while (it.hasNext()) {
            CFGNode node = (CFGNode)it.next();
            if (!anchor.equals(node)) {
                track.clear();
                track = cfg.getForwardReachableNodes(anchor, node);

                if (track.contains(node) && !track.contains(cfg.getEndNode())) {
                    postDominator.add(node);
                }
            }
        }
    }

    public boolean add(CFGNode node) {
        return postDominator.add(node);
    }

    public boolean contains(CFGNode node) {
        return postDominator.contains(node);
    }

    public boolean isEmpty() {
        return postDominator.isEmpty();
    }

    public Iterator iterator() {
        return postDominator.iterator();
    }

    public void printNodes() {
        Iterator it = postDominator.iterator();
        while (it.hasNext()) {
            CFGNode node = (CFGNode)it.next();
            node.print();
        }
    }
}
