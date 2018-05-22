/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.javamodel;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.VariableDeclaration;

/**
 * An object representing a local variable.
 * @author Katsuhisa Maruyama
 */
public class JavaLocal extends JavaVariable {
    
    protected long variableId;
    
    protected JavaLocal() {
    }
    
    public JavaLocal(VariableDeclaration node, JavaMethod jmethod) {
        this(node, node.resolveBinding().getVariableDeclaration(), jmethod);
    }
    
    protected JavaLocal(ASTNode node, IVariableBinding vbinding, JavaMethod jmethod) {
        super(node, jmethod.getFile());
        
        if (vbinding != null) {
            vbinding = vbinding.getVariableDeclaration();
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
    
    public JavaLocal(JavaMethod jmethod, String name) {
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
    
    protected long getVariableId() {
        return variableId;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JavaLocal) {
            return equals((JavaLocal)obj);
        }
        return false;
    }
    
    public boolean equals(JavaLocal jlocal) {
        if (jlocal == null || declaringMethod == null) {
            return false;
        }
        return this == jlocal ||
                (declaringMethod.equals(jlocal.declaringMethod) && name.equals(jlocal.name) && variableId == jlocal.variableId);
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
