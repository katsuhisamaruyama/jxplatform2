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
    
    ExternalJField(JClass clazz) {
        super(clazz);
    }
    
    ExternalJField(CtField ctField, JClass clazz) {
        super(clazz);
        this.ctField = ctField;
        
        name = ctField.getName();
        fqn = declaringClass.getQualifiedName() + JavaElement.QualifiedNameSeparator + name;
        type = findType();
        isPrimitiveType = checkPrimitiveType();
        modifiers = getModfifiers(ctField);
    }
    
    private String findType() {
        try {
            return ctField.getType().getName();
        } catch (NotFoundException e) {
            return "";
        }
    }
    
    private boolean checkPrimitiveType() {
        try {
            return ctField.getType().isPrimitive();
        } catch (NotFoundException e) {
            return false;
        }
    }
    
    private int getModfifiers(CtField ctField) {
        if (Modifier.isPublic(ctField.getModifiers())) {
            return org.eclipse.jdt.core.dom.Modifier.PUBLIC;
        } else if (Modifier.isProtected(ctField.getModifiers())) {
            return org.eclipse.jdt.core.dom.Modifier.PROTECTED;
        } else if (Modifier.isPrivate(ctField.getModifiers())) {
            return org.eclipse.jdt.core.dom.Modifier.PRIVATE;
        } else {
            return org.eclipse.jdt.core.dom.Modifier.DEFAULT;
        }
    }
    
    @Override
    public boolean isInProject() {
        return false;
    }
}
