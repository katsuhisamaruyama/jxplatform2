/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.pdg.builder;

import org.jtool.eclipse.model.pdg.DD;
import org.jtool.eclipse.model.pdg.PDG;
import org.jtool.eclipse.model.pdg.PDGNode;
import org.jtool.eclipse.model.pdg.PDGStatement;
import org.jtool.eclipse.model.pdg.ParameterEdge;
import org.jtool.eclipse.model.pdg.SDG;
import org.jtool.eclipse.model.cfg.CFGNode;
import org.jtool.eclipse.model.cfg.JVariable;
import java.util.Set;
import java.util.HashSet;

/**
 * Finds summary dependences of a PDG.
 * @author Katsuhisa Maruyama
 */
public class SummaryFinder {
    
    public static void find(SDG sdg, PDG pdg) {
        Set<PDGStatement> ains = findActualIns(pdg);
        Set<PDGStatement> aouts = findActualOuts(pdg);
        
        Set<PDGStatement> nodes = new HashSet<PDGStatement>();
        for (PDGStatement aout : aouts) {
            traverseBackward(nodes, aout, ains);
            
            for (PDGStatement ain : ains) {
                if (nodes.contains(ain)) {
                    JVariable jvar = ain.getDefVariables().get(0);
                    ParameterEdge edge = new ParameterEdge(ain, aout, jvar);
                    edge.setSummary();
                    pdg.add(edge);
                    sdg.add(edge);
                }
            }
        }
    }
    
    private static Set<PDGStatement> findActualIns(PDG pdg) {
        Set<PDGStatement> nodes = new HashSet<PDGStatement>();
        for (PDGNode node : pdg.getNodes()) {
            CFGNode cfgnode = node.getCFGNode();
            if (cfgnode.isActualIn()) {
                nodes.add((PDGStatement)node);
            }
        }
        return nodes;
    }
    
    private static Set<PDGStatement> findActualOuts(PDG pdg) {
        Set<PDGStatement> nodes = new HashSet<PDGStatement>();
        for (PDGNode node : pdg.getNodes()) {
            CFGNode cfgnode = node.getCFGNode();
            if (cfgnode.isActualOut()) {
                nodes.add((PDGStatement)node);
            }
        }
        return nodes;
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
