/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.slice;

import org.jtool.eclipse.pdg.PDG;
import org.jtool.eclipse.pdg.PDGNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

/**
 * Visits a Java program and extracts statements contained in a slice.
 * 
 * @author Katsuhisa Maruyama
 */
public class SliceExtractor extends ASTVisitor {
    
    private Set<ASTNode> sliceNodes = new HashSet<ASTNode>();
    private int origOffset;
    private int newOffset;
    
    public SliceExtractor(PDG pdg, Set<PDGNode> nodes) {
        origOffset = pdg.getEntryNode().getCFGEntry().getASTNode().getStartPosition();
        for (PDGNode pdfnode : nodes) {
            if (pdg.contains(pdfnode)) {
                sliceNodes.add(pdfnode.getCFGNode().getASTNode());
            }
        }
    }
    
    public void extract(MethodDeclaration methodDeclaration) {
        newOffset = methodDeclaration.getStartPosition();
        methodDeclaration.accept(this);
    }
    
    @Override
    public boolean visit(MethodDeclaration node) {
        Set<ASTNode> removeNodes = new HashSet<ASTNode>();
        for (Object obj : node.parameters()) {
            SingleVariableDeclaration param = (SingleVariableDeclaration)obj;
            if (!contains(param)) {
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
    
    /*
    @SuppressWarnings("unchecked")
    private boolean checkNode(ASTNode base, ASTNode node, List<Expression> exprs) {
        if (!contains(node)) {
            List<Expression> removeNodes = new ArrayList<Expression>();
            for (Expression expr : exprs) {
                node.setProperty(LIVE, false);
                expr.accept(this);
                boolean live = (boolean)node.getProperty(LIVE);
                if (live) {
                    if (node instanceof ExpressionStatement) {
                        continue;
                    }
                    
                    Expression newExpression = (Expression)ASTNode.copySubtree(expr.getAST(), expr);
                    ExpressionStatement newStatement = (ExpressionStatement)expr.getAST().newExpressionStatement(newExpression);
                    ASTNode parent = base.getParent();
                    
                    if (parent instanceof Block) {
                        Block block = (Block)parent;
                        for (int index = 0; index < block.statements().size(); index++) {
                            Statement st = (Statement)block.statements().get(index);
                            if (st.getStartPosition() == base.getStartPosition() && st.getLength() == base.getLength()) {
                                block.statements().add(index, newStatement);
                                return true;
                            }
                        }
                    }
                }
            }
            node.delete();
        }
        return false;
    }
    */
    
    private Statement parentStatement(ASTNode node) {
        while (node != null) {
            if (node instanceof Statement) {
                return (Statement)node;
            }
            node = node.getParent();
        }
        return null;
    }
    
    private boolean contains(ASTNode node) {
        for (ASTNode n : sliceNodes) {
            if (n.getNodeType() == node.getNodeType() &&
                n.getStartPosition() - origOffset == node.getStartPosition() - newOffset) {
                return true;
            }
        }
        return false;
    }
    
    private boolean existsSliceNode(Expression expr) {
        InsideSliceChecker checker = new InsideSliceChecker(sliceNodes, origOffset, newOffset);
        expr.accept(checker);
        return checker.existsSliceNode();
    }
    
    @SuppressWarnings("unchecked")
    private boolean checkNode0(ASTNode node, Expression expr) {
        if (contains(node)) {
            return true;
        } else {
            if (contains(expr)) {
                Expression newExpression = (Expression)ASTNode.copySubtree(expr.getAST(), expr);
                ExpressionStatement newStatement = (ExpressionStatement)expr.getAST().newExpressionStatement(newExpression);
                
                Statement base = parentStatement(node);
                ASTNode baseParent = base.getParent();
                if (baseParent instanceof Block) {
                    Block block = (Block)baseParent;
                    for (int index = 0; index < block.statements().size(); index++) {
                        Statement st = (Statement)block.statements().get(index);
                        if (st.getStartPosition() == base.getStartPosition() && st.getLength() == base.getLength()) {
                            block.statements().add(index, newStatement);
                            return false;
                        }
                    }
                }
                return existsSliceNode(expr);
           }
        }
        return false;
    }
    
    private void checkNode(ASTNode node, List<Expression> exprs) {
        boolean live = true;
        for (Expression expr : exprs) {
            live = checkNode0(node, expr) & live;
        }
        if (!live) {
            Statement st = parentStatement(node);
            st.delete();
        }
    }
    
    private void checkNode(ASTNode node, Expression expr) {
        boolean live = checkNode0(node, expr);
        if (!live) {
            Statement st = parentStatement(node);
            st.delete();
        }
    }
    
    private void checkNode(ASTNode node) {
        if (!contains(node)) {
            Statement st = parentStatement(node);
            st.delete();
        }
    }
    
    @Override
    public boolean visit(Assignment node) {
        checkNode(node, node.getRightHandSide());
        return false;
    }
    
    @Override
    public boolean visit(ArrayAccess node) {
        checkNode(node, node.getIndex());
        return false;
    }
    
    @Override
    public boolean visit(PrefixExpression node) {
        checkNode(node, node.getOperand());
        return false;
    }
    
    @Override
    public boolean visit(PostfixExpression node) {
        checkNode(node, node.getOperand());
        return false;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(MethodInvocation node) {
        checkNode(node, node.arguments());
        return false;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(SuperMethodInvocation node) {
        checkNode(node, node.arguments());
        return true;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(ClassInstanceCreation node) {
        checkNode(node, node.arguments());
        return true;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(VariableDeclarationStatement node) {
        List<VariableDeclarationFragment> removeNodes = new ArrayList<VariableDeclarationFragment>();
        for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>)node.fragments()) {
            if (!contains(frag)) {
                removeNodes.add(frag);
            }
        }
        for (VariableDeclarationFragment n : removeNodes) {
            checkNode(n, n.getInitializer());
        }
        if (node.fragments().size() == 0) {
            node.delete();
        }
        return true;
    }
    
    @Override
    public boolean visit(AssertStatement node) {
        return true;
    }
    
    @Override
    public boolean visit(BreakStatement node) {
        checkNode(node);
        return true;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(ConstructorInvocation node) {
        checkNode(node, node.arguments());
        return true;
    }
    
    @Override
    public boolean visit(ContinueStatement node) {
        checkNode(node);
        return true;
    }
    
    @Override
    public boolean visit(DoStatement node) {
        checkNode(node, node.getExpression());
        return true;
    }
    
    @Override
    public boolean visit(EnhancedForStatement node) {
        checkNode(node, node.getExpression());
        return true;
    }
    
    @Override
    public boolean visit(ExpressionStatement node) {
        checkNode(node, node.getExpression());
        return true;
    }
    
    @Override
    public boolean visit(ForStatement node) {
        checkNode(node, node.getExpression());
        return true;
    }
    
    @Override
    public boolean visit(IfStatement node) {
        checkNode(node, node.getExpression());
        return true;
    }
    
    @Override
    public boolean visit(LabeledStatement node) {
        return true;
    }
    
    @Override
    public boolean visit(ReturnStatement node) {
        checkNode(node, node.getExpression());
        return true;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(SuperConstructorInvocation node) {
        checkNode(node, node.arguments());
        return true;
    }
    
    @Override
    public boolean visit(SwitchCase node) {
        checkNode(node, node.getExpression());
        return true;
    }
    
    @Override
    public boolean visit(SwitchStatement node) {
        checkNode(node, node.getExpression());
        return true;
    }
    
    @Override
    public boolean visit(SynchronizedStatement node) {
        return true;
    }
    
    @Override
    public boolean visit(ThrowStatement node) {
        return true;
    }
    
    @Override
    public boolean visit(TryStatement node) {
        return true;
    }
    
    @Override
    public boolean visit(TypeDeclarationStatement node) {
        return true;
    }
    
    @Override
    public boolean visit(WhileStatement node) {
        checkNode(node, node.getExpression());
        return true;
    }
}
