/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

/**
 * An interface that represents a class.
 * 
 * @author Katsuhisa Maruyama
 */
public abstract class JClass {
    
    protected JClass declaringClass;
    protected JMethod declaringMethod;
    
    protected JField[] fields;
    protected JMethod[] methods;
    protected JClass[] innerClasses;
    
    protected JClass[] ancestors;
    protected JClass[] descendants;
    
    protected JClass() {
    }
    
    public JClass getDeclaringClass() {
        return declaringClass;
    }
    
    public JMethod getDeclaringMethod() {
        return declaringMethod;
    }
    
    public JField[] getFields() {
        return fields;
    }
    
    public JField getField(String name) {
        for (JField field : fields) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }
    
    public JMethod[] getMethods() {
        return methods;
    }
    
    public JMethod getMethod(String sig) {
        for (JMethod method : methods) {
            if (method.getSignature().equals(sig)) {
                return method;
            }
        }
        return null;
    }
    
    public JClass[] getInnerClasses() {
        return innerClasses;
    }
    
    public JClass[] getAncestors() {
        return ancestors;
    }
    
    public JClass[] getDescendants() {
        return descendants;
    }
    
    public abstract String getName();
    
    public abstract String getQualifiedName();
    
    public abstract boolean isClass();
    
    public abstract boolean isInterface();
    
    public abstract boolean isEnum();
    
    public abstract boolean isPublic();
    
    public abstract boolean isProtected();
    
    public abstract boolean isPrivate();
    
    public abstract boolean isDefault();
    
    public abstract boolean isInProject();
    
    public boolean equals(JClass clazz) {
        if (clazz == null) {
            return false;
        }
        return this == clazz || getQualifiedName().equals(clazz.getQualifiedName());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JClass) {
            return equals((JClass)obj);
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
