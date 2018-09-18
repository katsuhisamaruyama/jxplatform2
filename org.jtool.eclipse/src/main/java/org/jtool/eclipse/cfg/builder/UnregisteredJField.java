/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.JField;

/**
 * An object that represents a field unregistered.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class UnregisteredJField extends JField {
    
    private static UnregisteredJField instance = new UnregisteredJField();
    
    private UnregisteredJField() {
        super(UnregisteredJClass.getInstance());
    }
    
    public static UnregisteredJField getInstance() {
        return instance;
    }
    
    @Override
    public String getName() {
        return "";
    }
    
    @Override
    public String getQualifiedName() {
        return "";
    }
    
    @Override
    public String getType() {
        return "";
    }
    
    @Override
    public boolean isPrimitiveType() {
        return false;
    }
    
    @Override
    public boolean isPublic() {
        return false;
    }
    
    @Override
    public boolean isProtected() {
        return false;
    }
    
    @Override
    public boolean isPrivate() {
        return false;
    }
    
    @Override
    public boolean isDefault() {
        return false;
    }
    
    @Override
    public boolean isInProject() {
        return false;
    }
}
