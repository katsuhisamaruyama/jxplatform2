/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg.builder;

import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.PDGClassEntry;
import org.jtool.eclipse.pdg.PDGEntry;
import org.jtool.eclipse.pdg.PDGNode;
import org.jtool.eclipse.pdg.PDGStatement;
import org.jtool.eclipse.pdg.ParameterEdge;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGEntry;
import org.jtool.eclipse.cfg.CFGMethodCall;
import org.jtool.eclipse.cfg.CFGMethodEntry;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGParameter;
import org.jtool.eclipse.cfg.CFGStatement;
import org.jtool.eclipse.cfg.JReference;
import org.jtool.eclipse.cfg.builder.CFGFieldBuilder;
import org.jtool.eclipse.cfg.builder.CFGMethodBuilder;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaMethod;
import java.util.Set;
import java.util.HashSet;

/**
 * Builds a PDG for a class member (a method, constructor, initializer, or field).
 * 
 * @author Katsuhisa Maruyama
 */
public class PDGBuilder {
    
    public static PDG build(JavaMethod jmethod) {
        CFG cfg = CFGMethodBuilder.build(jmethod);
        return PDGBuilder.build(cfg);
    }
    
    public static PDG build(JavaField jfield) {
        CFG cfg = CFGFieldBuilder.build(jfield);
        PDG pdg = PDGBuilder.build(cfg);
        return pdg;
    }
    
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
    
    static void connectParameters(PDG pdg, CFGMethodCall caller, CFGMethodEntry callee) {
        for (int ordinal = 0; ordinal < caller.getActualIns().size(); ordinal++) {
            CFGParameter actualIn = caller.getActualIn(ordinal);
            CFGParameter formalIn = callee.getFormalIn(Math.min(ordinal, callee.getFormalIns().size() - 1));
            
            JReference jvar = formalIn.getUseVariables().get(0);
            ParameterEdge edge = new ParameterEdge(actualIn.getPDGNode(), formalIn.getPDGNode(), jvar);
            edge.setParameterIn();
            pdg.add(edge);
        }
        
        if (!callee.isVoidType()) {
            CFGParameter actualOut = caller.getActualOuts().get(0);
            CFGParameter formalOut = callee.getFormalOuts().get(0);
            
            JReference jvar = formalOut.getDefVariables().get(0);
            ParameterEdge edge = new ParameterEdge(formalOut.getPDGNode(), actualOut.getPDGNode(), jvar);
            edge.setParameterOut();
            pdg.add(edge);
        }
    }
    
    public static void connectParametersConservatively(PDG pdg) {
        for (PDGNode pdgnode : pdg.getNodes()) {
            CFGNode cfgnode = pdgnode.getCFGNode();
            if (cfgnode.isMethodCall()) {
                CFGMethodCall callnode = (CFGMethodCall)cfgnode;
                Set<PDGStatement> ains = findActualIns(callnode);
                Set<PDGStatement> aouts = findActualOuts(callnode);
                
                for (PDGStatement aout : aouts) {
                    for (PDGStatement ain : ains) {
                        JReference jvar = ain.getDefVariables().get(0);
                        ParameterEdge edge = new ParameterEdge(ain, aout, jvar);
                        edge.setSummary();
                        pdg.add(edge);
                    }
                }
            }
        }
    }
    
    static Set<PDGStatement> findActualIns(CFGMethodCall callnode) {
        Set<PDGStatement> nodes = new HashSet<PDGStatement>();
        for (CFGNode node : callnode.getActualIns()) {
            nodes.add((PDGStatement)node.getPDGNode());
        }
        return nodes;
    }
    
    static Set<PDGStatement> findActualOuts(CFGMethodCall callnode) {
        Set<PDGStatement> nodes = new HashSet<PDGStatement>();
        for (CFGNode node : callnode.getActualOuts()) {
            nodes.add((PDGStatement)node.getPDGNode());
        }
        return nodes;
    }
}
