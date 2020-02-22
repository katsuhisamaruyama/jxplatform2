/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.CCFG;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGClassEntry;
import org.jtool.eclipse.cfg.CFGMethodCall;
import org.jtool.eclipse.cfg.CFGStatement;
import org.jtool.eclipse.cfg.JReference;
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
    
    public static CCFG build(JavaProject jproject, boolean force, JInfoStore infoStore) {
        CCFG ccfg = new CCFG();
        for (JavaClass jclass : jproject.getClasses()) {
            build(ccfg, jclass, force, infoStore);
        }
        addFiledAccesses(ccfg);
        return ccfg;
    }
    
    public static CCFG build(JavaClass jclass, boolean force, JInfoStore infoStore) {
        CCFG ccfg = new CCFG();
        build(ccfg, jclass, force, infoStore);
        addFiledAccesses(ccfg);
        return ccfg;
    }
    
    private static void build(CCFG ccfg, JavaClass jclass, boolean force, JInfoStore infoStore) {
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
        
        for (JavaMethod jmethod : jclass.getMethods()) {
            CFG cfg = infoStore.getCFGStore().getCFG(jmethod, force);
            if (cfg == null) {
                cfg = CFGMethodBuilder.build(jmethod, infoStore);
            }
            ccfg.add(cfg);
            entry.addMethod(cfg);
        }
        
        for (JavaField jfild : jclass.getFields()) {
            CFG cfg = infoStore.getCFGStore().getCFG(jfild, force);
            if (cfg == null) {
                cfg = CFGFieldBuilder.build(jfild, infoStore);
            }
            ccfg.add(cfg);
            entry.addField(cfg);
        }
        
        for (JavaClass jc : jclass.getInnerClasses()) {
            CCFG ccfg2 = build(jc, force, infoStore);
            entry.addType(ccfg2);
        }
    }
    
    private static void addFiledAccesses(CCFG ccfg) {
        for (CFGNode cfgnode : ccfg.getNodes()) {
            if (cfgnode.isMethodCall()) {
                CFGMethodCall callnode = (CFGMethodCall)cfgnode;
                CFG cfg = ccfg.getCFG(callnode.getQualifiedName());
                if (cfg != null) {
                    for (CFGNode node : cfg.getNodes()) {
                        if (node.isStatement()) {
                            CFGStatement st = (CFGStatement)node;
                            for (JReference def : st.getDefVariables()) {
                                if (def.isFieldAccess()) {
                                    callnode.addDefVariable(def);
                                }
                            }
                            for (JReference def : st.getUseVariables()) {
                                if (def.isFieldAccess()) {
                                    callnode.addUseVariable(def);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
