/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg;

import org.jtool.eclipse.javamodel.JavaClass;
import org.jtool.eclipse.javamodel.JavaProject;
import org.eclipse.jdt.core.dom.Modifier;

/**
 * An abstract class that provides concise information on a class.
 * 
 * @author Katsuhisa Maruyama
 */
public abstract class JClass {
    
    protected JavaProject jproject;
    
    protected String name;
    protected String fqn;
    protected JavaClass.Kind kind;
    protected int modifiers;
    
    protected JClass[] ancestors = null;
    protected JClass[] descendants = null;
    
    protected JField[] fields;
    protected JMethod[] methods;
    
    protected JClass() {
    }
    
    protected JClass(JavaProject jproject) {
        this.jproject = jproject;
    }
    
    public void setAttribute(String name, String fqn, String kind, int modifiers) {
        this.name = name;
        this.fqn = fqn;
        this.kind = JavaClass.Kind.valueOf(kind);
        this.modifiers = modifiers;
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
    
    public String getName() {
        return name;
    }
    
    public String getQualifiedName() {
        return fqn;
    }
    
    public JavaClass.Kind getKind() {
        return kind;
    }
    
    public boolean isClass() {
        return kind == JavaClass.Kind.J_CLASS;
    }
    
    public boolean isInterface() {
        return kind == JavaClass.Kind.J_INTERFACE;
    }
    
    public boolean isEnum() {
        return kind == JavaClass.Kind.J_ENUM;
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
    
    public abstract boolean isTopLevelClass();
    
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
