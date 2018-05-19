/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.java.builder;

import org.jtool.eclipse.model.java.JavaLocal;
import org.jtool.eclipse.model.java.JavaMethod;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import java.util.Set;
import java.util.HashSet;

/**
 * Parses Java source code and stores information on on local variable declarations.
 * 
 * VariableDeclaration:
 *   SingleVariableDeclaration
 *   VariableDeclarationFragment
 * 
 * @see org.eclipse.jdt.core.dom.VariableDeclaration
 * @author Katsuhisa Maruyama
 */
public class LocalDeclarationCollector extends ASTVisitor {
    
    private JavaMethod jmethod;
    private Set<JavaLocal> localDeclarations = new HashSet<JavaLocal>();
    private boolean bindingOk = true;
    
    public LocalDeclarationCollector(JavaMethod jmethod) {
        this.jmethod = jmethod;
    }
    
    public Set<JavaLocal> getLocalDeclarations() {
        return localDeclarations;
    }
    
    public boolean isBindingOk() {
        return bindingOk;
    }
    
    @Override
    public boolean visit(SingleVariableDeclaration node) {
        addVariableDeclaration(node);
        return false;
    }
    
    @Override
    public boolean visit(VariableDeclarationFragment node) {
        addVariableDeclaration(node);
        return false;
    }
    
    private void addVariableDeclaration(VariableDeclaration node) {
        if (isLocal(node.getName())) {
            JavaLocal jlocal = new JavaLocal(node, jmethod);
            localDeclarations.add(jlocal);
        }
    }
    
    private boolean isLocal(Name node) {
        IBinding binding = node.resolveBinding();
        if (binding != null && binding.getKind() == IBinding.VARIABLE) {
            IVariableBinding vbinding = (IVariableBinding)binding;
            return vbinding != null && !vbinding.isField() && !vbinding.isEnumConstant();
        } else {
            bindingOk = false;
        }
        return false;
    }
    
    @Override
    public boolean visit(TypeDeclaration node) {
        return false;
    }
    
    @Override
    public boolean visit(AnonymousClassDeclaration node) {
        return false;
    }
    
    @Override
    public boolean visit(EnumDeclaration node) {
        return false;
    }
    
    @Override
    public boolean visit(LambdaExpression node) {
        return false;
    }
    
    @Override
    public boolean visit(CreationReference node) {
        return false;
    }
    
    @Override
    public boolean visit(ExpressionMethodReference node) {
        return false;
    }
    
    @Override
    public boolean visit(SuperMethodReference node) {
        return false;
    }
    
    @Override
    public boolean visit(TypeMethodReference node) {
        return false;
    }
}
