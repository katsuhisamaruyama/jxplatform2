/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.model.java.builder;

import org.jtool.eclipse.model.java.JavaFile;
import org.jtool.eclipse.model.java.JavaClass;
import org.jtool.eclipse.model.java.JavaElement;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.WildcardType;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Parses Java source code and stores information on types.
 * 
 * Type:
 *   AnnotatableType:
 *     PrimitiveType
 *     SimpleType
 *     QualifiedType
 *     NameQualifiedType
 *     WildcardType
 *   ArrayType
 *   ParameterizedType
 *   ----UnionType
 *   ----IntersectionType
 * PrimitiveType:
 *   { Annotation } byte
 *   { Annotation } short
 *   { Annotation } char
 *   { Annotation } int
 *   { Annotation } long
 *   { Annotation } float
 *   { Annotation } double
 *   { Annotation } boolean
 *   { Annotation } void
 * ArrayType:
 *   Type { Annotation } [] { { Annotation } [] }
 * SimpleType:
 *   { Annotation } TypeName
 * QualifiedType:
 *   Type . {Annotation} SimpleName
 * NameQualifiedType:
 *   Name . { Annotation } SimpleName
 * WildcardType:
 *   { Annotation } ? [ ( extends | super) Type ]
 * ParameterizedType:
 *   Type < Type { , Type } >
 * UnionType:
 *   Type | Type { | Type }
 * IntersectionType:
 *   Type & Type { & Type }
 * 
 * @see org.eclipse.jdt.core.dom.Type
 * @author Katsuhisa Maruyama
 */
public class TypeCollector extends ASTVisitor {
    
    private JavaFile jfile;
    private JavaClass jclass;
    private boolean bindingOk = true;
    
    public TypeCollector(JavaClass jclass) {
        this.jclass = jclass;
        this.jfile = jclass.getFile();
    }
    
    public boolean isBindingOk() {
        return bindingOk;
    }
    
    @Override
    public boolean visit(PrimitiveType node) {
        return false;
    }
    
    @Override
    public boolean visit(SimpleType node) {
        ITypeBinding tbinding = node.resolveBinding();
        if (tbinding != null) {
            JavaClass jc = JavaElement.findDeclaringClass(jfile.getProject(), tbinding);
            jclass.addUsedClass(jc);
        }
        return false;
    }
    
    @Override
    public boolean visit(QualifiedType node) {
        return true;
    }
    
    @Override
    public boolean visit(NameQualifiedType node) {
        return false;
    }
    
    @Override
    public boolean visit(WildcardType node) {
        return false;
    }
    
    @Override
    public boolean visit(ArrayType node) {
        return true;
    }
    
    @Override
    public boolean visit(ParameterizedType node) {
        ITypeBinding tbinding = node.resolveBinding();
        if (tbinding != null) {
            JavaClass jc = JavaElement.findDeclaringClass(jfile.getProject(), tbinding);
            if (jc != null) {
                jclass.addUsedClass(jc);
                for (ITypeBinding b : tbinding.getTypeArguments()) {
                    collectUsedClasses(jc, b);
                }
            } else {
                bindingOk = false;
            }
        } else {
            bindingOk = false;
        }
        return false;
    }
    
    private void collectUsedClasses(JavaClass jc, ITypeBinding tbinding) {
        if (tbinding.isRawType()) {
            JavaClass jc2 = JavaElement.findDeclaringClass(jfile.getProject(), tbinding);
            if (jc2 != null) {
                jc.addUsedClass(jc2);
            } else {
                bindingOk = false;
                jfile.getProject().printUnresolvedError(tbinding.getQualifiedName());
            }
        } else if (tbinding.isParameterizedType()) {
            JavaClass jc2 = JavaElement.findDeclaringClass(jfile.getProject(), tbinding);
            if (jc2 != null) {
                jc.addUsedClass(jc2);
                for (ITypeBinding b : tbinding.getTypeArguments()) {
                    collectUsedClasses(jc2, b);
                }
            } else {
                bindingOk = false;
                jfile.getProject().printUnresolvedError(tbinding.getQualifiedName());
            }
        }
    }
    
    @Override
    public boolean visit(UnionType node) {
        return true;
    }
    
    @Override
    public boolean visit(IntersectionType node) {
        return true;
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
