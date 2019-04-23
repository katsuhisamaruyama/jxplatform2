/*
 *     BBFactory.java  Oct 16, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.graphs.cfg;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.*;
import java.util.Iterator;
 
public class BBFactory {
    static BBFactory factory = new BBFactory();

    private BBFactory() {
    }

    public static BBFactory getInstance() {
        return factory;
    }

    public void create(CFG cfg) {
        CFGNode startNode = cfg.getStartNode();
        CFGNode firstNode = (CFGNode)startNode.getDstNodes().getFirst();
        
        Iterator it = cfg.getNodes().iterator();
        while (it.hasNext()) {
            CFGNode node = (CFGNode)it.next();
            if (node.equals(firstNode) || node.isJoin()
              || (node.isNextToBranch() && !node.equals(startNode))) {
                BasicBlock block = new BasicBlock(node);
                cfg.add(block);
                block.add(node);
            }
        }
        it = cfg.getBasicBlocks().iterator();
        while (it.hasNext()) {
            BasicBlock block = (BasicBlock)it.next();
            findNodesInBlock(block, cfg);
            // block.print();
        }
    }

    private void findNodesInBlock(BasicBlock block, CFG cfg) {
        CFGNode node = getTrueSucc(block.getLeader());
        while (node != null && !node.isLeader() && !node.equals(cfg.getEndNode())) {
            block.add(node);   
            node = getTrueSucc(node);
        }
    }

    private CFGNode getTrueSucc(CFGNode node) {
        Iterator it = node.getOutgoingEdges().iterator();
        while (it.hasNext()) {
            Flow flow = (Flow)it.next();
            if (flow.isTrue()) {
                return (CFGNode)flow.getDstNode();
            }
        }
        return null;
    }
}
