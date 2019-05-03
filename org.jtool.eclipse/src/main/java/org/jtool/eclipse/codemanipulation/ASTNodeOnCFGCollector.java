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
import java.util.Set;
import java.util.HashSet;

/**
 * Collects AST nodes appearing on a CFG.
 * 
 * @author Katsuhisa Maruyama
 */
public class ASTNodeOnCFGCollector extends ASTVisitor {
    
    private Set<ASTNode> nodes = new HashSet<ASTNode>();
    
    public ASTNodeOnCFGCollector() {
    }
    
    public Set<ASTNode> collect(ASTNode node) {
        node.accept(this);
        return nodes;
    }
    
    @Override
    public void preVisit(ASTNode node) {
        if (!nodes.contains(node) && isCFGNode(node)) {
            nodes.add(node);
        }
    }
    
    private boolean isCFGNode(ASTNode node) {
        return node instanceof BodyDeclaration ||
               StatementVisitor.isCFGNode(node) ||
               ExpressionVisitor.isCFGNode(node) ||
               ExpressionVisitor.isCFGNodeOnLiteral(node);
    }
}
