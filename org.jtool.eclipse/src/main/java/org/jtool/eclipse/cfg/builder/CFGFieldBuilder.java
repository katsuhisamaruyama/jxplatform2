/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import static org.jtool.eclipse.javamodel.JavaElement.QualifiedNameSeparator;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGExit;
import org.jtool.eclipse.cfg.CFGFieldEntry;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGStatement;
import org.jtool.eclipse.cfg.ControlFlow;
import org.jtool.eclipse.cfg.JFieldReference;
import org.jtool.eclipse.cfg.JReference;
import org.jtool.eclipse.cfg.JMethod;
import org.jtool.eclipse.javamodel.JavaField;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.IVariableBinding;
import java.util.Set;
import java.util.HashSet;

/**
 * Builds a CFG that corresponds to a field.
 * 
 * @author Katsuhisa Maruyama
 */
public class CFGFieldBuilder {
    
    public static CFG build(JavaField jfield) {
        return build(jfield, new HashSet<JMethod>());
    }
    
    public static CFG build(JavaField jfield, Set<JMethod> visitedMethods) {
        String name = jfield.getName();
        String fqn = jfield.getQualifiedName();
        String className = jfield.getDeclaringClass().getQualifiedName();
        return build(jfield.getASTNode(), jfield.getVariableBinding(), name, fqn, className, visitedMethods);
    }
    
    public static CFG build(VariableDeclaration node) {
        IVariableBinding vbinding = node.resolveBinding().getVariableDeclaration();
        String name = vbinding.getName();
        String className = vbinding.getDeclaringClass().getTypeDeclaration().getQualifiedName();
        String fqn = className + QualifiedNameSeparator + name;
        return build(node, vbinding, name, fqn, className, new HashSet<JMethod>());
    }
    
    public static CFG build(VariableDeclarationFragment node) {
        IVariableBinding vbinding = node.resolveBinding().getVariableDeclaration();
        String name = vbinding.getName();
        String className = vbinding.getDeclaringClass().getTypeDeclaration().getQualifiedName();
        String fqn = className + QualifiedNameSeparator + name;
        return build(node, vbinding, name, fqn, className, new HashSet<JMethod>());
    }
    
    public static CFG build(EnumConstantDeclaration node) {
        IVariableBinding vbinding = node.resolveVariable().getVariableDeclaration();
        String name = vbinding.getName();
        String className = vbinding.getDeclaringClass().getTypeDeclaration().getQualifiedName();
        String fqn = className + QualifiedNameSeparator + name;
        return build(node, vbinding, name, fqn, className, new HashSet<JMethod>());
    }
    
    private static CFG build(ASTNode node, IVariableBinding vbinding,
            String name, String fqn, String className, Set<JMethod> visitedMethods) {
        CFG cfg = new CFG();
        ExpressionVisitor.paramNumber = 1;
        
        CFGFieldEntry entry;
        if (vbinding.isEnumConstant()) {
            entry = new CFGFieldEntry(node, CFGNode.Kind.enumConstantEntry, name, fqn, className);
        } else {
            entry = new CFGFieldEntry(node, CFGNode.Kind.fieldEntry, name, fqn, className);
        }
        entry.setType(vbinding.getType().getTypeDeclaration().getQualifiedName());
        cfg.setStartNode(entry);
        cfg.add(entry);
        
        CFGStatement declNode = new CFGStatement(node, CFGNode.Kind.fieldDeclaration);
        JReference jvar = new JFieldReference(node, vbinding);
        declNode.addDefVariable(jvar);
        declNode.addUseVariable(jvar);
        cfg.add(declNode);
        
        ControlFlow edge = new ControlFlow(entry, declNode);
        edge.setTrue();
        cfg.add(edge);
        
        CFGNode curNode = declNode;
        if (vbinding.isEnumConstant()) {
            EnumConstantDeclaration decl = (EnumConstantDeclaration)node;
            if (decl.resolveConstructorBinding() != null) {
                ExpressionVisitor visitor = new ExpressionVisitor(cfg, declNode, visitedMethods);
                decl.accept(visitor);
                curNode = visitor.getExitNode();
            }
        } else {
            VariableDeclarationFragment decl = (VariableDeclarationFragment)node;
            Expression initializer = decl.getInitializer();
            if (initializer != null) {
                ExpressionVisitor visitor = new ExpressionVisitor(cfg, declNode, visitedMethods);
                initializer.accept(visitor);
                curNode = visitor.getExitNode();
            }
        }
        
        CFGExit exit;
        if (vbinding.isEnumConstant()) {
            exit = new CFGExit(node, CFGNode.Kind.enumConstantExit);
        } else {
            exit = new CFGExit(node, CFGNode.Kind.fieldExit);
        }
        cfg.setEndNode(exit);
        cfg.add(exit);
        
        edge = new ControlFlow(curNode, exit);
        edge.setTrue();
        cfg.add(edge);
        
        return cfg;
    }
}
