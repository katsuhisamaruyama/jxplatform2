/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg.builder;

import org.jtool.eclipse.pdg.DD;
import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.PDGNode;
import org.jtool.eclipse.pdg.PDGStatement;
import org.jtool.eclipse.pdg.ParameterEdge;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGMethodCall;
import org.jtool.eclipse.cfg.JReference;
import java.util.Set;
import java.util.HashSet;

/**
 * Finds summary edges for data dependences between actual-in nodes and actual-out nodes in a PDG.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class SummaryEdgeFinder {
    
    public static void find(PDG pdg) {
        for (PDGNode pdgnode : pdg.getNodes()) {
            CFGNode cfgnode = pdgnode.getCFGNode();
            if (cfgnode.isMethodCall()) {
                CFGMethodCall callnode = (CFGMethodCall)cfgnode;
                Set<PDGStatement> ains = PDGBuilder.findActualIns(callnode);
                Set<PDGStatement> aouts = PDGBuilder.findActualOuts(callnode);
                
                Set<PDGStatement> nodes = new HashSet<PDGStatement>();
                for (PDGStatement aout : aouts) {
                    traverseBackward(nodes, aout, ains);
                    
                    for (PDGStatement ain : ains) {
                        if (nodes.contains(ain)) {
                            JReference jvar = ain.getDefVariables().get(0);
                            ParameterEdge edge = new ParameterEdge(ain, aout, jvar);
                            edge.setSummary();
                            pdg.add(edge);
                        }
                    }
                }
            }
        }
    }
    
    private static void traverseBackward(Set<PDGStatement> nodes, PDGStatement anchor, Set<PDGStatement> ains) {
        nodes.add(anchor);
        for (DD edge : anchor.getIncomingDDEdges()) {
            PDGStatement node = (PDGStatement)edge.getSrcNode();
            if (ains.contains(node)) {
                nodes.add(node);
            } else if (!nodes.contains(node)) {
                traverseBackward(nodes, node, ains);
            }
        }
    }
}
