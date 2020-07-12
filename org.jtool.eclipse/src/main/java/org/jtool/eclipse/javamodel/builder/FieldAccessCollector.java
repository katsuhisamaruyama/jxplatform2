/*
 *  Copyright 2018-2020
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel.builder;

import org.jtool.eclipse.javamodel.JavaElement;
import org.jtool.eclipse.javamodel.JavaField;
import org.jtool.eclipse.javamodel.JavaProject;
import org.jtool.eclipse.util.Logger;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import java.util.Set;
import java.util.HashSet;

/**
 * Parses Java source code and stores information on variable access appearing in a method or field.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @see org.eclipse.jdt.core.dom.Expression
 * 
 * Name:
 *   SimpleName
 *   QualifiedName
 * 
 * @author Katsuhisa Maruyama
 */
public class FieldAccessCollector extends ASTVisitor {
    
    private JavaProject jproject;
    
    private Set<JavaField> accessedFields = new HashSet<>();
    private boolean bindingOk = true;
    
    public FieldAccessCollector(JavaProject jproject) {
        this.jproject = jproject;
    }
    
    public Set<JavaField> getAccessedFields() {
        return accessedFields;
    }
    
    public boolean isBindingOk() {
        return bindingOk;
    }
    
    @Override
    public boolean visit(SimpleName node) { 
        addVariableAccess(node, node.resolveBinding());
        return true;
    }
    
    @Override
    public boolean visit(QualifiedName node) {
        addVariableAccess(node.getName(), node.resolveBinding());
        return true;
    }
    
    @Override
    public boolean visit(LabeledStatement node) {
        node.getBody().accept(this);
        return true;
    }
    
    @Override
    public boolean visit(BreakStatement node) {
        return false;
    }
    
    @Override
    public boolean visit(ContinueStatement node) {
        return false;
    }
    
    private void addVariableAccess(SimpleName name, IBinding binding) {
        if (binding != null) {
            if (binding.getKind() == IBinding.VARIABLE) {
                IVariableBinding vbinding = (IVariableBinding)binding;
                if (vbinding.isField() || vbinding.isEnumConstant()) {
                    JavaField jfield = JavaElement.findDeclaringField(jproject, vbinding);
                    if (jfield != null) {
                        if (!accessedFields.contains(jfield)) {
                            accessedFields.add(jfield);
                        }
                    } else {
                        bindingOk = false;
                        Logger.getInstance().printUnresolvedError(binding.getName() + " of " + vbinding.getDeclaringClass().getQualifiedName());
                    }
                }
            }
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
