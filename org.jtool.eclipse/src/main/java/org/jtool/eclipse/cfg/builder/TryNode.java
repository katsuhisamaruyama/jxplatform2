/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGStatement;
import org.eclipse.jdt.core.dom.ASTNode;
import java.util.List;
import java.util.ArrayList;

/**
 * A node for a <code>try</code> statement of a CFG.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
class TryNode extends CFGStatement {
    
    private List<CatchNode> catchClauses = new ArrayList<CatchNode>();
    private CFGStatement finallyBlock;
    private CFGNode finallyEnd;
    private CFGNode tryEnd;
    
    TryNode() {
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
    
    void setFinallyBlockEnd(CFGNode node) {
        finallyEnd = node;
    }
    
    CFGNode getFinallyBlockEnd() {
        return finallyEnd;
    }
    
    void setTryEnd(CFGNode node) {
        tryEnd = node;
    }
    
    CFGNode getTryEnd() {
        return tryEnd;
    }
}
