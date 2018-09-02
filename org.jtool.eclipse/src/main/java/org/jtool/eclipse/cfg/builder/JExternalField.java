/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.JClass;
import org.jtool.eclipse.cfg.JMethod;
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
public class JExternalField extends JField {
    
    protected CtField cfield;
    
    public JExternalField(JClass clazz, CtField cfield) {
        this.cfield = cfield;
        declaringClass = clazz;
    }
    
    public CtField getCtField() {
        return cfield;
    }
    
    @Override
    public String getName() {
        return cfield.getName();
    }
    
    @Override
    public String getQualifiedName() {
        return declaringClass.getQualifiedName() + JavaElement.QualifiedNameSeparator + cfield.getName();
    }
    
    @Override
    public String getType() {
        try {
            return cfield.getType().getName();
        } catch (NotFoundException e) {
            return "";
        }
    }
    
    @Override
    public boolean isPrimitiveType() {
        try {
            return cfield.getType().isPrimitive();
        } catch (NotFoundException e) {
            return false;
        }
    }
    
    @Override
    public boolean isPublic() {
        return Modifier.isPublic(cfield.getModifiers());
    }
    
    @Override
    public boolean isProtected() {
        return Modifier.isProtected(cfield.getModifiers());
    }
    
    @Override
    public boolean isPrivate() {
        return Modifier.isPrivate(cfield.getModifiers());
    }
    
    @Override
    public boolean isDefault() {
        return !isPublic() && !isProtected() && !isPrivate();
    }
    
    @Override
    public boolean isInProject() {
        return false;
    }
    
    void collectInfo() {
        accessedMethods = new JMethod[0];
        accessedFields = new JField[0];
    }
}
