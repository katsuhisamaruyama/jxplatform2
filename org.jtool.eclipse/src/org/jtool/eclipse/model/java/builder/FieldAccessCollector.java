/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.java.builder;

import org.jtool.eclipse.model.java.JavaFile;
import org.jtool.eclipse.model.java.JavaClass;
import org.jtool.eclipse.model.java.JavaElement;
import org.jtool.eclipse.model.java.JavaField;
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
 * 
 * Name:
 *   SimpleName
 *   QualifiedName
 * 
 * @see org.eclipse.jdt.core.dom.Expression
 * @author Katsuhisa Maruyama
 */
public class FieldAccessCollector extends ASTVisitor {
    
    private JavaFile jfile;
    private JavaClass jclass;
    private Set<JavaField> accessedFields = new HashSet<JavaField>();
    private boolean bindingOk = true;
    
    public FieldAccessCollector(JavaClass jclass) {
        this.jclass = jclass;
        this.jfile = jclass.getFile();
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
        return false;
    }
    
    @Override
    public boolean visit(QualifiedName node) {
        addVariableAccess(node.getName(), node.resolveBinding());
        return false;
    }
    
    @Override
    public boolean visit(LabeledStatement node) {
        node.getBody().accept(this);
        return false;
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
                    JavaField jfield = JavaElement.findDeclaringField(jfile.getProject(), vbinding);
                    if (jfield != null) {
                        if (!accessedFields.contains(jfield)) {
                            accessedFields.add(jfield);
                        }
                    } else {
                        bindingOk = false;
                        jfile.getProject().addUnresolvedBindingError(binding.getName() + " of " +
                                vbinding.getDeclaringClass().getQualifiedName());
                    }
                }
            }
        } else {
            bindingOk = false;
            if (name != null) {
                jfile.getProject().addUnresolvedBindingError(name.getIdentifier() + " in " +
                        jclass.getQualifiedName());
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
