/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel.builder;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import java.util.List;
import java.util.ArrayList;

/**
 * Visits a Java program and stores information on statements appearing in a method.
 * 
 * AssertStatement
 * Block
 * BreakStatement
 * ConstructorInvocation
 * ContinueStatement
 * DoStatement
 * EmptyStatement
 * EnhancedForStatement
 * ExpressionStatement
 * ForStatement
 * IfStatement
 * LabeledStatement
 * ReturnStatement
 * SuperConstructorInvocation
 * SwitchCase
 * SwitchStatement
 * SynchronizedStatement
 * ThrowStatement
 * TryStatement
 * TypeDeclarationStatement
 * VariableDeclarationStatement
 * WhileStatement
 * 
 * @see org.eclipse.jdt.core.dom.Statement
 * @author Katsuhisa Maruyama
 */
public class StatementCollector extends ASTVisitor {
    
    private List<ASTNode> nodes = new ArrayList<ASTNode>();
    
    private int numberOfNesting;
    private int maxNumberOfNesting;
    private int cyclomaticNumber;
    
    public StatementCollector() {
        super();
        
        nodes = new ArrayList<ASTNode>();
        numberOfNesting = 0;
        maxNumberOfNesting = 0;
        cyclomaticNumber = 1;
    }
    
    @Override
    public boolean visit(AssertStatement node) {
        nodes.add(node);
        return true;
    }
    
    @Override
    public boolean visit(BreakStatement node) {
        nodes.add(node);
        return true;
    }
    
    @Override
    public boolean visit(ConstructorInvocation node) {
        nodes.add(node);
        return true;
    }
    
    @Override
    public boolean visit(ContinueStatement node) {
        nodes.add(node);
        return true;
    }
    
    @Override
    public boolean visit(DoStatement node) {
        nodes.add(node);
        cyclomaticNumber++;
        incNestCount();
        return true;
    }
    
    @Override
    public void endVisit(DoStatement node) {
        decNestCount();
    }
    
    @Override
    public boolean visit(EnhancedForStatement node) {
        nodes.add(node);
        cyclomaticNumber++;
        incNestCount();
        return true;
    }
    
    @Override
    public void endVisit(EnhancedForStatement node) {
        decNestCount();
    }
    
    @Override
    public boolean visit(ExpressionStatement node) {
        nodes.add(node);
        return true;
    }
    
    @Override
    public boolean visit(ForStatement node) {
        nodes.add(node);
        cyclomaticNumber++;
        incNestCount();
        return true;
    }
    
    @Override
    public void endVisit(ForStatement node) {
        decNestCount();
    }
    
    @Override
    public boolean visit(IfStatement node) {
        nodes.add(node);
        cyclomaticNumber++;
        incNestCount();
        return true;
    }
    
    @Override
    public void endVisit(IfStatement node) {
        decNestCount();
    }
    
    @Override
    public boolean visit(LabeledStatement node) {
        nodes.add(node);
        return true;
    }
    
    @Override
    public boolean visit(ReturnStatement node) {
        nodes.add(node);
        return true;
    }
    
    @Override
    public boolean visit(SuperConstructorInvocation node) {
        nodes.add(node);
        return true;
    }
    
    @Override
    public boolean visit(SwitchCase node) {
        nodes.add(node);
        cyclomaticNumber++;
        incNestCount();
        return true;
    }
    
    @Override
    public void endVisit(SwitchCase node) {
        decNestCount();
    }
    
    @Override
    public boolean visit(SwitchStatement node) {
        nodes.add(node);
        return true;
    }
    
    @Override
    public boolean visit(SynchronizedStatement node) {
        nodes.add(node);
        return true;
    }
    
    @Override
    public boolean visit(ThrowStatement node) {
        nodes.add(node);
        return true;
    }
    
    @Override
    public boolean visit(TryStatement node) {
        nodes.add(node);
        cyclomaticNumber++;
        incNestCount();
        return true;
    }
    
    @Override
    public void endVisit(TryStatement node) {
        decNestCount();
    }
    
    @Override
    public boolean visit(TypeDeclarationStatement node) {
        nodes.add(node);
        return true;
    }
    
    @Override
    public boolean visit(VariableDeclarationStatement node) {
        nodes.add(node);
        return true;
    }
    
    @Override
    public boolean visit(WhileStatement node) {
        nodes.add(node);
        cyclomaticNumber++;
        incNestCount();
        return true;
    }
    
    @Override
    public void endVisit(WhileStatement node) {
        decNestCount();
    }
    
    private void incNestCount() {
        numberOfNesting++;
        
        if (maxNumberOfNesting < numberOfNesting) {
            maxNumberOfNesting = numberOfNesting;
        }
    }
    
    private void decNestCount() {
        numberOfNesting--;
    }
    
    public int getNumberOfStatements() {
        return nodes.size();
    }
    
    public int getMaximumNuberOfNesting() {
        return maxNumberOfNesting;
    }
    
    public int getCyclomaticNumber() {
        return cyclomaticNumber;
    }
}
