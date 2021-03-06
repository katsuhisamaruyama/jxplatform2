/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg.builder;

import org.jtool.eclipse.pdg.CD;
import org.jtool.eclipse.pdg.DD;
import org.jtool.eclipse.pdg.Dependence;
import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.PDGNode;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGStatement;
import org.jtool.eclipse.cfg.ControlFlow;
import org.jtool.eclipse.cfg.JReference;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * Finds data dependences of a PDG from its CFG.
 * This class does not guarantee a loop carried node to be correct if a given PDG contains no control dependence.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class DDFinder {
    
    private static Map<PDGNode, List<PDGNode>> dominatorMap = new HashMap<>();
    
    public static void find(PDG pdg, CFG cfg) {
        dominatorMap.clear();
        
        findDDs(pdg, cfg);
        findDefOrderDDs(pdg, cfg);
    }
    
    private static void findDDs(PDG pdg, CFG cfg) {
        for (CFGNode cfgnode : cfg.getNodes()) {
            if (cfgnode.isStatement() && cfgnode.hasDefVariable()) {
                findDDs(pdg, cfg, (CFGStatement)cfgnode);
            }
        }
    }
    
    private static void findDDs(PDG pdg, CFG cfg, CFGStatement anchor) {
        for (JReference jvar : anchor.getDefVariables()) {
            findDDs(pdg, cfg, anchor, jvar);
        }
    }
    
    private static void findDDs(PDG pdg, CFG cfg, CFGStatement anchor, JReference jvar) {
        for (ControlFlow flow : anchor.getOutgoingFlows()) {
            if (!flow.isFallThrough()) {
                Set<CFGNode> track = new HashSet<>();
                CFGNode cfgnode = (CFGNode)flow.getDstNode();
                findDD(pdg, cfg, anchor, cfgnode, jvar, track);
            }
        }
    }
    
    private static void findDD(PDG pdg, CFG cfg, CFGNode anchor, CFGNode node, JReference jvar, Set<CFGNode> track) {
        track.add(node);
        if (node.hasUseVariable()) {
            CFGStatement candidate = (CFGStatement)node;
            if (candidate.useVariable(jvar)) {
                DD edge;
                if (anchor.isFormalIn()) {
                    edge = new DD(anchor.getPDGNode(), candidate.getPDGNode(), jvar);
                    edge.setLIDD();
                
                } else if (candidate.isFormalOut()) {
                    edge = new DD(anchor.getPDGNode(), candidate.getPDGNode(), jvar);
                    edge.setLIDD();
                
                } else {
                    PDGNode lc = getLoopCarried(pdg, cfg, anchor, candidate);
                    edge = new DD(anchor.getPDGNode(), candidate.getPDGNode(), jvar);
                    if (lc != null && track.contains(lc.getCFGNode())) {
                        edge.setLCDD();
                        edge.setLoopCarriedNode(lc);
                    } else {
                        edge.setLIDD();
                    }
                }
                pdg.add(edge);
            }
        }
        
        if (node.hasDefVariable()) {
            CFGStatement candidate = (CFGStatement)node;
            if (candidate.defineVariable(jvar)) {
                if (!candidate.useVariable(jvar)) {
                    DD edge = new DD(anchor.getPDGNode(), candidate.getPDGNode(), jvar);
                    edge.setOutput();
                    pdg.add(edge);
                }
                return;
            }
        }
        
        for (ControlFlow flow : node.getOutgoingFlows()) {
            if (!flow.isFallThrough()) {
                CFGNode succ = (CFGNode)flow.getDstNode();
                if (!track.contains(succ)) {
                    findDD(pdg, cfg, anchor, succ, jvar, track);
                }
            }
        } 
    }
    
    private static PDGNode getLoopCarried(PDG pdg, CFG cfg, CFGNode def, CFGNode use) {
        List<PDGNode> dtrack = findDominators(def.getPDGNode());
        if (dtrack.isEmpty()) {
            return null;
        }
        
        List<PDGNode> utrack = findDominators(use.getPDGNode());
        if (utrack.isEmpty()) {
            return null;
        }
        
        Set<CFGNode> reachanbleNodes = cfg.constrainedReachableNodes(def, use);
        for (PDGNode pdgnode : dtrack) {
            CFGNode cfgnode = pdgnode.getCFGNode();
            if (utrack.contains(pdgnode) && reachanbleNodes.contains(cfgnode)) {
                return pdgnode;
            }
        }
        return null;
    }
    
    private static List<PDGNode> findDominators(PDGNode pdgnode) {
        Set<PDGNode> track = new HashSet<>();
        List<PDGNode> dominators = new ArrayList<>();
        findDominators(pdgnode, dominators, track);
        return dominators;
    }
    
    private static void findDominators(PDGNode pdgnode, List<PDGNode> dominators, Set<PDGNode> track) {
        List<PDGNode> nodes = dominatorMap.get(pdgnode);
        if (nodes != null) {
            dominators.addAll(nodes);
            return;
        }
        
        track.add(pdgnode);
        if (pdgnode.isLoop()) {
            dominators.add(pdgnode);
        }
        
        for (CD edge : pdgnode.getIncomingCDEdges()) {
            PDGNode src = edge.getSrcNode();
            if (!track.contains(src)) {
                findDominators(src, dominators, track);
            }
        }
        
        dominatorMap.put(pdgnode, dominators);
    }
    
    private static void findDefOrderDDs(PDG pdg, CFG cfg) {
        for (Dependence edge : pdg.getEdges()) {
            if (edge.isDD()) {
                DD dd = (DD)edge;
                if (dd.isOutput() && isDefOrder(dd)) {
                    dd.setDefOrder();
                }
            }
        }
    }
    
    private static boolean isDefOrder(DD dd) {
        PDGNode src = dd.getSrcNode();
        PDGNode dst = dd.getDstNode();
        CD[] srcCDs = src.getIncomingCDEdges().toArray(new CD[0]);
        CD[] dstCDs = dst.getIncomingCDEdges().toArray(new CD[0]);
        if (!srcCDs[0].getSrcNode().equals(dstCDs[0].getSrcNode())) {
            return false;
        }
        
        for (DD srcDD : src.getOutgoingDDEdges()) {
            if (srcDD.isLCDD() || srcDD.isLIDD()) {
                for (DD dstDD : dst.getOutgoingDDEdges()) {
                    if (dstDD.isLCDD() || dstDD.isLIDD()) {
                        if (srcDD.getDstNode().equals(dstDD.getDstNode())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
