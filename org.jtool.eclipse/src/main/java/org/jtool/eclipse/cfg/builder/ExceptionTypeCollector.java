/*
 *  Copyright 2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.javamodel.JavaElement;
import org.jtool.eclipse.javamodel.JavaMethod;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import java.util.Set;
import java.util.HashSet;

/**
 * Parses Java source code and stores information on possible exceptions when calling methods and constructors.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @see org.eclipse.jdt.core.dom.Expression
 * 
 * MethodInvocation
 * SuperMethodInvocation
 * ConstructorInvocation
 * SuperConstructorInvocation
 * ClassInstanceCreation
 * ThrowStatement
 * 
 * @author Katsuhisa Maruyama
 */
class ExceptionTypeCollector {
    
    private Set<ITypeBinding> exceptionTypes = new HashSet<ITypeBinding>();
    
    Set<ITypeBinding> getExceptions(JavaMethod jmethod) {
        Set<JavaMethod> methods = new HashSet<JavaMethod>();
        collectCalledMethods(jmethod, methods);
        return exceptionTypes;
    }
    
    private void collectCalledMethods(JavaMethod jmethod, Set<JavaMethod> methods) {
        if (methods.contains(jmethod)) {
            return;
        }
        
        methods.add(jmethod);
        
        MethodCallVositor visitor = new MethodCallVositor();
        jmethod.getASTNode().accept(visitor);
        exceptionTypes.addAll(visitor.exceptionTypes);
        for (JavaMethod jm : visitor.calledMethods) {
            collectCalledMethods(jm, methods);
        }
    }
    
    private class MethodCallVositor extends ASTVisitor {
        
        private static final String EXCEPTION_NAME = "java.lang.Exception";
        private static final String RUNTIME_EXCEPTION_NAME = "java.lang.RuntimeException";
        
        private Set<JavaMethod> calledMethods = new HashSet<JavaMethod>();
        private Set<ITypeBinding> exceptionTypes = new HashSet<ITypeBinding>();
        
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
            }
            return false;
        }
        
        @Override
        public boolean visit(ThrowStatement node) {
            ITypeBinding tbinding = node.getExpression().resolveTypeBinding();
            if (tbinding != null) {
                exceptionTypes.add(tbinding);
            }
            return false;
        }
        
        private void addMethodCall(IMethodBinding mbinding) {
            if (mbinding != null) {
                if (mbinding.isDefaultConstructor() || mbinding.isAnnotationMember()) {
                    return;
                }
                
                JavaMethod jmethod = JavaElement.findDeclaringMethod(mbinding);
                if (jmethod != null && jmethod.getASTNode() != null) {
                    calledMethods.add(jmethod);
                }
                for (ITypeBinding tbinding : mbinding.getExceptionTypes()) {
                    if (isUncheckedException(tbinding)) {
                        exceptionTypes.add(tbinding);
                    }
                }
            }
        }
        
        private boolean isUncheckedException(ITypeBinding tbinding) {
            while (tbinding != null) {
                String type = tbinding.getQualifiedName();
                if (type.equals(EXCEPTION_NAME) || type.equals(RUNTIME_EXCEPTION_NAME)) {
                    return true;
                }
                tbinding = tbinding.getSuperclass();
            }
            return false;
        }
    }
}
