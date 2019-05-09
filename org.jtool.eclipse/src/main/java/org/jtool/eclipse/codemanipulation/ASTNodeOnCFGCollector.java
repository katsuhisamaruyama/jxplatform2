/*
 *  Copyright 2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.codemanipulation;

import org.jtool.eclipse.cfg.builder.ExpressionVisitor;
import org.jtool.eclipse.cfg.builder.StatementVisitor;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * Collects AST nodes appearing on a CFG.
 * 
 * @author Katsuhisa Maruyama
 */
public class ASTNodeOnCFGCollector extends ASTVisitor {
    
    private Map<Integer, ASTNode> nodeMap = new HashMap<Integer, ASTNode>();
    
    public ASTNodeOnCFGCollector(ASTNode node) {
        node.accept(this);
    }
    
    public Set<ASTNode> getNodeSet() {
        return new HashSet<ASTNode>(nodeMap.values());
    }
    
    public Map<Integer, ASTNode> getNodeMap() {
        return nodeMap;
    }
    
    @Override
    public void preVisit(ASTNode node) {
        if (isCFGNode(node)) {
            nodeMap.put(node.getStartPosition(), node);
        }
    }
    
    private boolean isCFGNode(ASTNode node) {
        return node instanceof BodyDeclaration ||
               StatementVisitor.isCFGNode(node) ||
               ExpressionVisitor.isCFGNode(node) ||
               ExpressionVisitor.isCFGNodeOnLiteral(node);
    }
}
