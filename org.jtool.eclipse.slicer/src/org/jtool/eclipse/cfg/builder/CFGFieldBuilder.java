/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGExit;
import org.jtool.eclipse.cfg.CFGFieldEntry;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.cfg.CFGStatement;
import org.jtool.eclipse.cfg.ControlFlow;
import org.jtool.eclipse.cfg.JFieldAccess;
import org.jtool.eclipse.cfg.JVariable;
import org.jtool.eclipse.javamodel.JavaField;

import static org.jtool.eclipse.javamodel.JavaElement.QualifiedNameSeparator;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * Builds a CFG that corresponds to a field.
 * @author Katsuhisa Maruyama
 */
public class CFGFieldBuilder {
    
    public static CFG build(JavaField jfield) {
        return build(jfield.getASTNode(), jfield.getVariableBinding(), jfield.getName(), jfield.getQualifiedName());
    }
    
    public static CFG build(VariableDeclaration node) {
        IVariableBinding vbinding = node.resolveBinding();
        String fqn = getQualifiedName(vbinding.getDeclaringClass(), vbinding);
        String name = vbinding.getName();
        return build(node, vbinding, name, fqn);
    }
    
    public static CFG build(VariableDeclarationFragment node) {
        IVariableBinding vbinding = node.resolveBinding();
        String fqn = getQualifiedName(vbinding.getDeclaringClass(), vbinding);
        String name = vbinding.getName();
        return build(node, vbinding, name, fqn);
    }
    
    public static CFG build(EnumConstantDeclaration node) {
        IVariableBinding vbinding = node.resolveVariable();
        String fqn = getQualifiedName(vbinding.getDeclaringClass(), vbinding);
        String name = vbinding.getName();
        return build(node, vbinding, name, fqn);
    }
    
    private static String getQualifiedName(ITypeBinding tbinding, IVariableBinding vbinding) {
        tbinding = tbinding.getTypeDeclaration();
        vbinding = vbinding.getVariableDeclaration();
        return tbinding.getQualifiedName() + QualifiedNameSeparator + vbinding.getName();
    }
    
    private static CFG build(ASTNode node, IVariableBinding vbinding, String name, String fqn) {
        CFG cfg = new CFG();
        ExpressionVisitor.paramNumber = 1;
        
        CFGFieldEntry entry;
        if (vbinding.isEnumConstant()) {
            entry = new CFGFieldEntry(node, CFGNode.Kind.enumConstantExit, name, fqn);
        } else {
            entry = new CFGFieldEntry(node, CFGNode.Kind.fieldEntry, name, fqn);
        }
        entry.setType(vbinding.getType().getTypeDeclaration().getQualifiedName());
        cfg.setStartNode(entry);
        cfg.add(entry);
        
        CFGStatement declNode = new CFGStatement(node, CFGNode.Kind.fieldDeclaration);
        JVariable jvar = new JFieldAccess(node, vbinding);
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
                ExpressionVisitor visitor = new ExpressionVisitor(cfg, declNode);
                decl.accept(visitor);
                curNode = visitor.getExitNode();
            }
        } else {
            VariableDeclarationFragment decl = (VariableDeclarationFragment)node;
            Expression initializer = decl.getInitializer();
            if (initializer != null) {
                ExpressionVisitor visitor = new ExpressionVisitor(cfg, declNode);
                initializer.accept(visitor);
                curNode = visitor.getExitNode();
            }
        }
        
        CFGExit exit = new CFGExit(node, CFGNode.Kind.fieldExit);
        cfg.setEndNode(exit);
        cfg.add(exit);
        
        edge = new ControlFlow(curNode, exit);
        edge.setTrue();
        cfg.add(edge);
        
        return cfg;
    }
}
