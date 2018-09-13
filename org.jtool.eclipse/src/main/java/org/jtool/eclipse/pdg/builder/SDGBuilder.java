/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.pdg.builder;

import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.ClDG;
import org.jtool.eclipse.pdg.SDG;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGMethodCall;
import org.jtool.eclipse.cfg.CFGMethodEntry;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGStore;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaMethod;
import java.util.List;
import java.util.ArrayList;

/**
 * Builds a SDG.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class SDGBuilder {
    
    public static SDG build(List<JavaClass> jclasses) {
        SDG sdg = new SDG();
        for (JavaClass jclass : jclasses) {
            ClDG cldg = ClDGBuilder.build(jclass);
            sdg.add(cldg);
        }
        return sdg;
    }
    
    public static void connectParameters(SDG sdg) {
        if (CFGStore.getInstance().creatingActualNodes()) {
            for (PDG pdg : sdg.getPDGs()) {
                CFG cfg = pdg.getCFG();
                for (CFGNode node : cfg.getNodes()) {
                    if (node.isMethodCall()) {
                        CFGMethodCall callnode = (CFGMethodCall)node;
                        PDG callee = sdg.getPDG(callnode.getQualifiedName());
                        PDGBuilder.connectParameters(sdg, callnode, (CFGMethodEntry)callee.getCFG().getStartNode());
                    }
                }
            }
            for (PDG pdg : sdg.getPDGs()) {
                SummaryEdgeFinder.find(pdg);
            }
        }
    }
    
    public static SDG build(JavaClass jclass) {
        List<JavaClass> jclasses = new ArrayList<JavaClass>();
        collectEfferentClasses(jclass, jclasses);
        SDG sdg = build(jclasses);
        return sdg;
    }
    
    public static SDG build(JavaMethod jmethod) {
        return build(jmethod.getDeclaringClass());
    }
    
    public static SDG build(JavaField jfield) {
        return build(jfield.getDeclaringClass());
    }
    
    private static void collectEfferentClasses(JavaClass jclass, List<JavaClass> jclasses) {
        if (jclasses.contains(jclass)) {
            return;
        }
        jclasses.add(jclass);
        
        for (JavaClass jc : jclass.getEfferentClassesInProject()) {
            collectEfferentClasses(jc, jclasses);
        }
    }
}
