/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGStatement;
import org.jtool.eclipse.cfg.ControlFlow;
import org.jtool.eclipse.cfg.JReference;
import org.jtool.eclipse.cfg.JLocalReference;
import java.util.Set;
import java.util.HashSet;

/**
 * Resolves the alias relation for local variables.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
class LocalAliasResolver {
    
    public static void resolve(CFG cfg) {
        for (CFGNode node : cfg.getNodes()) {
            Alias alias = getAliasRelation(node);
            if (alias != null) {
                walkForward(node, alias);
            }
        }
    }
    
    private static Alias getAliasRelation(CFGNode node) {
        if (!node.isAssignment() && !node.isLocalDeclaration()) {
            return null;
        }
        
        CFGStatement stNode = (CFGStatement)node;
        if (stNode.getDefVariables().size() != 1 || stNode.getUseVariables().size() != 1) {
            return null;
        }
        JReference def = stNode.getDefVariables().get(0);
        if (!def.isLocalAccess() || def.isPrimitiveType()) {
            return null;
        }
        
        JReference use = stNode.getUseVariables().get(0);
        if (use.isMethodCall()) {
            return null;
        }
        
        return new Alias((JLocalReference)def, use);
    }
    
    private static void walkForward(CFGNode node, Alias alias) {
        Set<CFGNode> track = new HashSet<CFGNode>();
        track.add(node);
        for (ControlFlow flow : node.getOutgoingFlows()) {
            CFGNode succ = flow.getDstNode();
            if (!track.contains(succ)) {
                walkForward(succ, alias, track);
            }
        }
    }
    
    private static void walkForward(CFGNode node, Alias alias, Set<CFGNode> track) {
        Alias newAlias = getAliasRelation(node);
        if (newAlias != null) {
            return;
        }
        
        if (node.isStatement()) {
            CFGStatement stNode = (CFGStatement)node;
            if (stNode.defineVariable(alias.newName)) {
                stNode.addDefVariable(alias.origName);
            } else if (stNode.defineVariable(alias.origName)) {
                stNode.addDefVariable(alias.newName);
            }
        }
        track.add(node);
        
        for (ControlFlow flow : node.getOutgoingFlows()) {
            CFGNode succ = flow.getDstNode();
            if (!track.contains(succ)) {
                walkForward(succ, alias, track);
            }
        }
    }
}

class Alias {
    
    JLocalReference newName;
    JReference origName;
    
    Alias(JLocalReference newName, JReference origName) {
        this.newName = newName;
        this.origName = origName;
    }
}
