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
    
    public static void find(PDG pdg, CFG cfg) {
        findCDs(pdg, cfg);
        findCDsFromEntry(pdg, cfg);
        addCDsFromEntry(pdg);
        findCDsOnDeclarations(pdg, cfg);
    }
    
    private static void findCDs(PDG pdg, CFG cfg) {
        for (CFGNode cfgnode : cfg.getNodes()) {
            if (cfgnode.isBranch()) {
                findCDs(pdg, cfg, cfgnode);
            }
            if (cfgnode.isMethodCall()) {
                findCDsOnParameters(pdg, (CFGMethodCall)cfgnode);
            }
        }
    }
    
    private static void findCDs(PDG pdg, CFG cfg, CFGNode branchNode) {
        Set<CFGNode> postDominator = cfg.postDominator(branchNode);
        for (ControlFlow branch : branchNode.getOutgoingFlows()) {
            CFGNode branchDstNode = branch.getDstNode();
            Set<CFGNode> postDominatorForBranch = cfg.postDominator(branchDstNode);
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
}
