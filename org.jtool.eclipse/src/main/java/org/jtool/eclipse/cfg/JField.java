/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

/**
 * An abstract class that represents a field.
 * 
 * @author Katsuhisa Maruyama
 */
public abstract class JField {
    
    protected JClass declaringClass;
    
    protected JMethod[] accessedMethods;
    protected JField[] accessedFields;
    
    protected JField() {
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
    
    public abstract String getName();
    
    public abstract String getQualifiedName();
    
    public abstract String getType();
    
    public abstract boolean isPrimitiveType();
    
    public abstract boolean isPublic();
    
    public abstract boolean isProtected();
    
    public abstract boolean isPrivate();
    
    public abstract boolean isDefault();
    
    public abstract boolean isInProject();
    
    public boolean equals(JField field) {
        if (field == null) {
            return false;
        }
        return this == field || getQualifiedName().equals(field.getQualifiedName());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JField) {
            return equals((JField)obj);
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
