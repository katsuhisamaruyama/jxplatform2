/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.javamodel.JavaField;

/**
 * An object that represents a field inside the project.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class JFieldInternal extends JField {
    
    protected JavaField jfield;
    
    JFieldInternal(JavaField jfield, CFGStore cfgStore) {
        super(jfield.getQualifiedName(), cfgStore, jfield.getDeclaringClass().getQualifiedName(), jfield.getName(),
              jfield.getModifiers(), jfield.getType(), jfield.isPrimitiveType());
        this.jfield = jfield;
    }
    
    @Override
    public boolean isInProject() {
        return true;
    }
}
