/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.eclipse.jdt.core.dom.ASTNode;
import org.jtool.eclipse.cfg.CFGStatement;
import org.jtool.eclipse.cfg.CFGCatch;
import org.eclipse.jdt.core.dom.ITypeBinding;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * A node for a <code>try</code> statement of a CFG.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
class TryNode extends CFGStatement {
    
    private Set<ExceptionOccurrence> exceptionOccurrences = new HashSet<>();
    
    private List<CFGCatch> catchNodes = new ArrayList<>();
    private CFGStatement finallyNode;
    
    TryNode(ASTNode node, Kind kind) {
        super(node, kind);
    }
    
    void addExceptionOccurrence(CFGStatement node, ITypeBinding type, boolean methidCall) {
        exceptionOccurrences.add(new ExceptionOccurrence(node, type, methidCall));
    }
    
    Set<ExceptionOccurrence> getExceptionOccurrences() {
        return exceptionOccurrences;
    }
    
    void addCatchNode(CFGCatch node) {
        catchNodes.add(node);
    }
    
    void setCatchNodes(List<CFGCatch> nodes) {
        for (CFGCatch node : nodes) {
            addCatchNode(node);
        }
    }
    
    List<CFGCatch> getCatchNodes() {
        return catchNodes;
    }
    
    void setFinallyNode(CFGStatement node) {
        finallyNode = node;
    }
    
    CFGStatement getFinallyNode() {
        return finallyNode;
    }
    
    class ExceptionOccurrence {
        private CFGStatement node;
        private ITypeBinding type;
        private boolean methodCall;
        
        ExceptionOccurrence(CFGStatement node, ITypeBinding type, boolean methodCall) {
            this.node = node;
            this.type = type;
            this.methodCall = methodCall;
        }
        
        CFGStatement getNode() {
            return node;
        }
        
        ITypeBinding getType() {
          return type;
        }
        
        String getTypeName() {
            return type.getQualifiedName();
        }
        
        boolean isMethodCall() {
            return methodCall;
        }
    }
}
