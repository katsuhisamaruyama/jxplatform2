/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.eclipse.jdt.core.dom.Modifier;

/**
 * An abstract class that provides concise information on a field.
 * 
 * @author Katsuhisa Maruyama
 */
public abstract class JField {
    
    protected JClass declaringClass;
    
    protected String name;
    protected String fqn;
    protected String type;
    protected boolean isPrimitiveType;
    protected int modifiers;
    
    protected JField(JClass clazz) {
        declaringClass = clazz;
    }
    
    public void setAttribute(String name, String fqn, String type, boolean isPrimitiveType, int modifiers) {
        this.name = name;
        this.fqn = fqn;
        this.type = type;
        this.isPrimitiveType = isPrimitiveType;
        this.modifiers = modifiers;
    }
    
    public JClass getDeclaringClass() {
        return declaringClass;
    }
    
    public String getName() {
        return name;
    }
    
    public String getQualifiedName() {
        return fqn;
    }
    
    public String getType() {
        return type;
    }
    
    public boolean isPrimitiveType() {
        return isPrimitiveType;
    }
    
    public int getModifiers() {
        return modifiers;
    }
    
    public boolean isPublic() {
        return Modifier.isPublic(modifiers);
    }
    
    public boolean isProtected() {
        return Modifier.isProtected(modifiers);
    }
    
    public boolean isPrivate() {
        return Modifier.isPrivate(modifiers);
    }
    
    public boolean isDefault() {
        return !isPublic() && !isProtected() && !isPrivate();
    }
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
