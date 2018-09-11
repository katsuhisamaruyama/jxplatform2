/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.JClass;
import org.jtool.eclipse.cfg.JField;
import org.jtool.eclipse.cfg.JMethod;

/**
 * An object that represents a class unregistered.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class UnregisteredJClass extends JClass {
    
    private static UnregisteredJClass instance = new UnregisteredJClass();
    
    private UnregisteredJClass() {
        fields = new JField[0];;
        methods = new JMethod[0];
    }
    
    public static UnregisteredJClass getInstance() {
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
    public boolean isClass() {
        return false;
    }
    
    @Override
    public boolean isInterface() {
        return false;
    }
    
    @Override
    public boolean isEnum() {
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
    
    @Override
    public JClass[] findAncestors() {
        return new JClass[0];
    }
    
    @Override
    public JClass[] findDescendants() {
        return new JClass[0];
    }
}
