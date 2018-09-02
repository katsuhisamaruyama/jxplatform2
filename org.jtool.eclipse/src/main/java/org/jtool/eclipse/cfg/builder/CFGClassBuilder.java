/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.CCFG;
import org.jtool.eclipse.cfg.CFG;
import org.jtool.eclipse.cfg.CFGClassEntry;
import org.jtool.eclipse.cfg.CFGNode;
import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import java.util.List;

/**
 * Builds a CFG that corresponds to a field.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class CFGClassBuilder {
    
    public static CCFG build(JavaClass jclass) {
        CCFG ccfg = new CCFG();
        
        CFGClassEntry entry;
        if (jclass.isEnum()) {
            entry = new CFGClassEntry(jclass.getASTNode(), CFGNode.Kind.enumEntry, jclass.getName(), jclass.getQualifiedName());
        } else if (jclass.isInterface()) {
            entry = new CFGClassEntry(jclass.getASTNode(), CFGNode.Kind.interfaceEntry, jclass.getName(), jclass.getQualifiedName());
        } else {
            entry = new CFGClassEntry(jclass.getASTNode(), CFGNode.Kind.classEntry, jclass.getName(), jclass.getQualifiedName());
        }
        ccfg.setStartNode(entry);
        ccfg.add(entry);
        
        for (JavaMethod jm : jclass.getMethods()) {
            CFG cfg = CFGMethodBuilder.build(jm);
            entry.addMethod(cfg);
        }
        
        for (JavaField jf : jclass.getFields()) {
            CFG cfg = CFGFieldBuilder.build(jf);
            entry.addField(cfg);
        }
        /*
        for (JavaClass jc : jclass.getInnerClasses()) {
            CFG cfg = CFGClassBuilder.build(jc);
            entry.addType(cfg);
        }
        */
        return ccfg;
    }
    
    public static CCFG build(TypeDeclaration node) {
        return build(node, node.resolveBinding().getTypeDeclaration(), node.getMethods(), node.getFields(), node.getTypes());
    }
    
    public static CCFG build(AnonymousClassDeclaration node) {
        return build(node, node.resolveBinding().getTypeDeclaration(), null, null, null);
    }
    
    public static CCFG build(EnumDeclaration node) {
        return build(node, node.resolveBinding().getTypeDeclaration(), null, null, null);
    }
    
    private static CCFG build(ASTNode node, ITypeBinding tbinding, MethodDeclaration[] methods, FieldDeclaration[] fields, TypeDeclaration[] types) {
        CCFG ccfg = new CCFG();
        
        CFGClassEntry entry;
        if (tbinding.isEnum()) {
            entry = new CFGClassEntry(node, CFGNode.Kind.enumEntry, tbinding.getName(), tbinding.getQualifiedName());
        } else if (tbinding.isInterface()) {
            entry = new CFGClassEntry(node, CFGNode.Kind.interfaceEntry, tbinding.getName(), tbinding.getQualifiedName());
        } else {
            entry = new CFGClassEntry(node, CFGNode.Kind.classEntry, tbinding.getName(), tbinding.getQualifiedName());
        }
        ccfg.setStartNode(entry);
        ccfg.add(entry);
        
        if (methods != null) {
            for (MethodDeclaration methodDecl : methods) {
                CFG cfg = CFGMethodBuilder.build(methodDecl);
                entry.addMethod(cfg);
            }
        }
        
        if (fields != null) {
            for (FieldDeclaration fieldDecl : fields) {
                @SuppressWarnings("unchecked")
                List<VariableDeclarationFragment> decls = (List<VariableDeclarationFragment>)fieldDecl.fragments();
                for (VariableDeclarationFragment decl : decls) {
                    CFG cfg = CFGFieldBuilder.build(decl);
                    entry.addField(cfg);
                }
            }
        }
        
        if (types != null) {
            for (TypeDeclaration typeDecl : types) {
                CFG cfg = CFGClassBuilder.build(typeDecl);
                entry.addType(cfg);
            }
        }
        
        return ccfg;
    }
}
