/*
 *     CDGFactory.java  Dec 1, 2001
 *
 *     Shota Ueno (mi@fse.cs.ritsumei.ac.jp)
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.pdg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.cfg.*;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import java.util.Iterator;
 
public class CDGFactory {
    private static CDGFactory factory = new CDGFactory();
    private PDG pdg;
    private CFG cfg;

    private CDGFactory() {
    }

    public static CDGFactory getInstance() {
        return factory;
    }

    public void create(PDG pdg, CFG cfg) {
        this.pdg = pdg;
        this.cfg = cfg;
        setCDs();
    }

    private void setCDs() {
        Iterator it = cfg.getNodes().iterator();
        while (it.hasNext()) {
            CFGNode node = (CFGNode)it.next();

            if (node.isBranch()) {
                findCDs(node);
            }

            if (node.isCallSt()) {
                findParameterCDs((CFGCallNode)node);
            }
        }
        findCDsAtEntry();
    }

    private void findCDsAtEntry() {
        CFGNode entryNode = cfg.getStartNode();
        GetPostDominator postDominator = new GetPostDominator(cfg, entryNode);

        Iterator it = postDominator.iterator();
        while (it.hasNext()) {
            CFGNode node = (CFGNode)it.next();
            if (node.isNormalStatement() || node.isFormalSt()) {
                CD edge = new CD(pdg.getNode(entryNode), pdg.getNode(node));
                edge.setTrue();
                pdg.add(edge);
            }
        }
    }

    private void findCDs(CFGNode branchNode) {
        GetPostDominator postDominator = new GetPostDominator(cfg, branchNode);

        Iterator edgeIt = branchNode.getOutgoingEdges().iterator();
        while (edgeIt.hasNext()) {
            Flow branch = (Flow)edgeIt.next();
            CFGNode branchDstNode = (CFGNode)branch.getDstNode();
            GetPostDominator postDominatorL = new GetPostDominator(cfg, branchDstNode);
            postDominatorL.add(branchDstNode);

            Iterator nodeIt = cfg.getNodes().iterator();
            while (nodeIt.hasNext()) {
                CFGNode node = (CFGNode)nodeIt.next();
                if (node.isNormalStatement() && !branchNode.equals(node)
                    && !postDominator.contains(node) && postDominatorL.contains(node)) {
                    CD edge = new CD(pdg.getNode(branchNode), pdg.getNode(node));
                    if (branch.isTrue()) {
                        edge.setTrue();
                    } else if (branch.isFalse()) {
                        edge.setFalse();
                    } else {
                        edge.setFall();
                    }
                    pdg.add(edge);
                }
            }
        }
    }

    private void findParameterCDs(CFGCallNode callNode) {
        Iterator it = callNode.getActualIns().iterator();
        while (it.hasNext()) {
            CFGParameterNode parNode = (CFGParameterNode)it.next();

            CD edge = new CD(pdg.getNode(callNode), pdg.getNode(parNode));
            edge.setTrue();
            pdg.add(edge);
        }

        it = callNode.getActualOuts().iterator();
        while (it.hasNext()) {
            CFGParameterNode parNode = (CFGParameterNode)it.next();

            CD edge = new CD(pdg.getNode(callNode), pdg.getNode(parNode));
            edge.setTrue();
            pdg.add(edge);
        }
    }
}
