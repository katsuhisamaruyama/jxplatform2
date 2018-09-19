/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

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
public class JFieldExternal extends JField {
    
    protected JClass declaringClass;
    protected CtField ctField;
    
    JFieldExternal(CtField ctField, CFGStore cfgStore, JClass declaringClass) {
        super(declaringClass.getQualifiedName() + JavaElement.QualifiedNameSeparator + ctField.getName(), cfgStore,
              declaringClass.getQualifiedName(), ctField.getName(),
              getModfifiers(ctField), findType(ctField), checkPrimitiveType(ctField));
        this.declaringClass = declaringClass;
        this.ctField = ctField;
    }
    
    private static int getModfifiers(CtField ctField) {
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
    
    private static String findType(CtField ctField) {
        try {
            return ctField.getType().getName();
        } catch (NotFoundException e) {
            return "";
        }
    }
    
    private static boolean checkPrimitiveType(CtField ctField) {
        try {
            return ctField.getType().isPrimitive();
        } catch (NotFoundException e) {
            return false;
        }
    }
    
    @Override
    public boolean isInProject() {
        return false;
    }
}
