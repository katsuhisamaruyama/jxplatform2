/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.VariableDeclaration;

/**
 * An object representing a local variable or a parameter.
 * 
 * @author Katsuhisa Maruyama
 */
public class JavaLocalVar extends JavaVariable {
    
    protected IVariableBinding binding;
    protected long variableId;
    
    public JavaLocalVar(VariableDeclaration node, JavaMethod jmethod) {
        this(node, node.resolveBinding().getVariableDeclaration(), jmethod);
    }
    
    protected JavaLocalVar(ASTNode node, IVariableBinding vbinding, JavaMethod jmethod) {
        super(node, jmethod.getFile());
        
        if (vbinding != null && vbinding.getType() != null) {
            binding = vbinding.getVariableDeclaration();
            name = vbinding.getName();
            fqn = name;
            type = vbinding.getType().getQualifiedName();
            isPrimitive = vbinding.getType().isPrimitive();
            modifiers = vbinding.getModifiers();
            kind = getKind(vbinding);
            variableId = vbinding.getVariableId();
            
            declaringClass = jmethod.getDeclaringClass();
            declaringMethod = jmethod;
        } else {
            name = ".UNKNOWN";
            fqn = ".UNKNOWN";
            kind = JavaVariable.Kind.UNKNOWN;
        }
    }
    
    protected JavaLocalVar(ITypeBinding tbinding, JavaMethod jmethod) {
        super(null, jmethod.getFile());
        
        tbinding = tbinding.getTypeDeclaration();
        name = tbinding.getName().toLowerCase();
        fqn = tbinding.getName();
        type = tbinding.getQualifiedName();
        isPrimitive = tbinding.isPrimitive();
        modifiers = tbinding.getModifiers();
        kind = JavaVariable.Kind.J_PARAMETER;
        variableId = -1;
        
        declaringClass = jmethod.getDeclaringClass();
        declaringMethod = jmethod;
    }
    
    public JavaLocalVar(JavaMethod jmethod, String name) {
        super(null, jmethod.getFile());
        
        declaringClass = jmethod.getDeclaringClass();
        declaringMethod = jmethod;
        
        this.name = name;
        fqn = name;
        modifiers = jmethod.getModifiers();
        type = jmethod.getReturnType();
        isPrimitive = jmethod.isPrimitiveReturnType();
        kind = JavaVariable.Kind.J_PARAMETER;
        variableId = -1;
        
        declaringClass = jmethod.getDeclaringClass();
        declaringMethod = jmethod;
    }
    
    public IVariableBinding getVariableBinding() {
        return binding;
    }
    
    protected long getVariableId() {
        return variableId;
    }
    
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JavaLocalVar) ? equals((JavaLocalVar)obj) : false;
    }
    
    public boolean equals(JavaLocalVar jlocal) {
        return jlocal != null && declaringMethod != null &&
                (this == jlocal || (declaringMethod.equals(jlocal.declaringMethod) && name.equals(jlocal.name) && variableId == jlocal.variableId));
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("\n");
        buf.append("LOCAL: ");
        buf.append(getName());
        buf.append("@");
        buf.append(getType());
        return buf.toString();
    }
}
