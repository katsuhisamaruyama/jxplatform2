/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGExit;
import org.jtool.eclipse.cfg.CFGMethodEntry;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGParameter;
import org.jtool.eclipse.cfg.ControlFlow;
import org.jtool.eclipse.cfg.JInvisibleVarReference;
import org.jtool.eclipse.cfg.JLocalVarReference;
import org.jtool.eclipse.cfg.JReference;
import org.jtool.eclipse.graph.GraphEdge;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.eclipse.jdt.core.dom.ASTNode;
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
        ExpressionVisitor.paramNumber = 1;
        
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
        
        if (entry.getJavaMethod().isVoid()) {
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
        
        PrimaryCompactor.compact(cfg);
        LocalAliasResolver.resolve(cfg);
        
        return cfg;
    }
    
    private static void replace2(CFG cfg, CFGNode tmpNode, CFGNode node) {
        Set<GraphEdge> edges = new HashSet<GraphEdge>(tmpNode.getIncomingEdges());
        for (GraphEdge edge : edges) {
            System.out.println(edge.toString());
            
            edge.setDstNode(node);
        }
    }
    
    private static void replace(CFG cfg, CFGNode tmpNode, CFGNode node) {
        Set<GraphEdge> edges = new HashSet<GraphEdge>(tmpNode.getIncomingEdges());
        for (GraphEdge edge : edges) {
            edge.setDstNode(node);
        }
    }
    
    private static CFGNode createFormalIn(List<VariableDeclaration> params, CFG cfg, CFGMethodEntry entry, CFGNode prevNode) {
        for (int ordinal = 0; ordinal < params.size(); ordinal++) {
            VariableDeclaration param = params.get(ordinal);
            CFGParameter formalInNode = new CFGParameter(param, CFGNode.Kind.formalIn, ordinal);
            formalInNode.setParent(entry);
            entry.addFormalIn(formalInNode);
            cfg.add(formalInNode);
            
            JReference jvout = new JLocalVarReference(param.getName(), param.resolveBinding());
            formalInNode.setDefVariable(jvout);
            
            JReference jvin = new JInvisibleVarReference(param.getName(), "$" + String.valueOf(ExpressionVisitor.paramNumber), jvout.getType(), jvout.isPrimitiveType());
            formalInNode.setUseVariable(jvin);
            ExpressionVisitor.paramNumber++;
            
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
            if (!param.resolveBinding().getType().isPrimitive()) {
                CFGParameter formalOutNode = new CFGParameter(param, CFGNode.Kind.formalOut, ordinal);
                formalOutNode.setParent(entry);
                entry.addFormalOut(formalOutNode);
                cfg.add(formalOutNode);
                
                JReference jvout = new JLocalVarReference(param.getName(), param.resolveBinding());
                formalOutNode.setUseVariable(jvout);
                
                JReference jvin = new JInvisibleVarReference(param.getName(), "$" + String.valueOf(ExpressionVisitor.paramNumber), jvout.getType(), jvout.isPrimitiveType());
                formalOutNode.setDefVariable(jvin);
                ExpressionVisitor.paramNumber++;
                
                replace(cfg, nextNode, formalOutNode);
                ControlFlow edge = new ControlFlow(formalOutNode, nextNode);
                edge.setTrue();
                cfg.add(edge);
                formalOuts.add(formalOutNode);
            }
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
        
        JReference jvout = new JInvisibleVarReference(node, "$" + String.valueOf(ExpressionVisitor.paramNumber), returnType, isPrimitiveType);
        formalOutNode.addDefVariable(jvout);
        ExpressionVisitor.paramNumber++;
        
        JReference jvin = new JInvisibleVarReference(node, "$_", returnType, isPrimitiveType);
        formalOutNode.addUseVariable(jvin);
        return formalOutNode;
    }
}
