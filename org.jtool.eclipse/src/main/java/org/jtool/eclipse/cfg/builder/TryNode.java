/*
 *  Copyright 2018-2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.eclipse.jdt.core.dom.ASTNode;
import org.jtool.eclipse.cfg.CFGStatement;
import org.jtool.eclipse.cfg.CFGNode;
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
    
    private List<CatchNode> catchClauses = new ArrayList<CatchNode>();
    private CFGStatement finallyBlock;
    
    TryNode() {
    }
    
    void addExceptionOccurrence(CFGNode node, String type) {
        exceptionOccurrences.add(new ExceptionOccurrence(node, type));
    }
    
    Set<ExceptionOccurrence> getExceptionOccurrences() {
        return exceptionOccurrences;
    }
    
    TryNode(ASTNode node, Kind kind) {
        super(node, kind);
    }
    
    void addCatchClause(CatchNode node) {
        catchClauses.add(node);
    }
    
    void setCatchClauses(List<CatchNode> clauses) {
        for (CatchNode node : clauses) {
            addCatchClause(node);
        }
    }
    
    List<CatchNode> getCatchClauses() {
        return catchClauses;
    }
    
    void setFinallyBlock(CFGStatement node) {
        finallyBlock = node;
    }
    
    CFGStatement getFinallyBlock() {
        return finallyBlock;
    }
    
    class ExceptionOccurrence {
        CFGNode node;
        String type;
        
        ExceptionOccurrence(CFGNode node, String type) {
            this.node = node;
            this.type = type;
        }
    }
}
