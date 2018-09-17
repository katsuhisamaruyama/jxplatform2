/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.JClass;
import org.jtool.eclipse.cfg.JField;
import org.jtool.eclipse.javamodel.JavaElement;
import javassist.CtField;
import javassist.Modifier;
import javassist.NotFoundException;

/**
 * An object that represents a field outside the project.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class ExternalJField extends JField {
    
    protected CtField ctField;
    
    public ExternalJField(JClass clazz, CtField cfield) {
        this.ctField = cfield;
        declaringClass = clazz;
    }
    
    public CtField getCtField() {
        return ctField;
    }
    
    @Override
    public String getName() {
        return ctField.getName();
    }
    
    @Override
    public String getQualifiedName() {
        return declaringClass.getQualifiedName() + JavaElement.QualifiedNameSeparator + ctField.getName();
    }
    
    @Override
    public String getType() {
        try {
            return ctField.getType().getName();
        } catch (NotFoundException e) {
            return "";
        }
    }
    
    @Override
    public boolean isPrimitiveType() {
        try {
            return ctField.getType().isPrimitive();
        } catch (NotFoundException e) {
            return false;
        }
    }
    
    @Override
    public boolean isPublic() {
        return Modifier.isPublic(ctField.getModifiers());
    }
    
    @Override
    public boolean isProtected() {
        return Modifier.isProtected(ctField.getModifiers());
    }
    
    @Override
    public boolean isPrivate() {
        return Modifier.isPrivate(ctField.getModifiers());
    }
    
    @Override
    public boolean isDefault() {
        return !isPublic() && !isProtected() && !isPrivate();
    }
    
    @Override
    public boolean isInProject() {
        return false;
    }
}
