/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.CCFG;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGClassEntry;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaMethod;

/**
 * Builds a CCFG that corresponds to a class.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class CCFGBuilder {
    
    public static CCFG build(JavaProject jproject, JInfoStore infoStore) {
        CCFG ccfg = new CCFG();
        for (JavaClass jclass : jproject.getClasses()) {
            build(ccfg, jclass, infoStore);
        }
        return ccfg;
    }
    
    public static CCFG build(JavaClass jclass, JInfoStore infoStore) {
        CCFG ccfg = new CCFG();
        return build(ccfg, jclass, infoStore);
    }
    
    private static CCFG build(CCFG ccfg, JavaClass jclass, JInfoStore infoStore) {
        CFGClassEntry entry;
        if (jclass.isEnum()) {
            entry = new CFGClassEntry(jclass, CFGNode.Kind.enumEntry);
        } else if (jclass.isInterface()) {
            entry = new CFGClassEntry(jclass, CFGNode.Kind.interfaceEntry);
        } else {
            entry = new CFGClassEntry(jclass, CFGNode.Kind.classEntry);
        }
        ccfg.setStartNode(entry);
        ccfg.add(entry);
        
        for (JavaMethod jm : jclass.getMethods()) {
            CFG cfg = CFGMethodBuilder.build(jm, infoStore);
            ccfg.add(cfg);
            entry.addMethod(cfg);
        }
        
        for (JavaField jf : jclass.getFields()) {
            CFG cfg = CFGFieldBuilder.build(jf, infoStore);
            ccfg.add(cfg);
            entry.addField(cfg);
        }
        
        for (JavaClass jc : jclass.getInnerClasses()) {
            CFG cfg = build(jc, infoStore);
            ccfg.add(cfg);
            entry.addType(cfg);
        }
        return ccfg;
    }
}
