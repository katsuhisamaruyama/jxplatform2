/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGMerge;
import org.jtool.eclipse.cfg.CFGMethodEntry;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGParameter;
import org.jtool.eclipse.cfg.CFGStatement;
import org.jtool.eclipse.cfg.ControlFlow;
import org.jtool.eclipse.cfg.JVirtualReference;
import org.jtool.eclipse.cfg.JLocalReference;
import org.jtool.eclipse.cfg.JReference;
import org.jtool.eclipse.cfg.JMethod;
import org.jtool.eclipse.graph.GraphEdge;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.IVariableBinding;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Stack;

/**
 * Visits AST nodes within a statement.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @see org.eclipse.jdt.core.dom.Statement
 * 
 * Statement:
 * 
 *   Block
 *   EmptyStatement
 *   TypeDeclarationStatement (not needed to visit because it was already visited under model creation)
 *   ExpressionStatement
 *   VariableDeclarationStatement
 *   ConstructorInvocation
 *   SuperConstructorInvocation
 *   IfStatement
 *   SwitchStatement
 *   SwitchCase
 *   WhileStatement
 *   DoStatement,
 *   ForStatement
 *   EnhancedForStatement
 *   BreakStatement
 *   ContinueStatement
 *   ReturnStatement
 *   AssertStatement
 *   LabeledStatement  
 *   SynchronizedStatement
 *   ThrowStatement
 *   TryStatement
 *   
 *   @author Katsuhisa Maruyama
 */
public class StatementVisitor extends ASTVisitor {
    
    protected CFG cfg;
    protected CFGNode prevNode;
    protected CFGNode nextNode;
    
    private Stack<CFGNode> blockEntries = new Stack<CFGNode>();
    private Stack<CFGNode> blockExits = new Stack<CFGNode>();
    
    private Set<Label> labels = new HashSet<Label>();
    
    private Set<JMethod> visitedMethods;
    
    protected StatementVisitor(CFG cfg, CFGNode prevNode, CFGNode nextNode, Set<JMethod> visitedMethods) {
         this.cfg = cfg;
         this.prevNode = prevNode;
         this.nextNode = nextNode;
         this.visitedMethods = visitedMethods;
    }
    
    protected CFGNode getNextCFGNode() {
        return nextNode;
    }
    
    private ControlFlow createFlow(CFGNode src, CFGNode dst) {
        ControlFlow edge = new ControlFlow(src, dst);
        cfg.add(edge);
        return edge;
    }
    
    private void reconnect(CFGNode node) {
        Set<GraphEdge> edges = new HashSet<GraphEdge>(nextNode.getIncomingEdges());
        for (GraphEdge edge : edges) {
            edge.setDstNode(node);
        }
        
        cfg.add(node);
        nextNode.clear();
        prevNode = node;
    }
    
    @Override
    public boolean visit(Block node) {
        return true;
    }
    
    @Override
    public boolean visit(EmptyStatement node) {
        CFGStatement emptyNode = new CFGStatement(node, CFGNode.Kind.emptySt);
        reconnect(emptyNode);
        
        ControlFlow edge = createFlow(emptyNode, nextNode);
        edge.setTrue();
        return false;
    }
    
    @Override
    public boolean visit(TypeDeclarationStatement node) {
        return false;
    }
    
    @Override
    public boolean visit(ExpressionStatement node) {
        CFGStatement expNode = new CFGStatement(node, CFGNode.Kind.assignment);
        reconnect(expNode);
        
        Expression expression = node.getExpression();
        ExpressionVisitor visitor = new ExpressionVisitor(cfg, expNode, visitedMethods);
        expression.accept(visitor);
        CFGNode curNode = visitor.getExitNode();
        
        ControlFlow edge = createFlow(curNode, nextNode);
        edge.setTrue();
        return false;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(VariableDeclarationStatement node) {
        for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>)node.fragments()) {
            CFGStatement declNode = new CFGStatement(node, CFGNode.Kind.assignment);
            reconnect(declNode);
            
            ExpressionVisitor visitor = new ExpressionVisitor(cfg, declNode, visitedMethods);
            frag.accept(visitor);
            CFGNode curNode = visitor.getExitNode();
            
            ControlFlow edge = createFlow(curNode, nextNode);
            edge.setTrue();
        }
        return false;
    }
    
    @Override
    public boolean visit(ConstructorInvocation node) {
        CFGStatement invNode = new CFGStatement(node, CFGNode.Kind.assignment);
        reconnect(invNode);
        
        ExpressionVisitor visitor = new ExpressionVisitor(cfg, invNode, visitedMethods);
        node.accept(visitor);
        CFGNode curNode = visitor.getExitNode();
        
        ControlFlow edge = createFlow(curNode, nextNode);
        edge.setTrue();
        return false;
    }
    
    @Override
    public boolean visit(SuperConstructorInvocation node) {
        CFGStatement invNode = new CFGStatement(node, CFGNode.Kind.assignment);
        reconnect(invNode);
        
        ExpressionVisitor visitor = new ExpressionVisitor(cfg, invNode, visitedMethods);
        node.accept(visitor);
        CFGNode curNode = visitor.getExitNode();
        
        ControlFlow edge = createFlow(curNode, nextNode);
        edge.setTrue();
        return false;
    }
    
    @Override
    public boolean visit(IfStatement node) {
        CFGStatement ifNode = new CFGStatement(node, CFGNode.Kind.ifSt);
        reconnect(ifNode);
        
        Expression condition = node.getExpression();
        ExpressionVisitor condVisitor = new ExpressionVisitor(cfg, ifNode, visitedMethods);
        condition.accept(condVisitor);
        CFGNode curNode = condVisitor.getExitNode();
        
        ControlFlow trueEdge = createFlow(curNode, nextNode);
        trueEdge.setTrue();
        
        Statement thenSt = node.getThenStatement();
        thenSt.accept(this);
        
        ControlFlow trueMergeEdge = cfg.getFlow(prevNode, nextNode);
        ControlFlow falseEdge = createFlow(curNode, nextNode);
        falseEdge.setFalse();
        
        Statement elseSt = node.getElseStatement();
        if (elseSt != null) {
            elseSt.accept(this);
            if (trueMergeEdge != null) {
                trueMergeEdge.setDstNode(nextNode);
            }
        }
        CFGMerge mergeNode = new CFGMerge(node, ifNode);
        reconnect(mergeNode);
        
        ControlFlow edge = createFlow(mergeNode, nextNode);
        edge.setTrue();
        return false;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(SwitchStatement node) {
        SwitchNode switchNode = new SwitchNode(node, CFGNode.Kind.switchSt);
        reconnect(switchNode);
        
        Expression condition = node.getExpression();
        ExpressionVisitor condVisitor = new ExpressionVisitor(cfg, switchNode, visitedMethods);
        condition.accept(condVisitor);
        CFGNode curNode = condVisitor.getExitNode();
        
        ControlFlow caseEdge = createFlow(curNode, nextNode);
        caseEdge.setTrue();
        
        CFGNode exitNode = new CFGNode();
        blockEntries.push(switchNode);
        blockExits.push(exitNode);
        
        List<Statement> remaining = new ArrayList<Statement>();
        for (Statement statement : (List<Statement>)node.statements()) {
            remaining.add(statement);
        }
        for (Statement statement : (List<Statement>)node.statements()) {
            remaining.remove(0);
            
            if (statement instanceof SwitchCase) {  
                visitSwitchCase((SwitchCase)statement, switchNode, remaining);
            }
        }
        if (switchNode.hasDefault()) {
            CFGNode successor = switchNode.getSuccessorOfDefault();
            List<GraphEdge> nextEdges = new ArrayList<GraphEdge>();
            for (GraphEdge edge : nextNode.getIncomingEdges()) {
                nextEdges.add(edge);
            }
            List<GraphEdge> incomingEdges = new ArrayList<GraphEdge>();
            for (GraphEdge edge : switchNode.getDefaultStartNode().getIncomingEdges()) {
                incomingEdges.add(edge);
            }
            List<GraphEdge> outgoingEdges = new ArrayList<GraphEdge>();
            for (GraphEdge edge : successor.getIncomingEdges()) {
                outgoingEdges.add(edge);
            }
            for (GraphEdge edge : nextEdges) {
                ControlFlow flow = (ControlFlow)edge;
                flow.setDstNode(switchNode.getDefaultStartNode());
            }
            for (GraphEdge edge : incomingEdges) {
                ControlFlow flow = (ControlFlow)edge;
                flow.setDstNode(successor);
            }
            for (GraphEdge edge : outgoingEdges) {
                ControlFlow flow = (ControlFlow)edge;
                if (flow.isFalse()) {
                    cfg.remove(flow);
                } else {
                    flow.setDstNode(nextNode);
                } 
            }
        }
        
        nextNode.addIncomingEdges(exitNode.getIncomingEdges());
        CFGMerge mergeNode = new CFGMerge(node, switchNode);
        reconnect(mergeNode);
        
        ControlFlow falseEdge = createFlow(switchNode, mergeNode);
        falseEdge.setFalse();
        
        blockEntries.pop();
        blockExits.pop();
        
        ControlFlow edge = createFlow(mergeNode, nextNode);
        edge.setTrue();
        return false;
    }
    
    private void visitSwitchCase(SwitchCase node, SwitchNode switchNode, List<Statement> remaining)  {
        CFGStatement caseNode;
        if (!node.isDefault()) {
            caseNode = new CFGStatement(node, CFGNode.Kind.switchCaseSt);
            reconnect(caseNode);
            
            Expression condition = node.getExpression();
            ExpressionVisitor condVisitor = new ExpressionVisitor(cfg, caseNode, visitedMethods);
            condition.accept(condVisitor);
            caseNode.addDefVariables(switchNode.getDefVariables());
            caseNode.addUseVariables(switchNode.getUseVariables());
            CFGNode curNode = condVisitor.getExitNode();
            
            ControlFlow edge = createFlow(curNode, nextNode);
            edge.setTrue();
        } else {
            caseNode = new CFGStatement(node, CFGNode.Kind.switchDefaultSt);
            reconnect(caseNode);
            
            ControlFlow edge = createFlow(caseNode, nextNode);
            edge.setTrue();
            switchNode.setDefaultStartNode(caseNode);
        }
        for (Statement statement : remaining) {
            if (statement instanceof SwitchCase) {
                break;
            }
            statement.accept(this); 
        }
        
        ControlFlow edge = createFlow(caseNode, nextNode);
        edge.setFalse();
        if (node.isDefault()) {
            switchNode.setDefaultEndNode(prevNode);
        }
    }
    
    @Override
    public boolean visit(WhileStatement node) {
        CFGStatement whileNode = new CFGStatement(node, CFGNode.Kind.whileSt);
        reconnect(whileNode);
        
        Expression condition = node.getExpression();
        ExpressionVisitor condVisitor = new ExpressionVisitor(cfg, whileNode, visitedMethods);
        condition.accept(condVisitor);
        CFGNode curNode = condVisitor.getExitNode();
        
        ControlFlow trueEdge = createFlow(curNode, nextNode);
        trueEdge.setTrue();
        
        CFGNode entryNode = condVisitor.getEntryNode();
        CFGNode exitNode = new CFGNode();
        blockEntries.push(entryNode);
        blockExits.push(exitNode);
        Statement body = node.getBody();
        body.accept(this);
        
        ControlFlow loopbackEdge = cfg.getFlow(prevNode, nextNode);
        if (loopbackEdge != null) {
            loopbackEdge.setDstNode(entryNode);
            loopbackEdge.setLoopBack(whileNode);
        }
         
        ControlFlow falseEdge = createFlow(whileNode, nextNode);
        falseEdge.setFalse();
        prevNode = whileNode;
        nextNode.addIncomingEdges(exitNode.getIncomingEdges());
        
        blockEntries.pop();
        blockExits.pop();
        return false;
    }
    
    @Override
    public boolean visit(DoStatement node) {
        CFGNode entryNode = new CFGNode();
        CFGNode exitNode = new CFGNode();
        blockEntries.push(entryNode);
        blockExits.push(exitNode);
        
        ControlFlow entryEdge = cfg.getFlow(prevNode, nextNode);
        Statement body = node.getBody();
        body.accept(this);
        
        nextNode.addIncomingEdges(entryNode.getIncomingEdges());
        CFGStatement doNode = new CFGStatement(node, CFGNode.Kind.doSt);
        reconnect(doNode);
        
        Expression condition = node.getExpression();
        ExpressionVisitor condVisitor = new ExpressionVisitor(cfg, doNode, visitedMethods);
        condition.accept(condVisitor);
        CFGNode curNode = condVisitor.getExitNode();
        
        ControlFlow loopbackEdge = createFlow(curNode, entryEdge.getDstNode());
        loopbackEdge.setTrue();
        loopbackEdge.setLoopBack(doNode);
        
        ControlFlow falseEdge = createFlow(doNode, nextNode);
        falseEdge.setFalse();
        nextNode.addIncomingEdges(exitNode.getIncomingEdges());
        
        blockEntries.pop();
        blockExits.pop();
        return false;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(ForStatement node) {
        for (Expression initializer : (List<Expression>)node.initializers()) {
            CFGStatement initNode = new CFGStatement(node, CFGNode.Kind.assignment);
            ExpressionVisitor initVisitor = new ExpressionVisitor(cfg, initNode, visitedMethods);
            initializer.accept(initVisitor);
            CFGNode curNode = initVisitor.getExitNode();
            reconnect(initNode);
            
            ControlFlow edge = createFlow(curNode, nextNode);
            edge.setTrue();
        }
        
        CFGStatement forNode = new CFGStatement(node, CFGNode.Kind.forSt);
        CFGNode entryNode;
        Expression condition = node.getExpression();
        if (condition != null) {
            ExpressionVisitor condVisitor = new ExpressionVisitor(cfg, forNode, visitedMethods);
            condition.accept(condVisitor);
            CFGNode curNode = condVisitor.getExitNode();
            reconnect(forNode);
            
            ControlFlow edge = createFlow(curNode, nextNode);
            edge.setTrue();
            entryNode = condVisitor.getEntryNode();
        } else {
            ControlFlow edge = createFlow(forNode, nextNode);
            edge.setTrue();
            entryNode = forNode;
        }
        
        CFGNode exitNode = new CFGNode();
        blockEntries.push(entryNode);
        blockExits.push(exitNode);
        Statement body = node.getBody();
        body.accept(this);
        for (Expression update : (List<Expression>)node.updaters()) {
            CFGStatement updateNode = new CFGStatement(update, CFGNode.Kind.assignment);
            ExpressionVisitor updateVisitor = new ExpressionVisitor(cfg, updateNode, visitedMethods);
            update.accept(updateVisitor);
            CFGNode curNode = updateVisitor.getExitNode();
            reconnect(updateNode);
            
            ControlFlow edge = createFlow(curNode, nextNode);
            edge.setTrue();
        }
        
        ControlFlow loopbackEdge = cfg.getFlow(prevNode, nextNode);
        if (loopbackEdge != null) {
            loopbackEdge.setDstNode(entryNode);
            loopbackEdge.setLoopBack(forNode);
        }
        
        ControlFlow falseEdge = createFlow(forNode, nextNode);
        falseEdge.setFalse();
        prevNode = forNode;
        nextNode.addIncomingEdges(exitNode.getIncomingEdges());
        
        blockEntries.pop();
        blockExits.pop();
        return false;
    }
    
    @Override
    public boolean visit(EnhancedForStatement node) {
        CFGStatement forNode = new CFGStatement(node, CFGNode.Kind.assignment);
        reconnect(forNode);
        
        SingleVariableDeclaration parameter = node.getParameter();
        ExpressionVisitor paramVisitor = new ExpressionVisitor(cfg, forNode, visitedMethods);
        parameter.accept(paramVisitor);
        Expression expression = node.getExpression();
        ExpressionVisitor exprVisitor = new ExpressionVisitor(cfg, forNode, visitedMethods);
        expression.accept(exprVisitor);
        CFGNode curNode = exprVisitor.getExitNode();
        
        ControlFlow edge = createFlow(curNode, nextNode);
        edge.setTrue();
        
        CFGNode exitNode = new CFGNode();
        blockEntries.push(curNode);
        blockExits.push(exitNode);
        Statement body = node.getBody();
        body.accept(this);
        
        ControlFlow loopbackEdge = cfg.getFlow(prevNode, nextNode);
        if (loopbackEdge != null) {
            loopbackEdge.setDstNode(curNode);
            loopbackEdge.setLoopBack(forNode);
        }
        
        ControlFlow falseEdge = createFlow(forNode, nextNode);
        falseEdge.setFalse();
        prevNode = forNode;
        nextNode.addIncomingEdges(exitNode.getIncomingEdges());
        
        blockEntries.pop();
        blockExits.pop();
        return false;
    }
    
    @Override
    public boolean visit(BreakStatement node) {
        CFGStatement breakNode = new CFGStatement(node, CFGNode.Kind.breakSt);
        reconnect(breakNode);
        
        CFGNode jumpNode;
        if (node.getLabel() != null) {
            String name = node.getLabel().getFullyQualifiedName();
            jumpNode = getLabel(name).getNode();
        } else {
            jumpNode = (CFGNode)blockEntries.peek();
            // Goes to the entry point and moves its false-successor immediately.
            // Not go to the exit point directly according to the Java specification.
        }
        if (jumpNode != null) {
            ControlFlow edge = createFlow(breakNode, jumpNode);
            edge.setTrue();
            edge = createFlow(breakNode, nextNode);
            edge.setFallThrough();
        }
        return false;
    }
    
    @Override
    public boolean visit(ContinueStatement node) {
        CFGStatement continueNode = new CFGStatement(node, CFGNode.Kind.continueSt);
        reconnect(continueNode);
        
        CFGNode jumpNode;
        if (node.getLabel() != null) {
            String name = node.getLabel().getFullyQualifiedName();
            jumpNode = getLabel(name).getNode();
        } else {
            jumpNode = (CFGNode)blockEntries.peek();
        }
        if (jumpNode != null) {
            ControlFlow edge = createFlow(continueNode, jumpNode);
            edge.setTrue();
            edge = createFlow(continueNode, nextNode);
            edge.setFallThrough();
        }
        return false;
    }
    
    @Override
    public boolean visit(ReturnStatement node) {
        CFGStatement returnNode = new CFGStatement(node, CFGNode.Kind.returnSt);
        reconnect(returnNode);
        
        CFGNode curNode = returnNode;
        Expression expression = node.getExpression();
        if (expression != null) {
            ExpressionVisitor exprVisitor = new ExpressionVisitor(cfg, returnNode, visitedMethods);
            expression.accept(exprVisitor);
            CFGMethodEntry methodNode = (CFGMethodEntry)cfg.getStartNode();
            String type = methodNode.getReturnType();
            boolean primitive = methodNode.isPrimitiveType();
            JReference jvar = new JVirtualReference(methodNode.getASTNode(), "$_", type, primitive);
            returnNode.addDefVariable(jvar);
            curNode = exprVisitor.getExitNode();
        }
        
        ControlFlow trueEdge = createFlow(curNode, cfg.getEndNode());
        trueEdge.setTrue();
        
        ControlFlow fallEdge = createFlow(curNode, nextNode);
        fallEdge.setFallThrough();
        return false;
    }
    
    @Override
    public boolean visit(AssertStatement node) {
        CFGStatement assertNode = new CFGStatement(node, CFGNode.Kind.assignment);
        reconnect(assertNode);
        
        Expression expression = node.getExpression();
        ExpressionVisitor exprVisitor = new ExpressionVisitor(cfg, assertNode, visitedMethods);
        expression.accept(exprVisitor);
        CFGNode curNode = exprVisitor.getExitNode();
        Expression message = node.getMessage();
        if (message != null) {
            ExpressionVisitor mesgVisitor = new ExpressionVisitor(cfg, assertNode, visitedMethods);
            message.accept(mesgVisitor);
            curNode = mesgVisitor.getExitNode();
        }
        
        ControlFlow edge = createFlow(curNode, nextNode);
        edge.setTrue();
        return false;
    }
    
    @Override
    public boolean visit(LabeledStatement node) {
        CFGStatement labelNode = new CFGStatement(node, CFGNode.Kind.labelSt);
        reconnect(labelNode);
        
        ControlFlow trueEdge = createFlow(labelNode, nextNode);
        trueEdge.setTrue();
        
        String name = node.getLabel().getFullyQualifiedName();
        labels.add(new Label(name, labelNode));
        Statement body = node.getBody();
        body.accept(this);
        
        ControlFlow jumpEdge = createFlow(labelNode, cfg.getEndNode());
        jumpEdge.setTrue();
        return false;
    }
    
    @Override
    public boolean visit(ThrowStatement node) {
        CFGStatement throwNode = new CFGStatement(node, CFGNode.Kind.throwSt);
        reconnect(throwNode);
        
        Expression expression = node.getExpression();
        ExpressionVisitor exprVisitor = new ExpressionVisitor(cfg, throwNode, visitedMethods);
        expression.accept(exprVisitor);
        CFGNode curNode = exprVisitor.getExitNode();
        
        ControlFlow trueEdge = createFlow(curNode, cfg.getEndNode());
        trueEdge.setTrue();
        
        ControlFlow fallEdge = createFlow(curNode, nextNode);
        fallEdge.setFalse();
        return false;
    }
    
    @Override
    public boolean visit(SynchronizedStatement node) {
        CFGStatement syncNode = new CFGStatement(node, CFGNode.Kind.synchronizedSt);
        reconnect(syncNode);
        
        Expression expression = node.getExpression();
        ExpressionVisitor exprVisitor = new ExpressionVisitor(cfg, syncNode, visitedMethods);
        expression.accept(exprVisitor);
        CFGNode curNode = exprVisitor.getExitNode();
        
        ControlFlow trueEdge = createFlow(curNode, nextNode);
        trueEdge.setTrue();
        
        Statement body = node.getBody();
        body.accept(this);
        return false;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean visit(TryStatement node) {
        TryNode tryNode = new TryNode(node, CFGNode.Kind.trySt);
        reconnect(tryNode);
        
        ControlFlow edge = createFlow(tryNode, nextNode);
        edge.setTrue();
        
        Statement body = node.getBody();
        body.accept(this);
        CFGMerge mergeNode = new CFGMerge(node, tryNode);
        reconnect(mergeNode);
        
        for (CatchClause clause : (List<CatchClause>)node.catchClauses()) { 
            visitCatchClause(tryNode, clause, mergeNode);
        }
        Block finallyBlock = node.getFinally();
        if (finallyBlock != null) {
            visitFinallyBlock(tryNode, finallyBlock, mergeNode);
        }
        
        ControlFlow endEdge = createFlow(mergeNode, cfg.getEndNode());
        endEdge.setTrue();
        return false;
    }
    
    private void visitCatchClause(TryNode tryNode, CatchClause node, CFGMerge mergeNode) {
        CatchNode catchNode = new CatchNode(node, CFGNode.Kind.catchSt);
        tryNode.addCatchClause(catchNode);
        reconnect(catchNode);
        
        CFGParameter paramNode = new CFGParameter(node.getException(), CFGNode.Kind.formalIn, 0);
        reconnect(paramNode);
        
        IVariableBinding vbinding = node.getException().resolveBinding();
        JReference jvar = new JLocalReference(node.getException(), vbinding);
        paramNode.addDefVariable(jvar);
        JReference jvin = new JVirtualReference(node.getException(), "$" + vbinding.getName(), vbinding);
        paramNode.addUseVariable(jvin);
        
        ControlFlow trueEdge = createFlow(paramNode, nextNode);
        trueEdge.setTrue();
        
        Statement body = node.getBody();
        body.accept(this);
        reconnect(mergeNode);
    }
    
    private void visitFinallyBlock(TryNode tryNode, Block block, CFGMerge mergeNode) {
        CFGStatement finallyNode = new CFGStatement(block, CFGNode.Kind.finallySt);
        tryNode.setFinallyBlock(finallyNode);
        reconnect(finallyNode);
        
        ControlFlow truEdge = createFlow(finallyNode, nextNode);
        truEdge.setTrue();
        
        block.accept(this);
        reconnect(mergeNode);
    }
    
    class Label {
        String name = "";
        CFGNode node;
        
        Label(String name, CFGNode node) {
            this.name = name;
            this.node = node;
        }
        
        CFGNode getNode() {
            return node;
        }
    }
    
    private Label getLabel(String name) {
        for (Label label : labels) {
            if (label.name.compareTo(name) == 0) {
                return label;
            }
        }
        return null;
    }
}
