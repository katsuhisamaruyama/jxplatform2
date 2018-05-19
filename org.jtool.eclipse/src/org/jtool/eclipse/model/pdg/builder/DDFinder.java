/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.pdg.builder;

import org.jtool.eclipse.model.pdg.CD;
import org.jtool.eclipse.model.pdg.DD;
import org.jtool.eclipse.model.pdg.DependenceEdge;
import org.jtool.eclipse.model.pdg.PDG;
import org.jtool.eclipse.model.pdg.PDGNode;
import org.jtool.eclipse.model.cfg.CFG;
import org.jtool.eclipse.model.cfg.CFGNode;
import org.jtool.eclipse.model.cfg.CFGStatement;
import org.jtool.eclipse.model.cfg.ControlFlow;
import org.jtool.eclipse.model.cfg.JVariable;
import org.jtool.eclipse.model.graph.GraphEdge;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * Finds data dependences of a PDG from its CFG.
 * This class does not guarantee a loop carried node to be correct if a given PDG contains no control dependence.
 * @author Katsuhisa Maruyama
 */
public class DDFinder {
    
    public static void find(PDG pdg, CFG cfg) {
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
        for (JVariable jvar : anchor.getDefVariables()) {
            findDDs(pdg, cfg, anchor, jvar);
        }
    }
    
    private static void findDDs(PDG pdg, CFG cfg, CFGStatement anchor, JVariable jvar) {
        for (ControlFlow flow : anchor.getOutgoingFlows()) {
            if (!flow.isFallThrough()) {
                Set<CFGNode> track = new HashSet<CFGNode>();
                CFGNode cfgNode = (CFGNode)flow.getDstNode();
                findDD(pdg, cfg, anchor, cfgNode, jvar, track);
            }
        }
    }
    
    private static void findDD(PDG pdg, CFG cfg, CFGNode anchor, CFGNode node, JVariable jvar, Set<CFGNode> track) {
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
                DD edge = new DD(anchor.getPDGNode(), candidate.getPDGNode(), jvar);
                edge.setOutput();
                pdg.add(edge);
                return;
            }
        }
        
        for (ControlFlow flow : node.getOutgoingFlows()) {
            if (!flow.isFallThrough()) {
                CFGNode succ = (CFGNode)flow.getDstNode();
                if (!track.contains(succ))
                    findDD(pdg, cfg, anchor, succ, jvar, track);
            }
        } 
    }
    
    private static PDGNode getLoopCarried(PDG pdg, CFG cfg, CFGNode def, CFGNode use) {
        Set<PDGNode> atrack = new HashSet<PDGNode>();
        
        ArrayList<PDGNode> dtrack = new ArrayList<PDGNode>();
        atrack.clear();
        findDominators(def.getPDGNode(), dtrack, atrack);
        if (dtrack.isEmpty()) {
            return null;
        }
        
        ArrayList<PDGNode> utrack = new ArrayList<PDGNode>();
        atrack.clear();
        findDominators(use.getPDGNode(), utrack, atrack);
        if (utrack.isEmpty()) {
            return null;
        }
        
        ConstrainedReachableNodes path = new ConstrainedReachableNodes(cfg, def, use);
        for (PDGNode pdgnode : dtrack) {
            CFGNode cfgNode = pdgnode.getCFGNode();
            if (utrack.contains(pdgnode) && path.contains(cfgNode)) {
                return pdgnode;
            }
        }
        return null;
    }
    
    private static void findDominators(PDGNode pdgnode, ArrayList<PDGNode> dominators, Set<PDGNode> atrack) {
        atrack.add(pdgnode);
        if (pdgnode.isLoop()) {
            dominators.add(pdgnode);
        }
        
        for (GraphEdge edge : pdgnode.getIncomingEdges()) {
            DependenceEdge dependence = (DependenceEdge)edge;
            
            if (dependence.isCD()) {
                PDGNode src = dependence.getSrcNode();
                
                if (!atrack.contains(src)) {
                    findDominators(src, dominators, atrack);
                }
            }
        }
    }
    
    private static void findDefOrderDDs(PDG pdg, CFG cfg) {
        for (GraphEdge edge : pdg.getEdges()) {
            DependenceEdge dependence = (DependenceEdge)edge;
            
            if (dependence.isDD()) {
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
        List<CD> srcEdges = new ArrayList<CD>(src.getIncomingCDEdges());
        List<CD> dstEdges = new ArrayList<CD>(src.getIncomingCDEdges());
        CD srcCD = srcEdges.get(0);
        CD dstCD = dstEdges.get(0);
        if (!srcCD.getSrcNode().equals(dstCD.getSrcNode())) {
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
