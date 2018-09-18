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
    
    InternalJField(JClass clazz) {
        super(clazz);
    }
    
    InternalJField(JavaField jfield, JClass clazz) {
        super(clazz);
        this.jfield = jfield;
        
        name = jfield.getName();
        fqn = jfield.getQualifiedName();
        type = jfield.getType();
        isPrimitiveType = jfield.isPrimitiveType();
        modifiers = jfield.getModifiers();
    }
    
    @Override
    public boolean isInProject() {
        return true;
    }
}
