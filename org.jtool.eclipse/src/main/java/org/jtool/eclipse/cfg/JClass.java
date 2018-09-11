/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

/**
 * An abstract class that provides concise information on a class.
 * 
 * @author Katsuhisa Maruyama
 */
public abstract class JClass {
    
    protected JField[] fields;
    protected JMethod[] methods;
    
    protected JClass[] ancestors = null;
    protected JClass[] descendants = null;
    
    protected JClass() {
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
    
    public JClass[] getAncestors() {
        if (ancestors == null) {
            ancestors = findAncestors();
        }
        return ancestors;
    }
    
    public JClass[] getDescendants() {
        if (descendants == null) {
            descendants = findDescendants();
        }
        return descendants;
    }
    
    protected abstract JClass[] findAncestors();
    
    protected abstract JClass[] findDescendants();
    
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
