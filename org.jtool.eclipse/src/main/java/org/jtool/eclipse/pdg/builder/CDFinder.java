/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg.builder;

import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.PDGNode;
import org.jtool.eclipse.pdg.CD;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGMethodCall;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGParameter;
import org.jtool.eclipse.cfg.ControlFlow;
import java.util.Set;
import java.util.HashSet;

/**
 * Finds control dependences in a PDG.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class CDFinder {
    
    public static void find(PDG pdg, CFG cfg, boolean ignoringFallThrough) {
        findCDs(pdg, cfg, ignoringFallThrough);
        findCDsFromEntry(pdg, cfg);
        addCDsFromEntry(pdg);
    }
    
    private static void findCDs(PDG pdg, CFG cfg, boolean ignoringFallThrough) {
        for (CFGNode cfgnode : cfg.getNodes()) {
            if (cfgnode.isBranch()) {
                findCDs(pdg, cfg, cfgnode, ignoringFallThrough);
            } else if (cfgnode.isMethodCall()) {
                findCDsOnParameters(pdg, (CFGMethodCall)cfgnode);
            }
        }
    }
    
    private static void findCDs(PDG pdg, CFG cfg, CFGNode branchNode, boolean ignoringFallThrough) {
        Set<CFGNode> postDominator = postDominator(cfg, branchNode, ignoringFallThrough);
        for (ControlFlow branch : branchNode.getOutgoingFlows()) {
            CFGNode branchDstNode = branch.getDstNode();
            Set<CFGNode> postDominatorForBranch = postDominator(cfg, branchDstNode, ignoringFallThrough);
            postDominatorForBranch.add(branchDstNode);
            
            for (CFGNode cfgnode : postDominatorForBranch) {
                if (cfgnode.isStatementNotParameter() && !branchNode.equals(cfgnode) && !postDominator.contains(cfgnode)) {
                    CD edge = new CD(branchNode.getPDGNode(), cfgnode.getPDGNode());
                    if (branch.isTrue()) {
                        edge.setTrue();
                    } else if (branch.isFalse()) {
                        edge.setFalse();
                    } else {
                        edge.setFallThrough();
                    }
                    pdg.add(edge);
                }
            }
        }
    }
    
    public static Set<CFGNode> postDominator(CFG cfg, CFGNode anchor, boolean ignoringFallThrough) {
        Set<CFGNode> postDominator = new HashSet<CFGNode>();
        for (CFGNode node : cfg.getNodes()) {
            if (!anchor.equals(node)) {
                Set<CFGNode> track = forwardReachableNodes(anchor, node, ignoringFallThrough);
                if (track.contains(node) && !track.contains(cfg.getEndNode())) {
                    postDominator.add(node);
                }
            }
        }
        return postDominator;
    }
    
    private static Set<CFGNode> forwardReachableNodes(CFGNode anchor, CFGNode to, boolean ignoringFallThrough) {
        Set<CFGNode> track = new HashSet<CFGNode>();
        walkForward(anchor, to, ignoringFallThrough, track);
        track.add(to);
        return track;
    }
    
    private static void walkForward(CFGNode node, CFGNode to, boolean ignoringFallThrough, Set<CFGNode> track) {
        if (node.equals(to)) {
            return;
        }
        track.add(node);
        
        for (ControlFlow flow : node.getOutgoingFlows()) {
            if (!ignoringFallThrough || !flow.isJump()) {
                CFGNode succ = flow.getDstNode();
                if (!track.contains(succ)) {
                    walkForward(succ, to, ignoringFallThrough, track);
                }
            }
        }
    }
    
    private static void findCDsOnParameters(PDG pdg, CFGMethodCall callnode) {
        for (CFGParameter cfgnode : callnode.getActualIns()) {
            CD edge = new CD(callnode.getPDGNode(), cfgnode.getPDGNode());
            edge.setTrue();
            pdg.add(edge);
        }
        
        for (CFGParameter cfgnode : callnode.getActualOuts()) {
            CD edge = new CD(callnode.getPDGNode(), cfgnode.getPDGNode());
            edge.setTrue();
            pdg.add(edge);
        }
    }
    
    private static void findCDsFromEntry(PDG pdg, CFG cfg) {
        CFGNode startNode = cfg.getStartNode();
        Set<CFGNode> postDominator = cfg.postDominator(startNode);
        for (CFGNode cfgnode : postDominator) {
            if (cfgnode.isStatementNotParameter() || cfgnode.isFormal()) {
                CD edge = new CD(startNode.getPDGNode(), cfgnode.getPDGNode());
                edge.setTrue();
                pdg.add(edge);
            }
        }
    }
    
    private static void addCDsFromEntry(PDG pdg) {
        for (PDGNode pdgnode : pdg.getNodes()) {
            if (!pdgnode.equals(pdg.getEntryNode()) && pdgnode.getNumOfIncomingTrueFalseCDs() == 0) {
                CD edge = new CD(pdg.getEntryNode(), pdgnode);
                edge.setTrue();
                pdg.add(edge);
            }
        }
    }
}
