/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg.builder;

import org.jtool.eclipse.pdg.ClDG;
import org.jtool.eclipse.pdg.SDG;
import org.jtool.eclipse.pdg.ClassMemberEdge;
import org.jtool.eclipse.pdg.Dependence;
import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.PDGClassEntry;
import org.jtool.eclipse.pdg.PDGEntry;
import org.jtool.eclipse.pdg.PDGNode;
import org.jtool.eclipse.pdg.PDGStatement;
import org.jtool.eclipse.pdg.ParameterEdge;
import org.jtool.eclipse.pdg.CallEdge;
import org.jtool.eclipse.cfg.CCFG;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGEntry;
import org.jtool.eclipse.cfg.CFGMethodCall;
import org.jtool.eclipse.cfg.CFGMethodEntry;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGParameter;
import org.jtool.eclipse.cfg.CFGStatement;
import org.jtool.eclipse.cfg.JReference;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaMethod;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

/**
 * Builds a PDG for a class member (a method, constructor, initializer, or field).
 * 
 * @author Katsuhisa Maruyama
 */
public class PDGBuilder {
    
    public static PDG buildPDG(CFG cfg) {
        PDG pdg = new PDG();
        createNodes(pdg, cfg);
        CDFinder.find(pdg, cfg, PDGStore.getInstance().ignoringJumpEdge());
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
    
    public static ClDG buildClDG(CCFG ccfg) {
        ClDG cldg = new ClDG();
        PDGClassEntry entry = new PDGClassEntry(ccfg.getStartNode());
        cldg.setEntryNode(entry);
        cldg.add(entry);
        
        for (CFG cfg : ccfg.getCFGs()) {
            PDG pdg = buildPDG(cfg);
            cldg.add(pdg);
            
            ClassMemberEdge edge = new ClassMemberEdge(entry, pdg.getEntryNode());
            edge.setKind(Dependence.Kind.classMember);
            cldg.add(edge);
        }
        return cldg;
    }
    
    public static void connectParameters(ClDG cldg) {
        for (PDG pdg : cldg.getPDGs()) {
            CFG cfg = pdg.getCFG();
            for (CFGNode node : cfg.getNodes()) {
                if (node.isMethodCall()) {
                    CFGMethodCall callnode = (CFGMethodCall)node;
                    PDG callee = cldg.getPDG(callnode.getQualifiedName());
                    if (callee != null) {
                        CallEdge edge = new CallEdge(callnode.getPDGNode(), callee.getEntryNode());
                        edge.setCall();
                        pdg.add(edge);
                        connectParameters(pdg, callnode, (CFGMethodEntry)callee.getCFG().getStartNode());
                    }
                }
            }
        }
        for (PDG pdg : cldg.getPDGs()) {
            SummaryEdgeFinder.find(pdg);
        }
    }
    
    public static void connectParameters(List<JavaClass> classes, SDG sdg) {
        for (PDG pdg : sdg.getPDGs()) {
            CFG cfg = pdg.getCFG();
            for (CFGNode node : cfg.getNodes()) {
                if (node.isMethodCall()) {
                    CFGMethodCall callnode = (CFGMethodCall)node;
                    PDG callee = sdg.getPDG(callnode.getQualifiedName());
                    if (callee != null) {
                        CallEdge edge = new CallEdge(callnode.getPDGNode(), callee.getEntryNode());
                        edge.setCall();
                        pdg.add(edge);
                        connectParameters(pdg, callnode, (CFGMethodEntry)callee.getCFG().getStartNode());
                    }
                    for (JavaMethod jm : findOverrindingMethods(classes, callnode)) {
                        PDG callee2 = sdg.getPDG(jm.getQualifiedName());
                        if (callee2 != null) {
                            CallEdge edge = new CallEdge(callnode.getPDGNode(), callee2.getEntryNode());
                            edge.setCall();
                            pdg.add(edge);
                            connectParameters(pdg, callnode, (CFGMethodEntry)callee2.getCFG().getStartNode());
                        }
                    }
                }
            }
        }
        for (PDG pdg : sdg.getPDGs()) {
            SummaryEdgeFinder.find(pdg);
        }
    }
    
    private static Set<JavaMethod> findOverrindingMethods(List<JavaClass> classes, CFGMethodCall callnode) {
        String className = callnode.getMethodCall().getDeclaringClassName();
        for (JavaClass jclass : classes) {
            if (jclass.getQualifiedName().endsWith(className)) {
                JavaMethod jmethod = jclass.getMethod(callnode.getSignature());
                if (jmethod != null) {
                    return jmethod.getOverridingMethods();
                }
            }
        }
        return new HashSet<JavaMethod>();
    }
    
    private static void connectParameters(PDG pdg, CFGMethodCall caller, CFGMethodEntry callee) {
        for (int ordinal = 0; ordinal < caller.getActualIns().size(); ordinal++) {
            CFGParameter actualIn = caller.getActualIn(ordinal);
            CFGParameter formalIn = callee.getFormalIn(ordinal);
            JReference jvar = formalIn.getUseVariables().get(0);
            ParameterEdge edge = new ParameterEdge(actualIn.getPDGNode(), formalIn.getPDGNode(), jvar);
            edge.setParameterIn();
            pdg.add(edge);
        }
        
        for (int ordinal = 0; ordinal < caller.getActualOuts().size(); ordinal++) {
            CFGParameter actualOut = caller.getActualOut(ordinal);
            CFGParameter formalOut = callee.getFormalOut(ordinal);
            JReference jvar = formalOut.getUseVariables().get(0);
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
