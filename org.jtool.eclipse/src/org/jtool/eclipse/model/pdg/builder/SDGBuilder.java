/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.pdg.builder;

import org.jtool.eclipse.model.pdg.PDG;
import org.jtool.eclipse.model.pdg.PDGNode;
import org.jtool.eclipse.model.pdg.PDGStatement;
import org.jtool.eclipse.model.pdg.PDGStore;
import org.jtool.eclipse.model.pdg.ParameterEdge;
import org.jtool.eclipse.model.pdg.SDG;
import org.jtool.eclipse.model.cfg.CFGMethodCall;
import org.jtool.eclipse.model.cfg.CFGMethodEntry;
import org.jtool.eclipse.model.cfg.CFGParameter;
import org.jtool.eclipse.model.cfg.JFieldAccess;
import org.jtool.eclipse.model.cfg.JVariable;
import org.jtool.eclipse.model.java.JavaProject;
import org.jtool.eclipse.model.java.JavaClass;
import org.jtool.eclipse.model.java.JavaField;
import org.jtool.eclipse.model.java.JavaMethod;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashMap;

/**
 * Creates a system dependence graph (SDG) which stores PDGs and relationships between them.
 * @author Katsuhisa Maruyama
 */
public class SDGBuilder {
    
    private static HashMap<String, PDG> pdgs = new HashMap<String, PDG>();
    
    public static SDG build(Set<JavaClass> jclasses) {
        SDG sdg = new SDG();
        pdgs.clear();
        for (JavaClass jc : jclasses) {
            build(sdg, jc);
        }
        return sdg;
    }
    
    public static SDG build(List<JavaClass> jclasses) {
        SDG sdg = new SDG();
        pdgs.clear();
        for (JavaClass jc : jclasses) {
            build(sdg, jc);
        }
        return sdg;
    }
    
    public static SDG create(JavaClass jc) {
        SDG sdg = new SDG();
        pdgs.clear();
        build(sdg, jc);
        return sdg;
    }
    
    public static void build(SDG sdg, JavaClass jc) {
        for (JavaMethod jm : jc.getMethods()) {
            build(sdg, jm);
        }
        
        for (JavaField jf : jc.getFields()) {
            build(sdg, jf);
        }
    }
    
    public static SDG build(JavaMethod jmethod) {
        SDG sdg = new SDG();
        pdgs.clear();
        build(sdg, jmethod);
        return sdg;
    }
    
    public static PDG build(SDG sdg, JavaMethod jmethod) {
        PDG pdg = pdgs.get(jmethod.getQualifiedName());
        if (pdg == null) {
            pdg = PDGStore.getInstance().getPDG(jmethod);
            pdgs.put(jmethod.getQualifiedName(), pdg);
            sdg.add(pdg);
            buildPDGsForMethod(jmethod.getFile().getProject(), sdg, pdg);
            createPDGsForField(jmethod.getFile().getProject(), sdg, pdg);
        }
        return pdg;
    }
    
    public static PDG build(SDG sdg, JavaField jfield) {
        PDG pdg = pdgs.get(jfield.getQualifiedName());
        if (pdg == null) {
            pdg = PDGStore.getInstance().getPDG(jfield);
            pdgs.put(jfield.getQualifiedName(), pdg);
            sdg.add(pdg);
            buildPDGsForMethod(jfield.getFile().getProject(), sdg, pdg);
            createPDGsForField(jfield.getFile().getProject(), sdg, pdg);
        }
        return pdg;
    }
    
    private static void buildPDGsForMethod(JavaProject jproject, SDG sdg, PDG pdg) {
        for (CFGMethodCall callnode : collectMethodCallNodes(pdg)) {
            JavaClass jclass = jproject.getClass(callnode.getDeclaringClassName());
            JavaMethod jmethod = jclass.getMethod(callnode.getSignature());
            if (jmethod.isInProject()) {
                PDG cpdg = build(sdg, jmethod);
                connectParameters(sdg, callnode, (CFGMethodEntry)cpdg.getEntryNode().getCFGEntry());
            }
        }
        SummaryFinder.find(sdg, pdg);
    }
    
    private static void createPDGsForField(JavaProject jproject, SDG sdg, PDG pdg) {
        for (JFieldAccess jvar : collectFieldAccesses(pdg)) {
            JavaClass jclass = jproject.getClass(jvar.getDeclaringClassName());
            JavaField jfield = jclass.getField(jvar.getName());
            if (jfield.isInProject()) {
                build(sdg, jfield);
            }
        }
    }
    
    private static List<CFGMethodCall> collectMethodCallNodes(PDG pdg) {
        List<CFGMethodCall> callnodes = new ArrayList<CFGMethodCall>();
        for (PDGNode pdgnode : pdg.getNodes()) {
            if (pdgnode.getCFGNode().isMethodCall()) {
                callnodes.add((CFGMethodCall)pdgnode.getCFGNode());
            }
        }
        return callnodes;
    }
    
    private static void connectParameters(SDG sdg, CFGMethodCall caller, CFGMethodEntry callee) {
        for (int ordinal = 0; ordinal < caller.getActualIns().size(); ordinal++) {
            CFGParameter actualIn = caller.getActualIn(ordinal);
            CFGParameter formalIn = callee.getFormalIn(Math.min(ordinal, callee.getFormalIns().size() - 1));
            JVariable jvar = formalIn.getUseVariables().get(0);
            ParameterEdge edge = new ParameterEdge(actualIn.getPDGNode(), formalIn.getPDGNode(), jvar);
            edge.setParameterIn();
            sdg.add(edge);
        }
        
        if (!callee.isVoidType()) {
            CFGParameter actualOut = caller.getActualOuts().get(0);
            CFGParameter formalOut = callee.getFormalOuts().get(0);
            
            JVariable jvar = formalOut.getDefVariables().get(0);
            ParameterEdge edge = new ParameterEdge(formalOut.getPDGNode(), actualOut.getPDGNode(), jvar);
            edge.setParameterOut();
            sdg.add(edge);
        }
    }
    
    private static List<JFieldAccess> collectFieldAccesses(PDG pdg) {
        List<JFieldAccess> fieldaccesses = new ArrayList<JFieldAccess>();
        for (PDGNode pdgnode : pdg.getNodes()) {
            if (pdgnode.isStatement()) {
                PDGStatement stnode = (PDGStatement)pdgnode;
                for (JVariable jvar : stnode.getDefVariables()) {
                    if (jvar.isFieldAccess()) {
                        fieldaccesses.add((JFieldAccess)jvar);
                    }
                }
                for (JVariable jvar : stnode.getUseVariables()) {
                    if (jvar.isFieldAccess()) {
                        fieldaccesses.add((JFieldAccess)jvar);
                    }
                }
            }
        }
        return fieldaccesses;
    }
}
