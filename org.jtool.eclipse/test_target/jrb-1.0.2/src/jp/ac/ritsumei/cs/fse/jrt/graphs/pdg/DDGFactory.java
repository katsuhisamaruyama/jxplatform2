/*
 *     DDGFactory.java  Dec 1, 2001
 *
 *     Katsuhiko Yoshikawa (kappy@fse.cs.ritsumei.ac.jp)
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.pdg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.cfg.*;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import jp.ac.ritsumei.cs.fse.jrt.model.JavaVariable;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
 
public class DDGFactory {
    private static DDGFactory factory = new DDGFactory();
    private PDG pdg;
    private CFG cfg;
    transient private List track = new ArrayList();  // CFGNode
    transient private List atrack = new ArrayList();  // CFGNode

    private DDGFactory() {
    }

    public static DDGFactory getInstance() {
        return factory;
    }

    public void create(PDG pdg, CFG cfg) {
        this.pdg = pdg;
        this.cfg = cfg;
        setDDs();
    }

    private void setDDs() {
        Iterator it = cfg.getNodes().iterator();
        while(it.hasNext()) {
            CFGNode node = (CFGNode)it.next();
            if (node.hasDefVariable()) {
                findDDs(node);
            }
        }
    }
    
    private void findDDs(CFGNode node) {
        CFGStatementNode anchor = (CFGStatementNode)node;
        Iterator it = anchor.getDefVariables().iterator();
        while(it.hasNext()) {
            JavaVariable var = (JavaVariable)it.next();
            findDDs(anchor, var);
        }
    }

    private void findDDs(CFGNode anchor, JavaVariable var) {
        Iterator it = anchor.getDstNodes().iterator();
        while(it.hasNext()) {
            track.clear();
            CFGNode node = (CFGNode)it.next();
            checkDD(anchor, node, var);
        }
    }

    private void checkDD(CFGNode anchor, CFGNode node, JavaVariable var) {
        track.add(node);
        if (node.hasUseVariable()) {
            CFGStatementNode candidate = (CFGStatementNode)node;
            if (candidate.containsUseVariable(var)) {
                DD edge = new DD(pdg.getNode(anchor), pdg.getNode(candidate), var);
                PDGNode lc = getLoopCarried(anchor, candidate);
                edge.setDefUse();
                pdg.add(edge);
                if (lc != null && track.contains(lc.getCFGNode())) {            
                    edge.setLoopCarriedNode(lc);
                }
            }
        }
        
        if (node.hasDefVariable()) {
            CFGStatementNode candidate = (CFGStatementNode)node;
            if (candidate.containsDefVariable(var)) {
                DD edge = new DD(pdg.getNode(anchor), pdg.getNode(candidate), var);
                edge.isOutput();
                pdg.add(edge);
                return;
            }
        }

        Iterator it = node.getDstNodes().iterator();
        while (it.hasNext()) {
            CFGNode succ = (CFGNode)it.next();
            if (!track.contains(succ)) {
                checkDD(anchor, succ, var);
            }
        }
    }

    private PDGNode getLoopCarried(CFGNode defNode, CFGNode useNode) {
        ArrayList defTrack = new ArrayList();
        atrack.clear();
        findCDAncestors(pdg.getNode(defNode), defTrack);
        if (defTrack.isEmpty()) {
            return null;
        }

        ArrayList useTrack = new ArrayList();
        atrack.clear();
        findCDAncestors(pdg.getNode(useNode), useTrack);
        if (useTrack.isEmpty()) {
            return null;
        }

        GetConstrainedReachableNode reachablePath 
          = new GetConstrainedReachableNode(cfg, defNode, useNode);
        ArrayList commonNodes = new ArrayList();
        Iterator it = reachablePath.iterator();
        while (it.hasNext()) {
            CFGNode node = (CFGNode)it.next();
            PDGNode pnode = pdg.getNode(node);
            if (defTrack.contains(pnode) && useTrack.contains(pnode)) {
                commonNodes.add(pnode);
            }
        }
        if (commonNodes.isEmpty()) {
            return null;
        }

        PDGNode loopCarriedNode = (PDGNode)commonNodes.get(0);  // nearest one
        return loopCarriedNode;
    }

    private void findCDAncestors(PDGNode anchor, List ancestors) {
        atrack.add(anchor);
        if (anchor.isLoop()) {
            ancestors.add(anchor);
        }

        Iterator it = anchor.getIncomingEdges().iterator();
        while (it.hasNext()) {
            Dependence edge = (Dependence)it.next();
            if (edge.isCD()) {
                PDGNode node = (PDGNode)edge.getSrcNode();
                if (!atrack.contains(node)) {
                    findCDAncestors(node, ancestors);
                }
            }
        }
    }
}
