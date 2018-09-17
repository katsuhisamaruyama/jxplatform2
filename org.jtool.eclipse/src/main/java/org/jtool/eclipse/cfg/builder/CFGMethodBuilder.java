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
import org.jtool.eclipse.cfg.CFGStore;
import org.jtool.eclipse.cfg.ControlFlow;
import org.jtool.eclipse.cfg.JVirtualReference;
import org.jtool.eclipse.cfg.JLocalReference;
import org.jtool.eclipse.cfg.JReference;
import org.jtool.eclipse.cfg.JMethod;
import org.jtool.eclipse.graph.GraphEdge;
import org.jtool.eclipse.javamodel.JavaMethod;
import static org.jtool.eclipse.javamodel.JavaElement.QualifiedNameSeparator;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.Initializer;
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
    
    public static CFG build(JavaMethod jmethod) {
        return build(jmethod, new HashSet<JMethod>());
    }
    
    @SuppressWarnings("unchecked")
    public static CFG build(JavaMethod jmethod, Set<JMethod> visitedMethods) {
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
        
        IMethodBinding mbinding = jmethod.getMethodBinding();
        String name = jmethod.getName();
        String sig = jmethod.getSignature();
        String fqn = jmethod.getQualifiedName();
        String className = jmethod.getDeclaringClass().getQualifiedName();
        return build(jmethod.getASTNode(), mbinding, params, name, sig, fqn, className, visitedMethods);
    }
    
    @SuppressWarnings("unchecked")
    public static CFG build(MethodDeclaration node) {
        ITypeBinding tbinding = node.resolveBinding().getDeclaringClass();
        IMethodBinding mbinding = node.resolveBinding().getMethodDeclaration();
        String name = mbinding.getName();
        String sig = JavaMethod.getSignature(mbinding);
        String className = tbinding.getTypeDeclaration().getQualifiedName();
        String fqn = className + QualifiedNameSeparator + sig;
        return build(node, mbinding, node.parameters(), name, sig, fqn, className, new HashSet<JMethod>());
    }
    
    public static CFG build(Initializer node) {
        ITypeBinding tbinding = JReference.findEnclosingClass(node).getTypeDeclaration();
        String name = JavaMethod.InitializerName;
        String className = tbinding.getTypeDeclaration().getQualifiedName();
        String fqn = className + QualifiedNameSeparator + name;
        return build(node, null, null, name, name, fqn, className, new HashSet<JMethod>());
    }
    
    @SuppressWarnings("unchecked")
    public static CFG build(LambdaExpression node) {
        ITypeBinding tbinding = node.resolveTypeBinding().getTypeDeclaration();
        IMethodBinding mbinding = tbinding.getFunctionalInterfaceMethod().getMethodDeclaration();
        String name = mbinding.getName();
        String sig = JavaMethod.getSignature(mbinding);
        String className = tbinding.getTypeDeclaration().getQualifiedName() + "$" + String.valueOf(CFGStore.getInstance().size());
        String fqn = className + QualifiedNameSeparator + sig;
        return build(node, mbinding, node.parameters(), name, sig, fqn, className, new HashSet<JMethod>());
    }
    
    private static CFG build(ASTNode node, IMethodBinding mbinding,
            List<VariableDeclaration> params, String name, String sig, String fqn, String className, Set<JMethod> visitedMethods) {
        CFG cfg = new CFG();
        ExpressionVisitor.paramNumber = 1;
        
        CFGMethodEntry entry;
        if (mbinding == null) {
            entry = new CFGMethodEntry(node, CFGNode.Kind.initializerEntry, name, sig, fqn, className);
            entry.setReturnType("void");
            entry.setPrimitiveType(false);
        } else {
            if (mbinding.isConstructor()) {
                entry = new CFGMethodEntry(node, CFGNode.Kind.constructorEntry, name, sig, fqn, className);
                entry.setReturnType(mbinding.getReturnType().getTypeDeclaration().getQualifiedName());
                entry.setPrimitiveType(mbinding.getReturnType().isPrimitive());
            } else {
                entry = new CFGMethodEntry(node, CFGNode.Kind.methodEntry, name, sig, fqn, className);
                entry.setReturnType(mbinding.getReturnType().getTypeDeclaration().getQualifiedName());
                entry.setPrimitiveType(mbinding.getReturnType().isPrimitive());
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
        
        StatementVisitor visitor = new StatementVisitor(cfg, finalFormalInNode, nextNode, visitedMethods);
        node.accept(visitor);
        nextNode = visitor.getNextCFGNode();
        
        CFGNode formalOutNodeForReturn = createFormalOutForReturn(node, cfg, entry);
        replace(cfg, nextNode, formalOutNodeForReturn);
        replace(cfg, tmpExit, formalOutNodeForReturn);
        
        List<CFGParameter> formalOutNodes = createFormalOut(params, cfg, entry, formalOutNodeForReturn);
        
        CFGExit exit;
        if (mbinding == null) {
            exit = new CFGExit(node, CFGNode.Kind.initializerExit);
        } else {
            if (mbinding.isConstructor()) {
                exit = new CFGExit(node, CFGNode.Kind.constructorExit);
            } else {
                exit = new CFGExit(node, CFGNode.Kind.methodExit);
            }
        }
        cfg.setEndNode(exit);
        cfg.add(exit);
        
        if (entry.isVoidType()) {
            if (formalOutNodes.size() > 0) {
                replace(cfg, formalOutNodeForReturn, formalOutNodes.get(0));
                
                ControlFlow exitEdge = new ControlFlow(formalOutNodes.get(formalOutNodes.size() - 1), exit);
                exitEdge.setTrue();
                cfg.add(exitEdge);
            } else {
                replace(cfg, formalOutNodeForReturn, exit);
            }
        } else {
            
            if (formalOutNodes.size() > 0) {
                ControlFlow exitEdge = new ControlFlow(formalOutNodes.get(formalOutNodes.size() - 1), exit);
                exitEdge.setTrue();
                cfg.add(exitEdge);
            } else {
                ControlFlow exitEdge = new ControlFlow(formalOutNodeForReturn, exit);
                exitEdge.setTrue();
                cfg.add(exitEdge);
            }
        }
        return cfg;
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
            
            JReference jvout = new JLocalReference(param, param.resolveBinding());
            formalInNode.setDefVariable(jvout);
            
            JReference jvin = new JVirtualReference(param, "$" + String.valueOf(ExpressionVisitor.paramNumber), jvout.getType(), jvout.isPrimitiveType());
            formalInNode.setUseVariable(jvin);
            ExpressionVisitor.paramNumber++;
            
            ControlFlow edge = new ControlFlow(prevNode, formalInNode);
            edge.setTrue();
            cfg.add(edge);
            
            prevNode = formalInNode;
        }
        return prevNode;
    }
    
    private static List<CFGParameter> createFormalOut(List<VariableDeclaration> params, CFG cfg, CFGMethodEntry entry, CFGNode prevNode) {
        List<CFGParameter> formalOuts = new ArrayList<CFGParameter>();
        for (int ordinal = 0; ordinal < params.size(); ordinal++) {
            VariableDeclaration param = params.get(ordinal);
            if (!param.resolveBinding().getType().isPrimitive()) {
                CFGParameter formalOutNode = new CFGParameter(param, CFGNode.Kind.formalOut, ordinal);
                formalOutNode.setParent(entry);
                entry.addFormalOut(formalOutNode);
                cfg.add(formalOutNode);
                
                JReference jvout = new JLocalReference(param, param.resolveBinding());
                formalOutNode.setUseVariable(jvout);
                
                JReference jvin = new JVirtualReference(param, "$" + String.valueOf(ExpressionVisitor.paramNumber), jvout.getType(), jvout.isPrimitiveType());
                formalOutNode.setDefVariable(jvin);
                ExpressionVisitor.paramNumber++;
                
                ControlFlow edge = new ControlFlow(prevNode, formalOutNode);
                edge.setTrue();
                cfg.add(edge);
                
                prevNode = formalOutNode;
                formalOuts.add(formalOutNode);
            }
        }
        return formalOuts;
    }
    
    private static CFGNode createFormalOutForReturn(ASTNode node, CFG cfg, CFGMethodEntry entry) {
        CFGParameter formalOutNode = new CFGParameter(node, CFGNode.Kind.formalOut, 0);
        if (!entry.isVoidType()) {
            formalOutNode.setParent(entry);
            entry.addFormalOut(formalOutNode);
            cfg.add(formalOutNode);
            
            JReference jvout = new JVirtualReference(node, "$" + String.valueOf(ExpressionVisitor.paramNumber), entry.getReturnType(), entry.isPrimitiveType());
            formalOutNode.addDefVariable(jvout);
            ExpressionVisitor.paramNumber++;
            
            JReference jvin = new JVirtualReference(node, "$_", entry.getReturnType(), entry.isPrimitiveType());
            formalOutNode.addUseVariable(jvin);
        }
        return formalOutNode;
    }
}
