/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.cfg.builder;

import org.jtool.eclipse.cfg.JMethod;
import org.jtool.eclipse.cfg.JField;
import java.util.Set;

/**
 * An object that represents a method unregistered.
 * All methods of this class are not intended to be directly called by clients.
 * 
 * @author Katsuhisa Maruyama
 */
public class UnregisteredJMethod extends JMethod {
    
    private static UnregisteredJMethod instance = new UnregisteredJMethod();
    
    private UnregisteredJMethod() {
        super(UnregisteredJClass.getInstance());
        accessedMethods = new JMethod[0];
        accessedFields = new JField[0];
        overrindingMethods = new JMethod[0];
        overriddenMethods = new JMethod[0];
    }
    
    public static UnregisteredJMethod getInstance() {
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
    public String getSignature() {
        return "";
    }
    
    @Override
    public String getReturnType() {
        return "";
    }
    
    @Override
    public boolean isPrimitiveReturnType() {
        return false;
    }
    
    @Override
    public boolean isVoid() {
        return false;
    }
    
    @Override
    public boolean isMethod() {
        return false;
    }
    
    @Override
    public boolean isConstructor() {
        return false;
    }
    
    @Override
    public boolean isInitializer() {
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
    
    public boolean isPrivate() {
        return false;
    }
    
    @Override
    public boolean isDefault() {
        return false;
    }
    
    @Override
    public boolean isInProject() {
        return true;
    }
    
    @Override
    protected JMethod[] findAccessedMethods() {
        return new JMethod[0];
    }
    
    @Override
    protected JField[] findAccessedFields() {
        return new JField[0];
    }
    
    @Override
    protected JMethod[] findOverridingMethods() {
        return new JMethod[0];
    }
    
    @Override
    protected JMethod[] findOverriddenMethods() {
        return new JMethod[0];
    }
    
    @Override
    public boolean hasSideEffects(Set<JMethod> visitedMethods) {
        return true;
    }
}
