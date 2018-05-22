/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel.builder;

import org.jtool.eclipse.javamodel.JavaElement;
import org.jtool.eclipse.javamodel.JavaField;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import java.util.Set;
import java.util.HashSet;

/**
 * Parses Java source code and stores information on field initializers appearing in a field.
 * 
 * VariableDeclarationFragment
 * 
 * @see org.eclipse.jdt.core.dom.Expression
 * @author Katsuhisa Maruyama
 */
public class FieldInitializerCollector extends ASTVisitor {
    
    private Set<JavaField> accessedFields = new HashSet<JavaField>();
    private boolean bindingOk = true;
    
    public FieldInitializerCollector() {
    }
    
    public Set<JavaField> getAccessedFields() {
        return accessedFields;
    }
    
    public boolean isBindingOk() {
        return bindingOk;
    }
    
    @Override
    public boolean visit(VariableDeclarationFragment node) {
        IVariableBinding binding = node.resolveBinding();
        if (binding != null && binding.isField()) {
            Expression initializer = node.getInitializer();
            if (initializer != null) {
                initializer.accept(this);
            }
        }
        return false;
    }
    
    @Override
    public boolean visit(SimpleName node) {
        addFieldAccess(node.resolveBinding());
        return false;
    }
    
    private void addFieldAccess(IBinding binding) {
        if (binding != null) {
            if (binding.getKind() == IBinding.VARIABLE) {
                IVariableBinding vbinding = (IVariableBinding)binding;
                if (vbinding.isField() || vbinding.isEnumConstant()) {
                    JavaField jfield = JavaElement.findDeclaringField(vbinding);
                    if (jfield != null) {
                        if (!accessedFields.contains(jfield)) {
                            accessedFields.add(jfield);
                        }
                    } else {
                        bindingOk = false;
                    }
                }
            }
        } else {
            bindingOk = false;
        }
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
