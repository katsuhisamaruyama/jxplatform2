/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg.builder;

import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGMethodCall;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGParameter;
import org.jtool.eclipse.cfg.ControlFlow;
import org.jtool.eclipse.pdg.CD;
import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.PDGNode;

/**
 * Finds control dependences of a PDG from its CFG.
 * @author Katsuhisa Maruyama
 */
public class CDFinder {
    
    public static void find(PDG pdg, CFG cfg) {
        findCDs(pdg, cfg);
        findControlDependencesAtEntry(pdg, cfg);
        addControlDependencesAtEntry(pdg);
    }
    
    private static void findCDs(PDG pdg, CFG cfg) {
        for (CFGNode cfgnode : cfg.getNodes()) {
            if (cfgnode.isBranch()) {
                findCDs(pdg, cfg, cfgnode);
            } else if (cfgnode.isMethodCall()) {
                findParameterCDs(pdg, (CFGMethodCall)cfgnode);
            }
        }
    }
    
    private static void findCDs(PDG pdg, CFG cfg, CFGNode branchNode) {
        PostDominator postDominator = new PostDominator(cfg, branchNode);
        for (ControlFlow branch : branchNode.getOutgoingFlows()) {
            CFGNode branchDstNode = branch.getDstNode();
            PostDominator postDominatorLocal = new PostDominator(cfg, branchDstNode);
            postDominatorLocal.add(branchDstNode);
            
            for (CFGNode cfgnode : cfg.getNodes()) {
                if (cfgnode.isStatementNotParameter() && !branchNode.equals(cfgnode) &&
                    !postDominator.contains(cfgnode) && postDominatorLocal.contains(cfgnode)) {
                    CD edge = new CD(branchNode.getPDGNode(), cfgnode.getPDGNode());
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
    
    private static void findParameterCDs(PDG pdg, CFGMethodCall callNode) {
        for (CFGParameter cfgnode : callNode.getActualIns()) {
            CD edge = new CD(callNode.getPDGNode(), cfgnode.getPDGNode());
            edge.setTrue();
            pdg.add(edge);
        }
        
        for (CFGParameter cfgnode : callNode.getActualOuts()) {
            CD edge = new CD(callNode.getPDGNode(), cfgnode.getPDGNode());
            edge.setTrue();
            pdg.add(edge);
        }
    }
    
    private static void findControlDependencesAtEntry(PDG pdg, CFG cfg) {
        CFGNode startNode = cfg.getStartNode();
        PostDominator postDominator = new PostDominator(cfg, startNode);
        for (CFGNode cfgnode : postDominator) {
            if (cfgnode.isStatementNotParameter() || cfgnode.isFormal()) {
                CD edge = new CD(startNode.getPDGNode(), cfgnode.getPDGNode());
                edge.setTrue();
                pdg.add(edge);
            }
        }
    }
    
    private static void addControlDependencesAtEntry(PDG pdg) {
        for (PDGNode pdgnode : pdg.getNodes()) {
            if (!pdgnode.equals(pdg.getEntryNode()) && pdgnode.getNumOfIncomingTrueFalseCDs() == 0) {
                
                CD edge = new CD(pdg.getEntryNode(), pdgnode);
                edge.setTrue();
                pdg.add(edge);
            }
        }
    }
}
