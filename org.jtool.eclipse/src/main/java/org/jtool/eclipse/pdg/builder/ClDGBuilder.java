/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg.builder;

import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.PDGClassEntry;
import org.jtool.eclipse.pdg.PDGStore;
import org.jtool.eclipse.pdg.ClDG;
import org.jtool.eclipse.pdg.ClassMemberEdge;
import org.jtool.eclipse.pdg.Dependence;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CCFG;
import org.jtool.eclipse.cfg.CFGMethodCall;
import org.jtool.eclipse.cfg.CFGMethodEntry;
import org.jtool.eclipse.cfg.builder.CCFGBuilder;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaMethod;

/**
 * Builds a ClDG.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class ClDGBuilder {
    
    public static ClDG build(JavaClass jclass) {
        CCFG ccfg = CCFGBuilder.build(jclass);
        
        ClDG cldg = new ClDG();
        PDGClassEntry entry = new PDGClassEntry(ccfg.getStartNode());
        cldg.setEntryNode(entry);
        cldg.add(entry);
        
        for (JavaMethod jmethod : jclass.getMethods()) {
            PDG pdg = PDGStore.getInstance().getPDG(jmethod);
            cldg.add(pdg);
            
            ClassMemberEdge edge = new ClassMemberEdge(entry, pdg.getEntryNode());
            edge.setKind(Dependence.Kind.classMember);
            cldg.add(edge);
        }
        
        for (JavaField jfield : jclass.getFields()) {
            PDG pdg = PDGStore.getInstance().getPDG(jfield);
            cldg.add(pdg);
            
            ClassMemberEdge edge = new ClassMemberEdge(entry, pdg.getEntryNode());
            edge.setKind(Dependence.Kind.classMember);
            cldg.add(edge);
        }
        return cldg;
    }
    
    public static ClDG build(JavaMethod jmethod) {
        return build(jmethod.getDeclaringClass());
    }
    
    public static ClDG build(JavaField jfield) {
        return build(jfield.getDeclaringClass());
    }
    
    public static void connectParameters(ClDG cldg) {
        for (PDG pdg : cldg.getPDGs()) {
            CFG cfg = pdg.getCFG();
            for (CFGNode node : cfg.getNodes()) {
                if (node.isMethodCall()) {
                    CFGMethodCall callnode = (CFGMethodCall)node;
                    PDG callee = cldg.getPDG(callnode.getQualifiedName());
                    PDGBuilder.connectParameters(cldg, callnode, (CFGMethodEntry)callee.getCFG().getStartNode());
                }
            }
        }
        for (PDG pdg : cldg.getPDGs()) {
            SummaryEdgeFinder.find(pdg);
        }
    }
}
