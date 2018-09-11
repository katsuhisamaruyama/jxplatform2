/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.JClass;
import org.jtool.eclipse.cfg.JField;
import org.jtool.eclipse.javamodel.JavaField;

/**
 * An object that represents a field inside the project.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class InternalJField extends JField {
    
    protected JavaField jfield;
    
    public InternalJField(JClass clazz, JavaField jfield) {
        this.jfield = jfield;
        declaringClass = clazz;
    }
    
    public JavaField getJavaField() {
        return jfield;
    }
    
    @Override
    public String getName() {
        return jfield.getName();
    }
    
    @Override
    public String getQualifiedName() {
        return jfield.getQualifiedName();
    }
    
    @Override
    public String getType() {
        return jfield.getType();
    }
    
    @Override
    public boolean isPrimitiveType() {
        return jfield.isPrimitiveType();
    }
    
    @Override
    public boolean isPublic() {
        return jfield.isPublic();
    }
    
    @Override
    public boolean isProtected() {
        return jfield.isProtected();
    }
    
    @Override
    public boolean isPrivate() {
        return jfield.isPrivate();
    }
    
    @Override
    public boolean isDefault() {
        return jfield.isDefault();
    }
    
    @Override
    public boolean isInProject() {
        return true;
    }
}
