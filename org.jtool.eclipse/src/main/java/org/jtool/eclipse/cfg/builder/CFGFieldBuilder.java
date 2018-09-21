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
import org.jtool.eclipse.cfg.JFieldReference;
import org.jtool.eclipse.cfg.JReference;
import org.jtool.eclipse.javamodel.JavaField;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.IVariableBinding;
import java.util.Set;
import java.util.HashSet;

/**
 * Builds a CFG that corresponds to a field.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class CFGFieldBuilder {
    
    public static CFG build(JavaField jfield, JInfoStore infoStore) {
        return build(jfield, infoStore, new HashSet<JMethod>());
    }
    
    public static CFG build(JavaField jfield, JInfoStore infoStore, Set<JMethod> visitedMethods) {
        return build(jfield, jfield.getVariableBinding(), infoStore, visitedMethods);
    }
    
    private static CFG build(JavaField jfield, IVariableBinding vbinding, JInfoStore infoStore, Set<JMethod> visitedMethods) {
        CFG cfg = new CFG();
        ExpressionVisitor.paramNumber = 1;
        
        CFGFieldEntry entry;
        if (vbinding.isEnumConstant()) {
            entry = new CFGFieldEntry(jfield, CFGNode.Kind.enumConstantEntry);
        } else {
            entry = new CFGFieldEntry(jfield, CFGNode.Kind.fieldEntry);
        }
        cfg.setStartNode(entry);
        cfg.add(entry);
        
        CFGStatement declNode = new CFGStatement(jfield.getASTNode(), CFGNode.Kind.fieldDeclaration);
        JReference jvar = new JFieldReference(jfield.getASTNode(), vbinding);
        declNode.addDefVariable(jvar);
        declNode.addUseVariable(jvar);
        cfg.add(declNode);
        
        ControlFlow edge = new ControlFlow(entry, declNode);
        edge.setTrue();
        cfg.add(edge);
        
        CFGNode curNode = declNode;
        if (vbinding.isEnumConstant()) {
            EnumConstantDeclaration decl = (EnumConstantDeclaration)jfield.getASTNode();
            if (decl.resolveConstructorBinding() != null) {
                ExpressionVisitor visitor = new ExpressionVisitor(cfg, declNode, infoStore, visitedMethods);
                decl.accept(visitor);
                curNode = visitor.getExitNode();
            }
        } else {
            VariableDeclarationFragment decl = (VariableDeclarationFragment)jfield.getASTNode();
            Expression initializer = decl.getInitializer();
            if (initializer != null) {
                ExpressionVisitor visitor = new ExpressionVisitor(cfg, declNode, infoStore, visitedMethods);
                initializer.accept(visitor);
                curNode = visitor.getExitNode();
            }
        }
        
        CFGExit exit;
        if (vbinding.isEnumConstant()) {
            exit = new CFGExit(jfield.getASTNode(), CFGNode.Kind.enumConstantExit);
        } else {
            exit = new CFGExit(jfield.getASTNode(), CFGNode.Kind.fieldExit);
        }
        cfg.setEndNode(exit);
        cfg.add(exit);
        
        edge = new ControlFlow(curNode, exit);
        edge.setTrue();
        cfg.add(edge);
        
        return cfg;
    }
}
