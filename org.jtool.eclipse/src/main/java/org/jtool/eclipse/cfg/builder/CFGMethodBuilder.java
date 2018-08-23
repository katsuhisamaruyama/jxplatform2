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
import org.jtool.eclipse.cfg.JApparentAccess;
import org.jtool.eclipse.cfg.JLocalAccess;
import org.jtool.eclipse.cfg.JVariable;
import org.jtool.eclipse.graph.GraphEdge;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import java.util.List;
import java.util.Set;

import static org.jtool.eclipse.javamodel.JavaElement.QualifiedNameSeparator;

import java.util.HashSet;

/**
 * Builds a CFG that corresponds to a method.
 * @author Katsuhisa Maruyama
 */
public class CFGMethodBuilder {
    
    @SuppressWarnings("unchecked")
    public static CFG build(JavaMethod jmethod) {
        List<VariableDeclaration> params = null;
        if (!jmethod.isInitializer()) {
            if (jmethod.isLambda()) {
                LambdaExpression node = (LambdaExpression)jmethod.getASTNode();
                params = node.parameters();
            } else {
                MethodDeclaration node = (MethodDeclaration)jmethod.getASTNode();
                params = node.parameters();
            }
        }
        return build(jmethod.getASTNode(), jmethod.getMethodBinding(), params, jmethod.getName(), jmethod.getSignature(), jmethod.getQualifiedName());
    }
    
    @SuppressWarnings("unchecked")
    public static CFG build(MethodDeclaration node) {
        ITypeBinding tbinding = node.resolveBinding().getDeclaringClass();
        IMethodBinding mbinding = node.resolveBinding().getMethodDeclaration();
        String name = mbinding.getName();
        String sig = JavaMethod.getSignature(mbinding);
        String fqn = tbinding.getTypeDeclaration().getQualifiedName() + QualifiedNameSeparator + sig;
        return build(node, mbinding, node.parameters(), name, sig, fqn);
    }
    
    public static CFG build(Initializer node) {
        ITypeBinding tbinding = JVariable.findEnclosingClass(node).getTypeDeclaration();
        String name = JavaMethod.InitializerName;
        String fqn = tbinding.getTypeDeclaration().getQualifiedName() + QualifiedNameSeparator + name;
        return build(node, null, null, name, name, fqn);
    }
    
    @SuppressWarnings("unchecked")
    public static CFG build(LambdaExpression node) {
        ITypeBinding tbinding = node.resolveTypeBinding().getTypeDeclaration();
        IMethodBinding mbinding = tbinding.getFunctionalInterfaceMethod().getMethodDeclaration();
        String name = mbinding.getName();
        String sig = JavaMethod.getSignature(mbinding);
        String fqn = tbinding.getTypeDeclaration().getQualifiedName() + "$" + String.valueOf(CFGStore.getInstance().size()) + QualifiedNameSeparator + sig;
        return build(node, mbinding, node.parameters(), name, sig, fqn);
    }
    
    private static CFG build(ASTNode node, IMethodBinding mbinding, List<VariableDeclaration> params, String name, String sig, String fqn) {
        CFG cfg = new CFG();
        ExpressionVisitor.paramNumber = 1;
        
        CFGMethodEntry entry;
        if (mbinding == null) {
            entry = new CFGMethodEntry(node, CFGNode.Kind.initializerEntry, name, sig, fqn);
            entry.setReturnType("void");
            entry.setPrimitiveType(false);
        } else {
            if (mbinding.isConstructor()) {
                entry = new CFGMethodEntry(node, CFGNode.Kind.constructorEntry, name, sig, fqn);
                entry.setReturnType(mbinding.getReturnType().getTypeDeclaration().getQualifiedName());
                entry.setPrimitiveType(mbinding.getReturnType().isPrimitive());
            } else {
                entry = new CFGMethodEntry(node, CFGNode.Kind.methodEntry, name, sig, fqn);
                entry.setReturnType(mbinding.getReturnType().getTypeDeclaration().getQualifiedName());
                entry.setPrimitiveType(mbinding.getReturnType().isPrimitive());
            }
        }
        
        cfg.setStartNode(entry);
        cfg.add(entry);
        
        CFGNode tmpExit = new CFGNode();
        cfg.setEndNode(tmpExit);
        
        CFGNode formalInNode = createFormalIn(params, cfg, entry, entry);
        CFGNode nextNode = new CFGNode();
        
        ControlFlow entryEdge = new ControlFlow(formalInNode, nextNode);
        entryEdge.setTrue();
        cfg.add(entryEdge);
        
        StatementVisitor visitor = new StatementVisitor(cfg, formalInNode, nextNode);
        node.accept(visitor);
        nextNode = visitor.getNextCFGNode();
        
        CFGNode formalOutNode = createFormalOut(node, cfg, entry, nextNode);
        
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
        
        if (formalOutNode != null) {
            replace(cfg, nextNode, formalOutNode);
            replace(cfg, tmpExit, formalOutNode);
            cfg.add(exit);
            
            ControlFlow exitEdge = new ControlFlow(formalOutNode, exit);
            exitEdge.setTrue();
            cfg.add(exitEdge);
            
        } else {
            replace(cfg, nextNode, exit);
            replace(cfg, tmpExit, exit);
            cfg.add(exit);
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
            
            JVariable jvout = new JLocalAccess(param, param.resolveBinding());
            formalInNode.setDefVariable(jvout);
            
            JVariable jvin = new JApparentAccess(param, "$" + String.valueOf(ExpressionVisitor.paramNumber), jvout.getType(), jvout.isPrimitiveType());
            formalInNode.setUseVariable(jvin);
            ExpressionVisitor.paramNumber++;
            
            ControlFlow edge = new ControlFlow(prevNode, formalInNode);
            edge.setTrue();
            cfg.add(edge);
            
            prevNode = formalInNode;
        }
        
        return prevNode;
    }
    
    private static CFGNode createFormalOut(ASTNode node, CFG cfg, CFGMethodEntry entry, CFGNode prevNode) {
        if (entry.isVoidType()) {
            return null;
        }
        
        CFGParameter formalOutNode = new CFGParameter(node, CFGNode.Kind.formalOut, 0);
        formalOutNode.setParent(entry);
        entry.addFormalOut(formalOutNode);
        cfg.add(formalOutNode);
        
        JVariable jvout = new JApparentAccess(node, "$" + String.valueOf(ExpressionVisitor.paramNumber), entry.getReturnType(), entry.isPrimitiveType());
        formalOutNode.addDefVariable(jvout);
        ExpressionVisitor.paramNumber++;
        
        JVariable jvin = new JApparentAccess(node, "$_", entry.getReturnType(), entry.isPrimitiveType());
        formalOutNode.addUseVariable(jvin);
        
        return formalOutNode;
    }
}
