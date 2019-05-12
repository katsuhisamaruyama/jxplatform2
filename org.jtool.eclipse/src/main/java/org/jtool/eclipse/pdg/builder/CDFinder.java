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
import org.jtool.eclipse.graph.GraphNode;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * Finds control dependences in a PDG.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class CDFinder {
    
    private static List<CFGNode> checkedNodes = new ArrayList<CFGNode>();
    private static Map<CFGNode, Set<CFGNode>> postDominatorCache = new HashMap<CFGNode, Set<CFGNode>>();
    
    public static void find(PDG pdg, CFG cfg, boolean containingFallThroughEdge) {
        checkedNodes = reverseSortGraphNode(cfg.getNodes());
        postDominatorCache.clear();
        
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
                    } else {
                        edge.setFallThrough();
                    }
                    pdg.add(edge);
                    
                    checkedNodes.remove(cfgnode);
                }
            }
        }
    }
    
    public static Set<CFGNode> postDominator(CFG cfg, CFGNode anchor, boolean containingFallThroughEdge) {
        Set<CFGNode> postDominator = postDominatorCache.get(anchor);
        if (postDominator != null) {
            return postDominator;
        }
        
        postDominator = new HashSet<CFGNode>();
        for (CFGNode node : checkedNodes) {
            if (!anchor.equals(node)) {
                Set<CFGNode> track = forwardReachableNodes(anchor, node, containingFallThroughEdge);
                if (track.contains(node) && !track.contains(cfg.getEndNode())) {
                    postDominator.add(node);
                }
            }
        }
        postDominatorCache.put(anchor, postDominator);
        return postDominator;
    }
    
    private static Set<CFGNode> forwardReachableNodes(CFGNode anchor, CFGNode to, boolean containingFallThroughEdge) {
        Set<CFGNode> track = new HashSet<CFGNode>();
        walkForward(anchor, to, containingFallThroughEdge, track);
        track.add(to);
        return track;
    }
    
    private static void walkForward(CFGNode node, CFGNode to, boolean containingFallThroughEdge, Set<CFGNode> track) {
        if (node.equals(to)) {
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
    
    private static List<CFGNode> reverseSortGraphNode(Collection<CFGNode> co) {
        List<CFGNode> nodes = new ArrayList<CFGNode>(co);
        Collections.sort(nodes, new Comparator<GraphNode>() {
            public int compare(GraphNode node1, GraphNode node2) {
                if (node2.getId() == node1.getId()) {
                    return 0;
                } else if (node1.getId() < node2.getId()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return nodes;
    }
}
