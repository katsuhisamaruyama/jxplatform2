/*
 *  Copyright 2018-2019
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
import org.jtool.eclipse.cfg.CFGStatement;
import org.jtool.eclipse.cfg.ControlFlow;
import org.jtool.eclipse.cfg.JReference;
import org.jtool.eclipse.cfg.StopConditionOnReachablePath;
import java.util.Set;
import java.util.HashSet;

/**
 * Finds control dependences in a PDG.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class CDFinder {
    
    public static void find(PDG pdg, CFG cfg, boolean containingFallThroughEdge) {
        findCDs(pdg, cfg, containingFallThroughEdge);
        findCDsFromEntry(pdg, cfg);
        addCDsFromEntry(pdg);
        findCDsOnDeclarations(pdg, cfg);
    }
    
    private static void findCDs(PDG pdg, CFG cfg, boolean containingFallThroughEdge) {
        for (CFGNode cfgnode : cfg.getNodes()) {
            if (cfgnode.isBranch()) {
                findCDs(pdg, cfg, cfgnode, containingFallThroughEdge);
            } else if (cfgnode.isMethodCall()) {
                findCDsOnParameters(pdg, (CFGMethodCall)cfgnode);
            }
        }
    }
    
    private static void findCDs(PDG pdg, CFG cfg, CFGNode branchNode, boolean containingFallThroughEdge) {
        Set<CFGNode> postDominator = postDominator(cfg, branchNode, containingFallThroughEdge);
        for (ControlFlow branch : branchNode.getOutgoingFlows()) {
            CFGNode branchDstNode = branch.getDstNode();
            Set<CFGNode> postDominatorForBranch = postDominator(cfg, branchDstNode, containingFallThroughEdge);
            postDominatorForBranch.add(branchDstNode);
            
            for (CFGNode cfgnode : postDominatorForBranch) {
                if (cfgnode.isStatementNotParameter() && !branchNode.equals(cfgnode) && !postDominator.contains(cfgnode)) {
                    
                    CD edge = new CD(branchNode.getPDGNode(), cfgnode.getPDGNode());
                    if (branch.isTrue()) {
                        edge.setTrue();
                    } else if (branch.isFalse()) {
                        edge.setFalse();
                    } else if (branch.isFallThrough()) {
                        edge.setFallThrough();
                    } else if (branch.isExceptionCatch()) {
                        edge.setExceptionCatch();
                    }
                    pdg.add(edge);
                }
            }
        }
    }
    
    public static Set<CFGNode> postDominator(CFG cfg, CFGNode anchor, boolean containingFallThroughEdge) {
        Set<CFGNode> postDominator = new HashSet<CFGNode>();
        for (CFGNode node : cfg.getNodes()) {
            if (!anchor.equals(node)) {
                Set<CFGNode> track = getForwardTrack(anchor, node, containingFallThroughEdge);
                if (track.contains(node) && !track.contains(cfg.getEndNode())) {
                    postDominator.add(node);
                }
            }
        }
        return postDominator;
    }
    
    private static Set<CFGNode> getForwardTrack(CFGNode from, CFGNode to, boolean containingFallThroughEdge) {
        Set<CFGNode> track = new HashSet<CFGNode>();
        walkForward(from, to, containingFallThroughEdge, track);
        track.add(to);
        return track;
    }
    
    private static void walkForward(CFGNode node, CFGNode to, boolean containingFallThroughEdge, Set<CFGNode> track) {
        if (node.equals(to)) {
            return;
        }
        
        track.add(node);
        
        for (ControlFlow flow : node.getOutgoingFlows()) {
            if (containingFallThroughEdge || (!flow.isJump() && !flow.isExceptionCatch())) {
                CFGNode succ = flow.getDstNode();
                if (!track.contains(succ)) {
                    walkForward(succ, to, containingFallThroughEdge, track);
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
    
    private static void findCDsOnDeclarations(PDG pdg, CFG cfg) {
        for (CFGNode cfgnode : cfg.getNodes()) {
            if (cfgnode.isStatement()) {
                findCDsOnDeclarations(pdg,cfg, (CFGStatement)cfgnode);
            }
        }
    }
    
    private static void findCDsOnDeclarations(PDG pdg, CFG cfg, CFGStatement cfgnode) {
        Set<JReference> vars = new HashSet<JReference>();
        vars.addAll(cfgnode.getDefVariables());
        vars.addAll(cfgnode.getUseVariables());
        for (JReference jv : vars) {
            cfg.backwardReachableNodes(cfgnode, true, new StopConditionOnReachablePath() {
                @Override
                public boolean isStop(CFGNode node) {
                    if (node.isLocalDeclaration()) {
                        CFGStatement decnode = (CFGStatement)node;
                        if (decnode.defineVariable(jv)) {
                            CD edge = new CD(decnode.getPDGNode(), cfgnode.getPDGNode());
                            edge.setDeclaration();
                            pdg.add(edge);
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }
    
    /*****************
      Try to reduce the processing time to find control dependences.
    ******************
    
    private static Map<CFGNode, Set<CFGNode>> forwardReachableNodes = new HashMap<CFGNode, Set<CFGNode>>();
    private static Map<CFGNode, Set<CFGNode>> backwardReachableNodes = new HashMap<CFGNode, Set<CFGNode>>();
    private static Map<CFGNode, CFGNode> firstForwardDominanceMap = new HashMap<CFGNode, CFGNode>();
    
    public static void find2(PDG pdg, CFG cfg, boolean containingFallThroughEdge) {
        collectReachableNodes(cfg, containingFallThroughEdge);
        collectForwardDominanceNodes(cfg);
        
        findCDs2(pdg, cfg, containingFallThroughEdge);
        findCDsFromEntry(pdg, cfg);
        addCDsFromEntry(pdg);
        findCDsOnDeclarations(pdg, cfg);
    }
    
    private static void collectReachableNodes(CFG cfg, boolean containingFallThroughEdge) {
        List<CFGNode> cfgNodeList = CFGNode.sortCFGNode(cfg.getNodes());
        
        forwardReachableNodes.clear();
        for (int index = cfgNodeList.size() - 1; index >= 0 ; index--) {
            CFGNode cfgnode = cfgNodeList.get(index);
            Set<CFGNode> nodes = forward(cfgnode, cfg.getEndNode(), containingFallThroughEdge);
            forwardReachableNodes.put(cfgnode, nodes);
        }
        
        backwardReachableNodes.clear();
        for (int index = 0; index < cfgNodeList.size() ; index++) {
            CFGNode cfgnode = cfgNodeList.get(index);
            Set<CFGNode> nodes = backward(cfg.getStartNode(), cfgnode, containingFallThroughEdge);
            backwardReachableNodes.put(cfgnode, nodes);
        }
    }
    
    private static Set<CFGNode> forward(CFGNode from, CFGNode to, boolean containingFallThroughEdge) {
        Set<CFGNode> track = new HashSet<CFGNode>();
        walkForward(from, to, containingFallThroughEdge, track);
        track.add(to);
        return track;
    }
    
    private static void walkForward(CFGNode node, CFGNode to, boolean containingFallThroughEdge, Set<CFGNode> track) {
        if (node.equals(to)) {
            return;
        }
        
        Set<CFGNode> nodes = forwardReachableNodes.get(node);
        if (nodes != null) {
            track.addAll(nodes);
            return;
        }
        
        track.add(node);
        
        for (ControlFlow flow : node.getOutgoingFlows()) {
            if (containingFallThroughEdge || !flow.isJump()) {
                CFGNode succ = flow.getDstNode();
                if (!track.contains(succ)) {
                    walkForward(succ, to, containingFallThroughEdge, track);
                }
            }
        }
    }
    
    private static Set<CFGNode> backward(CFGNode from, CFGNode to, boolean containingFallThroughEdge) {
        Set<CFGNode> track = new HashSet<CFGNode>();
        walkBackward(from, to, containingFallThroughEdge, track);
        track.add(from);
        return track;
    }
    
    private static void walkBackward(CFGNode from, CFGNode node, boolean containingFallThroughEdge, Set<CFGNode> track) {
        if (node.equals(from)) {
            return;
        }
        
        Set<CFGNode> nodes = backwardReachableNodes.get(node);
        if (nodes != null) {
            track.addAll(nodes);
            return;
        }
        
        track.add(node);
        
        for (ControlFlow flow : node.getIncomingFlows()) {
            if (containingFallThroughEdge || !flow.isJump()) {
                CFGNode pred = flow.getSrcNode();
                if (!track.contains(pred)) {
                    walkBackward(from, pred, containingFallThroughEdge, track);
                }
            }
        }
    }
    
    private static void collectForwardDominanceNodes(CFG cfg) {
        firstForwardDominanceMap.clear();
        for (CFGNode cfgnode : cfg.getNodes()) {
            if (cfgnode.getOutgoingFlows().size() == 1) {
                ControlFlow flow = cfgnode.getOutgoingFlows().iterator().next();
                CFGNode dominator = flow.getDstNode();
                firstForwardDominanceMap.put(cfgnode, dominator);
                
            } else {
                Set<CFGNode> dominators = new HashSet<CFGNode>();
                for (ControlFlow branch : cfgnode.getOutgoingFlows()) {
                    CFGNode dst = branch.getDstNode();
                    Set<CFGNode> postNodes = forwardReachableNodes.get(dst);
                    if (dominators.size() == 0) {
                        dominators = postNodes;
                    } else {
                        dominators = GraphElement.intersection(dominators, postNodes);
                    }
                }
                if (cfgnode.isJoin()) {
                    dominators.add(cfgnode);
                }
                
                Set<CFGNode> diff = new HashSet<CFGNode>(dominators);
                for (CFGNode dominator : dominators) {
                    for (ControlFlow flow : dominator.getOutgoingFlows()) {
                        CFGNode dst = flow.getDstNode();
                        if (dominators.contains(dst)) {
                            Set<CFGNode> nodes = forwardReachableNodes.get(dst);
                            diff = GraphElement.difference(diff, nodes);
                        }
                    }
                }
                if (diff.size() == 1) {
                    firstForwardDominanceMap.put(cfgnode, diff.iterator().next());
                }
            }
        }
        
        for (CFGNode cfgnode : cfg.getNodes()) {
            if (firstForwardDominanceMap.get(cfgnode) != null) {
                System.out.println("FFD(" + cfgnode.getId() + ") = " + firstForwardDominanceMap.get(cfgnode).getId());
            }
        }
    }
    
    private static Set<CFGNode> reachableNodes(CFGNode from, CFGNode to) {
        Set<CFGNode> ftrack = forwardReachableNodes.get(from);
        Set<CFGNode> btrack = backwardReachableNodes.get(to);
        Set<CFGNode> track = GraphElement.intersection(ftrack, btrack);
        return track;
    }
    
    private static void findCDs2(PDG pdg, CFG cfg, boolean containingFallThroughEdge) {
        Set<CFGNode> branchNodes = new HashSet<CFGNode>();
        for (CFGNode cfgnode : cfg.getNodes()) {
            if (cfgnode.isBranch()) {
                branchNodes.add(cfgnode);
            }
        }
        
        for (CFGNode cfgnode : cfg.getNodes()) {
            if (cfgnode.isStatementNotParameter()) {
                for (CFGNode branchNode : branchNodes) {
                    if (!branchNode.equals(cfgnode)) {
                        
                        Set<CFGNode> track = reachableNodes(branchNode, cfgnode);
                        if (track.size() > 0 && !track.contains(firstForwardDominanceMap.get(branchNode))) {
                            CD edge = new CD(branchNode .getPDGNode(), cfgnode.getPDGNode());
                            setCDSort(branchNode, track, edge);
                            pdg.add(edge);
                        }
                    }
                }
            }
        }
        
        for (CFGNode cfgnode : cfg.getNodes()) {
            if (cfgnode.isMethodCall()) {
                findCDsOnParameters(pdg, (CFGMethodCall)cfgnode);
            }
        }
    }
    
    private static void setCDSort(CFGNode branchNode, Set<CFGNode> nodes, CD edge) {
        for (ControlFlow branch : branchNode.getOutgoingFlows()) {
            CFGNode dst = branch.getDstNode();
            if (nodes.contains(dst)) {
                if (branch.isTrue()) {
                    edge.setTrue();
                } else if (branch.isFalse()) {
                    edge.setFalse();
                } else {
                    edge.setFallThrough();
                }
            }
        }
    }
    */
}
