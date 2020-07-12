/*
 *  Copyright 2018-2020
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
import org.jtool.eclipse.pdg.DD;
import org.jtool.eclipse.pdg.CD;
import org.jtool.eclipse.pdg.ParameterEdge;
import org.jtool.eclipse.pdg.CallEdge;
import org.jtool.eclipse.cfg.CCFG;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGEntry;
import org.jtool.eclipse.cfg.CFGClassEntry;
import org.jtool.eclipse.cfg.CFGMethodCall;
import org.jtool.eclipse.cfg.CFGMethodEntry;
import org.jtool.eclipse.cfg.CFGFieldEntry;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGParameter;
import org.jtool.eclipse.cfg.CFGStatement;
import org.jtool.eclipse.cfg.CFGCatch;
import org.jtool.eclipse.cfg.ControlFlow;
import org.jtool.eclipse.cfg.JReference;
import org.jtool.eclipse.cfg.JFieldReference;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.eclipse.jdt.core.dom.ITypeBinding;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Builds a PDG for a class member (a method, constructor, initializer, or field).
 * 
 * @author Katsuhisa Maruyama
 */
public class PDGBuilder {
    
    public static PDG buildPDG(CFG cfg) {
        PDG pdg = new PDG();
        createNodes(pdg, cfg);
        CDFinder.find(pdg, cfg);
        DDFinder.find(pdg, cfg);
        return pdg;
    }
    
    private static void createNodes(PDG pdg, CFG cfg) {
        cfg.getNodes().stream()
           .map(cfgnode -> createNode(pdg, cfgnode))
           .filter(pdgnode -> pdgnode != null)
           .forEach(pdgnode -> pdg.add(pdgnode));
    }
    
    private static PDGNode createNode(PDG pdg, CFGNode node) {
        if (node.isInterfaceEntry() || node.isClassEntry() || node.isEnumEntry()) {
            PDGClassEntry pdgNode = new PDGClassEntry((CFGClassEntry)node);
            pdg.setEntryNode(pdgNode);
            return pdgNode;
            
        } else if (node.isMethodEntry() || node.isConstructorEntry() || node.isInitializerEntry() ||
                   node.isFieldEntry() || node.isEnumConstantEntry()) {
            PDGEntry pdgNode = new PDGEntry((CFGEntry)node);
            pdg.setEntryNode(pdgNode);
            return pdgNode;
            
        } else if (node.isStatement()) {
            PDGStatement pdgNode = new PDGStatement((CFGStatement)node);
            return pdgNode;
        }
        return null;
    }
    
    public static ClDG buildClDG(CCFG ccfg) {
        ClDG cldg = new ClDG();
        PDGClassEntry entry = new PDGClassEntry(ccfg.getEntryNode());
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
    
    public static void connectFieldAccesses(SDG sdg) {
        Set<CFGFieldEntry> fieldEntries = sdg.getPDGs()
                .stream()
                .map(pdg -> pdg.getCFG())
                .filter(cfg -> cfg.isField())
                .map(cfg -> (CFGFieldEntry)cfg.getEntryNode())
                .collect(Collectors.toSet());
        
        for (PDG pdg : sdg.getPDGs()) {
            CFG cfg = pdg.getCFG();
            for (CFGNode node : cfg.getNodes()) {
                if (node.isStatement()) {
                    CFGStatement stNode = (CFGStatement)node;
                    
                    for (JReference var : stNode.getDefVariables()) {
                        if (var.isFieldAccess()) {
                            JFieldReference fvar = (JFieldReference)var;
                            
                            for (CFGFieldEntry fieldEntry : fieldEntries) {
                                if (fieldEntry.getQualifiedName().equals(fvar.getQualifiedName())) {
                                    DD edge = new DD(node.getPDGNode(), fieldEntry.getDeclarationNode().getPDGNode(), fvar);
                                    edge.setFieldAccess();
                                    pdg.add(edge);
                                    
                                    if (cfg.isMethod()) {
                                        CFGMethodEntry cfgEntry = (CFGMethodEntry)cfg.getEntryNode();
                                        if (cfgEntry.isConstructorEntry()) {
                                            CFGParameter foutForInstance = cfgEntry.getFormalOutForReturn();
                                            
                                            DD instanceCreationEdge = new DD(fieldEntry.getDeclarationNode().getPDGNode(), foutForInstance.getPDGNode(), fvar);
                                            instanceCreationEdge.setFieldAccess();
                                            pdg.add(instanceCreationEdge);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    for (JReference var : stNode.getUseVariables()) {
                        if (var.isFieldAccess()) {
                            JFieldReference fvar = (JFieldReference)var;
                            
                            for (CFGFieldEntry fieldEntry : fieldEntries) {
                                if (fieldEntry.getQualifiedName().equals(fvar.getQualifiedName())) {
                                    DD edge = new DD(fieldEntry.getDeclarationNode().getPDGNode(), node.getPDGNode(), fvar);
                                    edge.setFieldAccess();
                                    pdg.add(edge);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static void connectMethodCalls(ClDG cldg) {
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
                        
                        connectParameters(pdg, callnode, (CFGMethodEntry)callee.getCFG().getEntryNode());
                        connectExceptionCatch(pdg, callnode, (CFGMethodEntry)callee.getCFG().getEntryNode());
                    }
                }
            }
        }
        for (PDG pdg : cldg.getPDGs()) {
            SummaryEdgeFinder.find(pdg);
        }
    }
    
    public static void connectMethodCalls(Set<JavaClass> classes, SDG sdg) {
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
                        
                        connectParameters(pdg, callnode, (CFGMethodEntry)callee.getCFG().getEntryNode());
                        connectExceptionCatch(pdg, callnode, (CFGMethodEntry)callee.getCFG().getEntryNode());
                    }
                    for (JavaMethod jm : findOverrindingMethods(classes, callnode)) {
                        PDG callee2 = sdg.getPDG(jm.getQualifiedName());
                        if (callee2 != null) {
                            CallEdge edge = new CallEdge(callnode.getPDGNode(), callee2.getEntryNode());
                            edge.setCall();
                            pdg.add(edge);
                            
                            connectParameters(pdg, callnode, (CFGMethodEntry)callee2.getCFG().getEntryNode());
                            connectExceptionCatch(pdg, callnode, (CFGMethodEntry)callee.getCFG().getEntryNode());
                        }
                    }
                }
            }
        }
        for (PDG pdg : sdg.getPDGs()) {
            SummaryEdgeFinder.find(pdg);
        }
    }
    
    private static Set<JavaMethod> findOverrindingMethods(Set<JavaClass> classes, CFGMethodCall callnode) {
        String className = callnode.getMethodCall().getDeclaringClassName();
        for (JavaClass jclass : classes) {
            if (jclass.getQualifiedName().endsWith(className)) {
                JavaMethod jmethod = jclass.getMethod(callnode.getSignature());
                if (jmethod != null) {
                    return jmethod.getOverridingMethods();
                }
            }
        }
        return new HashSet<>();
    }
    
    private static void connectParameters(PDG pdg, CFGMethodCall caller, CFGMethodEntry callee) {
        CFGParameter lastFormalIn = null;
        for (int ordinal = 0; ordinal < caller.getActualIns().size(); ordinal++) {
            CFGParameter actualIn = caller.getActualIn(ordinal);
            CFGParameter formalIn = callee.getFormalIn(ordinal);
            if (formalIn == null) {
                formalIn = lastFormalIn;
            }
            
            JReference jvar = formalIn.getUseVariables().get(0);
            ParameterEdge edge = new ParameterEdge(actualIn.getPDGNode(), formalIn.getPDGNode(), jvar);
            edge.setParameterIn();
            pdg.add(edge);
            
            lastFormalIn = formalIn;
        }
        
        for (int ordinal = 0; ordinal < caller.getActualOuts().size(); ordinal++) {
            CFGParameter lastFormalOut = null;
            CFGParameter actualOut = caller.getActualOut(ordinal);
            if (actualOut.getDefVariables().size() == 1) {
                CFGParameter formalOut = callee.getFormalOut(ordinal);
                if (formalOut == null) {
                    formalOut = lastFormalOut;
                }
                
                JReference jvar = formalOut.getUseVariables().get(0);
                ParameterEdge edge = new ParameterEdge(formalOut.getPDGNode(), actualOut.getPDGNode(), jvar);
                edge.setParameterOut();
                pdg.add(edge);
                
                lastFormalOut = formalOut;
            }
        }
        
        if (!caller.isVoidType() || caller.isConstructorCall()) {
            CFGParameter actualOut = caller.getActualOutForReturn();
            CFGParameter formalOut = callee.getFormalOutForReturn();
            
            JReference jvar = formalOut.getUseVariables().get(0);
            ParameterEdge edge = new ParameterEdge(formalOut.getPDGNode(), actualOut.getPDGNode(), jvar);
            edge.setParameterOut();
            pdg.add(edge);
        }
    }
    
    public static void connectMethodCallsConservatively(PDG pdg) {
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
        return callnode.getActualIns().stream()
                       .map(node -> (PDGStatement)node.getPDGNode())
                       .collect(Collectors.toSet());
    }
    
    static Set<PDGStatement> findActualOuts(CFGMethodCall callnode) {
        return callnode.getActualOuts().stream()
                .map(node -> (PDGStatement)node.getPDGNode())
                .collect(Collectors.toSet());
    }
    
    private static void connectExceptionCatch(PDG pdg, CFGMethodCall caller, CFGMethodEntry callee) {
        for (ControlFlow flow : caller.getOutgoingFlows()) {
            if (flow.isExceptionCatch()) {
                CFGCatch catchNode = (CFGCatch)flow.getDstNode();
                for (CFGCatch exceptionNode : callee.getExceptionNodes()) {
                    if (getCatchTypes(exceptionNode.getType()).contains(catchNode.getTypeName())) {
                        CD edge = new CD(exceptionNode.getPDGNode(), catchNode.getPDGNode());
                        edge.setExceptionCatch();
                        pdg.add(edge);
                    }
                }
            }
        }
    }
    
    private static Set<String> getCatchTypes(ITypeBinding type) {
        Set<String> types = new HashSet<>();
        while (type != null) {
            types.add(type.getQualifiedName());
            type = type.getSuperclass();
        }
        return types;
    }
}
