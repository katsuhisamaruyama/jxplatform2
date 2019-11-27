/*
 *  Copyright 2018-2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGExit;
import org.jtool.eclipse.cfg.CFGMethodEntry;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGParameter;
import org.jtool.eclipse.cfg.CFGCatch;
import org.jtool.eclipse.cfg.ControlFlow;
import org.jtool.eclipse.cfg.JSpecialVarReference;
import org.jtool.eclipse.cfg.JLocalVarReference;
import org.jtool.eclipse.cfg.JReference;
import org.jtool.eclipse.graph.GraphEdge;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * Builds a CFG that corresponds to a method.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class CFGMethodBuilder {
    
    public static CFG build(JavaMethod jmethod, JInfoStore infoStore) {
        return build(jmethod, infoStore, new HashSet<JMethod>());
    }
    
    @SuppressWarnings("unchecked")
    public static CFG build(JavaMethod jmethod, JInfoStore infoStore, Set<JMethod> visited) {
        List<VariableDeclaration> params;
        if (!jmethod.isInitializer()) {
            if (jmethod.isLambda()) {
                LambdaExpression node = (LambdaExpression)jmethod.getASTNode();
                params = node.parameters();
            } else {
                MethodDeclaration node = (MethodDeclaration)jmethod.getASTNode();
                params = node.parameters();
            }
        } else {
            params = new ArrayList<VariableDeclaration>();
        }
        return build(jmethod, jmethod.getMethodBinding(), params, infoStore, visited);
    }
    
    private static CFG build(JavaMethod jmethod, IMethodBinding mbinding, List<VariableDeclaration> params,
                             JInfoStore infoStore, Set<JMethod> visited) {
        CFG cfg = new CFG();
        
        CFGMethodEntry entry;
        if (mbinding == null) {
            entry = new CFGMethodEntry(jmethod, CFGNode.Kind.initializerEntry);
        } else {
            if (mbinding.isConstructor()) {
                entry = new CFGMethodEntry(jmethod, CFGNode.Kind.constructorEntry);
            } else {
                entry = new CFGMethodEntry(jmethod, CFGNode.Kind.methodEntry);
            }
        }
        
        cfg.setStartNode(entry);
        cfg.add(entry);
        
        CFGNode tmpExit = new CFGNode();
        cfg.setEndNode(tmpExit);
        
        Set<CFGCatch> exceptionNodes = createExceptionNodes(jmethod, entry, cfg);
        
        CFGNode finalFormalInNode = createFormalIn(params, cfg, entry, entry);
        CFGNode nextNode = new CFGNode();
        
        ControlFlow entryEdge = new ControlFlow(finalFormalInNode, nextNode);
        entryEdge.setTrue();
        cfg.add(entryEdge);
        
        StatementVisitor visitor = new StatementVisitor(cfg, finalFormalInNode, nextNode, infoStore, visited);
        jmethod.getASTNode().accept(visitor);
        nextNode = visitor.getNextCFGNode();
        
        CFGExit exit;
        if (mbinding == null) {
            exit = new CFGExit(jmethod.getASTNode(), CFGNode.Kind.initializerExit);
        } else {
            if (mbinding.isConstructor()) {
                exit = new CFGExit(jmethod.getASTNode(), CFGNode.Kind.constructorExit);
            } else {
                exit = new CFGExit(jmethod.getASTNode(), CFGNode.Kind.methodExit);
            }
        }
        cfg.setEndNode(exit);
        cfg.add(exit);
        
        if (entry.getJavaMethod().isVoid() && !entry.getJavaMethod().isConstructor()) {
            if (params.size() > 0) {
                createFormalOut(params, cfg, entry, nextNode);
            }
            replace(cfg, nextNode, exit);
            replace(cfg, tmpExit, exit);
            
        } else {
            int ordinal = 0;
            if (params.size() > 0) {
                List<CFGParameter> formalOutNodes = createFormalOut(params, cfg, entry, nextNode);
                ordinal = formalOutNodes.size();
            }
            
            CFGNode formalOutNodeForReturn = createFormalOutForReturn(jmethod.getASTNode(), cfg, entry, ordinal);
            replace(cfg, nextNode, formalOutNodeForReturn);
            replace(cfg, tmpExit, formalOutNodeForReturn);
            
            ControlFlow exitEdge = new ControlFlow(formalOutNodeForReturn, exit);
            exitEdge.setTrue();
            cfg.add(exitEdge);
        }
        
        for (CFGCatch n : exceptionNodes) {
            ControlFlow exitEdge = new ControlFlow(n, exit);
            exitEdge.setTrue();
            cfg.add(exitEdge);
        }
        
        ReceiverCollector.collect(cfg);
        LocalAliasResolver.resolve(cfg);
        
        return cfg;
    }
    
    private static void replace(CFG cfg, CFGNode tmpNode, CFGNode node) {
        Set<GraphEdge> edges = new HashSet<GraphEdge>(tmpNode.getIncomingEdges());
        for (GraphEdge edge : edges) {
            edge.setDstNode(node);
        }
    }
    
    private static Set<CFGCatch> createExceptionNodes(JavaMethod jmethod, CFGMethodEntry entry, CFG cfg) {
        Set<CFGCatch> nodes = new HashSet<CFGCatch>();
        for (Type type : jmethod.getExceptionTypeNodes().values()) {
            CFGCatch exceptionNode = createExceptionNode(entry, cfg, type.resolveBinding().getTypeDeclaration());
            nodes.add(exceptionNode);
        }
        
        ExceptionTypeCollector collector = new ExceptionTypeCollector();
        for (ITypeBinding tbinding : collector.getExceptions(jmethod)) {
            CFGCatch exceptionNode = createExceptionNode(entry, cfg, tbinding);
            nodes.add(exceptionNode);
        }
        
        return nodes;
    }
    
    private static CFGCatch createExceptionNode(CFGMethodEntry entry, CFG cfg, ITypeBinding tbinding) {
        CFGCatch exceptionNode = new CFGCatch(entry.getASTNode(), CFGNode.Kind.catchSt, tbinding);
        exceptionNode.setParent(entry);
        
        entry.addExceptionNode(exceptionNode);
        cfg.add(exceptionNode);
        return exceptionNode;
    }
    
    private static CFGNode createFormalIn(List<VariableDeclaration> params, CFG cfg, CFGMethodEntry entry, CFGNode prevNode) {
        for (int ordinal = 0; ordinal < params.size(); ordinal++) {
            VariableDeclaration param = params.get(ordinal);
            CFGParameter formalInNode = new CFGParameter(param, CFGNode.Kind.formalIn, ordinal);
            formalInNode.setParent(entry);
            entry.addFormalIn(formalInNode);
            cfg.add(formalInNode);
            
            JReference def = new JLocalVarReference(param.getName(), param.resolveBinding());
            formalInNode.setDefVariable(def);
            
            JReference use = new JSpecialVarReference(param.getName(),
                    "$" + String.valueOf(ExpressionVisitor.temporaryVariableId), def.getType(), def.isPrimitiveType());
            formalInNode.setUseVariable(use);
            ExpressionVisitor.temporaryVariableId++;
            
            ControlFlow edge = new ControlFlow(prevNode, formalInNode);
            edge.setTrue();
            cfg.add(edge);
            
            prevNode = formalInNode;
        }
        return prevNode;
    }
    
    private static List<CFGParameter> createFormalOut(List<VariableDeclaration> params, CFG cfg, CFGMethodEntry entry, CFGNode nextNode) {
        List<CFGParameter> formalOuts = new ArrayList<CFGParameter>();
        for (int ordinal = 0; ordinal < params.size(); ordinal++) {
            VariableDeclaration param = params.get(ordinal);
            CFGParameter formalOutNode = new CFGParameter(param, CFGNode.Kind.formalOut, ordinal);
            formalOutNode.setParent(entry);
            entry.addFormalOut(formalOutNode);
            cfg.add(formalOutNode);
            
            JReference use = new JLocalVarReference(param.getName(), param.resolveBinding());
            formalOutNode.setUseVariable(use);
            
            JReference def = new JSpecialVarReference(param.getName(),
                    "$" + String.valueOf(ExpressionVisitor.temporaryVariableId), use.getType(), use.isPrimitiveType());
            formalOutNode.setDefVariable(def);
            ExpressionVisitor.temporaryVariableId++;
            
            replace(cfg, nextNode, formalOutNode);
            ControlFlow edge = new ControlFlow(formalOutNode, nextNode);
            edge.setTrue();
            cfg.add(edge);
            formalOuts.add(formalOutNode);
        }
        return formalOuts;
    }
    
    private static CFGNode createFormalOutForReturn(ASTNode node, CFG cfg, CFGMethodEntry entry, int ordinal) {
        CFGParameter formalOutNode = new CFGParameter(node, CFGNode.Kind.formalOut, ordinal);
        formalOutNode.setParent(entry);
        entry.addFormalOut(formalOutNode);
        cfg.add(formalOutNode);
        
        String returnType = entry.getJavaMethod().getReturnType();
        boolean isPrimitiveType = entry.getJavaMethod().isPrimitiveReturnType();
        
        JReference def = new JSpecialVarReference(node, "$" + String.valueOf(ExpressionVisitor.temporaryVariableId), returnType, isPrimitiveType);
        formalOutNode.addDefVariable(def);
        ExpressionVisitor.temporaryVariableId++;
        
        JReference use = new JSpecialVarReference(node, "$_", returnType, isPrimitiveType);
        formalOutNode.addUseVariable(use);
        
        return formalOutNode;
    }
}
