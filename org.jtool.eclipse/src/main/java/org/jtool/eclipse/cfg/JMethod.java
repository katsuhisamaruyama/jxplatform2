/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

/**
 * An abstract class that represents a method or a constructor.
 * 
 * @author Katsuhisa Maruyama
 */
public abstract class JMethod {
    
    protected JClass declaringClass;
    
    protected JMethod[] accessedMethods;
    protected JField[] accessedFields;
    protected JMethod[] overrindingMethods;
    protected JMethod[] overriddenMethods;
    
    protected JMethod() {
    }
    
    public JClass getDeclaringClass() {
        return declaringClass;
    }
    
    public JMethod[] getAccessedMethod() {
        return accessedMethods;
    }
    
    public JField[] getAccessedFields() {
        return accessedFields;
    }
    
    public JMethod[] getOverrindingMethods() {
        return overrindingMethods;
    }
    
    public JMethod[] getOverriddenMethods() {
        return overriddenMethods;
    }
    
    public abstract String getName();
    
    public abstract String getQualifiedName();
    
    public abstract String getSignature();
    
    public abstract String getReturnType();
    
    public abstract boolean isPrimitiveReturnType();
    
    public abstract boolean isVoid();
    
    public abstract boolean isMethod();
    
    public abstract boolean isConstructor();
    
    public abstract boolean isInitializer();
    
    public abstract boolean isPublic();
    
    public abstract boolean isProtected();
    
    public abstract boolean isPrivate();
    
    public abstract boolean isDefault();
    
    public abstract boolean isInProject();
    
    public boolean equals(JMethod method) {
        if (method == null) {
            return false;
        }
        return this == method || getQualifiedName().equals(method.getQualifiedName());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JMethod) {
            return equals((JMethod)obj);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return getQualifiedName().hashCode();
    }
    
    @Override
    public String toString() {
        return getQualifiedName();
    }
}
