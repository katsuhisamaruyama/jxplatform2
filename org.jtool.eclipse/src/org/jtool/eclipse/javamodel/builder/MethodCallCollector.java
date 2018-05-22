/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel.builder;

import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaElement;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.IMethodBinding;
import java.util.Set;
import java.util.HashSet;

/**
 * Parses Java source code and stores information on method invocation appearing in a method or field.
 * 
 * MethodInvocation
 * SuperMethodInvocation
 * ConstructorInvocation
 * SuperConstructorInvocation
 * ClassInstanceCreation
 * 
 * @see org.eclipse.jdt.core.dom.Expression
 * @author Katsuhisa Maruyama
 */
public class MethodCallCollector extends ASTVisitor {
    
    private Set<JavaMethod> calledMethods = new HashSet<JavaMethod>();
    private boolean bindingOk = true;
    
    public MethodCallCollector() {
    }
    
    public Set<JavaMethod> getCalledMethods() {
        return calledMethods;
    }
    
    public boolean isBindingOk() {
        return bindingOk;
    }
    
    @Override
    public boolean visit(MethodInvocation node) {
        if (node.resolveMethodBinding() != null) {
            addMethodCall(node.resolveMethodBinding());
        }
        return false;
    }
    
    @Override
    public boolean visit(SuperMethodInvocation node) {
        if (node.resolveMethodBinding() != null) {
            addMethodCall(node.resolveMethodBinding());
        }
        return false;
    }
    
    @Override
    public boolean visit(ConstructorInvocation node) {
        if (node.resolveConstructorBinding() != null) {
            addMethodCall(node.resolveConstructorBinding());
        }
        return false;
    }
    
    @Override
    public boolean visit(SuperConstructorInvocation node) {
        if (node.resolveConstructorBinding() != null) {
            addMethodCall(node.resolveConstructorBinding());
        }
        return false;
    }
    
    @Override
    public boolean visit(ClassInstanceCreation node) {
        if (node.resolveConstructorBinding() != null) {
            addMethodCall(node.resolveConstructorBinding());
        } else {
            addMethodCall(node.getType());
        }
        return false;
    }
    
    private void addMethodCall(IMethodBinding mbinding) {
        if (mbinding != null) {
            if (mbinding.isDefaultConstructor() || mbinding.isAnnotationMember()) {
                return;
            }
            
            JavaMethod jmethod = JavaElement.findDeclaringMethod(mbinding);
            if (jmethod != null) {
                if (!calledMethods.contains(jmethod)) {
                    calledMethods.add(jmethod);
                }
            } else {
                if (!mbinding.getDeclaringClass().isEnum() || !isImplicitMethodOfEnum(mbinding.getName())) {
                    ProjectStore.getInstance().printUnresolvedError(mbinding.getName() + " of " + mbinding.getDeclaringClass().getQualifiedName());
                    bindingOk = false;
                }
            }
        }
    }
    
    private void addMethodCall(Type type) {
        if (type != null) {
            JavaClass jc = JavaElement.findDeclaringClass(type.resolveBinding());
            if (jc.isInProject()) {
                bindingOk = false;
                ProjectStore.getInstance().printUnresolvedError("$ClassInstanceCreation" + " of " + jc.getQualifiedName());
            }
        }
    }
    
    private boolean isImplicitMethodOfEnum(String name) {
        return "compareTo".equals(name) || "getDeclaringClass".equals(name) ||
               "name".equals(name) || "ordinal".equals(name) || "valueOf".equals(name) || "values".equals(name);
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
