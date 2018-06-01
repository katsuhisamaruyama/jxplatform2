/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg.builder;

import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGEntry;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGStatement;
import org.jtool.eclipse.cfg.JVariable;
import org.jtool.eclipse.graph.GraphNode;
import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.PDGClassEntry;
import org.jtool.eclipse.pdg.PDGEntry;
import org.jtool.eclipse.pdg.PDGNode;
import org.jtool.eclipse.pdg.PDGStatement;
import org.jtool.eclipse.pdg.ParameterEdge;

/**
 * Builds a PDG for a class member (a method, constructor, initializer, and field).
 * @author Katsuhisa Maruyama
 */
public class PDGBuilder {
    
    public static PDG build(CFG cfg) {
        PDG pdg = new PDG();
        createNodes(pdg, cfg);
        CDFinder.find(pdg, cfg);
        DDFinder.find(pdg, cfg);
        return pdg;
    }
    
    private static void createNodes(PDG pdg, CFG cfg) {
        for (CFGNode cfgnode : cfg.getNodes()) {
            PDGNode pdgnode = createNode(pdg, cfgnode);
            if (pdgnode != null) {
                pdg.add(pdgnode);
            }
        }
    }
    
    private static PDGNode createNode(PDG pdg, CFGNode node) {
        if (node.isClassEntry() || node.isEnumEntry()) {
            PDGClassEntry pnode = new PDGClassEntry((CFGEntry)node);
            pdg.setEntryNode(pnode);
            return pnode;
        } else if (node.isMethodEntry() || node.isConstructorEntry() || node.isInitializerEntry() ||
                node.isFieldEntry() || node.isEnumConstantEntry()) {
            PDGEntry pnode = new PDGEntry((CFGEntry)node);
            pdg.setEntryNode(pnode);
            return pnode;
        } else if (node.isStatement()) {
            PDGStatement pnode = new PDGStatement((CFGStatement)node);
            return pnode;
        }
        return null;
    }
    
    public static void connectActualParameters(PDG pdg) {
        for (PDGNode callnode : pdg.getNodes()) {
            if (callnode.getCFGNode().isMethodCall()) {
                PDGNode aout = null;
                for (GraphNode node : callnode.getDstNodes()) {
                    PDGNode pdgnode = (PDGNode)node;
                    if (pdgnode.getCFGNode().isActualOut()) {
                        aout = pdgnode;
                    }
                }
                
                if (aout != null) {
                    for (GraphNode node : callnode.getDstNodes()) {
                        PDGNode pdgnode = (PDGNode)node;
                        if (pdgnode.getCFGNode().isActualIn()) {
                            PDGStatement ain = (PDGStatement)pdgnode;
                            JVariable jvar = ain.getDefVariables().get(0);
                            ParameterEdge edge = new ParameterEdge(ain, aout, jvar);
                            edge.setSummary();
                            pdg.add(edge);
                        }
                    }
                }
            }
        }
    }
}
