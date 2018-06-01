/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel.builder;

import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeMethodReference;

/**
 * Parses Java source code and stores information on lambda expressions.
 * 
 * LambdaExpression
 * 
 * @see org.eclipse.jdt.core.dom.LambdaExpression
 * @author Katsuhisa Maruyama
 */
public class LambdaCollector extends ASTVisitor {
    
    private JavaMethod jmethod;
    
    private int id = 1;
    
    public LambdaCollector(JavaMethod jmethod) {
        this.jmethod = jmethod;
    }
    
    @Override
    public boolean visit(LambdaExpression node) {
        registerLambda(node);
        id++;
        return true;
    }
    
    private void registerLambda(LambdaExpression node) {
        ITypeBinding tbinding = node.resolveTypeBinding();
        IMethodBinding mbinding = node.resolveMethodBinding();
        if (tbinding != null && mbinding != null) {
            tbinding = tbinding.getTypeDeclaration();
            mbinding = mbinding.getMethodDeclaration();
            IMethodBinding ibinding = tbinding.getFunctionalInterfaceMethod().getMethodDeclaration();
            
            if (ibinding != null) {
                String name = tbinding.getQualifiedName() + "$" + jmethod.getQualifiedName() + "$" + String.valueOf(id);
                JavaClass anonymousClass = new JavaClass(node, name, tbinding, jmethod);
                JavaMethod anonymousMethod = new JavaMethod(node, ibinding, anonymousClass);
                anonymousClass.addMethod(anonymousMethod);
                jmethod.getDeclaringClass().addInnerClass(anonymousClass);
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
