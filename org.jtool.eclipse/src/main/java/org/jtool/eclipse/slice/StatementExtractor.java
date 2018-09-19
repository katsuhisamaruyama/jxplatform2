/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.slice;

import org.jtool.eclipse.pdg.PDGNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import java.util.Set;
import java.util.HashSet;

/**
 * Visits a Java program and extracts statements contained in a slice.
 * 
 * @author Katsuhisa Maruyama
 */
public class StatementExtractor extends ASTVisitor {
    
    private Set<ASTNode> sliceNodes = new HashSet<ASTNode>();
    
    public StatementExtractor(Set<PDGNode> nodes) {
        for (PDGNode pdfNode : nodes) {
            sliceNodes.add(pdfNode.getCFGNode().getASTNode());
        }
    }
    
    @Override
    public boolean visit(MethodDeclaration node) {
        Set<ASTNode> removeNodes = new HashSet<ASTNode>();
        for (Object obj : node.parameters()) {
            VariableDeclaration param = (VariableDeclaration)obj;
            if (!sliceNodes.contains(param)) {
                removeNodes.add(param);
            }
        }
        for (ASTNode rnode : removeNodes) {
            rnode.delete();
        }
        
        node.getBody().accept(this);
        return false;
    }
    
    @Override
    public boolean visit(Initializer node) {
        node.getBody().accept(this);
        return false;
    }
    
    private void visitStatement(Statement node) {
        if (!sliceNodes.contains(node)) {
            node.delete();
        }
    }
    
    @Override
    public boolean visit(AssertStatement node) {
        visitStatement(node);
        return false;
    }
    
    @Override
    public boolean visit(BreakStatement node) {
        visitStatement(node);
        return false;
    }
    
    @Override
    public boolean visit(ConstructorInvocation node) {
        visitStatement(node);
        return false;
    }
    
    @Override
    public boolean visit(ContinueStatement node) {
        visitStatement(node);
        return false;
    }
    
    @Override
    public boolean visit(DoStatement node) {
        visitStatement(node);
        return false;
    }
    
    @Override
    public boolean visit(EnhancedForStatement node) {
        visitStatement(node);
        return false;
    }
    
    @Override
    public boolean visit(ExpressionStatement node) {
        visitStatement(node);
        return false;
    }
    
    @Override
    public boolean visit(ForStatement node) {
        visitStatement(node);
        return false;
    }
    
    @Override
    public boolean visit(IfStatement node) {
        visitStatement(node);
        return false;
    }
    
    @Override
    public boolean visit(LabeledStatement node) {
        // Always remains
        return false;
    }
    
    @Override
    public boolean visit(ReturnStatement node) {
        visitStatement(node);
        return false;
    }
    
    @Override
    public boolean visit(SuperConstructorInvocation node) {
        visitStatement(node);
        return false;
    }
    
    @Override
    public boolean visit(SwitchCase node) {
        visitStatement(node);
        return false;
    }
    
    @Override
    public boolean visit(SwitchStatement node) {
        visitStatement(node);
        return false;
    }
    
    @Override
    public boolean visit(SynchronizedStatement node) {
        // Always remains
        return false;
    }
    
    @Override
    public boolean visit(ThrowStatement node) {
        // Always remains
        return false;
    }
    
    @Override
    public boolean visit(TryStatement node) {
        // Always remains
        return false;
    }
    
    @Override
    public boolean visit(TypeDeclarationStatement node) {
        // Always remains
        return false;
    }
    
    @Override
    public boolean visit(VariableDeclarationStatement node) {
        visitStatement(node);
        return false;
    }
    
    @Override
    public boolean visit(WhileStatement node) {
        visitStatement(node);
        return false;
    }
}
