/*
 *  Copyright 2018-2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.eclipse.jdt.core.dom.ASTNode;
import org.jtool.eclipse.cfg.CFGStatement;
import org.jtool.eclipse.cfg.CFGNode;
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
    
    private Set<ExceptionOccurrence> exceptionOccurrences = new HashSet<ExceptionOccurrence>();
    
    private List<CatchNode> catchNodes = new ArrayList<CatchNode>();
    private CFGStatement finallyNode;
    
    TryNode() {
    }
    
    TryNode(ASTNode node, Kind kind) {
        super(node, kind);
    }
    
    void addExceptionOccurrence(CFGNode node, ITypeBinding type, boolean methidCall) {
        exceptionOccurrences.add(new ExceptionOccurrence(node, type, methidCall));
    }
    
    Set<ExceptionOccurrence> getExceptionOccurrences() {
        return exceptionOccurrences;
    }
    
    void addCatchNode(CatchNode node) {
        catchNodes.add(node);
    }
    
    void setCatchNodes(List<CatchNode> nodes) {
        for (CatchNode node : nodes) {
            addCatchNode(node);
        }
    }
    
    List<CatchNode> getCatchNodes() {
        return catchNodes;
    }
    
    void setFinallyNode(CFGStatement node) {
        finallyNode = node;
    }
    
    CFGStatement getFinallyNode() {
        return finallyNode;
    }
    
    class ExceptionOccurrence {
        private CFGNode node;
        private ITypeBinding type;
        private boolean methodCall;
        
        ExceptionOccurrence(CFGNode node, ITypeBinding type, boolean methodCall) {
            this.node = node;
            this.type = type;
            this.methodCall = methodCall;
        }
        
        CFGNode getNode() {
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
