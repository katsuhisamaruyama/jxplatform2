/*
 *  Copyright 2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.slice;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Collects AST nodes appearing on a CFG.
 * 
 * @author Katsuhisa Maruyama
 */
class CopiedNodeCollector {
    
    private Map<Expression, Expression> expressionMap;
    private Map<Statement, Statement> statementMap;
    
    CopiedNodeCollector() {
        expressionMap = new HashMap<>();
        statementMap = new HashMap<>();
    }
    
    void collect(ASTNode oldRoot, ASTNode newRoot) {
        NodeCollector oldCollector = new NodeCollector();
        NodeCollector newCollector = new NodeCollector();
        oldRoot.accept(oldCollector);
        newRoot.accept(newCollector);
        
        for (int index = 0; index < oldCollector.expressionList.size(); index++) {
            expressionMap.put(oldCollector.expressionList.get(index), newCollector.expressionList.get(index));
        }
        for (int index = 0; index < oldCollector.statementList.size(); index++) {
            statementMap.put(oldCollector.statementList.get(index), newCollector.statementList.get(index));
        }
    }
    
    Map<Expression, Expression> getExpressionMap() {
        return expressionMap;
    }
    
    Map<Statement, Statement> getStatementMap() {
        return statementMap;
    }
    
    class NodeCollector extends ASTVisitor {
        
        List<Expression> expressionList = new ArrayList<>();
        List<Statement> statementList = new ArrayList<>();
        
        @Override
        public boolean preVisit2(ASTNode node) {
            if (node instanceof Expression) {
                expressionList.add((Expression)node);
            } else if (node instanceof Statement) {
                statementList.add((Statement)node);
            }
            return true;
        }
    }
}
